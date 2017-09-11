print('hogehoge');

var arrayList = Java.type("java.util.ArrayList");
var a = new arrayList;
var ManagedObject = Java.type("com.ntt.tc.data.inventory.ManagedObject");
var JsonType = Java.type("abdom.data.json.JsonType");
var j = new ManagedObject();
j.id =  "12345";
j.name = "Test Device";
j.c8y_IsDevice = JsonType.parse("{}");
print(j.toString("  "));

var k = new ManagedObject();
k.id = "22222";

print(j.getDifference(k));
print(k.getDifference(j));

//var System = Java.type("java.lang.System");
//System.exit(-1);

j.fill(k.getDifference(j));
print(j);
print(k.toJson().equals(j.toJson()));

var byteArray = [0,0x10,0xff,3,4,5,6,7];
var jbyteArray = Java.to(byteArray, "byte[]");
for (var i = 0; i < jbyteArray.length; i++)
	print(jbyteArray[i]);

print("cos0="+Math.cos(1));
var JMath = Java.type("java.lang.Math");
print("jcos0="+JMath.cos(1));
