package abdom.app.gpsview;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.text.SimpleDateFormat;

import java.util.Date;

import abdom.data.json.*;
import abdom.image.exif.MyExifUtils;
import abdom.location.*;
import abdom.location.altitude.Altitude5m;
import abdom.location.filter.CutReturningCertainPlotsFilter;
import abdom.location.filter.CutOutlierPlotsFilter;
import abdom.location.filter.ULMPlotsFilter;
import abdom.location.interval.Interval;
import abdom.location.interval.StopPicker;
import abdom.location.interval.IntervalDivider;
import abdom.math.stats.Stats;
import abdom.net.FtpClient;

/**
 * GPSRefiner ����͂Ƃ��āAphotoFileName �ȂǁAGoogle Maps �ɓn�����𐶐����܂��B
 * �܂��AWeb�ɃA�b�v���[�h���邽�߂̍ŏI�I�ȃt�@�C���𐶐����A�A�b�v���[�h���܂��B
 *
 * 
 */
public class GPSDecorator {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HH:mm:ss");
	private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd(EEE) HH:mm");
	
//	protected String jsonDir	= "json/";
	
//	protected String photoUrl	= "img/";
	
	protected SortedMap<Long, String> photoMap;
	
	protected boolean manual = false;
	
	/** �Ή��t���ł����ʐ^�̃��X�g(�A�b�v���[�h�p) */
	protected List<String> photoList;
	
	/** �쐬���� json�t�@�C���̃��X�g(�A�b�v���[�h�p) */
	protected List<String> jsonList;
	
/*-------------
 * constructor
 */
	public GPSDecorator() {
		photoMap = new TreeMap<Long, String>();
		photoList = new ArrayList<String>();
		jsonList = new ArrayList<String>();
	}
	
/*------------------
 * instance methods
 */
	/**
	 * �s���Ɠˍ������s�� jpeg �t�@�C���̊i�[���ꂽ���[�J���f�B���N�g����
	 * �ǉ����܂��B���̑���ł́A�q�f�B���N�g�����ׂĂ��ΏۂƂȂ�܂��B
	 *
	 * @param	photoDir	�ʐ^�̊i�[���ꂽ���[�J���f�B���N�g���p�X
	 */
	public void addPhotoDirectory(String photoDir) {
//		if ( (!photoDir.endsWith("\\"))&&(!photoDir.endsWith("/")) )
//			throw new IllegalArgumentException("directory�� \\ �܂��� / �ŏI���K�v������܂�");
		// photo map (�t�@�C�����Ǝ���)���擾���Ă���
		File f = new File(photoDir);
		if (f.isDirectory()) {
			String[] p = f.list();
			if (p == null) return;
			for (String pname : p) {
				addPhotoDirectory(f+"/"+pname);
			}
		} else {
			// �Ōオ .jpg �ŏI���(jpg �͑啶���A��������ʂ��Ȃ� (?i) )
			if (f.getName().matches(".+(?i)jpg$")) {
				Date date = null;
				try {
					date = MyExifUtils.getJpegDate(photoDir);
				} catch (IOException e) {
					System.out.println(e);
				}
				if (date == null) {
					System.out.println(photoDir + "�t�@�C������������擾�ł��܂���");
					return;
				}
				photoMap.put(date.getTime(), f.getAbsolutePath());
			}
		}
	}
	/**
	 * GPSRefiner �� plots �̎ʐ^��� (photoFileName) �t�B�[���h��ݒ肵�܂��B
	 * �ʐ^�̎������Ԃ� plot ���V�K�ɑ}������܂��B
	 * setStops() �ł� plots ���k��(�팸)����A�ʗ�ʐ^�͂��鎞�Ԏ~�܂��ĎB�e
	 * ����邱�Ƃ���팸����Ă��܂����Ƃ�����܂��B
	 * �����h�����߁A���̃��\�b�h�� setStops() �̌�ɌĂԂ悤�ɂ��ĉ������B
	 *
	 * @param	g	plots ��ێ����� GPSRefiner
	 * @see		GPSRefiner
	 */
	public void setPhotoFileName(GPSRefiner g) {
		manual = true;
		
		// photoMap �̏��� map �ɑ}��(�K�؂Ȉʒu�� Plot �Ƃ��đ}������)
		List<Plot> plots = g.getPlots();
		
		long startTime	= plots.get(0).time;
		long endTime	= plots.get(plots.size()-1).time;
		
		for (Long pmapTime : photoMap.keySet()) {
			long time = pmapTime;
			if ( (startTime <= time)&&(time <= endTime) ) {
				// �ʐ^�̓������w�肳�ꂽ�t�@�C���̎��ԂɊ܂܂�Ă���
//System.out.println(photoMap.get(pmapTime) + " �� " + g.filename + " �ɑ}�����܂�");
				
				// �߂�����(�O��)���擾
				int i = g.getIndexOf(time);
				int prev = i - 1;
				if (prev < 0) prev = 0;
				
				Plot pre = plots.get(prev);
				Plot post = plots.get(i);
				
				// geodesic �łȂ��A�����Ōv�Z(�蔲��)
				double rate;
				if (post.time == pre.time) rate = 0d;
				else rate = ((double)(time - pre.time))/((double)(post.time - pre.time));
				
				double lat = (1.0d - rate) * pre.latitude + rate * post.latitude;
				double lng = (1.0d - rate) * pre.longitude + rate * post.longitude;
				
				// Plot �I�u�W�F�N�g����
				Plot photoPlot = new Plot(lat, lng, time, (pre.accuracy + post.accuracy)/2f);
				photoPlot.photoFileName = "photo:" + getFileName(photoMap.get(pmapTime));
				
				// plots, interval �̐��������}��
				g.addPlot(i, photoPlot);
				
				// photoList �ɒǉ�
				photoList.add(photoMap.get(pmapTime));
			}
		}
	}
	
