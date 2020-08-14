package com.viettel.vtpgw.support.soap;

import java.io.StringReader;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import com.viettel.vtpgw.context.RequestContext;
import com.viettel.vtpgw.support.ParametersExtractor;
import com.viettel.vtpgw.util.AppendableString;

import io.vertx.core.impl.StringEscapeUtils;

public class KxmlSoapExtractor implements ParametersExtractor {

    private static int initSize = 64;
    public static final KxmlSoapExtractor INSTANCE = new KxmlSoapExtractor();
    private static final Logger LOG = LogManager.getLogger(KxmlSoapExtractor.class);

    private static void updateInitSize(int size) {
        initSize = size;
    }

    @Override
    public final void extract(Map<String, String> patterns, String content, RequestContext request) {
        if (patterns == null) {
            return;
        }
        KXmlParser parser = new KXmlParser();
        int[] stack = new int[32];
        int stackSize = 0;
        StringBuilder xpath = new StringBuilder(initSize);
        StringBuilder result = new StringBuilder();
        String params = request.getParams();
        if (params != null) {
            result.append(params);
        }
        try {

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(new StringReader(content));
            int event;
            boolean found = false;
            boolean func = false;
            String name = null;
            boolean body = false;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        String namespace = parser.getNamespace();
                        if (!body) {
                            if ("Body".equals(parser.getName())) {
                                body = true;
                            }
                        } else {
                            String qName = parser.getName();
                            int mark = xpath.length();
                            if (namespace.isEmpty()) {
                                xpath.append('/').append(qName);
                            } else {
                                xpath.append("/{").append(namespace).append('}').append(qName);
                            }
                            int length = xpath.length();
                            if (initSize < length) {
                                updateInitSize(length);
                            }
                            name = patterns.get(xpath.toString());
                            found = name != null;
                            if (found) {
                                char firstChar = name.charAt(0);
                                if (firstChar == '@') {
                                    int pos = name.indexOf('=');
                                    String nameAttribute = name.substring(1, pos);
                                    String valueAttribute = name.substring(pos + 1);
                                    String value = name = null;
                                    for (int i = parser.getAttributeCount() - 1; i >= 0; i--) {
                                        if (nameAttribute.equals(parser.getAttributeName(i))) {
                                            name = parser.getAttributeValue(i);
                                        } else if (valueAttribute.equals(parser.getAttributeName(i))) {
                                            value = parser.getAttributeValue(i);
                                        }
                                    }
                                    found = false;
                                    if (name != null && value != null) {
                                        if ("msisdn".equals(name)) {
                                            if (value.startsWith("0")) {
                                                value = "84" + value.substring(1);
                                            } else if (!value.startsWith("84")) {
                                                value = "84" + value;
                                            }
                                        }
                                        if (result.length() != 0) {
                                            result.append(',');
                                        }
                                        result.append('"').append(name).append("\":\"").append(StringEscapeUtils.escapeJava(value)).append('"');
                                    }
                                } else if (firstChar == '$') {
                                    found = false;
                                    if ("$$".equals(name)) {
                                        func = true;
                                    } else {
                                        request.setFunc(name.substring(1));
                                    }
                                }
                            }
                            stack[stackSize++] = mark;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (stackSize != 0) {
                            xpath.setLength(stack[--stackSize]);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (func) {
                            request.setFunc(parser.getText());
                            func = false;
                        } else if (found) {
                            if (result.length() != 0) {
                                result.append(',');
                            }
                            result.append('"').append(name).append("\":\"").append(StringEscapeUtils.escapeJava(parser.getText())).append('"');
                            found = false;
                        }

                        break;
                }
            }
            if (result.length() > 0) {
                request.setParams(result.toString());
            }
        } catch (Exception e) {
            LOG.info("Parse Soap", e);
        }
    }

