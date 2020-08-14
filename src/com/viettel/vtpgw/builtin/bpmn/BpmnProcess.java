package com.viettel.vtpgw.builtin.bpmn;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.viettel.vtpgw.context.RequestContext;
import com.viettel.vtpgw.model.Endpoint;
import com.viettel.vtpgw.model.HttpService;
import com.viettel.vtpgw.model.Service;
import com.viettel.vtpgw.repository.ServiceRepository;
import com.viettel.vtpgw.support.soap.KxmlSoapExtractor;
import com.viettel.vtpgw.util.BufferOutputStream;
import com.viettel.vtpgw.util.Utils;
import com.viettel.xslt.Fx;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.StringEscapeUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by dinhnn on 8/10/16.
 */
public class BpmnProcess {

    abstract class AbstractBpmnTask implements BpmnTask {

        DocumentBuilderFactory documentBuilderFactory;
        MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        List<TaskTransformer> inputTransformers = new ArrayList<>();
        final String outgoingTask;
        List<TaskTransformer> outputTransformers = new ArrayList<>();
        String taskId;
        List<TextTransformer> textOutputTransformers = new ArrayList<>();
        TransformerFactory transformerFactory;

        public AbstractBpmnTask(Element task, DocumentBuilderFactory documentBuilderFactory,
                TransformerFactory transformerFactory, XPathFactory xpathFactory, Map<String, Element> elementsById)
                throws TaskException {
            this.transformerFactory = transformerFactory;
            this.documentBuilderFactory = documentBuilderFactory;
            String taskId = task.getAttribute("id");
            this.taskId = taskId;

            Node extensionElements = Utils.findChild(task, BPMN2, "extensionElements");
            if (extensionElements != null) {
                Node extensionElement = extensionElements.getFirstChild();
                while (extensionElement != null) {
                    if (extensionElement.getNodeType() == Node.ELEMENT_NODE
                            && CAMUNDA.equals(extensionElement.getNamespaceURI())) {
                        switch (extensionElement.getLocalName()) {
                            case "properties":
                                NodeList items = ((Element) extensionElement).getElementsByTagNameNS(CAMUNDA, "property");
                                for (int i = 0; i < items.getLength(); i++) {
                                    Element item = (Element) items.item(i);
                                    headers.add(item.getAttribute("name"), item.getAttribute("value"));
                                }
                                break;
                            case "inputOutput":
                                Node parameter = extensionElement.getFirstChild();
                                while (parameter != null) {
                                    if (parameter.getNodeType() == Node.ELEMENT_NODE && CAMUNDA.equals(parameter.getNamespaceURI())) {
                                        switch (parameter.getLocalName()) {
                                            case "inputParameter":
                                                Element script = Utils.findChild(parameter, CAMUNDA, "script");
                                                if (script != null) {
                                                    String format = script.getAttribute("scriptFormat");
                                                    if ("xslt".equalsIgnoreCase(format)) {
                                                        inputTransformers.add(new XsltTransformer(script, transformerFactory));
                                                    } else if ("xpath".equalsIgnoreCase(format)) {
                                                        inputTransformers.add(new XpathTransformer(script, documentBuilderFactory, xpathFactory));
                                                    }
                                                }
                                                break;
                                            case "outputParameter":
                                                script = Utils.findChild(parameter, CAMUNDA, "script");
                                                if (script != null) {
                                                    String format = script.getAttribute("scriptFormat");
                                                    if ("xslt".equalsIgnoreCase(format)) {
                                                        outputTransformers.add(new XsltTransformer(script, transformerFactory));
                                                    } else if ("xpath".equalsIgnoreCase(format)) {
                                                        outputTransformers.add(new XpathTransformer(script, documentBuilderFactory, xpathFactory));
                                                    } else if ("regex".equalsIgnoreCase(format)) {
                                                        textOutputTransformers.add(new RegexTransformer(script));
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                    parameter = parameter.getNextSibling();
                                }
                                break;
                        }
                    }
                    extensionElement = extensionElement.getNextSibling();
                }
            }
            LOG.info("textOutputTransformers:{}" + textOutputTransformers.size());

            Element outgoing = Utils.findChild(task, BPMN2, "outgoing");
            if (outgoing == null) {
                throw new TaskException("Outgoing not found");
            }
            String sequenceFlowId = Utils.getNodeValue(outgoing);
            Element sequenceFlow = elementsById.get(sequenceFlowId);
            if (sequenceFlow == null) {
                throw new TaskException("Unknown outgoing #" + sequenceFlowId);
            }
            String targetRef = sequenceFlow.getAttribute("targetRef");
            Element targetElement = elementsById.get(targetRef);
            if (targetElement != null && BPMN2.equals(targetElement.getNamespaceURI())) {
                this.outgoingTask = targetRef;
            } else {
                throw new TaskException("Unknown outgoing node #" + targetRef);
            }
        }

        protected void afterTask(final Node out, MultiMap properties, RequestContext context, HttpClient http,
                HttpClient https) throws TaskException {
            Node output = out;
            for (TaskTransformer transformer : outputTransformers) {
                try {
                    output = transformer.transform(output, properties);
                } catch (TaskException e) {
                    throw new TaskException(e);
                }
            }
            BpmnTask task = tasks.get(outgoingTask);
            task.doTask(output, properties, context, http, https);
        }

        protected Node beforeTask(final Node in, MultiMap properties) throws TaskException {
            Node input = in;
            for (TaskTransformer transformer : inputTransformers) {
                try {
                    input = transformer.transform(input, properties);
                } catch (TaskException e) {
                    throw new TaskException(e);
                }

            }
            return input;
        }

        @Override
        public String getId() {
            return taskId;
        }
    }

    static class TaskException extends Exception {

        private static final long serialVersionUID = 1L;

        public TaskException(Exception e) {
            super(e);
        }

        public TaskException(String msg) {
            super(msg);
        }
    }

    interface BpmnTask {

        void doTask(Node input, MultiMap properties, RequestContext context, HttpClient http, HttpClient https)
                throws TaskException;

        String getId();
    }

    class EndTask implements BpmnTask {

        final String id;
        final TransformerFactory transformerFactory;

        EndTask(Element task, TransformerFactory transformerFactory) {
            id = task.getAttribute("id");
            this.transformerFactory = transformerFactory;
        }

        @Override
        public void doTask(Node input, MultiMap properties, RequestContext context, HttpClient http, HttpClient https)
                throws TaskException {
            try (BufferOutputStream requestBody = new BufferOutputStream()) {
                Transformer transformer = transformerFactory.newTransformer();
                transformer.transform(new DOMSource(input), new StreamResult(requestBody));
                context.response().end(requestBody.getBuff());
            } catch (Exception e) {
                throw new TaskException(e);
            }
        }

        @Override
        public String getId() {
            return id;
        }
    }

    class ExclusiveGateway implements BpmnTask {

        String defaultOutgoingTask;
        String id;
        List<Outgoing> outgoings = new ArrayList<>();

        public ExclusiveGateway(Element task, XPathFactory xpathFactory, Map<String, Element> elementsById)
                throws TaskException {
            id = task.getAttribute("id");
            NodeList items = task.getElementsByTagNameNS(BPMN2, "outgoing");
            int defaultTaskCounter = 0;
            for (int i = 0; i < items.getLength(); i++) {
                String sequenceFlowId = Utils.getNodeValue(items.item(i));
                Element sequenceFlow = elementsById.get(sequenceFlowId);
                if (sequenceFlow == null) {
                    throw new TaskException("Unknown outgoing #" + sequenceFlowId);
                }
                String targetRef = sequenceFlow.getAttribute("targetRef");
                Element targetElement = elementsById.get(targetRef);
                if (targetElement != null && BPMN2.equals(targetElement.getNamespaceURI())) {
                    Element conditionExpression = Utils.findChild(sequenceFlow, BPMN2, "conditionExpression");
                    if (conditionExpression != null) {
                        try {
                            outgoings.add(
                                    new Outgoing(xpathFactory.newXPath().compile(Utils.getNodeValue(conditionExpression)), targetRef));
                        } catch (XPathExpressionException e) {
                            throw new TaskException(e);
                        }
                    } else {
                        this.defaultOutgoingTask = targetRef;
                        defaultTaskCounter++;
                    }
                } else {
                    throw new TaskException("Unknown outgoing node #" + targetRef);
                }
            }
            if (defaultTaskCounter != 1) {
                throw new TaskException("No default or more than 1 default outgoing");
            }
        }

        @Override
        public void doTask(Node input, MultiMap properties, RequestContext context, HttpClient http, HttpClient https)
                throws TaskException {
            for (Outgoing outgoing : outgoings) {
                try {
                    String test = outgoing.expression.evaluate(input);
                    if (test != null && !test.isEmpty()) {
                        tasks.get(outgoing.outgoingTask).doTask(input, properties, context, http, https);
                        return;
                    }
                } catch (XPathExpressionException e) {
                    throw new TaskException(e);
                }
            }
            tasks.get(defaultOutgoingTask).doTask(input, properties, context, http, https);
        }

        @Override
        public String getId() {
            return id;
        }
    }

    static class Outgoing {

        XPathExpression expression;
        String outgoingTask;

        Outgoing(XPathExpression expression, String outgoingTask) {
            this.expression = expression;
            this.outgoingTask = outgoingTask;
        }
    }

    static class RegexTransformer implements TextTransformer {

        Pattern pattern;
        String replacement;

        public RegexTransformer(Node node) throws TaskException {
            String script = Utils.getNodeValue(node);
            int pos = script.indexOf("=>");
            pattern = Pattern.compile(script.substring(0, pos).trim());
            replacement = script.substring(pos + 2).trim();
        }

        @Override
        public String transform(String text) {
            return pattern.matcher(text).replaceAll(replacement);
        }
    }

    class ScriptTask extends AbstractBpmnTask {

        Templates templates;

        public ScriptTask(Element task, DocumentBuilderFactory documentBuilderFactory,
                TransformerFactory transformerFactory, XPathFactory xpathFactory, Map<String, Element> elementsById)
                throws TaskException {
            super(task, documentBuilderFactory, transformerFactory, xpathFactory, elementsById);
            String script = Utils.getNodeValue(Utils.findChild(task, BPMN2, "script"));
            try {
                templates = transformerFactory.newTemplates(new SAXSource(new InputSource(new StringReader(script))));
            } catch (TransformerConfigurationException e) {
                throw new TaskException(e);
            }
        }

        @Override
        public void doTask(Node in, MultiMap properties, RequestContext context, HttpClient http, HttpClient https)
                throws TaskException {
            Node input = beforeTask(in, properties);
            DOMResult result = new DOMResult();
            try {
                Transformer transformer = templates.newTransformer();
                transformer.setParameter("fx", new Fx());
                transformer.setParameter("headers", properties);
                transformer.transform(new DOMSource(input), result);
                afterTask(result.getNode(), properties, context, http, https);
            } catch (TransformerException e) {
                throw new TaskException(e);
            }

        }
    }

    class ServiceTask extends AbstractBpmnTask {

        ServiceRepository serviceRepository;
        private String topic;

        public ServiceTask(Element task, DocumentBuilderFactory documentBuilderFactory,
                TransformerFactory transformerFactory, XPathFactory xpathFactory, ServiceRepository serviceRepository,
                Map<String, Element> elementsById) throws TaskException {
            super(task, documentBuilderFactory, transformerFactory, xpathFactory, elementsById);
            topic = task.getAttributeNS(CAMUNDA, "topic");
            this.serviceRepository = serviceRepository;
        }

        @Override
        public void doTask(Node input, MultiMap properties, RequestContext context, HttpClient http, HttpClient https)
                throws TaskException {
            serviceRepository.get(topic, serviceAsync -> {
                if (serviceAsync.succeeded()) {
                    try {
                        doTask(input, properties, context, serviceAsync.result(), http, https);
                    } catch (TaskException e) {
                        LOG.error("Fail to run task #{}", taskId, e);
                        context.response().setStatusCode(500).end(e.getMessage());
                    }
                } else {
                    context.response().setStatusCode(500).end("Unknow service #" + topic);
                }
            });
        }

        public void doTask(Node in, MultiMap properties, RequestContext context, Service service, HttpClient http,
                HttpClient https) throws TaskException {
            Node input = beforeTask(in, properties);
            Buffer body;
            try (BufferOutputStream requestBody = new BufferOutputStream()) {
                transformerFactory.newTransformer().transform(new DOMSource(input), new StreamResult(requestBody));
                body = requestBody.getBuff();
            } catch (Exception e) {
                throw new TaskException(e);
            }
            Endpoint endpoint = service.endpoint();

            long start = System.currentTimeMillis();
            HttpClientRequest cReq = (endpoint.ssl() ? https : http).requestAbs(HttpMethod.POST, endpoint.url(), cResp -> {
                int statusCode = cResp.statusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    cResp.exceptionHandler(th -> {
                        if (!context.response().ended()) {
                            context.response().setStatusCode(502).end("Bad gateway");
                            context.out(start, System.currentTimeMillis() - start, "POST", endpoint.url(),
                                    "fail to get response from external service", th);
                        }
                    });

                    cResp.bodyHandler(respBody -> {
                        String contentType = cResp.getHeader(HttpHeaders.CONTENT_TYPE);

                        long duration = System.currentTimeMillis() - start;
                        String contentAsString = respBody.toString(StandardCharsets.UTF_8);
                        for (TextTransformer transformer : textOutputTransformers) {
                            contentAsString = transformer.transform(contentAsString);
                        }
                        Map<String, String> respParams = service.respParams();

                        if (respParams != null) {
                            if (HttpService.BUILT_IN_SOAP.equals(service.module())) {
                                KxmlSoapExtractor.INSTANCE.extractResponse(respParams, contentAsString, context);
                            }
                        }
                        if (context.isDebug()) {
                            try {
                                String respJson = context.getRespParams();
                                context.setRespParams((respJson != null && !respJson.isEmpty() ? ",\"raw\":\"" : "\"raw\":\"")
                                        + StringEscapeUtils.escapeJava(contentAsString) + '"');
                            } catch (Exception e) {
                                LOG.error("Can not escape {}", contentAsString, e);
                            }
                        }
                        context.out(start, duration, "POST", endpoint.url(), cResp.statusCode(), cResp.statusMessage(), true);

                        try {

                            afterTask(fromText(contentAsString, contentType), properties, context, http, https);
                        } catch (TaskException e) {
                            if (!context.response().ended()) {
                                context.out(start, System.currentTimeMillis() - start, "POST", endpoint.url(),
                                        "fail to parse or call next task", e);
                                context.response().setStatusCode(502).end("Bad gateway");
                            }
                        }
                    });
                } else {
                    context.out(start, System.currentTimeMillis() - start, "POST", endpoint.url(), cResp.statusCode(),
                            cResp.statusMessage(), false);
                }
            });
            cReq.exceptionHandler(th -> {
                if (!context.response().ended()) {
                    context.out(start, System.currentTimeMillis() - start, "POST", endpoint.url(),
                            "fail to request external service", th);
                    context.response().setStatusCode(502).end("Bad gateway");
                }
            });
            cReq.headers().addAll(headers);
            long timeout = service.timeout();
            if (timeout > 0) {
                cReq.setTimeout(timeout);
            }
            cReq.end(body);
        }
    }

    class StartTask implements BpmnTask {

        final String id;
        final String outgoingTask;

        StartTask(Element task, Map<String, Element> elementsById) throws TaskException {
            id = task.getAttribute("id");
            Element outgoing = Utils.findChild(task, BPMN2, "outgoing");
            if (outgoing == null) {
                throw new TaskException("Outgoing not found");
            }
            String sequenceFlowId = Utils.getNodeValue(outgoing);
            Element sequenceFlow = elementsById.get(sequenceFlowId);
            if (sequenceFlow == null) {
                throw new TaskException("Unknown outgoing #" + sequenceFlowId);
            }
            String targetRef = sequenceFlow.getAttribute("targetRef");
            Element targetElement = elementsById.get(targetRef);
            if (targetElement != null && BPMN2.equals(targetElement.getNamespaceURI())) {
                this.outgoingTask = targetRef;
            } else {
                throw new TaskException("Unknown outgoing node #" + targetRef);
            }
        }

        @Override
        public void doTask(Node input, MultiMap properties, RequestContext context, HttpClient http, HttpClient https)
                throws TaskException {
            BpmnTask task = tasks.get(outgoingTask);
            task.doTask(input, properties, context, http, https);
        }

        @Override
        public String getId() {
            return id;
        }
    }

    interface TaskTransformer {

        Node transform(Node doc, MultiMap properties) throws TaskException;
    }

    interface TextTransformer {

        String transform(String text);
    }

    static class XpathTransformer implements TaskTransformer {

        DocumentBuilderFactory builderFactory;
        XPathExpression expression;

        public XpathTransformer(Node node, DocumentBuilderFactory builderFactory, XPathFactory factory)
                throws TaskException {
            try {
                expression = factory.newXPath().compile(Utils.getNodeValue(node));
            } catch (XPathExpressionException e) {
                throw new TaskException(e);
            }
            this.builderFactory = builderFactory;
        }

        @Override
        public Node transform(Node doc, MultiMap properties) throws TaskException {
            try {
                String value = expression.evaluate(new DOMSource(doc));
                return builderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(value)));
            } catch (XPathExpressionException | SAXException | IOException | ParserConfigurationException e) {
                throw new TaskException(e);
            }
        }
    }

