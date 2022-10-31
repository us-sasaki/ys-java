package com.ntt.tc.net;

import java.util.ArrayList;
import java.util.List;

import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;
import abdom.data.json.Jsonizable;

/**
 * SmartRest テンプレートを作成しやすくするための便利クラスです。
 * 使い方は以下の通りです。
 * <pre>
 * SmartRestBuilder srb = new SmartRestBuilder();
 * SmartRestBuilder.Entry entry = srb.createEntry("sasatest", 100);
 * entry.setEndpoint("PUT", "/inventory/managedObject/"+srb.placeholder);
 *
 * JsonType template = new JsonObject().put("c8y_Position", new C8y_Position());
 * entry.setTemplate(template); // Jsonizable でテンプレートの元を設定
 *
 * entry.setNumberHolder("c8y_Position.lat"); // プレースホルダを指定
 * entry.setNumberHolder("c8y_Position.lng"); // プレースホルダを指定
 *
 * entry.setResponseTemplate("$","$.name","$.name");
 *
 * String tmplcsv = srb.toTemplateCsv();
 * </pre>
 *
 * @author		Yusuke Sasaki
 * @version		October 12, 2018
 */
public class SmartRestBuilder implements Cloneable {
	public static final String PLACEHOLDER = "%%";
	
	private static final String STRING = "%%STRING";
	private static final String UNSIGNED = "%%UNSIGNED";
	private static final String INTEGER = "%%INTEGER";
	private static final String NUMBER = "%%NUMBER";
	private static final String NOW = "%%NOW";
	private static final String DATE = "%%DATE";
	private static final String[] HOLDERS = { STRING, UNSIGNED, INTEGER, NUMBER, NOW, DATE};
	
	protected List<Entry> entries;
	
/*-------------
 * constructor
 */
	/**
	 * SmartRest テンプレート作成オブジェクトを生成します。
	 */
	public SmartRestBuilder() {
		entries = new ArrayList<Entry>();
	}

/*------------------
 * instance methods
 */
	public Entry createEntry(int id) {
		Entry e = new Entry(id);
		entries.add(e);
		return e;
	}
	
	public Entry createEntry(String xid, int id) {
		Entry e = new Entry(xid, id);
		entries.add(e);
		return e;
	}
	
	public String toTemplateCsv() {
		StringBuilder sb = new StringBuilder();
		
		String lastXid = null;
		for (Entry e : entries) {
			if (e.xid == null) {
				if (lastXid == null) throw new IllegalArgumentException("最初の Entry には xid が必要です");
			} else {
				// xid が変更
				if (!e.xid.equals(lastXid)) {
					// xid
					sb.append("15,");
					sb.append(e.xid);
					sb.append("\r\n");
					lastXid = e.xid;
				}
			}
			sb.append(e.toTemplateCsv());
		}
		return sb.toString();
	}

/*-------------
 * inner class
 */
	public class Entry {
		protected String xid;
		protected int id;
		protected String method;
		protected String uri;
		protected String contentType;
		protected String accept = "application/json";
		protected JsonType template;
		protected String responseTemplate;
		
	/*-------------
	 * constructor
	 */
		/**
		 * id はリクエスト用メッセージ識別子で、100,101のような数値を指定します。
		 * 百の位が奇数である必要があります。
		 * (偶数のものは応答用メッセージ識別子で利用します)
		 *
		 * @param		id		リクエスト用メッセージ識別子。100, 101 など。
		 *						百の位が奇数である必要あり。
		 *						応答用メッセージ識別子がある場合、200, 201 など
		 *						リクエスト用メッセージ識別子に +100 した値を利用
		 *						します。
		 */
		private Entry(int id) {
			setId(id);
		}
		