    public void extract(String content, Map<AppendableString, String> patterns, RequestContext request) {
        if (patterns == null) {
            return;
        }
        KXmlParser parser = new KXmlParser();
        int[] stack = new int[32];
        int stackSize = 0;
        AppendableString xpath = new AppendableString(initSize);
        StringBuilder result = new StringBuilder();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(new StringReader(content));
            int event;
            boolean found = false;
            String name = null;
            boolean body = false;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        String namespace = parser.getNamespace();
                        if (!body) {
                            if ("Body".equals(parser.getName())) {
                                body = true;
                            }
                        } else {
                            String qName = parser.getName();
                            int mark = xpath.length();
                            if (namespace.isEmpty()) {
                                xpath.append('/').append(qName);
                            } else {
                                xpath.append("/{").append(namespace).append('}').append(qName);
                            }
                            int length = xpath.length();
                            if (initSize < length) {
                                updateInitSize(length);
                            }
                            name = patterns.get(xpath);
                            found = name != null;
                            if (found) {
                                char firstChar = name.charAt(0);
                                if (firstChar == '@') {
                                    int pos = name.indexOf('=');
                                    String nameAttribute = name.substring(1, pos);
                                    String valueAttribute = name.substring(pos + 1);
                                    String value = name = null;
                                    for (int i = parser.getAttributeCount() - 1; i >= 0; i--) {
                                        if (nameAttribute.equals(parser.getAttributeName(i))) {
                                            name = parser.getAttributeValue(i);
                                        } else if (valueAttribute.equals(parser.getAttributeName(i))) {
                                            value = parser.getAttributeValue(i);
                                        }
                                    }
                                    found = false;
                                    if (name != null && value != null) {
                                        if (result.length() != 0) {
                                            result.append(',');
                                        }
                                        result.append('"').append(name).append("\":\"").append(StringEscapeUtils.escapeJava(value)).append('"');
                                    }
                                } else if (firstChar == '$') {
                                    found = false;
                                    request.setFunc(name.substring(1));
                                }
                            }
                            stack[stackSize++] = mark;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (stackSize != 0) {
                            xpath.setLength(stack[--stackSize]);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (found) {
                            if (result.length() != 0) {
                                result.append(',');
                            }
                            result.append('"').append(name).append("\":\"").append(StringEscapeUtils.escapeJava(parser.getText())).append('"');
                        }
                        found = false;
                        break;
                }
            }
            if (result.length() > 0) {
                request.setParams(result.toString());
            }
        } catch (Exception e) {
            LOG.info("Parse Soap", e);
        }
    }

    public final void extractResponse(Map<String, String> patterns, String content, RequestContext request) {
        if (patterns == null) {
            return;
        }

        KXmlParser parser = new KXmlParser();
        int[] stack = new int[32];
        int stackSize = 0;
        StringBuilder xpath = new StringBuilder(initSize);
        StringBuilder result = new StringBuilder();
        try {

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(new StringReader(content));
            int event;
            boolean found = false;
            String name = null;
            boolean body = false;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        String namespace = parser.getNamespace();
                        if (!body) {
                            if ("Body".equals(parser.getName())) {
                                body = true;
                            }
                        } else {
                            String qName = parser.getName();
                            int mark = xpath.length();
                            if (namespace.isEmpty()) {
                                xpath.append('/').append(qName);
                            } else {
                                xpath.append("/{").append(namespace).append('}').append(qName);
                            }
                            int length = xpath.length();
                            if (initSize < length) {
                                updateInitSize(length);
                            }
                            name = patterns.get(xpath.toString());
                            found = name != null;
                            if (found) {
                                char firstChar = name.charAt(0);
                                if (firstChar == '@') {
                                    int pos = name.indexOf('=');
                                    String nameAttribute = name.substring(1, pos);
                                    String valueAttribute = name.substring(pos + 1);
                                    String value = name = null;
                                    for (int i = parser.getAttributeCount() - 1; i >= 0; i--) {
                                        if (nameAttribute.equals(parser.getAttributeName(i))) {
                                            name = parser.getAttributeValue(i);
                                        } else if (valueAttribute.equals(parser.getAttributeName(i))) {
                                            value = parser.getAttributeValue(i);
                                        }
                                    }
                                    found = false;
                                    if (name != null && value != null) {
                                        if (result.length() != 0) {
                                            result.append(',');
                                        }
                                        result.append('"').append(name).append("\":\"").append(StringEscapeUtils.escapeJava(value)).append('"');
                                    }
                                }
                            }
                            stack[stackSize++] = mark;
                            //stack.push(mark);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (stackSize != 0) {
                            xpath.setLength(stack[--stackSize]);
                        }
//            if (!stack.isEmpty()) {
//              xpath.setLength(stack.pop());
//            }
                        break;
                    case XmlPullParser.TEXT:
                        if (found) {
                            if (result.length() != 0) {
                                result.append(',');
                            }
                            result.append('"').append(name).append("\":\"").append(StringEscapeUtils.escapeJava(parser.getText())).append('"');
                        }
                        found = false;
                        break;
                }
            }
            if (result.length() > 0) {
                request.setRespParams(result.toString());
            }
        } catch (Exception e) {
            LOG.info("Parse Soap", e);
        }
    }
}
