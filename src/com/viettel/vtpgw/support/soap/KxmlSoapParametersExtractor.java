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

@Deprecated
public class KxmlSoapParametersExtractor implements ParametersExtractor {
  public static final ParametersExtractor INSTANCE = new KxmlSoapParametersExtractor();
	private static final Logger LOG = LogManager.getLogger(KxmlSoapParametersExtractor.class);
	@Override
	public void extract(Map<String, String> patterns, String content, RequestContext request) {
		KXmlParser parser = new KXmlParser();
    Deque<String> stack = new ArrayDeque<>();
    StringBuilder xpath = new StringBuilder();
    StringBuilder result = new StringBuilder();
    try {
      parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      parser.setInput(new StringReader(content));
      int event;
      boolean found=false;
      String name=null;
      while ((event = parser.next())!=XmlPullParser.END_DOCUMENT) {
        switch (event){
          case  XmlPullParser.START_TAG:
            int mark = xpath.length();
            String qName = parser.getName();
            xpath.append('/').append(qName.substring(qName.indexOf(':')+1));
            name = patterns.get(xpath.toString());
            found=name!=null;
            if(found && name.charAt(0)=='@'){
            	int pos = name.indexOf('=');
            	String nameAttribute = name.substring(0,pos);
            	String valueAttribute = name.substring(pos+1);
            	String value=name=null;
            	for(int i = parser.getAttributeCount()-1;i>=0;i--){
            		if(nameAttribute.equals(parser.getAttributeName(i))){
            			name = parser.getAttributeValue(i);
            		} else if(valueAttribute.equals(parser.getAttributeName(i))){
            			value = parser.getAttributeValue(i);
            		}
            	}
            	found = false;
            	if(name!=null && value!=null){
            		result.append('"').append(name).append("\":\"").append(StringEscapeUtils.escapeJava(value)).append('"');
            	}
            }
            stack.push(xpath.substring(0,mark));
            break;
          case  XmlPullParser.END_TAG:
            xpath.setLength(0);
            xpath.append(stack.pop());
            break;
          case XmlPullParser.TEXT:
            if (found) {
              if(name.startsWith("$")){
                request.setFunc(name.substring(1));
              } else {
                if (result.length() != 0)
                  result.append(',');
                result.append('"').append(name).append("\":\"").append(StringEscapeUtils.escapeJava(parser.getText())).append('"');
              }
            }
            found=false;
            break;
        }
      }
      LOG.info("extracted: {}",result);
      request.setParams(result.toString());
    }catch (Exception e){
    	LOG.error("Parse Soap",e);      
    }
	}
}
