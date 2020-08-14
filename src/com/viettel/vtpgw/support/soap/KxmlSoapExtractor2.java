package com.viettel.vtpgw.support.soap;

import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import com.viettel.vtpgw.context.RequestContext;
import com.viettel.vtpgw.support.ParametersExtractor;

import io.vertx.core.impl.StringEscapeUtils;

public class KxmlSoapExtractor2 implements ParametersExtractor {
  public static final ParametersExtractor INSTANCE = new KxmlSoapExtractor2();
  private static final Logger LOG = LogManager.getLogger(KxmlSoapExtractor2.class);

  @Override
  public void extract(Map<String, String> patterns, String content, RequestContext request) {
    KXmlParser parser = new KXmlParser();
    Deque<Integer> stack = new ArrayDeque<>();
    StringBuilder xpath = new StringBuilder();
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
                    if (result.length() != 0)
                      result.append(',');
                    result.append('"').append(name).append("\":\"").append(StringEscapeUtils.escapeJava(value)).append('"');
                  }
                } else if (firstChar == '$') {
                  found = false;
                  request.setFunc(name.substring(1));
                }                
              }
              stack.push(mark);
            }
            break;
          case XmlPullParser.END_TAG:
            if (!stack.isEmpty()) {
              xpath.setLength(stack.pop());
            }
            break;
          case XmlPullParser.TEXT:
            if (found) {
              if (result.length() != 0)
                result.append(',');
              result.append('"').append(name).append("\":\"").append(StringEscapeUtils.escapeJava(parser.getText())).append('"');
            }
            found = false;
            break;
        }
      }
      request.setParams(result.toString());
    } catch (Exception e) {
      LOG.error("Parse Soap", e);
    }
  }
}
