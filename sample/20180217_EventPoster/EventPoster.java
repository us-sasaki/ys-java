import java.io.*;
import java.util.*;

import abdom.data.json.*;
import abdom.data.json.object.*;

import com.ntt.tc.net.*;
import com.ntt.tc.data.*;
import com.ntt.tc.data.events.*;
import com.ntt.tc.data.inventory.*;

public class EventPoster {
	public static final int TEST_DEVICES = 3;
	public static final String EXTID = "cepTestId";
	public static final String LOG = "ceplog.txt";
	
	protected API api;
	protected Random rand;
	protected List<String> sources;
	
/*-------------
 * constructor
 */
	public EventPoster() {
		this.api	= new API(C8yAccount.get("sasa"));
		this.rand	= new Random(0L);
		
		// �e�X�g�p��ManagedObject��o�^
		sources = new ArrayList<String>();
		
		try {
			APIUtil util = new APIUtil(api);
			for (int i = 0; i < TEST_DEVICES; i++) {
				ManagedObject device = new ManagedObject();
				
				device.type = "sasa_cepTestDevice";
				device.c8y_IsDevice = new JsonObject();
				
				device.name = "cepTestDevice"+i;
				String extId = EXTID + i;
				String source = util.createManagedObjectIfAbsent(extId, device).id;
				sources.add(source);
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * �w�肳�ꂽ�p�����[�^�� Event ���o�͂��܂��B
	 */
	public void post(String source, String type, String text)
								throws IOException {
		Event e = new Event(source, type, text);
		api.createEvent(e);
	}
	
	/**
	 * �w�肳�ꂽ�l���o�͂��܂�
	 */
	public void postValue(String source, String type, int value)
								throws IOException {
		Event e = new Event(source, type, "postValue");
		e.set("postValue.value", value);
		
		api.createEvent(e);
		
		// post ���e����ʕ\��
		String msg = source+","+e.time.getValue()+","+value;
		System.out.println(msg);
		
		// post ���e�����O�o��(append)
		PrintWriter p = new PrintWriter(new FileWriter(LOG, true));
		p.println(msg);
		p.close();
	}
	
	/**
	 * 0�`99 �̐��l���o�͂��܂��B
	 */
	public void postRandom(String source, String type) throws IOException {
		int r = (int)(rand.nextDouble() * 100);
		postValue(source, type, r);
	}
	
/*---------------
 * class methods
 */
	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ie) {
		}
	}
	
}
