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
	 * �f�o�C�X�N���f���V������v�����܂��B
	 *
	 * @param	req		�f�o�C�X�N���f���V�����̃I�u�W�F�N�g
	 *					id �͕K�{�ł��B
	 * @return	�v����A�X�V���ꂽ�f�o�C�X�N���f���V�����̃I�u�W�F�N�g
	 *			���F���ꂽ�ꍇ�AisValid() �� true �ɂȂ�܂��B
	 *			���F����Ȃ������ꍇ(not found)�A�l�͕ω����܂���B
	 */
	public DeviceCredentials getDeviceCredential(DeviceCredentials req)
				throws IOException {
		if (req.isValid()) return req;
		if (req.id == null || req.id.equals(""))
				throw IllegalArgumentException("DeviceCredential �� id �ɒl���K�v�ł�");
		Rest.Response resp = rest.post("/devicecontrol/deviceCredentials", "deviceCredentials", req);
		if (resp.code == 404) return req;
		req.fill(resp);
		return req;
	}
	
    /**
     * Step 1.
     * externalId ���o�^����Ă��邩�m�F���܂��B
     * ManagedObject �o�^ �� externalId �t�^�̏��̂��߁AexternalId ���Ȃ�
     * ���Ƃ� ManagedObject ���Ȃ����Ƃ������܂��B
     * externalId �́Aconfig �t�@�C���Ŏw�肪�Ȃ������ꍇ�A
     * �f�t�H���g�l "ext-"+id �� c8y_Serial �Ƃ��Ċm�F���܂��B
     *
     */
    boolean existsExternalId() throws IOException {
        Rest r = getRest();
        Rest.Response resp = r.get("/identity/externalIds/c8y_Serial/" + hard.externalId, "externalId");
        if (resp.code == 200) return true; // ���݂���
        if (resp.code == 404) return false; // ���݂��Ȃ�

        printResp(resp);
        throw new IOException("An error occurred while externalId checking: "+resp.message);
    }

    /**
     * Step 2.
     * �Ǘ��I�u�W�F�N�g��V�K�o�^���܂��B
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
     * ManagerObject �� externalId ��R�Â��܂�
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
     * ManagedObject ���ŐV�����܂�
     * �X�V������ managedObject id �ɁA�X�V�����݂̂𑗐M�����OK
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
     * Measurement �L���[�ɒǉ����܂��A�Ƃ��悤�Ƃ������A����Z���T�[��
     * ���� Measurement ���M�̎d�����킩��Ȃ��̂ŁA�ŐV�̂��̂ɏ㏑���B
     * ���M�� sendMeasurements �ōs���܂��B
     */
    void addMeasurement(Jsonizable toAdd) {
        if (measurement == null) {
            measurement = new Measurement(managedObject, "YS_AndroidMeasurement");
        }
        measurement.time = new TC_Date(); // �ŐV��
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
    // ���́Aevent �� Location update ��������
    // ���ƁA�o�C�i���t�@�C���𑗎�M������
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
     * measurement ���M
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
     * Binary �A�b�v���[�h
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