		/**
		 * id はリクエスト用メッセージ識別子で、100,101のような数値を指定します。
		 * 百の位が奇数である必要があります。
		 * (偶数のものは応答用メッセージ識別子で利用します)
		 *
		 * @param		xid		X-Id (テンプレートID)の値, 文字列(myxid1など)
		 * @param		id		リクエスト用メッセージ識別子。100, 101 など。
		 *						百の位が奇数である必要あり。
		 *						応答用メッセージ識別子がある場合、200, 201 など
		 *						リクエスト用メッセージ識別子に +100 した値を利用
		 *						します。
		 */
		private Entry(String xid, int id) {
			setXid(xid);
			setId(id);
		}
	
	/*------------------
	 * instance methods
	 */
		/**
		 * テンプレート X-Id を設定します。
		 *
		 * @param		xid 	X-Id (テンプレートID)の値, 文字列(myxid1など)
		 */
		public void setXid(String xid) {
			this.xid = xid;
		}
		/**
		 * テンプレート id を設定します。
		 *
		 * @param		id 		リクエスト用メッセージ識別子。100, 101 など。
		 *						百の位が奇数である必要あり。
		 *						応答用メッセージ識別子がある場合、200, 201 など
		 *						リクエスト用メッセージ識別子に +100 した値を利用
		 */
		public void setId(int id) {
			if ( id < 0 || (id/100)%2 == 0)
				throw new IllegalArgumentException("id の百の位は奇数値として下さい");
			
			this.id = id;
		}
		/**
		 * http method を設定します。
		 *
		 * @param		method		GET/POST/PUT/DELETE のいずれか
		 * @param		uri			/ ではじまる endpoint で、PLACEHOLDER を含む場合
		 *							すべて STRING 指定となります。
		 */
		public void setEndpoint(String method, String uri) {
			if (!"GET".equals(method)
					&& !"POST".equals(method)
					&& !"PUT".equals(method)
					&& !"DELETE".equals(method) )
				throw new IllegalArgumentException("method は GET/POST/PUT/DELETE のいずれかである必要があります: "+method);
			if (contentType != null)
				if ("GET".equals(method) || "DELETE".equals(method))
					throw new IllegalArgumentException("GET/DELETE では Content-Type 指定できません");
			this.method = method;
			if (!uri.startsWith("/"))
				throw new IllegalArgumentException("uri は / ではじまる endpoint です: " + uri);
			this.uri = uri;
		}
		
		/**
		 * Content-Type を指定します。GET/DELETE では null とする必要があります。
		 *
		 * @param		contentType		application/vnd.com.nsn.cumulocity.managedObject+json
		 *								のような Content-Type
		 */
		public void setContentType(String contentType) {
			if (contentType != null) {
				if ("GET".equals(method))
					throw new IllegalArgumentException("GET では Content-Type を指定できません");
				if ("DELETE".equals(method))
					throw new IllegalArgumentException("DELETE では Content-Type を指定できません");
			}
			this.contentType = contentType;
		}
		
		/**
		 * Accept を指定します。本メソッドで明示的に指定しない場合、
		 * application/json が設定されます。
		 *
		 * @param		accept		application/vnd.com.nsn.cumulocity.managedObject+json
		 *								のような Accept 設定値
		 */
		public void setAccept(String accept) {
			this.accept = accept;
		}
		
		/**
		 * テンプレート文字列を設定します。テンプレートは JSON 文字列で指定しま	す。
		 * プレースホルダは "%%" (ダブルクオーテーションつき)を利用して下さい
		 *
		 * @param		template		テンプレート(JSON文字列)
		 */
		public void setTemplate(String template) {
			checkExcludesPlaceholders(template);
			this.template = JsonType.parse(template);
		}
		
