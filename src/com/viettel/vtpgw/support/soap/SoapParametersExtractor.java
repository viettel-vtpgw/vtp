package com.viettel.vtpgw.support.soap;

import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;

import com.viettel.vtpgw.context.RequestContext;
import com.viettel.vtpgw.support.ParametersExtractor;

@Deprecated
public class SoapParametersExtractor implements ParametersExtractor {
	private static final Logger LOG = LogManager.getLogger(SoapParametersExtractor.class);
	@Override
	public void extract(Map<String, String> patterns, String content, RequestContext request) {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			InputSource is = new InputSource(new StringReader(content));
			SoapHandler handler = new SoapHandler(patterns,request);
			parser.parse(is, handler);			
		} catch (Exception e) {
			LOG.error("Fail to extract parameter",e);
		}		
	}
}
