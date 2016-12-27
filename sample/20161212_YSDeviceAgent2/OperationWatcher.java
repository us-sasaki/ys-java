import java.io.IOException;

import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;

import com.ntt.tc.net.Rest;

/**
 * Long-polling �ɂ��C�x���g�Ď��X���b�h�𐶐����܂��B
 * ���̃I�u�W�F�N�g�Ƀ��X�i�[��ݒ肷�邱�ƂŁAOperation ���n���h�����O
 * �ł��܂��B
 *
 * @version		29 November, 2016
 * @author		Yusuke Sasaki
 */
public class OperationWatcher implements Runnable {
	protected Device2 device;
	
	/** ���X�i�[�͂P�����o�^�ł��� */
	protected OperationListener listener;
	
	/** �f�o�C�X�N���f���V�����v���p�̃f�o�C�XID(�������́H) */
	protected DeviceCredentialsResp credential;
	
/*-------------
 * Constructor
 */
	public OperationWatcher(Device2 device) {
		this.device = device;
	}
	
	
/*------------------
 * instance methods
 */
	/**
	 * long-polling ���J�n���܂��B
	 * ���̃��\�b�h�́A�f�o�C�X�N���f���V�����擾��ɌĂ�ł��������B
	 */
	public void watch() {
		Thread t = new Thread(this);
		t.start();
	}
	
/*-----------------------
 * implements (Runnable)
 */
	@Override
	public void run() {
		// Device ����N���f���V�����t���� Rest ���擾
		Rest r = device.getRest();
		
		// long-polling
		
		// hand-shake
		JsonType h = JsonType.o("id", "1")
					.put("supportedConnectionTypes", JsonType.a("long-polling"))
					.put("channel", "/meta/handshake")
					.put("version", "1.0");
		
	}
}