	/**
	 * path ������̂����A�Ō�̕���(�t�@�C����)�𒊏o���A�ԋp����
	 * path ��؂蕶���� \ �܂��� / �Ƃ���B
	 */
	private String getFileName(String path) {
		int sl = path.lastIndexOf('/');
		int bl = path.lastIndexOf('\\');
		if (sl == -1 && bl == -1) return path;
		return path.substring( (sl > bl)? sl+1 : bl+1);
	}
	
	/**
	 * �w�肳�ꂽ GPSRefiner �̂��� stop interval �����o���A
	 * ���� interval �� interval�̎n�_�A�d�S�A�I�_�@�̂R�_�ɏW�񂵂܂��B
	 * i.e. �ݒ肵�� GPSRefiner �� plots, interval ���ύX����܂��B
	 * �܂��A�d�S�̈ʒu�� photoFileName �� stop: �^�O�����AGoogle Maps
	 * ��ɃR�[�q�[�A�C�R����\������悤�ɂ��܂��B
	 * interval ���W�񂷂邽�߁AsetPhotoFileName �̌�ɌĂԂ�
	 * ���Ƃ��� stop ��Ԓ��ɂ����� photoFileName ���폜����Ă��܂����߁A
	 * ���ӂ��Ă��������B
	 *
	 * @param	g	plots, interval ��ێ����� GPSRefiner�B
	 */
	public void setStops(GPSRefiner g) {
		manual = true;
		
		List<Interval> interval = g.getInterval();
		for (Interval i : interval) {
			if (i.label.equals("stop")) {
				List<Plot> stops = g.getPlotsOfInterval(i);
				List<Plot> replacement = new ArrayList<Plot>();
				
				Plot sp = stops.get(0);
				replacement.add(sp); // �ŏ��̓_
				
				if (stops.size() > 1) {
					// ride ruler �ő��x�X���[�W���O�������悤��
					// ���̓_�������
					replacement.add( stops.get(1) ); // ���̓_
				}
				
				Plot centroid = PlotUtils.calcCentroid(stops);
				replacement.add(centroid); // �d�S
				
				Plot ep = stops.get(stops.size()-1);
				replacement.add(ep); // �Ō�̓_
				
				if (ep.time-sp.time >= 180000L) {
					centroid.photoFileName = "stop:"+((ep.time-sp.time)/60000L) + "min.";
				}
				
				g.replaceInterval(i, replacement);
			}
		}
	}
	
