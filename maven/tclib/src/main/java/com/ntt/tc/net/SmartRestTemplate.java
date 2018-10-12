package com.ntt.tc.net;

import java.util.List;

import abdom.data.json.JsonType;

/**
 * SmartRest テンプレートを作成しやすくするためのクラスです。
 *
 * @author		Yusuke Sasaki
 * @version		October 12, 2018
 */
public class SmartRestTemplate {
	protected String id;
	protected String method;
	protected String uri;
	protected String contentType;
	protected String accept;
	protected String placeholder;
	protected List<String> params;
	protected String template;
	
/*-------------
 * constructor
 */
	public SmartRestTemplate() {
	}
	
	public SmartRestTemplate(String id,
								String method,
								String uri,
								String contentType,
								String accept,
								String placeholder,
								List<String> params,
								String template) {
		//
		setId(id);
		setMethod(method);
		setUri(uri);
		setContentType(contentType);
		setAccept(accept);
		setPlaceholder(placeholder);
		setParams(params);
		setTemplate(template);
	}

/*------------------
 * instance methods
 */
	/**
	 * テンプレート id を設定します。
	 *
	 * @param		id 		設定する id(10,11のいずれか)
	 */
	public void setId(String id) {
		if (!"10".equals(id) && !"11".equals(id))
			throw new IllegalArgumentException("id は 10(request), 11(response) のいずれかである必要があります: "+id);
		this.id = id;
	}
	/**
	 * http method を設定します。
	 *
	 * @param		method		GET/POST/PUT/DELETE のいずれか
	 */
	public void setMethod(String method) {
		if (!"GET".equals(method)
				&& !"POST".equals(method)
				&& !"PUT".equals(method)
				&& !"DELETE".equals(method) )
			throw new IllegalArgumentException("method は GET/POST/PUT/DELETE のいずれかである必要があります: "+method);
		this.method = method;
	}
	/**
	 * URI(endpoint) を設定します。
	 *
	 * @param		uri		URI
	 */
	public void setUri(String uri) {
		if (!uri.startsWith("/"))
			throw new IllegalArgumentException("uri は / ではじまる endpoint です: " + uri);
		this.uri = uri;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public void setAccept(String accept) {
		this.accept = accept;
	}
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}
	public void setParams(List<String> params) {
		this.params = params;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	public void setTemplate(JsonType jsonTemplate) {
		String tmp = jsonTemplate.toString();
		tmp = tmp.replace("\"", "\"\"");
		setTemplate(tmp);
	}
}