		/**
		 * テンプレートとなる Jsonizable を指定します。
		 * Jsonizable のため、プレースホルダは含まれません。ここに
		 * setNumberHolder などのプレースホルダ設定メソッドでプレースホルダを
		 * 指定して下さい。指定した後、このメソッドを呼ぶとプレースホルダ指定が
		 * 上書き(キャンセル)されてしまうことに注意して下さい。
		 * Jsonizable として、JsonObject や ManagedObject などが利用できます。
		 * プレースホルダは、特定の文字列値を持つ JsonValue への置き換えで実装
		 * されています。したがって、もともとの JSON 文字列にプレースホルダ用の
		 * 文字列が含まれていた場合、誤動作します。実装制約としています。
		 * プレースホルダ用の特定の文字列値は以下の通りです。
		 * "%%STRING", %%INTEGER, %%UNSIGNED, %%NUMBER, %%DATE, %%NOW, %%
		 *
		 * @param		jsonTemplate	テンプレート
		 * @see			#setNumberHolder
		 * @see			#setUnsignedHolder
		 * @see			#setIntegerHolder
		 * @see			#setStringHolder
		 * @see			#setNowHolder
		 * @see			#setDateHolder
		 */
		public void setTemplate(Jsonizable jsonTemplate) {
			checkExcludesPlaceholders(jsonTemplate.toString());
			this.template = jsonTemplate.toJson();
		}
		
		/**
		 * 指定された文字列に PLACEHOLDER 文字列があるかチェックします。
		 * あった場合、 IllegalArgumentException をスローします。
		 *
		 * @param		target		チェック対象文字列
		 */
		private void checkExcludesPlaceholders(String target) {
			if (target.contains(PLACEHOLDER))
				throw new IllegalArgumentException("テンプレートにプレースホルダ用文字列 " + PLACEHOLDER + " が含まれています");
			for (String p : HOLDERS) {
				if (target.contains(p))
					throw new IllegalArgumentException("テンプレートにプレースホルダ用文字列 " + p + " が含まれています");
			}
		}
		
		/**
		 * 指定した JSON Path の値を NUMBER のプレースホルダに設定します。
		 * テンプレートにない path も指定可能です。(新しく生成されます)
		 *
		 * @param		path		JSON path
		 */
		public void setNumberHolder(String path) {
			if (template == null) template = new JsonObject();
			template.put(path, NUMBER);
		}
		
		/**
		 * 指定した JSON Path の値を INTEGER のプレースホルダに設定します。
		 * テンプレートにない path も指定可能です。(新しく生成されます)
		 *
		 * @param		path		JSON path
		 */
		public void setIntegerHolder(String path) {
			if (template == null) template = new JsonObject();
			template.put(path, INTEGER);
		}
		
		/**
		 * 指定した JSON Path の値を UNSIGNED のプレースホルダに設定します。
		 * テンプレートにない path も指定可能です。(新しく生成されます)
		 *
		 * @param		path		JSON path
		 */
		public void setUnsignedHolder(String path) {
			if (template == null) template = new JsonObject();
			template.put(path, UNSIGNED);
		}
		
		/**
		 * 指定した JSON Path の値を STRING のプレースホルダに設定します。
		 * テンプレートにない path も指定可能です。(新しく生成されます)
		 * STRING プレースホルダは、テンプレート内で "" でくくられます。
		 *
		 * @param		path		JSON path
		 */
		public void setStringHolder(String path) {
			if (template == null) template = new JsonObject();
			template.put(path, "\""+STRING+"\""); // "" でくくるための特殊処理
		}
		
		/**
		 * 指定した JSON Path の値を NOW のプレースホルダに設定します。
		 * テンプレートにない path も指定可能です。(新しく生成されます)
		 *
		 * @param		path		JSON path
		 */
		public void setNowHolder(String path) {
			if (template == null) template = new JsonObject();
			template.put(path, NOW);
		}
		
		/**
		 * 指定した JSON Path の値を DATE のプレースホルダに設定します。
		 * テンプレートにない path も指定可能です。(新しく生成されます)
		 *
		 * @param		path		JSON path
		 */
		public void setDateHolder(String path) {
			if (template == null) template = new JsonObject();
			template.put(path, DATE);
		}
		
