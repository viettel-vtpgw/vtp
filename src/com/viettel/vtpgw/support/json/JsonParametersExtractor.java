package com.viettel.vtpgw.support.json;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtpgw.context.RequestContext;
import com.viettel.vtpgw.support.ParametersExtractor;

import io.vertx.core.impl.StringEscapeUtils;

public class JsonParametersExtractor implements ParametersExtractor{
	private static final Logger LOG = LogManager.getLogger(JsonParametersExtractor.class);
	@Override
	public void extract(Map<String, String> patterns, String content, RequestContext request) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node;
		try {
			node = mapper.readTree(content);
		} catch (IOException e) {	
			LOG.error("Fail to extract parameter",e);
			return;
		}
		StringBuilder sb = new StringBuilder();
		patterns.forEach((pattern,name)->{
			JsonNode x = node.path(pattern);
			if(x!=null){
				String value = x.toString();				
				try{
					value = StringEscapeUtils.escapeJava(value);
					if (sb.length() != 0)
						sb.append(',');
					sb.append('"').append(name).append("\":\"").append(value).append('"');
				}catch(Exception e){
					LOG.error("Can not escape string",e);
				}
			}
		});		
		request.setParams(sb.toString());
	}

}
