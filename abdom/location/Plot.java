package abdom.location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import abdom.data.json.*;


/**
 * GPS �Ŏ擾�����ʒu�������Ƃɐ��������n�}��̓_
 *
 * 2016/8/28 Yusuke Sasaki
 */
public class Plot {
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HH:mm:ss");
	
    public double latitude;
    public double longitude;
    public double distance; // �P�ʂ� m  , 1�O�̓_��p���Čv�Z
    public double velocity; // �P�ʂ� m/s, 1�O�̓_��p���Čv�Z
    public boolean isOutlier = false;
    public long time; // �P�ʂ�msec
    public String date;
    
	// ���̓_�ɕR�Â��ʐ^�f�[�^(�ʏ�Ȃ��Anull)
	public String photoFileName;
	
	/** ���m��(=standard deviation(���[�g��)) */
	public float accuracy = -1f;
	
/*-------------
 * constructor
 */
	public Plot() {
	}
	
	public Plot(double latitude, double longitude, long time, float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.isOutlier = false;
        this.time = time;
        this.date = sdf.format(new Date(time));
        
        this.photoFileName = null;
        this.accuracy = accuracy;
	}
	
	@Deprecated
    public Plot(double latitude, double longitude, boolean isOutlier, long time, String date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.isOutlier = isOutlier;
        this.time = time;
        this.date = date;
        
        this.photoFileName = null;
    }
    
/*---------------
 * class methods
 */
	/**
	 * Plot �̔z��� JsonType �̔z��ɕϊ��B������ outlier �͏���
	 *
	 * JsonObject �́A���̌^
	 *    var lat; // number(double)
	 *    var lng; // number(double)
	 *    var date; // string
	 *    var photoFile; // string
	 */
	public static JsonObject[] toJson(Plot[] plots) {
		List<JsonType> result = new ArrayList<JsonType>();
		for (int i = 0; i < plots.length; i++) {
			if (plots[i].isOutlier) continue;
			result.add(plots[i].toJson());
		}
		return result.toArray(new JsonObject[0]);
	}
	
/*------------------
 * instance methods
 */
	public JsonObject toJson() {
		JsonObject jo = new JsonObject();
		jo.add("lat",(float)latitude); // ���������炷���� float ��(���x��10cm���炢)
		jo.add("lng",(float)longitude); // ���������炷���� float ��(���x��10cm���炢)
		jo.add("date",date);
		jo.add("velo",(float)velocity); // ���������炷���� float ��(���x��10cm���炢)
		if (photoFileName != null) {
			jo.add("photoFile", photoFileName);
		}
		return jo;
	}
	
/*-----------
 * overrides
 */
 	@Override
	public String toString() {
		return "outlier="+isOutlier+",time="+time+",lat="+latitude+",lng="+longitude+",dist="+distance+",velo="+velocity+",date="+date+"PhotoFileName="+photoFileName;
	}
	
}
