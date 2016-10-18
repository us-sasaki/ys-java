package abdom.location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import abdom.data.json.*;


/**
 * GPS で取得される位置情報をもとに生成される地図上の点
 *
 * @author	Yusuke Sasaki
 * @version 2016/8/28
 */
public class Plot {
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HH:mm:ss");
	
    public double latitude;
    public double longitude;
    public double distance; // 単位は m  , 1つ前の点を用いて計算
    public double velocity; // 単位は m/s, 1つ前の点を用いて計算
    public boolean isOutlier = false;
    public long time; // 単位はmsec
    public String date;
    
	// この点に紐づく写真データ(通常なく、null)
	public String photoFileName;
	
	/** 正確さ(=standard deviation(メートル)) */
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
	 * Plot の配列を JsonType の配列に変換。ただし outlier は除く
	 *
	 * JsonObject は、次の型
	 * <pre>
	 *    lat  : <number(float)>
	 *    lng  : <number(float)>
	 *    date : <string>
	 *    velo : <number(float)>
	 *    photoFile : <string>
	 * </pre>
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
	/**
	 * Plot を JsonObject に変換。
	 * JsonObject は、次の型。桁数を減らすため、内部表現の double を float に
	 * 変換しています。(float では精度が 10cm くらいになります)
	 * <pre>
	 *    lat  : <number(float)>
	 *    lng  : <number(float)>
	 *    date : <string>
	 *    velo : <number(float)>
	 *    photoFile : <string>
	 * </pre>
	 *
	 * @return	変換された JsonObject
	 */
	public JsonObject toJson() {
		JsonObject jo = new JsonObject();
		jo.add("lat",(float)latitude); // 桁数を減らすため float に(精度は10cmくらい)
		jo.add("lng",(float)longitude); // 桁数を減らすため float に(精度は10cmくらい)
		jo.add("date",date);
		jo.add("velo",(float)velocity); // 桁数を減らすため float に(精度は10cmくらい)
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
