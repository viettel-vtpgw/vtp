package com.viettel.vtpgw.builtin.bccs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.viettel.vtpgw.context.RequestContext;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

public class BCCSGateway {
	static class Input {

		protected List<Param> param;
		@XmlElement(required = true)
		protected String password;
		protected String rawData;
		@XmlElement(required = true)
		protected String username;
		@XmlElement(required = true)
		protected String wscode;

		/**
		 * Gets the value of the param property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list will
		 * be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the param property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getParam().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list {@link Param }
		 * 
		 * 
		 */
		public List<Param> getParam() {
			if (param == null) {
				param = new ArrayList<Param>();
			}
			return this.param;
		}

		/**
		 * Gets the value of the password property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * Gets the value of the rawData property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getRawData() {
			return rawData;
		}

		/**
		 * Gets the value of the username property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getUsername() {
			return username;
		}

		/**
		 * Gets the value of the wscode property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getWscode() {
			return wscode;
		}

		/**
		 * Sets the value of the password property.
		 * 
		 * @param value
		 *          allowed object is {@link String }
		 * 
		 */
		public void setPassword(String value) {
			this.password = value;
		}

		/**
		 * Sets the value of the rawData property.
		 * 
		 * @param value
		 *          allowed object is {@link String }
		 * 
		 */
		public void setRawData(String value) {
			this.rawData = value;
		}

		/**
		 * Sets the value of the username property.
		 * 
		 * @param value
		 *          allowed object is {@link String }
		 * 
		 */
		public void setUsername(String value) {
			this.username = value;
		}

		/**
		 * Sets the value of the wscode property.
		 * 
		 * @param value
		 *          allowed object is {@link String }
		 * 
		 */
		public void setWscode(String value) {
			this.wscode = value;
		}

	}

	static class Param {

		@XmlAttribute(name = "name")
		protected String name;
		@XmlAttribute(name = "value")
		protected String value;

		/**
		 * Gets the value of the name property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getName() {
			return name;
		}

		/**
		 * Gets the value of the value property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Sets the value of the name property.
		 * 
		 * @param value
		 *          allowed object is {@link String }
		 * 
		 */
		public void setName(String value) {
			this.name = value;
		}

		/**
		 * Sets the value of the value property.
		 * 
		 * @param value
		 *          allowed object is {@link String }
		 * 
		 */
		public void setValue(String value) {
			this.value = value;
		}

	}

	private static class StaticResource {
		String buffer;
		String mimeType;

		StaticResource(String mimeType, String buffer) {
			this.mimeType = mimeType;
			this.buffer = buffer;
		}
	}

	static ConcurrentHashMap<String, Buffer> cached = new ConcurrentHashMap<>();
	private static Pattern CONTEXT = Pattern.compile("\\s(?:schemaLocation|location)\\s*=\\s*[\"']([^\\?\"']*)");
	private static final Pattern KEY_VALUE = Pattern.compile("(\\w+)\\s*=\\s*([^\\s/>]+)",
	    Pattern.DOTALL | Pattern.MULTILINE);

	private static final Logger LOG = LogManager.getLogger(BCCSGateway.class);

	private static final Pattern PARAMS = Pattern.compile("<(?:\\w+:)?param\\s.*?>", Pattern.DOTALL | Pattern.MULTILINE);

	static Map<String, StaticResource> staticResources = new HashMap<>();

	private static final Pattern WSCODE = Pattern.compile("<(?:\\w+:)?wscode>(\\w+)</(?:\\w+:)?wscode>",
	    Pattern.DOTALL | Pattern.MULTILINE);

	static {
		staticResources.put("?wsdl",
		    new StaticResource("text/xml; charset=utf-8", loadResource(BCCSGateway.class, "BCCSGateway.wsdl")));
		resources().forEach((key, value) -> staticResources.put(key,
		    new StaticResource(value.getString("mimetype"), loadResource(BCCSGateway.class, value.getString("resource")))));
	}

