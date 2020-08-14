package com.viettel.vtpgw.support;

import java.util.Map;

import com.viettel.vtpgw.context.RequestContext;

public interface ParametersExtractor {
	/**
	 * 
	 * @param patterns
	 *          map of pattern->parameter name ex
	 *          {/Envelope/Body/getListVip/msisdn->msisdn}
	 * @param content
	 *
	 * @param request
	 * 
	 * @param raw store raw request as raw param
	 * @return name1=value1|name2=value2...
	 */
	void extract(Map<String, String> patterns, String content,RequestContext request);
}