    static class XsltTransformer implements TaskTransformer {

        private Templates templates;

        public XsltTransformer(Node node, TransformerFactory factory) throws TaskException {
            try {
                this.templates = factory
                        .newTemplates(new SAXSource(new InputSource(new StringReader(Utils.getNodeValue(node)))));
            } catch (TransformerConfigurationException e) {
                throw new TaskException(e);
            }
        }

        @Override
        public Node transform(Node doc, MultiMap properties) throws TaskException {
            try {
                Transformer transformer = templates.newTransformer();
                transformer.setParameter("headers", properties);
                transformer.setParameter("fx", new Fx());
                DOMResult out = new DOMResult();
                transformer.transform(new DOMSource(doc), out);
                return out.getNode();
            } catch (TransformerException e) {
                throw new TaskException(e);
            }
        }
    }

    private static final String BPMN2 = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    private static final String CAMUNDA = "http://camunda.org/schema/1.0/bpmn";
    private static final Logger LOG = LogManager.getLogger(BpmnProcess.class);

    public static BpmnProcess create(String bpmn, ServiceRepository serviceRepository) throws TaskException {
        return new BpmnProcess(bpmn, serviceRepository);
    }

    DocumentBuilderFactory documentBuilderFactory;

    StartTask startTask;

    Map<String, BpmnTask> tasks = new HashMap<>();