	public static void bccs(RequestContext reqContext, Buffer body) {
		HttpMethod method = reqContext.getMethod();
		String path = reqContext.getPath();
		String query = reqContext.getQuery();
		if (query != null)
			path += "?" + query;
		if (method == HttpMethod.GET) {
			try {
				StaticResource sr = staticResources.get(path);
				if (sr != null) {
					Buffer response = processWSDL(sr.buffer, reqContext.getContext());
					reqContext.response().putHeader(HttpHeaders.CONTENT_TYPE, sr.mimeType).end(response);
				} else {
					reqContext.response().setStatusCode(404).end(path + " not found");
				}
			} catch (Exception e) {
				LOG.error("Unknow Exception", e);
				reqContext.response().setStatusCode(500).end(e.getMessage());
			}
		} else if (method == HttpMethod.POST) {
			long start = System.currentTimeMillis();
			String content = body.toString();
			Matcher m = WSCODE.matcher(content);
			Input in = new Input();
			if (m.find()) {
				in.setWscode(m.group(1));
				reqContext.setFunc(in.getWscode());
				if ("getInfoSub201401".equals(in.getWscode())) {
					m = PARAMS.matcher(content);
					while (m.find()) {

						Matcher m2 = KEY_VALUE.matcher(m.group());
						String value = null;
						boolean found = false;
						while (m2.find()) {
							String g2 = m2.group(2);
							g2 = g2.substring(1, g2.length() - 1);// skip quote
							if ("name".equals(m2.group(1)) && "isdn".equals(g2)) {
								found = true;
							} else if ("value".equals(m2.group(1))) {
								value = g2;
							}
						}
						if (found && value != null) {
							boolean old = ((value.charAt(value.length() - 1) - '0') & 1) != 0;
							if (old)
								in.setWscode(in.getWscode() + "_2");
						}
					}
				}
			} else {
			}
			// Object in = SoapHelper.unmarshal2(context, msg.body());
			call(in, body, Future.<Buffer> future().setHandler(rs -> {
				if (rs.succeeded()) {
					try {
						reqContext.setExternalCall(System.currentTimeMillis() - start);
						reqContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/xml; charset=utf-8").end(rs.result());
					} catch (Exception e) {
						LOG.debug(e);
						reqContext.response().setStatusCode(500).end(e.getMessage());
					}
				} else {
					reqContext.response().setStatusCode(500).end(rs.cause().getMessage());
				}
			}), reqContext);
		}
	}

	protected static void call(Object in, Buffer rawReq, Future<Buffer> result, RequestContext context) {
		Input input = (Input) in;
		String key = input.getWscode();
		Buffer resp = cached.get(key);
		if (resp == null) {
			InputStream is = BCCSGateway.class.getResourceAsStream(input.getWscode() + ".xml");
			if (is == null)
				is = BCCSGateway.class.getResourceAsStream("notfound.xml");
			byte[] buff = new byte[1024];
			resp = Buffer.buffer();
			try {
				for (int r = is.read(buff); r != -1; r = is.read(buff)) {
					resp.appendBytes(buff, 0, r);
				}
			} catch (IOException e) {
				LOG.debug(e);
			}
			cached.put(key, resp);
		}
		result.complete(resp);
	}

	private static String loadResource(Class<?> clazz, String resource) {
		InputStream is = clazz.getResourceAsStream(resource);
		try {
			Buffer rs = Buffer.buffer(Math.max(128, is.available()));
			byte[] buff = new byte[128];
			for (int r = is.read(buff); r != -1; r = is.read(buff)) {
				rs.appendBytes(buff, 0, r);
			}
			return rs.toString(StandardCharsets.UTF_8);
		} catch (Exception e) {
			LOG.debug(e);
			return null;
		}
	}

	public static Buffer processWSDL(String xml, String newUri) {
		StringBuilder sb = new StringBuilder(xml.length());
		Matcher m = CONTEXT.matcher(xml);
		int start = 0;
		while (m.find()) {
			sb.append(xml, start, m.start(1)).append(newUri);
			start = m.end();
		}
		sb.append(xml, start, xml.length());
		return Buffer.buffer(sb.toString());
	}

	private static Map<String, JsonObject> resources() {
		Map<String, JsonObject> map = new HashMap<>();
		map.put("?xsd=1", new JsonObject().put("resource", "BCCSGateway.xsd").put("mimetype", "text/xml"));
		return map;
	}
}
