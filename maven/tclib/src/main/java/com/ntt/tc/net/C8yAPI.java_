package com.ntt.tc.net;

public class C8yAPI {
	protected Rest rest;
	
/*
 * Constructor
 */
	public C8yAPI(Rest rest) {
		this.rest = rest;
	}
	
	public C8yAPI(String urlStr, String tenant, String user, String password) {
		rest = new Rest(urlStr, tenant, user, password);
	}
	
	public C8yAPI(String urlStr, String user, String password) {
		this(urlStr, "", user, password);
	}
	
/*------------------
 * instance methods
 */
	/**
	 * デバイスクレデンシャルを要求します。
	 *
	 * @param	req		デバイスクレデンシャルのオブジェクト
	 *					id は必須です。
	 * @return	要求後、更新されたデバイスクレデンシャルのオブジェクト
	 *			承認された場合、isValid() が true になります。
	 *			承認されなかった場合(not found)、値は変化しません。
	 */
	public DeviceCredentials getDeviceCredential(DeviceCredentials req)
				throws IOException {
		if (req.isValid()) return req;
		if (req.id == null || req.id.equals(""))
				throw IllegalArgumentException("DeviceCredential の id に値が必要です");
		Rest.Response resp = rest.post("/devicecontrol/deviceCredentials", "deviceCredentials", req);
		if (resp.code == 404) return req;
		req.fill(resp);
		return req;
	}
	
    /**
     * Step 1.
     * externalId が登録されているか確認します。
     * ManagedObject 登録 → externalId 付与の順のため、externalId がない
     * ことは ManagedObject もないことを示します。
     * externalId は、config ファイルで指定がなかった場合、
     * デフォルト値 "ext-"+id を c8y_Serial として確認します。
     *
     */
    boolean existsExternalId() throws IOException {
        Rest r = getRest();
        Rest.Response resp = r.get("/identity/externalIds/c8y_Serial/" + hard.externalId, "externalId");
        if (resp.code == 200) return true; // 存在する
        if (resp.code == 404) return false; // 存在しない

        printResp(resp);
        throw new IOException("An error occurred while externalId checking: "+resp.message);
    }

    /**
     * Step 2.
     * 管理オブジェクトを新規登録します。
     */
    void registerManagedObject() throws IOException {
        Rest r = getRest();
        Rest.Response resp = r.post("/inventory/managedObjects", "managedObject", managedObject.toJson() );

        if (resp.code == 201) {	// Created
            println("resp of register ManagedObject:"+resp.toString());
            managedObject.id = resp.toJson().get("id").getValue();
            writePref();
        }
    }

    /**
     * Step 3.
     * ManagerObject に externalId を紐づけます
     */
    void registerExternalId() throws IOException {
        Rest r = getRest();
        Rest.Response resp = r.post("/identity/globalIds/"+managedObject.id+"/externalIds", "externalId", hard.toJson());

        if (resp.code == 201) { // Created
            hard.fill(resp);
            writePref();
            return;
        }
        printResp(resp);
        throw new IOException("An error occurred while registering externalId.");
    }

    /**
     * Step 4.
     * ManagedObject を最新化します
     * 更新したい managedObject id に、更新部分のみを送信すればOK
     */
    void updateManagedObject() throws IOException {

        ManagedObject mo = new ManagedObject();
        mo.c8y_Position = new C8y_Position();
        mo.c8y_Position.alt = 0d;
        mo.c8y_Position.lat = 38d;
        mo.c8y_Position.lng = 136d;

        Rest r = getRest();
        Rest.Response resp = r.put("/inventory/managedObjects/"+managedObject.id, "managedObject", mo);
        if (resp.code == 200) { // OK
            return;
        }
        throw new IOException("An error occurred while updating mo."+resp.message);
    }

    void updateManagedObjectLocation(double lat, double lng, double alt) throws IOException {
        ManagedObject mo = new ManagedObject();
        mo.c8y_Position = new C8y_Position();
        mo.c8y_Position.alt = alt;
        mo.c8y_Position.lat = lat;
        mo.c8y_Position.lng = lng;

        Rest r = getRest();
        Rest.Response resp = r.put("/inventory/managedObjects/"+managedObject.id, "managedObject", mo);
        if (resp.code == 200) { // OK
            return;
        }
        throw new IOException("An error occurred while updating mo location."+resp.message);
    }

