package com.viettel.vtpgw.support.soap;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.viettel.vtpgw.context.RequestContext;

public class SoapHandler extends DefaultHandler {
	private Map<String, String> patterns;
	RequestContext request;
	private StringBuilder result;
	private Deque<String> stack;
	private StringBuilder xpath;

	public SoapHandler(Map<String, String> patterns, RequestContext request) {
		this.patterns = patterns;
		stack = new ArrayDeque<>();
		xpath = new StringBuilder();
		result = new StringBuilder();
		this.request = request;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String name = patterns.get(xpath.toString());
		if (name != null) {
			if(name.startsWith("$")){
				request.setFunc(name.substring(1));
			} else {
				if (result.length() != 0)
					result.append(';');
				result.append(name).append(':').append(ch, start, length);
			}
		}
	}

	@Override
	public void endDocument() throws SAXException {
		request.setParams(result.toString());
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		xpath.setLength(0);
		xpath.append(stack.pop());
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		stack.push(xpath.toString());
		xpath.append('/').append(qName.substring(qName.indexOf(':') + 1));
	}
}
