package com.ntt.tc.data.inventory;

import abdom.data.json.JsonObject;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;

/**
 * ManagedObject class.
 * guides のリファレンスで Object * と記述されているもののうち、固定的に利用
 * される次のフィールドを明示的に定義。<br>
 * (1) サンプルに記述され、標準Applicationで参照されると考えられる
 * c8y_SupportedOperations, c8y_Hardware, c8y_Configuration 等<br>
 * (2) fragment のうち、Device-management, サンプル等で記述されている
 * c8y_IsDevice, com_cumulocity_model_Agent 等<br>
 * (3) Sensor に記載される、inventory における representation。
 * 例：c8y_TemperatureSensor, c8y_HumiditySensor 等。<br>
 * 
 * <pre>
 * ManagedObject の判定の仕方(type)
 *                        type=ActilityDeviceType
 * デバイスグループ       type=c8y_DeviceGroup
 * シミュレーター(設定)   type=c8y_DeviceSimulator
 *                        type=c8y_MQTTDevice
 *                        type=c8y_PrivateSmartRule
 *                        type=c8y_SmartRule
 *                        type=c8y_UserPreference
 *                        type=text/csv や text/plain や image/svg+xml など
 *
 * ManagedObject の判定の仕方(fragment)...type ではわからない
 * デバイス               c8y_IsDevice:{} がある
 * SmartRESTテンプレート  com_cumulocity_model_smartrest_SmartRestTemplateがある
 * </pre>
 */
public class ManagedObject extends C8yData {
	/**
	 * Unique identifier of the object, automatically allocated when the object
	 * is created (see above).
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String id;
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String self;
	
	/**
	 * The most specific type of the managed object as fully qualified
	 * Java-style type name, dots replaced by underscores.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : Optional
	 * </pre>
	 * デバイスグループの場合、"c8y_DeviceGroup"<br>
	 * サブグループでは深さによらず "c8y_DeviceSubgroup"<br>
	 * CEPメトリックを持つオブジェクト "c8y_CepAgent"<br>
	 */
	public String type;
	
	/**
	 * Human-readable name that is used for representing the object in user
	 * interfaces.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : Optional
	 * </pre>
	 */
	public String name;
	
	/**
	 * Additional properties associated with the specific ManagedObject.
	 * <pre>
	 * Occurs : 0..n
	 * PUT/POST : Optional
	 * </pre>
	 */
	//This field has omitted because of type and field = "*"
	
	/**
	 * The time when the object was last updated.
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public TC_Date lastUpdated;
	
	/**
	 * A collection of references to child devices.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : No
	 * </pre>
	 */
	public ManagedObjectReferenceCollection childDevices;
	
	/**
	 * A collection of references to child assets.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : No
	 * </pre>
	 */
	public ManagedObjectReferenceCollection childAssets;
	
	/**
	 * A collection of references to device parent objects.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : No
	 * </pre>
	 */
	public ManagedObjectReferenceCollection deviceParents;
	
	/**
	 * A collection of references to asset parent objects.
	 * <pre>
	 * Occurs : 0..1
	 * PUT/POST : No
	 * </pre>
	 */
	public ManagedObjectReferenceCollection assetParents;
	
	// 以下は、Reference にないが、サンプルとして出てくる予約語と思われる
	// フィールド
	/**
	 * "c8y_SupportedOperations" states that this device can be restarted and
	 * configured. In addition, it can carry out software and firmware updates.
	 * To enable reloading configuration through the user interface, add
	 * "c8y_SendConfiguration" to the list of supported operations as
	 * described above.
	 * inventory / ManagedObject では Object * 扱いで、
	 * Device Management Library に記載されるフィールド。
	 */
	public C8y_SupportedOperation[] c8y_SupportedOperations;
	
	/**
	 * Postman のテンプレートで見つけたもの。
	 * String 配列で、"c8y_TemperatureMeasurement" のようなメジャーメントを
	 * 指定するらしい。おそらく、実際にメジャーメントを送ることなく
	 * データとして選べるようにするためか。(2017/6/18)
	 */
	public String[] c8y_SupportedMeasurements;
	
