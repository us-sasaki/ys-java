package com.ntt.tc.data.rest;

import abdom.data.json.JsonObject;
import abdom.data.json.object.JData;

import com.ntt.tc.data.Id;

/**
 * POST /event/events �̗v���A����у��X�|���X
 */
public class EventResp extends JData {
	/**
	 * ���� Event �� id
	 */
	public String id;
	
	/**
	 * ���� Event ���\�[�X�ւ� URI
	 */
	public String self;
	
	/**
	 * �C�x���g��������(�f�o�C�X��)�B�v�����K�{�B
	 */
	public String time;
	
	/**
	 * �C�x���g��DB�o�^����
	 */
	public String creationTime;
	
	/**
	 * com_cumulocity_model_DoorSensorEvent �̂悤�ȕ�����B
	 * �v�����K�{�B
	 */
	public String type;
	
	/**
	 * Event �̐������ł��B�v�����K�{�B
	 */
	public String text;
	
	/**
	 * �C�x���g�̔������ł��B�v�����K�{�B
	 */
	public Id source;
}
