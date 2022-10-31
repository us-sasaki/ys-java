package com.ntt.tc.data.users;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import com.ntt.tc.data.C8yData;

/**
 * Role class
 * This source is machine-generated from c8y-markdown docs.
 *
 * 規定のロールを定数として追加
 */
public class Role extends C8yData {
	public static final String ROLE_SIMULATOR_ADMIN = "ROLE_SIMULATOR_ADMIN";
	public static final String ROLE_TENANT_STATISTICS_READ = "ROLE_TENANT_STATISTICS_READ";
	public static final String ROLE_OPTION_MANAGEMENT_ADMIN = "ROLE_OPTION_MANAGEMENT_ADMIN";
	public static final String ROLE_OPTION_MANAGEMENT_READ = "ROLE_OPTION_MANAGEMENT_READ";
	public static final String ROLE_APPLICATION_MANAGEMENT_ADMIN = "ROLE_APPLICATION_MANAGEMENT_ADMIN";
	public static final String ROLE_APPLICATION_MANAGEMENT_READ = "ROLE_APPLICATION_MANAGEMENT_READ";
	public static final String ROLE_USER_MANAGEMENT_ADMIN = "ROLE_USER_MANAGEMENT_ADMIN";
	public static final String ROLE_USER_MANAGEMENT_READ = "ROLE_USER_MANAGEMENT_READ";
	public static final String ROLE_USER_MANAGEMENT_CREATE = "ROLE_USER_MANAGEMENT_CREATE";
	public static final String ROLE_USER_MANAGEMENT_OWN_ADMIN = "ROLE_USER_MANAGEMENT_OWN_ADMIN";
	public static final String ROLE_USER_MANAGEMENT_OWN_READ = "ROLE_USER_MANAGEMENT_OWN_READ";
	public static final String ROLE_USER_MANAGEMENT_PASSWORD_RESET = "ROLE_USER_MANAGEMENT_PASSWORD_RESET";
	public static final String ROLE_IDENTITY_ADMIN = "ROLE_IDENTITY_ADMIN";
	public static final String ROLE_IDENTITY_READ = "ROLE_IDENTITY_READ";
	public static final String ROLE_INVENTORY_ADMIN = "ROLE_INVENTORY_ADMIN";
	public static final String ROLE_INVENTORY_CREATE = "ROLE_INVENTORY_CREATE";
	public static final String ROLE_INVENTORY_READ = "ROLE_INVENTORY_READ";
	public static final String ROLE_MEASUREMENT_ADMIN = "ROLE_MEASUREMENT_ADMIN";
	public static final String ROLE_MEASUREMENT_READ = "ROLE_MEASUREMENT_READ";
	public static final String ROLE_EVENT_ADMIN = "ROLE_EVENT_ADMIN";
	public static final String ROLE_EVENT_READ = "ROLE_EVENT_READ";
	public static final String ROLE_ALARM_ADMIN = "ROLE_ALARM_ADMIN";
	public static final String ROLE_ALARM_READ = "ROLE_ALARM_READ";
	public static final String ROLE_AUDIT_ADMIN = "ROLE_AUDIT_ADMIN";
	public static final String ROLE_AUDIT_READ = "ROLE_AUDIT_READ";
	public static final String ROLE_DEVICE_CONTROL_ADMIN = "ROLE_DEVICE_CONTROL_ADMIN";
	public static final String ROLE_DEVICE_CONTROL_READ = "ROLE_DEVICE_CONTROL_READ";
	public static final String ROLE_CEP_MANAGEMENT_ADMIN = "ROLE_CEP_MANAGEMENT_ADMIN";
	public static final String ROLE_CEP_MANAGEMENT_READ = "ROLE_CEP_MANAGEMENT_READ";
	public static final String ROLE_RETENTION_RULE_READ = "ROLE_RETENTION_RULE_READ";
	public static final String ROLE_RETENTION_RULE_ADMIN = "ROLE_RETENTION_RULE_ADMIN";
	public static final String ROLE_BULK_OPERATION_READ = "ROLE_BULK_OPERATION_READ";
	public static final String ROLE_BULK_OPERATION_ADMIN = "ROLE_BULK_OPERATION_ADMIN";
	public static final String ROLE_DATA_BROKER_ADMIN = "ROLE_DATA_BROKER_ADMIN";
	public static final String ROLE_DATA_BROKER_READ = "ROLE_DATA_BROKER_READ";
	public static final String ROLE_SMARTRULE_READ = "ROLE_SMARTRULE_READ";
	public static final String ROLE_SMARTRULE_ADMIN = "ROLE_SMARTRULE_ADMIN";
	public static final String ROLE_TENANT_MANAGEMENT_ADMIN = "ROLE_TENANT_MANAGEMENT_ADMIN";
	public static final String ROLE_TENANT_MANAGEMENT_READ = "ROLE_TENANT_MANAGEMENT_READ";
	public static final String ROLE_TENANT_MANAGEMENT_CREATE = "ROLE_TENANT_MANAGEMENT_CREATE";
	public static final String ROLE_TENANT_MANAGEMENT_UPDATE = "ROLE_TENANT_MANAGEMENT_UPDATE";
	public static final String ROLE_ACCOUNT_ADMIN = "ROLE_ACCOUNT_ADMIN";
	
	/**
	 * ロールの値を持つ Set
	 * API などで設定値のチェックなどに利用することを想定。
	 */
	public static final Set<String> ROLES;
	static {
		// public static final String ROLE_... で指定されるフィールドから
		// 自動生成
		ROLES = new HashSet<String>();
		Class<Role> c = Role.class;
		Field[] fields = c.getFields();
		for (Field f : fields) {
			if (f.getType() != String.class) continue;
			int m = f.getModifiers();
			if (!Modifier.isStatic(m)) continue;
			if (!Modifier.isFinal(m)) continue;
			String name = f.getName();
			if (!name.startsWith("ROLE_")) continue;
			ROLES.add(name);
		}
	}
	
	/**
	 * Uniquely identifies a Role
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String id;
	
	/**
	 * Descriptive name of the role, following role naming pattern.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String name;
	
}