	/**
	 * GPSRefiner �̎����̑������� JsonObject �Ƃ��ĕԋp���܂��B
	 *<pre>
	 *      log      : plot���O�t�@�C����
	 *     title     : �^�C�g��
	 * totalDistance : ���s��(m)
	 *     stops     : �ؗ���
	 *  totalTime    : �ړ�����(s)(�����Ԃ���ؗ����Ԃ�����������)
	 * averageSpeed  : ���ϑ��x(km/h) (�ؗ��͏���)
	 *     plots     : plot���O�t�@�C���Ɋ܂܂��_�̐�
	 *   maxSpeed    : �ő呬�x(km/h) �덷�ɂ��傫���Ȃ�X��������
	 * meanPlotInterval : plot���ϊԊu(s) �ؗ����Ԃ��܂ނ̂ő傫�����Ƃ�����
	 *  startDate    : �J�n����
	 *  finishDate   : �I������
	 *</pre>
	 */
	public JsonObject getMetaInfoAsJson(GPSRefiner g) {
		JsonObject jo = new JsonObject();
		// plots Json�t�@�C����
		String fname = g.getFileName();
		int i1 = fname.lastIndexOf('\\');
		int i2 = fname.lastIndexOf('/');
		if (i1 > i2) fname = fname.substring(i1+1);
		else if (i1 < i2) fname = fname.substring(i2+1);
		
		jo.add("log", fname.substring(0, fname.length()-3)+"json");
		
		// �^�C�g��
		i1 = fname.lastIndexOf('.');
		if (i1 == -1) jo.add("title", fname.substring(6));
		else jo.add("title", fname.substring(6, i1));
		
		// ���v���
		List<Plot> plots = g.getPlots();
		// ���s��(m)
		double dist = PlotUtils.calcTotalDistance(plots);
		jo.add("totalDistance", ((int)(dist/100d))/10f);
		// �ؗ��񐔂ƃg�[�^������
		int sc = 0;
		long time = 0;
		for (Interval i : g.getInterval()) {
			if (i.label.equals("stop")) sc++;
			else {
				time += plots.get(i.eind).time - plots.get(i.sind).time;
			}
		}
		jo.add("stops", sc);
		// �g�[�^������(sec)�A�������Ƃ܂��Ă�Ԃ͏���
		time = time/1000L;
		jo.add("totalTime", time);
		// ���ϑ��x(km/h)
		jo.add("averageSpeed", ((int)(dist/time*3600/1000*10))/10f);
		
		// ���x���
		Stats<Plot> vstats = new Stats<Plot>();
		vstats.apply(plots, p -> p.velocity);
		
		jo.add("plots", vstats.n);
		jo.add("maxSpeed", ((int)(vstats.max*3600/1000*10))/10f);
		
		// plot ���
		Stats<Plot> tstats = new Stats<Plot>();
		tstats.apply(plots, (pl, i) -> (double)(pl.get(i).time - pl.get( (i==0)?0:i-1).time));
		jo.add("meanPlotInterval", (long)(tstats.mean/1000));
		
		// photo ���
		int photos = 0;
		for (Plot p : plots) {
			if ((p.photoFileName != null)
					&&(p.photoFileName.startsWith("photo:"))) photos++;
		}
		jo.add("photos", photos);
		
		jo.add("startDate", sdf2.format(new Date(plots.get(0).time)));
		jo.add("finishDate", sdf2.format(new Date(plots.get(plots.size()-1).time)));
		
		return jo;
	}
	
