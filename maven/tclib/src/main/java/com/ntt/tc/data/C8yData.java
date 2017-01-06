package com.ntt.tc.data;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import abdom.data.json.object.JData;
import abdom.data.json.object.JDataDefinitionException;
import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;

/**
 * Cumulocity �̃f�[�^�̃X�[�p�[�N���X�ł��B
 * �J�e�S�������̂ق��AJData �̒��񉻁Ac8y ���L�̃��[���ɑΉ����郁�\�b�h��
 * �񋟂��܂��B
 * C8yData �� JData ���p�����Ă��邽�߁AJSON ���񉻂��T�|�[�g���܂��B
 * �Ⴆ�΁A<pre>
 * System.out.println(new ManagedObject().toJson().toString("  "));
 * </pre>
 * �����s����ƁAManagedObject �� JSON �`���������܂��B
 */
public abstract class C8yData extends JData {
	private static final JsonValue CACHED_NULL = new JsonValue(null);
	
	/**
	 * ���̃I�u�W�F�N�g���w�肳�ꂽ�I�u�W�F�N�g�ɒl����v������
	 * JsonObject �𒊏o���܂��B�Ԃ�l�� ret �Ƃ����ꍇ�A��ʂ�
	 * this.fill(ret).equals(another) ���������܂��B�������Afill �̐���ł���
	 * �v�f�� JsonValue(null) �𖾎��I�ɐݒ�ł��Ȃ����Ƃ͓��l�ł��B
	 *
	 * @param	another		��r�Ώ�
	 * @return	������\�� JsonObject�B
	 */
	public JsonObject getDifference(C8yData another) {
		if (getClass() != another.getClass())
			throw new IllegalArgumentException("getDifference() requires "+getClass() + " instance.");
		JsonType a = this.toJson();
		JsonType b = another.toJson();
		
		JsonObject result = new JsonObject();
		
		// �L�[���}�[�W���� map �𐶐�
		Set<String> merged = new HashSet<String>(a.keySet());
//System.out.println("merged(a) = " + merged);
		for (String toAdd : b.keySet() ) {
			merged.add(toAdd);
		}
//System.out.println("merged(a,b) = " + merged);
		for (String field : merged) {
			JsonType ja = a.get(field);
			JsonType jb = b.get(field);
			
			if (ja == null) {
				if (jb != null) result.put(field, jb);
				continue;
			}
			if (ja.equals(jb)) continue;
			
			if (jb == null) result.put(field, CACHED_NULL);
			else result.put(field, jb);
		}
		return result;
/*		try {
			@SuppressWarnings("unchecked")
			T newInstance = (T)(getClass().newInstance());
			newInstance.fill(result);
			
			return newInstance;
		} catch (ReflectiveOperationException roe) {
			throw new JDataDefinitionException();
		}*/
	}
	
}