    String wsdl;

    BpmnProcess(String bpmn, ServiceRepository serviceRepository) throws TaskException {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(false);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        XPathFactory xPathFactory = XPathFactory.newInstance();
        Document doc;
        try {
            doc = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(bpmn)));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new TaskException(e);
        }

        Element process = Utils.findChild(doc.getDocumentElement(), BPMN2, "process");
        if (process == null) {
            throw new TaskException("Process not found");
        }
        wsdl = Utils.getNodeValue(Utils.findChild(process, BPMN2, "documentation"));
        Map<String, Element> elementsById = new HashMap<>();

        Node child = process.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) child;
                String id = e.getAttribute("id");
                if (id != null) {
                    elementsById.put(id, e);
                }
            }
            child = child.getNextSibling();
        }

        child = process.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE && BPMN2.equals(child.getNamespaceURI())) {
                switch (child.getLocalName()) {
                    case "startEvent":
                        addTask(startTask = new StartTask((Element) child, elementsById));
                        break;
                    case "serviceTask":
                        addTask(new ServiceTask((Element) child, documentBuilderFactory, transformerFactory, xPathFactory,
                                serviceRepository, elementsById));
                        break;
                    case "scriptTask":
                        addTask(
                                new ScriptTask((Element) child, documentBuilderFactory, transformerFactory, xPathFactory, elementsById));
                        break;
                    case "exclusiveGateway":
                        addTask(new ExclusiveGateway((Element) child, xPathFactory, elementsById));
                        break;
                    case "endEvent":
                        addTask(new EndTask((Element) child, transformerFactory));
                        break;
                }
            }
            child = child.getNextSibling();
        }
    }

    private void addTask(BpmnTask task) {
        tasks.put(task.getId(), task);
    }

    private Document fromJson(JsonArray json) throws TaskException {
        try {
            Document doc = documentBuilderFactory.newDocumentBuilder().newDocument();
            Element root = doc.createElement("root");
            doc.appendChild(root);
            importJsonArray("item", json, root, doc);
            return doc;
        } catch (ParserConfigurationException e) {
            throw new TaskException(e);
        }
    }

    private Document fromJson(JsonObject json) throws TaskException {
        try {
            Document doc = documentBuilderFactory.newDocumentBuilder().newDocument();
            Element root = doc.createElement("root");
            doc.appendChild(root);
            importJsonObject(json, root, doc);
            return doc;
        } catch (ParserConfigurationException e) {
            throw new TaskException(e);
        }
    }

    private Node fromText(String text, String contentType) throws TaskException {
        if ("application/json".equals(contentType)) {
            if (text.startsWith("[")) {
                return fromJson(new JsonArray(text));
            }
            return fromJson(new JsonObject(text));
        } else {

            try {
                return documentBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(text)));
            } catch (SAXException | IOException | ParserConfigurationException e) {
                throw new TaskException(e);
            }
        }

    }

    private void importJsonArray(String name, JsonArray json, Element target, Document doc) {
        int size = json.size();
        for (int i = 0; i < size; i++) {
            Element e = doc.createElement(name);
            target.appendChild(e);
            Object obj = json.getValue(i);
            if (obj instanceof JsonObject) {
                importJsonObject((JsonObject) obj, e, doc);
            } else if (obj instanceof JsonArray) {
                importJsonArray("item", (JsonArray) obj, e, doc);
            } else {
                e.appendChild(doc.createTextNode(obj.toString()));
            }
        }
    }

    private void importJsonObject(JsonObject json, Element target, Document doc) {
        for (String field : json.fieldNames()) {
            Object obj = json.getValue(field);
            if (obj instanceof JsonObject) {
                Element e = doc.createElement(field);
                target.appendChild(e);
                importJsonObject((JsonObject) obj, e, doc);
            } else if (obj instanceof JsonArray) {
                importJsonArray(field, (JsonArray) obj, target, doc);
            } else {
                Element e = doc.createElement(field);
                e.appendChild(doc.createTextNode(obj.toString()));
                target.appendChild(e);
            }
        }
    }

    public void process(String xml, RequestContext context, HttpClient http, HttpClient https) {

        try {
            startTask.doTask(fromText(xml, context.headers().get(HttpHeaders.CONTENT_TYPE)),
                    MultiMap.caseInsensitiveMultiMap(), context, http, https);
        } catch (TaskException e) {
            LOG.error("Fail to process", e);
            context.response().setStatusCode(500).end(e.getMessage());
        }
    }

    public String wsdl() {
        return wsdl;
    }
}