	/**
	 * GPSRefiner ���쐬���AGPSDecorator �ɂ��C�����s���A�t�@�C���Ƃ��Ċi�[
	 * ���܂��BJson �t�@�C���̕����R�[�h�� UTF-8 ���w�肵�܂��B
	 * GPSRefiner �̊e�탁�\�b�h�AsetStops(), setPhotoFileName() �͓����ŌĂ΂��
	 * ���߁A�O������Ă�ł͂����܂���BIllegalStateException ���X���[����܂��B
	 */
	public void processAllLogs(String gpsLogDir, String jsonDir) throws IOException {
		if (manual) throw new IllegalStateException("setStops(), setPhotoFileName() ���Ă񂾏ꍇ�A���̃��\�b�h�͎g�p�ł��܂���");
		
		File dir = new File(gpsLogDir);
		List<String> flist = new ArrayList<String>();
		String[] f = dir.list();
		for (String fname : f) {
			// . �ꕶ�� + �C�ӌ̒��O�̕��� + txt �I���($)
			if (fname.matches("GPSLog.+txt$")) flist.add(fname);
		}
		flist.sort(null);
		
		List<JsonObject> joList = new ArrayList<JsonObject>();
		for (String fname : flist) {
System.out.println("processing.. " + fname);
			GPSRefiner g = new GPSRefiner();
			g.setGPSLogDirectory(gpsLogDir);
			g.setStandardAlgorithm();
			g.readGPSLog(fname);
			
			setStops(g);
			setPhotoFileName(g);
			
			if (g.getPlots().size() < 30) {
				System.out.println("    30plots �ɖ����Ȃ����߁A�X�L�b�v���܂�");
				continue;
			}
			
			joList.add(getMetaInfoAsJson(g));
			
			// JsonType �ɕϊ�
			
			// altitude (alt) �t��
			JsonArray ja = g.getPlotsAsJson();
			Altitude5m altDB = Altitude5m.getInstance();
			
			for (int i = 0; i < ja.size(); i++) {
				JsonType plot = ja.get(i);
				double lat = Double.parseDouble(plot.get("lat").toString());
				double lng = Double.parseDouble(plot.get("lng").toString());
				float alt = altDB.getAltitude(lat, lng);
				plot.add("alt", (int)(alt*10)/10f);
			}
			
			// �t�@�C����������
			String jsonfname = PlotUtils.changeExtension(jsonDir+fname, ".json");
			PlotUtils.writeString(jsonfname, ja.toString());
			
			// json���X�g�ɓo�^
			jsonList.add(jsonfname);
		}
		// meta data (GPSMetaData.json) ��������
		JsonArray ja = new JsonArray((Object[])joList.toArray(new JsonObject[0]));
		String metajsonfname = jsonDir+"GPSMetaData.json";
		PlotUtils.writeString(metajsonfname, ja.toString());
		
		// json���X�g�ɓo�^
		jsonList.add(metajsonfname);
	}
	
	/**
	 * FTP ���g�p���āA���̗����Ă���ʐ^�� web �T�[�o�ɃA�b�v���[�h���܂��B
	 * �A�b�v���[�h�́A�T�[�o�Ƀt�@�C�����Ȃ����A�����Ă��Â��ꍇ�ɍs���܂��B
	 *
	 * @param	host	web�T�[�o�z�X�g(URL or IP address)
	 * @param	user	FTP���O�C�����[�U
	 * @param	password FTP���O�C���p�X���[�h
	 * @param	path	�A�b�v���[�h��̃f�B���N�g���p�X(photo/ �Ȃ�)
	 */
	public void uploadPhotos(	String host,
								String user,
								String password,
								String path)
											throws IOException {
		if ( (!path.endsWith("\\"))&&(!path.endsWith("/")) )
			throw new IllegalArgumentException("path�� \\ �܂��� / �ŏI���K�v������܂�");
		FtpClient ftp = new FtpClient(host, user, password);
		ftp.connect();
		for (String photo : photoList) {
			String fname = new File(photo).getName();
			if (ftp.putIfNew(photo, path + fname))
				System.out.println(fname + " �� upload ���܂���");
		}
		ftp.close();
	}
	
	/**
	 * FTP ���g�p���āA�������ꂽJson�t�@�C���� web �T�[�o�ɃA�b�v���[�h���܂��B
	 * �A�b�v���[�h�́A�T�[�o�Ƀt�@�C�����Ȃ����A�����Ă��Â��ꍇ�ɍs���܂��B
	 *
	 * @param	host	web�T�[�o�z�X�g(URL or IP address)
	 * @param	user	FTP���O�C�����[�U
	 * @param	password FTP���O�C���p�X���[�h
	 * @param	path	�A�b�v���[�h��̃f�B���N�g���p�X(photo/ �Ȃ�)
	 */
	public void uploadJsons(	String host,
								String user,
								String password,
								String path)
											throws IOException {
		if ( (!path.endsWith("\\"))&&(!path.endsWith("/")) )
			throw new IllegalArgumentException("path�� \\ �܂��� / �ŏI���K�v������܂�");
		FtpClient ftp = new FtpClient(host, user, password);
		ftp.connect();
		for (String json : jsonList) {
			String fname = new File(json).getName();
			if (ftp.putIfNew(json, path + fname))
				System.out.println(fname + " �� upload ���܂���");
		}
		ftp.close();
	}
}