	/**
	 * c8y_Hardware contains basic hardware information for a device, such as
	 * make and serial number. Often, the hardware serial number is printed on
	 * the board of the device or on an asset tag on the device to uniquely
	 * identify the device within all devices of the same make.
	 * inventory / ManagedObject では Object * 扱いで、
	 * Device Management Library に記載されるフィールド。
	 * これを設定すると、デバイス管理で値が表示されるようになる。
	 */
	public C8y_Hardware c8y_Hardware;
	
	/**
	 * c8y_Configuration permits a text-based configuration of the device.
	 * Most devices support a textual system configuration file that can be
	 * presented and edited using this mechanism. In the inventory,
	 * "c8y_Configuration" represents the currently active configuration on
	 * the device. As part of an operation, "c8y_Configuration" requests the
	 * device to make the transmitted configuration the currently active one.
	 * To enable configuration through the user interface, add
	 * "c8y_Configuration" to the list of supported operations as described
	 * above.
	 * inventory / ManagedObject では Object * 扱いで、
	 * Device Management Library に記載されるフィールド。
	 */
	public C8y_Configuration c8y_Configuration;
	
	/**
	 * c8y_Mobile holds basic connectivity-related information, such as the
	 * equipment identifier of the modem (IMEI) in the device. This identifier
	 * is globally unique and often used to identify a mobile device.
	 * inventory / ManagedObject では Object * 扱いで、
	 * Device Management Library に記載されるフィールド。
	 */
	public C8y_Mobile c8y_Mobile;
	
	/**
	 * c8y_CellInfo provides detailed information about the closest mobile
	 * cell towers. When the functionality is activated, the location of the
	 * device is determined based on this fragment, in order to track the
	 * device whereabouts when GPS tracking is not available.
	 */
	public C8y_CellInfo c8y_CellInfo;
	
	/**
	 * c8y_Firmware contains information on a device's firmware. In the
	 * inventory, "c8y_Firmware" represents the currently installed firmware
	 * on the device. As part of an operation, "c8y_Firmware" requests the
	 * device to install the indicated firmware. To enable firmware
	 * installation through the user interface, add "c8y_Firmware" to the
	 * list of supported operations as described above.
	 * inventory / ManagedObject では Object * 扱いで、
	 * Device Management Library に記載されるフィールド。
	 */
	public C8y_Firmware c8y_Firmware;
	
	/**
	 * c8y_SoftwareList is a List of software entries that define the name,
	 * version and url for the software.
	 */
	public C8y_SoftwareList[] c8y_SoftwareList;
	
	/**
	 * inventory / ManagedObject では Object * 扱いで、
	 * Rest Developer's Guide の例で現れるフィールド。
	 * c8y_SupportedOperations の要素としてもこれが文字列として現れる。
	 */
	public C8y_Software c8y_Software;
	
	/**
	 * Map ウィジェットで表示される位置
	 */
	public C8y_Position c8y_Position;
	
	// { } の値しか持たないフィールド
	/**
	 * A device marked in the inventory with a c8y_IsDevice fragment supports
	 * device management. Only devices with this fragment appear in the device
	 * management user interface.
	 * "c8y_IsDevice" marks devices that can be managed using Cumulocity's
	 * Device Management.
	 * Rest developer's guide / Device Management Library に記載されており、
	 * 予約語扱いのため、明示的フィールド化。
	 * これを設定すると、デバイスとして認識され、デバイス管理で表示される
	 * デバイス数としてカウントされるようになる。
	 * 設定は、
	 * <pre>
	 * managedObject.c8y_IsDevice = new C8yData();
	 * </pre>
	 * のように、C8yData(のサブクラス) を設定して下さい。(JSON上、{} となる)
	 */
	public C8yData c8y_IsDevice;
	
	/**
	 * "com_cumulocity_model_Agent" marks devices running a Cumulocity agent.
	 * Such devices will receive all operations targeted to themselves and
	 * their children for routing.
	 * Rest developer's guide に記載されており、予約語扱いのため、明示的
	 * フィールド化。
	 *
	 * デバイスのオペレーションの作成／読み出し／更新を行うため、デバイスは
	 * 既存エージェントの「childDevices」階層に属していなければなりません。
	 * エージェントをインベントリ内で作成する場合、フラグメント
	 * 「com_cumulocity_model_Agent」を有するマネージドオブジェクトを作成する
	 * 必要があります。
	 * 設定は、
	 * <pre>
	 * managedObject.com_cumulocity_model_Agent = new C8yData();
	 * </pre>
	 * のように、C8yData を設定して下さい。(JSON上、{} となる)
	 */
	public C8yData com_cumulocity_model_Agent;
	
