package com.ntt.tc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;

import abdom.data.json.*;
import com.ntt.tc.data.*;
import com.ntt.tc.data.inventory.*;

/**
 * ManagedObject のテスト。
 */
class MOTest {
	
	@Nested
	class フィールドへの設定 {
		@Test
		void c8y_IsDeviceへの設定がJsonObjectでできる() {
			ManagedObject mo = new ManagedObject();
			mo.set("c8y_IsDevice", new JsonObject());
			assertEquals("{\"c8y_IsDevice\":{}}", mo.toString());
		}
		
		@Test
		void com_cumulocity_model_Agentへの設定がJsonObjectでできる() {
			ManagedObject mo = new ManagedObject();
			mo.set("com_cumulocity_model_Agent", new JsonObject());
			assertEquals("{\"com_cumulocity_model_Agent\":{}}", mo.toString());
			mo.set("com_cumulocity_model_Agent.someProp", 15);
			assertEquals("{\"com_cumulocity_model_Agent\":{\"someProp\":15}}", mo.toString());
		}
		
		@Test
		void com_cumulocity_model_Agentを使ってset_getできる() {
			ManagedObject mo = new ManagedObject();
			mo.set("com_cumulocity_model_Agent.someProp.someSubProp", "value");
			assertEquals("{\"com_cumulocity_model_Agent\":{\"someProp\":{\"someSubProp\":\"value\"}}}", mo.toString());
			JsonType j = mo.get("com_cumulocity_model_Agent.someProp");
			assertEquals("{\"someSubProp\":\"value\"}", j.toString());
		}
	}
	
}
