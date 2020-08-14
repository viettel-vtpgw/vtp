package com.viettel.vtpgw.builtin.bpmn;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
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

import com.viettel.vtpgw.util.BufferOutputStream;
import com.viettel.vtpgw.util.Utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;

/**
 * Created by dinhnn on 8/10/16.
 */
public class BpmnVerticle extends AbstractVerticle {
  abstract class AbstractBpmnTask implements BpmnTask {
    DocumentBuilderFactory documentBuilderFactory;
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    List<TaskTransformer> inputTransformers = new ArrayList<>();
    final String outgoingTask;
    List<TaskTransformer> outputTransformers = new ArrayList<>();
    String taskId;
    TransformerFactory transformerFactory;

    public AbstractBpmnTask(String bpmnId, Element task, DocumentBuilderFactory documentBuilderFactory,
        TransformerFactory transformerFactory, XPathFactory xpathFactory, Map<String, Element> elementsById)
        throws TransformerConfigurationException, TransformException, XPathExpressionException {
      this.transformerFactory = transformerFactory;
      this.documentBuilderFactory = documentBuilderFactory;
      String taskId = bpmnId + task.getAttribute("id");
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
                      }
                    }
                    break;
                  default:
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
      Element outgoing = Utils.findChild(task, BPMN2, "outgoing");
      if (outgoing == null)
        throw new NullPointerException("Outgoing not found");
      String sequenceFlowId = Utils.getNodeValue(outgoing);
      Element sequenceFlow = elementsById.get(sequenceFlowId);
      if (sequenceFlow == null)
        throw new NullPointerException("Unknown outgoing #" + sequenceFlowId);
      String targetRef = sequenceFlow.getAttribute("targetRef");
      Element targetElement = elementsById.get(targetRef);
      if (targetElement != null && BPMN2.equals(targetElement.getNamespaceURI())) {
        this.outgoingTask = bpmnId + targetRef;
      } else {
        throw new NullPointerException("Unknown outgoing node #" + targetRef);
      }
    }

    protected void afterTask(Node out, MultiMap properties) {
      Node output = out;
      try {
        for (TaskTransformer transformer : outputTransformers) {
          output = transformer.transform(output, properties);
        }
        eb.send(outgoingTask, output, new DeliveryOptions().setHeaders(properties));
      } catch (Exception e) {
        LOG.error("Can not transform output", e);
      }
    }

    protected Node beforeTask(Node in, MultiMap properties) {
      Node input = in;
      try {
        for (TaskTransformer transformer : inputTransformers) {
          input = transformer.transform(input, properties);
        }
      } catch (Exception e) {
        LOG.error("Can not transform input", e);
      }
      return input;

    }

    @Override
    public String getId() {
      return taskId;
    }
  }
  interface BpmnTask extends Handler<Message<Node>> {
    String getId();

    default boolean task() {
      return false;
    }
  }
  class ComplexGateway implements BpmnTask {
    String id;
    List<Object> outgoings = new ArrayList<>();

    public ComplexGateway(String bpmnId, Element task, XPathFactory xpathFactory, Map<String, Element> elementsById)
        throws XPathExpressionException {
      id = bpmnId + task.getAttribute("id");
      NodeList items = task.getElementsByTagNameNS(BPMN2, "outgoing");
      for (int i = 0; i < items.getLength(); i++) {
        String sequenceFlowId = Utils.getNodeValue(items.item(i));
        Element sequenceFlow = elementsById.get(sequenceFlowId);
        if (sequenceFlow == null)
          throw new NullPointerException("Unknown outgoing #" + sequenceFlowId);
        String targetRef = sequenceFlow.getAttribute("targetRef");
        Element targetElement = elementsById.get(targetRef);
        if (targetElement != null && BPMN2.equals(targetElement.getNamespaceURI())) {
          Element conditionExpression = Utils.findChild(sequenceFlow, BPMN2, "conditionExpression");
          if (conditionExpression != null) {
            outgoings.add(new Outgoing(xpathFactory.newXPath().compile(Utils.getNodeValue(conditionExpression)),
                bpmnId + targetRef));
          } else {
            outgoings.add(bpmnId + targetRef);
          }
        } else {
          throw new NullPointerException("Unknown outgoing node #" + targetRef);
        }
      }
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public void handle(Message<Node> event) {
      Node input = event.body();
      for (Object outgoing : outgoings) {
        if (outgoing instanceof Outgoing) {
          Outgoing o = (Outgoing) outgoing;
          try {
            String test = o.expression.evaluate(input);
            if (test != null && !test.isEmpty()) {
              eb.send(o.outgoingTask, input, new DeliveryOptions().setHeaders(event.headers()));
              return;
            }
          } catch (XPathExpressionException e) {
            LOG.error("Can not evaluate expression", e);
          }
        } else {
          eb.send((String) outgoing, input, new DeliveryOptions().setHeaders(event.headers()));
        }
      }
    }
  }
  class EndTask implements BpmnTask {
    final String id;
    final TransformerFactory transformerFactory;

    EndTask(String bpmnId, Element task, TransformerFactory transformerFactory) {
      id = bpmnId + task.getAttribute("id");
      this.transformerFactory = transformerFactory;
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public void handle(Message<Node> event) {
      String callback = event.headers().get("x-cb");
      if (callback != null)
        eb.send(callback, event.body());
    }
  }
  //
  class EventBasedGateway implements BpmnTask {
    String id;
    List<String> outgoings = new ArrayList<>();
    private long ttl = MAX_TTL;

    public EventBasedGateway(String bpmnId, Element task, Map<String, Element> elementsById) {
      id = bpmnId + task.getAttribute("id");
      NodeList items = task.getElementsByTagNameNS(BPMN2, "outgoing");
      for (int i = 0; i < items.getLength(); i++) {
        String sequenceFlowId = Utils.getNodeValue(items.item(i));
        Element sequenceFlow = elementsById.get(sequenceFlowId);
        if (sequenceFlow == null)
          throw new NullPointerException("Unknown outgoing #" + sequenceFlowId);
        String targetRef = sequenceFlow.getAttribute("targetRef");
        Element targetElement = elementsById.get(targetRef);
        if (targetElement != null && BPMN2.equals(targetElement.getNamespaceURI())) {
          outgoings.add(bpmnId + targetRef);
        } else {
          throw new NullPointerException("Unknown outgoing node #" + targetRef);
        }
      }
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public void handle(Message<Node> event) {
      String pid = event.headers().get("pid");
      marks.put(id + pid, 1, ttl, rs -> {
        Node input = event.body();
        for (String outgoing : outgoings) {
          eb.send(outgoing, input, new DeliveryOptions().setHeaders(event.headers()));
        }
      });
    }
  }
  class ExclusiveGateway implements BpmnTask {
    String defaultOutgoingTask;
    String id;
    List<Outgoing> outgoings = new ArrayList<>();
    private long ttl = MIN_TTL;

    public ExclusiveGateway(String bpmnId, Element task, XPathFactory xpathFactory, Map<String, Element> elementsById)
        throws XPathExpressionException {
      id = bpmnId + task.getAttribute("id");
      NodeList items = task.getElementsByTagNameNS(BPMN2, "outgoing");
      int defaultTaskCounter = 0;
      for (int i = 0; i < items.getLength(); i++) {
        String sequenceFlowId = Utils.getNodeValue(items.item(i));
        Element sequenceFlow = elementsById.get(sequenceFlowId);
        if (sequenceFlow == null)
          throw new NullPointerException("Unknown outgoing #" + sequenceFlowId);
        String targetRef = sequenceFlow.getAttribute("targetRef");
        Element targetElement = elementsById.get(targetRef);
        if (targetElement != null && BPMN2.equals(targetElement.getNamespaceURI())) {
          Element conditionExpression = Utils.findChild(sequenceFlow, BPMN2, "conditionExpression");
          if (conditionExpression != null) {
            outgoings.add(new Outgoing(xpathFactory.newXPath().compile(Utils.getNodeValue(conditionExpression)),
                bpmnId + targetRef));
          } else {
            this.defaultOutgoingTask = bpmnId + targetRef;
            defaultTaskCounter++;
          }
        } else {
          throw new NullPointerException("Unknown outgoing node #" + targetRef);
        }
      }
      if (defaultTaskCounter != 1) {
        throw new NullPointerException("No default or more than 1 default outgoing");
      }
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public void handle(Message<Node> event) {
      String pid = event.headers().get("pid");
      marks.putIfAbsent(id + pid, 0, ttl, rs -> {
        if (rs.failed()) {
          LOG.error("Can not put async map", rs.cause());
          return;
        }
        if (rs.result() != null)
          return;
        Node input = event.body();
        for (Outgoing outgoing : outgoings) {
          try {
            String test = outgoing.expression.evaluate(input);
            if (test != null && !test.isEmpty()) {
              eb.send(outgoing.outgoingTask, input, new DeliveryOptions().setHeaders(event.headers()));
              return;
            }
          } catch (XPathExpressionException e) {
            LOG.error("Can not evaluate expression", e);
          }
        }
        eb.send(defaultOutgoingTask, input, new DeliveryOptions().setHeaders(event.headers()));
      });
    }
  }
  class InclusiveGateway implements BpmnTask {
    String id;
    String joinId;
    List<Outgoing> outgoings = new ArrayList<>();
    private long ttl = MAX_TTL;

    public InclusiveGateway(String bpmnId, Element task, XPathFactory xpathFactory, Map<String, Element> elementsById)
        throws XPathExpressionException {
      id = bpmnId + task.getAttribute("id");
      joinId = bpmnId + getJoinId(task);
      NodeList items = task.getElementsByTagNameNS(BPMN2, "outgoing");
      for (int i = 0; i < items.getLength(); i++) {
        String sequenceFlowId = Utils.getNodeValue(items.item(i));
        Element sequenceFlow = elementsById.get(sequenceFlowId);
        if (sequenceFlow == null)
          throw new NullPointerException("Unknown outgoing #" + sequenceFlowId);
        String targetRef = sequenceFlow.getAttribute("targetRef");
        Element targetElement = elementsById.get(targetRef);
        if (targetElement != null && BPMN2.equals(targetElement.getNamespaceURI())) {
          Element conditionExpression = Utils.findChild(sequenceFlow, BPMN2, "conditionExpression");
          if (conditionExpression != null) {
            outgoings.add(new Outgoing(xpathFactory.newXPath().compile(Utils.getNodeValue(conditionExpression)),
                bpmnId + targetRef));
          }
        } else {
          throw new NullPointerException("Unknown outgoing node #" + targetRef);
        }
      }
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public void handle(Message<Node> event) {
      String pid = event.headers().get("pid");
      if (joinId != null) {
        vertx.sharedData().getLock(pid, lockAsync -> {
          if (lockAsync.succeeded()) {
            marks.get(joinId + pid, joinAsync -> {
              if (joinAsync.succeeded()) {
                Integer count = joinAsync.result();
                if (count != null && count > 0) {
                  count--;
                  marks.put(joinId, count, putAsync -> {
                    lockAsync.result().release();
                  });
                } else {
                  marks.remove(joinId + pid, removeAsync -> lockAsync.result().release());
                  List<String> passed = new ArrayList<>();
                  Node input = event.body();
                  for (Outgoing outgoing : outgoings) {
                    try {
                      String test = outgoing.expression.evaluate(input);
                      if (test != null && !test.isEmpty()) {
                        passed.add(outgoing.outgoingTask);
                      }
                    } catch (XPathExpressionException e) {
                      LOG.error("Can not evaluate expression", e);
                    }
                  }
                  if (!passed.isEmpty()) {
                    marks.put(id + pid, passed.size(), ttl, rs -> {
                      if (rs.succeeded()) {
                        for (String outgoing : passed) {
                          eb.send(outgoing, input, new DeliveryOptions().setHeaders(event.headers()));
                        }
                      } else {
                        LOG.error("Can not put async map", rs.cause());
                      }
                    });
                  }
                }
              } else {
                LOG.error("Can not get join counter", joinAsync.cause());
                lockAsync.result().release();
              }
            });

          } else {
            LOG.error("Can not lock", lockAsync.cause());
          }
        });
      } else {
        List<String> passed = new ArrayList<>();
        Node input = event.body();
        for (Outgoing outgoing : outgoings) {
          try {
            String test = outgoing.expression.evaluate(input);
            if (test != null && !test.isEmpty()) {
              passed.add(outgoing.outgoingTask);
            }
          } catch (XPathExpressionException e) {
            LOG.error("Can not evaluate expression", e);
          }
        }
        if (!passed.isEmpty()) {
          marks.put(id + pid, passed.size(), ttl, rs -> {
            if (rs.succeeded()) {
              for (String outgoing : passed) {
                eb.send(outgoing, input, new DeliveryOptions().setHeaders(event.headers()));
              }
            } else {
              LOG.error("Can not put async map", rs.cause());
            }
          });
        }
      }
    }
  }
  class MessageServiceTask extends AbstractBpmnTask {
    private String topic;

    public MessageServiceTask(String bpmnId, Element task, DocumentBuilderFactory documentBuilderFactory,
        TransformerFactory transformerFactory, XPathFactory xpathFactory, Map<String, Element> elementsById)
        throws TransformerConfigurationException, XPathExpressionException, TransformException {
      super(bpmnId, task, documentBuilderFactory, transformerFactory, xpathFactory, elementsById);
      topic = task.getAttributeNS(CAMUNDA, "topic");
    }

    @Override
    public void handle(Message<Node> event) {
      Node input = event.body();
      MultiMap properties = event.headers();
      input = beforeTask(input, properties);
      vertx.eventBus().<Node>send(topic, input, new DeliveryOptions().setHeaders(properties), reply -> {
        if (reply.succeeded()) {
          afterTask(reply.result().body(), properties);
        } else {
          throwException("fail to call topic #" + topic, reply.cause(), properties);
        }
      });
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
  class ParallelGateway implements BpmnTask {
    String id;
    String joinId;
    List<String> outgoings = new ArrayList<>();
    private long ttl = MAX_TTL;

    public ParallelGateway(String bpmnId, Element task, Map<String, Element> elementsById) {
      id = bpmnId + task.getAttribute("id");
      joinId = bpmnId + getJoinId(task);
      NodeList items = task.getElementsByTagNameNS(BPMN2, "outgoing");
      for (int i = 0; i < items.getLength(); i++) {
        String sequenceFlowId = Utils.getNodeValue(items.item(i));
        Element sequenceFlow = elementsById.get(sequenceFlowId);
        if (sequenceFlow == null)
          throw new NullPointerException("Unknown outgoing #" + sequenceFlowId);
        String targetRef = sequenceFlow.getAttribute("targetRef");
        Element targetElement = elementsById.get(targetRef);
        if (targetElement != null && BPMN2.equals(targetElement.getNamespaceURI())) {
          outgoings.add(bpmnId + targetRef);
        } else {
          throw new NullPointerException("Unknown outgoing node #" + targetRef);
        }
      }
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public void handle(Message<Node> event) {
      String pid = event.headers().get("pid");
      if (joinId != null) {
        vertx.sharedData().getLock(pid, lockAsync -> {
          if (lockAsync.succeeded()) {
            marks.get(joinId + pid, joinAsync -> {
              if (joinAsync.succeeded()) {
                Integer count = joinAsync.result();
                if (count != null && count > 0) {
                  count--;
                  marks.put(joinId, count, putAsync -> {
                    lockAsync.result().release();
                  });
                } else {
                  marks.remove(joinId + pid, removeAsync -> lockAsync.result().release());
                  marks.put(id + pid, outgoings.size(), ttl, rs -> {
                    if (rs.succeeded()) {
                      Node input = event.body();
                      for (String outgoing : outgoings) {
                        eb.send(outgoing, input, new DeliveryOptions().setHeaders(event.headers()));
                      }
                    } else {
                      LOG.error("Can not put async map", rs.cause());
                    }
                  });
                }
              } else {
                LOG.error("Can not get join counter", joinAsync.cause());
                lockAsync.result().release();
              }
            });

          } else {
            LOG.error("Can not lock", lockAsync.cause());
          }
        });
      } else {
        marks.put(id + pid, outgoings.size(), ttl, rs -> {
          if (rs.succeeded()) {
            Node input = event.body();
            for (String outgoing : outgoings) {
              eb.send(outgoing, input, new DeliveryOptions().setHeaders(event.headers()));
            }
          } else {
            LOG.error("Can not put async map", rs.cause());
          }
        });
      }
    }
  }

  abstract class ReceiveEvent implements BpmnTask {
    String gatewayId;
    String id;
    String outgoingTask;

    ReceiveEvent(String bpmnId, Element task, Map<String, Element> elementsById) {
      id = bpmnId + task.getAttribute("id");
      Element outgoing = Utils.findChild(task, BPMN2, "outgoing");
      if (outgoing == null)
        throw new NullPointerException("Outgoing not found");
      String sequenceFlowId = Utils.getNodeValue(outgoing);
      Element sequenceFlow = elementsById.get(sequenceFlowId);
      if (sequenceFlow == null)
        throw new NullPointerException("Unknown outgoing #" + sequenceFlowId);
      String targetRef = sequenceFlow.getAttribute("targetRef");
      Element targetElement = elementsById.get(targetRef);
      if (targetElement != null && BPMN2.equals(targetElement.getNamespaceURI())) {
        this.outgoingTask = bpmnId + targetRef;
      } else {
        throw new NullPointerException("Unknown outgoing node #" + targetRef);
      }

      Element incoming = Utils.findChild(task, BPMN2, "incoming");
      if (incoming == null)
        throw new NullPointerException("Incoming not found");
      sequenceFlowId = Utils.getNodeValue(outgoing);
      sequenceFlow = elementsById.get(sequenceFlowId);
      if (sequenceFlow == null)
        throw new NullPointerException("Unknown incoming #" + sequenceFlowId);
      String sourceRef = sequenceFlow.getAttribute("sourceRef");
      Element sourceElement = elementsById.get(sourceRef);
      if (sourceElement != null && BPMN2.equals(sourceElement.getNamespaceURI())
          && "eventBasedGateway".equals(sourceElement.getLocalName())) {
        this.gatewayId = bpmnId + targetRef;
      } else {
        throw new NullPointerException("Unknown outgoing node #" + targetRef);
      }
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public void handle(Message<Node> event) {
      String pid = event.headers().get("pid");
      initTask(pid, event.body(), event.headers());
    }

    protected abstract void initTask(String pid, Node input, MultiMap properties);

    protected void next(String pid, Node output, MultiMap properties) {
      if (gatewayId != null) {
        marks.remove(gatewayId + pid, rs -> {
          if (rs.succeeded() && rs.result() == 1) {
            eb.send(outgoingTask, output, new DeliveryOptions().setHeaders(properties));
            eb.publish(CONSUMER_CLEANUP, pid);
          }
        });
      } else {
        eb.send(outgoingTask, output, new DeliveryOptions().setHeaders(properties));
      }
    }
  }

  class ReceiveTask extends ReceiveEvent {
    ReceiveTask(String bpmnId, Element task, Map<String, Element> elementsById){
      super(bpmnId, task, elementsById);
    }

    @Override
    protected void initTask(String pid, Node input, MultiMap properties) {
      consumers.put(pid, eb.<Node>consumer("event-" + pid, msg -> {
        next(pid, msg.body(), properties);
      }));
    }
  }
  class ScriptTask extends AbstractBpmnTask {
    Templates templates;

    public ScriptTask(String bpmnId, Element task, DocumentBuilderFactory documentBuilderFactory,
        TransformerFactory transformerFactory, XPathFactory xpathFactory, Map<String, Element> elementsById)
        throws TransformerConfigurationException, XPathExpressionException, TransformException {
      super(bpmnId, task, documentBuilderFactory, transformerFactory, xpathFactory, elementsById);
      String script = Utils.getNodeValue(Utils.findChild(task, BPMN2, "script"));
      templates = transformerFactory.newTemplates(new SAXSource(new InputSource(new StringReader(script))));
    }

    @Override
    public void handle(Message<Node> event) {
      Node input = event.body();
      MultiMap properties = event.headers();
      input = beforeTask(input, properties);
      DOMResult result = new DOMResult();
      try {
        templates.newTransformer().transform(new DOMSource(input), result);
      } catch (Exception e) {
        LOG.error("Can not transform xslt script", e);
      }
      afterTask(result.getNode(), properties);
    }

    @Override
    public boolean task() {
      return true;
    }
  }
  class ServiceTask extends AbstractBpmnTask {
    private HttpClient client;
    private String endpoint;

    public ServiceTask(String bpmnId, Element task, String endpoint, DocumentBuilderFactory documentBuilderFactory,
        TransformerFactory transformerFactory, XPathFactory xpathFactory, Map<String, Element> elementsById)
        throws TransformerConfigurationException, TransformException, XPathExpressionException  {
      super(bpmnId, task, documentBuilderFactory, transformerFactory, xpathFactory, elementsById);
      this.endpoint = endpoint;
      client = endpoint.startsWith("https://") ? https : http;
    }

    @Override
    public void handle(Message<Node> event) {
      Node input = event.body();
      MultiMap properties = event.headers();
      input = beforeTask(input, properties);
      try (BufferOutputStream requestBody = new BufferOutputStream()){
        transformerFactory.newTransformer().transform(new DOMSource(input), new StreamResult(requestBody));
        Buffer body = requestBody.getBuff();
        HttpClientRequest cReq = client.requestAbs(HttpMethod.POST, endpoint, cResp -> {
          int statusCode = cResp.statusCode();
          if (statusCode >= 200 && statusCode < 300) {
            cResp.exceptionHandler(th -> throwException("fail to read response from " + endpoint, th, properties));
            cResp.bodyHandler(respBody -> {
              String contentType = cResp.getHeader(HttpHeaders.CONTENT_TYPE);
              String contentAsString = body.toString(StandardCharsets.UTF_8);
              try {
                afterTask(fromText(contentAsString, contentType), properties);
              } catch (ParserConfigurationException|SAXException|IOException  e) {
                LOG.error("After Task Execution Exception", e);
              }
            });
          }
        });
        cReq.exceptionHandler(th -> {

        });
        cReq.headers().addAll(headers);
        String timeout = properties.get("timeout");
        if (timeout != null)
          try {
            cReq.setTimeout(Long.parseLong(timeout));
          } catch (NumberFormatException e) {
            LOG.error("Invalid timeout", e);
          }

        cReq.end(body);
      } catch (Exception e) {
        LOG.error("Can not transform xml to text", e);
      }
    }

    @Override
    public boolean task() {
      return true;
    }
  }
  class StartTask implements BpmnTask {
    final String id;
    final String outgoingTask;

    StartTask(String bpmnId, Element task, Map<String, Element> elementsById) {
      id = bpmnId + task.getAttribute("id");
      Element outgoing = Utils.findChild(task, BPMN2, "outgoing");
      if (outgoing == null)
        throw new NullPointerException("Outgoing not found");
      String sequenceFlowId = Utils.getNodeValue(outgoing);
      Element sequenceFlow = elementsById.get(sequenceFlowId);
      if (sequenceFlow == null)
        throw new NullPointerException("Unknown outgoing #" + sequenceFlowId);
      String targetRef = sequenceFlow.getAttribute("targetRef");
      Element targetElement = elementsById.get(targetRef);
      if (targetElement != null && BPMN2.equals(targetElement.getNamespaceURI())) {
        this.outgoingTask = bpmnId + targetRef;
      } else {
        throw new NullPointerException("Unknown outgoing node #" + targetRef);
      }
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public void handle(Message<Node> event) {
      MultiMap header = event.headers();
      String callback = header.get("x-cb");
      if (callback == null) {
        header.set("x-cb", event.replyAddress());
      }
      eb.send(outgoingTask, event.body(), new DeliveryOptions().setHeaders(header));
    }
  }
  @FunctionalInterface
  interface TaskTransformer {
    Node transform(Node doc, MultiMap properties) throws TransformException;
  }

  class TimerTask extends ReceiveEvent {
    long delay;

    TimerTask(String bpmnId, Element task, long delay, Map<String, Element> elementsById) {
      super(bpmnId, task, elementsById);
      this.delay = delay;
    }

    @Override
    protected void initTask(String pid, Node input, MultiMap properties) {
      timers.put(pid, vertx.setTimer(delay, id -> {
        timers.remove(pid);
        next(pid, input, properties);
      }));
    }
  }

  static class TransformException extends Exception {

    private static final long serialVersionUID = 1L;

    public TransformException(Exception e) {
      super(e);
    }
  }
  static class XpathTransformer implements TaskTransformer {
    DocumentBuilderFactory builderFactory;
    XPathExpression expression;

    public XpathTransformer(Node node, DocumentBuilderFactory builderFactory, XPathFactory factory)
        throws XPathExpressionException {
      expression = factory.newXPath().compile(Utils.getNodeValue(node));
      this.builderFactory = builderFactory;
    }

    @Override
    public Node transform(Node doc, MultiMap properties) throws TransformException {
      try {
        String value = expression.evaluate(new DOMSource(doc));
        return builderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(value)));
      } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
        throw new TransformException(e);
      }

    }
  }
  static class XsltTransformer implements TaskTransformer {
    private Templates templates;

    public XsltTransformer(Node node, TransformerFactory factory)
        throws TransformException, TransformerConfigurationException {
      this.templates = factory.newTemplates(new SAXSource(new InputSource(new StringReader(Utils.getNodeValue(node)))));
    }

    @Override
    public Node transform(Node doc, MultiMap properties) throws TransformException {
      DOMResult out = new DOMResult();
      try {
        templates.newTransformer().transform(new DOMSource(doc), out);
      } catch (TransformerException e) {
        throw new TransformException(e);
      }

      return out.getNode();
    }
  }

  private static final String BPMN2 = "http://www.omg.org/spec/BPMN/20100524/MODEL";

  private static final String CAMUNDA = "http://camunda.org/schema/1.0/bpmn";

  private static final String CONSUMER_CLEANUP = "consumer-cleanup";

  private static final Logger LOG = LogManager.getLogger(BpmnVerticle.class);

  private static final long MAX_TTL = 300000;

  private static final long MIN_TTL = 300000;

  Map<String, List<MessageConsumer<Node>>> bpmns = new HashMap<>();

  private Map<String, MessageConsumer<Node>> consumers = new HashMap<>();

  DocumentBuilderFactory documentBuilderFactory;

  EventBus eb;

  HttpClient http;

  HttpClient https;

  private AsyncMap<String, Integer> marks;

  StartTask startTask;

  private Map<String, Long> timers = new HashMap<>();

  private void addTask(List<MessageConsumer<Node>> consumers, BpmnTask task) {
    if (task.task()) {
      consumers.add(eb.consumer(task.getId(), task));
    } else {
      consumers.add(eb.localConsumer(task.getId(), task));
    }
  }

  public Future<Void> deploy(String bpmnId, String bpmn) {
    return undeploy(bpmnId).compose(v -> deployImpl(bpmn,bpmn));
  }

  private Future<Void> deployImpl(String bpmnId, String bpmn) {
    Future<Void> future = Future.future();
    try {
      documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      documentBuilderFactory.setValidating(false);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      XPathFactory xPathFactory = XPathFactory.newInstance();

      Document doc = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(bpmn)));

      Element process = Utils.findChild(doc.getDocumentElement(), BPMN2, "process");
      if (process == null)
        throw new NullPointerException("Process not found");

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
      List<MessageConsumer<Node>> consumers = new ArrayList<>();
      bpmns.put(bpmnId, consumers);
      child = process.getFirstChild();
      while (child != null) {
        if (child.getNodeType() == Node.ELEMENT_NODE && BPMN2.equals(child.getNamespaceURI())) {
          switch (child.getLocalName()) {
          case "startEvent":
            startTask = new StartTask(bpmnId, (Element) child, elementsById);
            addTask(consumers, startTask);
            break;
          case "serviceTask":
            NodeList connectorId = ((Element) child).getElementsByTagNameNS(CAMUNDA, "connectorId");
            if (connectorId != null && connectorId.getLength() == 1) {
              addTask(consumers, new ServiceTask(bpmnId, (Element) child, Utils.getNodeValue(connectorId.item(0)),
                  documentBuilderFactory, transformerFactory, xPathFactory, elementsById));
            } else {
              addTask(consumers, new MessageServiceTask(bpmnId, (Element) child, documentBuilderFactory,
                  transformerFactory, xPathFactory, elementsById));
            }
            break;
          case "scriptTask":
            addTask(consumers, new ScriptTask(bpmnId, (Element) child, documentBuilderFactory, transformerFactory,
                xPathFactory, elementsById));
            break;
          case "receiveTask":
            addTask(consumers, new ReceiveTask(bpmnId, (Element) child, elementsById));
            break;
          case "intermediateCatchEvent":
            Element timerEventDefinition = Utils.findChild(child, BPMN2, "timerEventDefinition");
            if (timerEventDefinition != null) {
              long timeDuration = Long
                  .parseLong(Utils.getNodeValue(Utils.findChild(timerEventDefinition, BPMN2, "timeDuration")));
              addTask(consumers, new TimerTask(bpmnId, (Element) child, timeDuration, elementsById));
            } else {
              addTask(consumers, new ReceiveTask(bpmnId, (Element) child, elementsById));
            }
            break;
          case "inclusiveGateway":
            addTask(consumers, new InclusiveGateway(bpmnId, (Element) child, xPathFactory, elementsById));
            break;
          case "exclusiveGateway":
            addTask(consumers, new ExclusiveGateway(bpmnId, (Element) child, xPathFactory, elementsById));
            break;
          case "parallelGateway":
            addTask(consumers, new ParallelGateway(bpmnId, (Element) child, elementsById));
            break;
          case "complexGateway":
            addTask(consumers, new ComplexGateway(bpmnId, (Element) child, xPathFactory, elementsById));
            break;
          case "eventBasedGateway":
            addTask(consumers, new EventBasedGateway(bpmnId, (Element) child, elementsById));
            break;
          case "endEvent":
            addTask(consumers, new EndTask(bpmnId, (Element) child, transformerFactory));
            break;
          }
        }
        child = child.getNextSibling();
      }
      future.complete();
    } catch (Exception e) {
      future.fail(e);
    }
    return future;
  }

  private Document fromJson(JsonArray json) throws ParserConfigurationException {
    Document doc = documentBuilderFactory.newDocumentBuilder().newDocument();
    Element root = doc.createElement("root");
    doc.appendChild(root);
    importJsonArray("item", json, root, doc);
    return doc;
  }

  private Document fromJson(JsonObject json) throws ParserConfigurationException {
    Document doc = documentBuilderFactory.newDocumentBuilder().newDocument();
    Element root = doc.createElement("root");
    doc.appendChild(root);
    importJsonObject(json, root, doc);
    return doc;
  }

  private Node fromText(String text, String contentType)
      throws ParserConfigurationException, SAXException, IOException {
    if ("application/json".equals(contentType)) {
      if (text.startsWith("[")) {
        return fromJson(new JsonArray(text));
      }
      return fromJson(new JsonObject(text));
    } else {
      return documentBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(text)));
    }

  }

  private String getJoinId(Element task) {
    Node extensionElements = Utils.findChild(task, BPMN2, "extensionElements");
    if (extensionElements != null) {
      Node extensionElement = extensionElements.getFirstChild();
      while (extensionElement != null) {
        if (extensionElement.getNodeType() == Node.ELEMENT_NODE && CAMUNDA.equals(extensionElement.getNamespaceURI()) && "properties".equals(extensionElement.getLocalName())) {
          NodeList items = ((Element) extensionElement).getElementsByTagNameNS(CAMUNDA, "property");
          for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            if ("join".equals(item.getAttribute("name"))) {
              return item.getAttribute("value");
            }
          }
        }
        extensionElement = extensionElement.getNextSibling();
      }
    }
    return null;
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

  @Override
  public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
    eb = vertx.eventBus();
    eb.<String>consumer(CONSUMER_CLEANUP, msg -> {
      String key = msg.body();
      consumers.remove(key);
      Long timer = timers.remove(key);
      if (timer != null)
        vertx.cancelTimer(timer);
    });
    vertx.sharedData().<String, Integer>getClusterWideMap("bpmn-marks", async -> {
      if (async.succeeded()) {
        marks = async.result();
      } else {
        startFuture.fail(async.cause());
      }
    });
  }

  public void start(Message<Node> msg) {
    msg.headers().add("pid", UUID.randomUUID().toString());
    startTask.handle(msg);
  }

  private void throwException(String msg, Throwable e, MultiMap header) {
    LOG.error(msg, e);
    String callback = header.get("x-cb");
    eb.send(callback, e.getMessage(), new DeliveryOptions().addHeader("status", "fail"));
  }

  public Future<Void> undeploy(String bpmnId) {
    Future<Void> future = Future.future();
    List<MessageConsumer<Node>> consumers = bpmns.remove(bpmnId);
    if (consumers != null) {
      @SuppressWarnings("rawtypes")
      List<Future> futures = new ArrayList<>();
      for (MessageConsumer<Node> consumer : consumers) {
        Future<Void> f = Future.future();
        consumer.unregister(f.completer());
        futures.add(f);
      }
      CompositeFuture.all(futures).setHandler(rs -> {
        if (rs.succeeded()) {
          future.complete();
        } else {
          future.fail(rs.cause());
        }
      });
    } else {
      future.complete();
    }
    return future;
  }
}