    /**
     * Measurement キューに追加します、としようとしたが、同一センサーの
     * 複数 Measurement 送信の仕方がわからないので、最新のものに上書き。
     * 送信は sendMeasurements で行います。
     */
    void addMeasurement(Jsonizable toAdd) {
        if (measurement == null) {
            measurement = new Measurement(managedObject, "YS_AndroidMeasurement");
        }
        measurement.time = new TC_Date(); // 最新化
        measurement.fill(toAdd);
    }

    void addMeasurement(String field, String valueName, float value, String unit) {
        JsonType jt = JsonType.o("value", value).put("unit", unit);
        addMeasurement(JsonType.o(field, JsonType.o(valueName, jt)));
    }

    void addAccelerator(String field, float value) {
        addMeasurement(field, "acceleration", value, "m/s2");
    }

    void addGravity(String field, float value) {
        addMeasurement(field, "gravity", value, "m/s2");
    }

    void addLight(float value) {
        addMeasurement("c8y_LightMeasurement", "e", value, "lux"); // unit is SI lux
    }

    void addMagnetic(String field, float value) {
        addMeasurement(field, "magnetic", value, "uT"); // unit uT means micro-Tesla
    }

    void addOrient(String field, float value) {
        addMeasurement(field, "orient", value, "deg"); // degree
    }

    /**
     * Step 9 Send Measurements
     */
    public void sendMeasurements() throws IOException {
        if (measurement == null) {
            println("measurement is null");
            return;
        }
        Log.d(TAG, "sending measurement.");

        Rest r = getRest();
        Rest.Response resp = r.post("/measurement/measurements", "measurement", measurement);
        if (resp.code == 201) { // Created
            Log.d(TAG, "measurement was successfully sent.");
            Log.v(TAG, resp.toString("  "));
            return;
        }
        throw new IOException("An error occurred while sending measurement."+resp.message);
    }

    /**
     * Step 10 Send Events
     */
    // 次は、event で Location update をしたい
    // あと、バイナリファイルを送受信したい
    public void sendEvents() throws IOException {
        Event e = new Event(managedObject, "c8y_LocationUpdate", "location Changed event.");
        C8y_Position c8y_Position = new C8y_Position();
        c8y_Position.alt = 0d;
        c8y_Position.lat = 38d;
        c8y_Position.lng = 136d;
        c8y_Position.trackingProtocol = "TELIC";
        c8y_Position.reportReason = "Time Event";
        e.putExtra("c8y_Position", c8y_Position);

        Rest r = getRest();
        Rest.Response resp = r.post("/event/events", "event", e.toJson());
        if (resp.code == 201) { // Created
            e.fill(resp);
            println(resp.toString("  "));
            return;
        }
        throw new IOException("An error occurred while sending event."+resp.message);
    }

    public void sendLocationUpdate(double lat, double lng, double alt) throws IOException {
        Event e = new Event(managedObject, "c8y_LocationUpdate", "location changed");
        C8y_Position c8y_Position = new C8y_Position();
        c8y_Position.alt = alt;
        c8y_Position.lat = lat;
        c8y_Position.lng = lng;
        c8y_Position.trackingProtocol = "Google Fused Location Provider";
        c8y_Position.reportReason = "Update Event";
        e.putExtra("c8y_Position", c8y_Position.toJson());

        Rest r = getRest();
        Rest.Response resp = r.post("/event/events", "event", e.toJson());
        if (resp.code == 201) { // Created
            println(resp.toString("  "));
            return;
        }
        throw new IOException("An error occurred while sending location update.");
    }




    /**
     * Step 11 Send Alarms
     */
    public void sendAlarms() throws IOException {
        Alarm a = new Alarm(managedObject.id, "Test alarm");

        println(a.toString("  "));
        Rest r = getRest();
        Rest.Response resp = r.post("/alarm/alarms", "alarm", a.toJson());
        if (resp.code == 201) { // Created
            return;
        }
        println("Response : " + resp.code);
        println("Message  : " + resp.message);

        throw new IOException("An error occurred while sending alarm."+resp.message);
    }

    /**
     * measurement 送信
     */
    public void cycle() throws IOException {
        while (true) {
            sendMeasurements();
            //sendEvents();
            //sendAlarms();
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException ignored) {
                break;
            }
        }
    }

    /**
     * Binary アップロード
     */
    public void uploadBinary(String filename,
                              String mimetype,
                              byte[] binary) throws IOException {
        ManagedObject mo = new ManagedObject();
        mo.name = filename;
        mo.type = mimetype;

        Rest r = getRest();
        Rest.Response resp = r.postBinary(filename, mimetype, binary);
        println(resp.code);
        println(resp.message);
        println(resp.toString("  "));
    }
}