		/**
		 * 応答用テンプレート情報を登録します。登録は必須ではありません。
		 * メッセージ識別子(id)はリクエスト用テンプレート情報に 100 を加えたもの
		 * になります。
		 *
		 * @param		basePath	ベースJSONパス
		 * @param		checkPath	存在確認を行う JSON パス
		 * @param		values		応答に含める値のリスト(
		 */
		public void setResponseTemplate(String basePath, String checkPath, String... values) {
			if (values == null || values.length == 0)
				throw new IllegalArgumentException("values は省略できません");
			StringBuilder sb = new StringBuilder();
			sb.append("11,");
			sb.append(id+100);
			sb.append(',');
			sb.append('\"');
			if (basePath != null) sb.append(basePath);
			sb.append("\",");
			sb.append('\"');
			if (checkPath != null) sb.append(checkPath);
			for (String value : values) {
				sb.append("\",\"");
				sb.append(value);
				sb.append('\"');
			}
			
			this.responseTemplate = sb.toString();
		}
		
		/**
		 * SmartREST テンプレート登録用CSV形式を作成します。
		 * xid, method, uri, template が null(未指定)の場合、
		 * IllegalArgumentException がスローされます。
		 *
		 * @return	SmartREST で設定可能なテンプレート登録用CSV文字列
		 */
		public String toTemplateCsv() {
			if (xid == null)
				throw new IllegalArgumentException("xid が設定されていません");
			if (method == null || uri == null)
				throw new IllegalArgumentException("endpoint が設定されていません");
			if (template == null)
				throw new IllegalArgumentException("template が設定されていませ	ん");
			
			StringBuilder sb = new StringBuilder();
			// xid はスキップ
			
			// 固定値10
			sb.append("10,");
			
			// id
			sb.append(id);
			
			// endpoint
			sb.append(',');
			sb.append(method);
			sb.append(',');
			sb.append(uri);
			
			// content
			sb.append(',');
			if (contentType != null) {
				if ("GET".equals(method) || "DELETE".equals(method))
					throw new IllegalArgumentException("GET/DELETE では Content-Type 指定できません");
				sb.append(contentType);
			}
			if ("POST".equals(method) || "PUT".equals(method)) {
				sb.append("application/json");
			}
			
			// accept
			sb.append(',');
			sb.append(accept);
			
			// placeholder
			sb.append(',');
			sb.append(PLACEHOLDER);
			
			// params
			StringBuilder params = new StringBuilder();
			
			for (int uriind = 0; uriind < uri.length(); uriind++) {
				int index = uri.indexOf(PLACEHOLDER, uriind);
				if (index == -1) break;
				params.append(' ');
				params.append("STRING");
				uriind = index + PLACEHOLDER.length();
			}
			
			String tmpl = template.toString(); // JSON String
			for (int i = 0; i < tmpl.length(); i++) {
				int min = tmpl.length();
				String key = null;
				for (String p : HOLDERS) {
//				if (p == STRING) p = "\\\"%%STRING\\\"";
					int index = tmpl.indexOf(p, i);
					if (index > -1 && min > index) {
						// PLACEHOLDER が見つかった
						min = index;
						if (p == STRING) { // STRING 特例処理
						key = "STRING";
						} else {
							key = p.substring(2);
						}
					}
				}
				if (min < tmpl.length()) {
					// PLACEHOLDER が見つかっていた
					params.append(' ');
					params.append(key);
					i = min + 1;
				}
			}
			sb.append(',');
			sb.append(params.toString().substring(1));
			
			// template
			// tmpl の %%STRING などを %% に変換
			for (String p : HOLDERS) {
				if (p == STRING) continue; // STRING は "" でくくるため後で
				tmpl = tmpl.replace(p, PLACEHOLDER);
			}
			tmpl = tmpl.replace("\\\""+STRING+"\\\"","\""+PLACEHOLDER+"\"");
			
			sb.append(',');
			tmpl = tmpl.replace("\""+PLACEHOLDER+"\"", PLACEHOLDER);
			tmpl = tmpl.replace("\"", "\"\""); // escape "
			sb.append('\"');
			sb.append(tmpl);
			sb.append('\"');
			
			// response template がある場合
			if (responseTemplate != null) {
				sb.append("\r\n"); // CR+LF
				sb.append(responseTemplate);
			}
			
			sb.append("\r\n"); // CR+LF
			
			return sb.toString();
		}
	}
}