	/**
	 * Guides / Referense / Sensor にある inventory 表現。
	 * ManagementObject の要素としては Object * と表現されている。
	 *
	 * @see	com.ntt.tc.data.sensor.C8y_TemperatureMeasurement
	 */
	public C8yData c8y_TemperatureSensor;
	
	/**
	 * Guides / Referense / Sensor にある inventory 表現。
	 * ManagementObject の要素としては Object * と表現されている。
	 *
	 * @see	com.ntt.tc.data.sensor.C8y_MotionMeasurement
	 */
	public C8yData c8y_MotionSensor;
	
	/**
	 * Guides / Referense / Sensor にある inventory 表現。
	 * ManagementObject の要素としては Object * と表現されている。
	 *
	 * @see	com.ntt.tc.data.sensor.C8y_AccelerationMeasurement
	 */
	public C8yData c8y_AccelerationSensor;
	
	/**
	 * Guides / Referense / Sensor にある inventory 表現。
	 * ManagementObject の要素としては Object * と表現されている。
	 *
	 * @see	com.ntt.tc.data.sensor.C8y_LightMeasurement
	 */
	public C8yData c8y_LightSensor;
	
	/**
	 * Guides / Referense / Sensor にある inventory 表現。
	 * ManagementObject の要素としては Object * と表現されている。
	 *
	 * @see	com.ntt.tc.data.sensor.C8y_HumidityMeasurement
	 */
	public C8yData c8y_HumiditySensor;
	
	/**
	 * Guides / Referense / Sensor にある inventory 表現。
	 * ManagementObject の要素としては Object * と表現されている。
	 *
	 * @see	com.ntt.tc.data.sensor.C8y_MoistureMeasurement
	 */
	public C8yData c8y_MoistureSensor;
	
	/**
	 * Guides / Referense / Sensor にある inventory 表現。
	 * ManagementObject の要素としては Object * と表現されている。
	 *
	 * @see com.ntt.tc.data.sensor.C8y_DistanceMeasurement
	 */
	public C8yData c8y_DistanceSensor;
	
	/**
	 * Guides / Referense / Sensor にある inventory 表現。
	 * ManagementObject の要素としては Object * と表現されている。
	 *
	 * @see com.ntt.tc.data.sensor.C8y_SinglePhaseElectricityMeasurement
	 */
	public C8yData c8y_SinglePhaseElectricitySensor;
	
	/**
	 * Guides / Referense / Sensor にある inventory 表現。
	 * ManagementObject の要素としては Object * と表現されている。
	 *
	 * @see com.ntt.tc.data.sensor.C8y_ThreePhaseElectricityMeasurement
	 */
	public C8yData c8y_ThreePhaseElectricitySensor;
	
	/**
	 * Guides / Referense / Sensor にある inventory 表現。
	 * ManagementObject の要素としては Object * と表現されている。
	 *
	 * @see com.ntt.tc.data.sensor.C8y_CurrentMeasurement
	 */
	public C8yData c8y_CurrentSensor;
	
	/**
	 * Guides / Referense / Sensor にある inventory 表現。
	 * ManagementObject の要素としては Object * と表現されている。
	 *
	 * @see com.ntt.tc.data.sensor.C8y_VoltageMeasurement
	 */
	public C8yData c8y_VoltageSensor;
	
	/**
	 * Guides / Referense / Sensor にある inventory 表現。
	 * ManagementObject の要素としては Object * と表現されている。
	 */
	public C8y_Relay c8y_Relay;
	
/*
 * 以降、新たに見つけたプロパティを記載
 */
	/**
	 * SCADAウィジェットに利用する svg ファイルに付与されていた
	 * 値は {}
	 */
	//public C8y_Global c8y_Global;
	
	/**
	 * binaries で取得できるファイルを示すものと思われる
	 * 値は ""
	 */
	//public C8y_IsBinary c8y_IsBinary;
	
}
