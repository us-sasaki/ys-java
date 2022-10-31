package com.ntt.tc.data.tenants;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.tenants.ApplicationReferenceCollection;
import abdom.data.json.JsonObject;

/**
 * Tenant class
 * create する際の必須情報を引数としたコンストラクタを追加
 */
public class Tenant extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 0..1
	 * Visibility : Public
	 * </pre>
	 */
	public String self;
	
	/**
	 * Tenant id
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * max length : "32"
	 * </pre>
	 */
	public String id;
	
	/**
	 * Status of tenant, possible values [ACTIVE, SUSPENDED].
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public String status;
	
	/**
	 * Administrator user name. Whitespaces, slashes, +$: characters not
	 * allowed
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * max length : "50"
	 * </pre>
	 */
	public String adminName;
	
	/**
	 * Administrator Email
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public String adminEmail;
	
	/**
	 * Can this tenant create its own tenants.
	 * create 時に指定すると、エラーが発生するためカット
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public (Private in docs)
	 * </pre>
	 */
	//public boolean allowCreateTenants;
	
	/**
	 * Storage quota per device the user has.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public long storageLimitPerDevice;
	
	/**
	 * Administrator Password
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public String adminPass; // adminPassword in docs
	
	/**
	 * Enable password reset
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public boolean sendPasswordResetEmail;
	
	/**
	 * URL of tenants domain.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * max length : "256"
	 * </pre>
	 */
	public String domain;
	
	/**
	 * Tenants company name.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * max length : "256"
	 * </pre>
	 */
	public String company;
	
	/**
	 * Contact person name.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * max length : "30"
	 * </pre>
	 */
	public String contactName;
	
	/**
	 * Contact person phone number.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * max length : "20"
	 * </pre>
	 */
	public String contactPhone;
	
	/**
	 * Collection of tenant subscribed, applications.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Private
	 * </pre>
	 */
	public ApplicationReferenceCollection applications;
	
	/**
	 * Collection of tenant owned, applications.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public - only applications with availability MARKET
	 * </pre>
	 */
	public ApplicationReferenceCollection ownedApplications;
	
	/**
	 * Keeps a list of custom properties
	 * <pre>
	 * Occurs : 1
	 * Visibility : optional
	 * </pre>
	 */
	public JsonObject customProperties;
	
	/**
	 * Name of parent tenant, the creator of this tenant.
	 * <pre>
	 * Occurs : 1
	 * Visibility : Public
	 * </pre>
	 */
	public String parent;
	
/*-------------
 * constructor
 */
	public Tenant() {
	}
	
	/**
	 * 新規テナント生成に必要な最低限のパラメータを指定して Tenant オブジェクト
	 * を生成します。
	 * company は id が指定され、sendPasswordResetEmail = false、
	 * storageLimitPerDevice は 0 となります。
	 *
	 * @param		id			テナントid
	 * @param		domain		ドメイン(tenant.domain.com の形式)
	 * @param		adminName	管理者のログインID
	 * @param		adminPass	管理者のログインパスワード
	 * @param		adminEmail	管理者のメールアドレス
	 */
	public Tenant(String id,
					String domain,
					String adminName,
					String adminPass,
					String adminEmail) {
		this.id			= id;
		this.domain		= domain;
		this.adminName	= adminName;
		this.adminPass	= adminPass;
		this.adminEmail	= adminEmail;
		company					= id;
		sendPasswordResetEmail	= false;
		storageLimitPerDevice	= 0;
	}
	
}
