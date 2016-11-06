import java.util.List;

import abdom.app.gpsview.GPSRefiner;
import abdom.app.gpsview.GPSDecorator;
import abdom.data.json.JsonObject;
import abdom.location.Plot;
import abdom.location.PlotUtils;
import abdom.location.filter.AveragingPlotsFilter;
import abdom.location.filter.CutSamePlotsFilter;
import abdom.location.filter.CutOutlierPlotsFilter;
import abdom.location.filter.CutReturningCertainPlotsFilter;
import abdom.location.filter.MarkerPlotsFilter;
import abdom.location.filter.MedianPlotsFilter;
import abdom.location.filter.PrintPlotsFilter;
import abdom.location.filter.ULMPlotsFilter;
import abdom.location.filter.VelocityPlotsFilter;

import abdom.location.interval.Interval;

public class Gpstest {
	/**
	 * main�B�ʏ�N���ł́A�S�����ŉ�́A�t�@�C���i�[���s���B
	 * args ���w�肳��Ă���ꍇ�A���̃t�@�C���ɑ΂��Amanual() ���ĂԁB
	 * manual() �͒������s���A�ڍׂȗ�O��͂ɖ𗧂Ă�B
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			GPSDecorator d = new GPSDecorator();
			d.addPhotoDirectory(".");
			d.processAllLogs("gpslog/", "json/");
			
			// Firefox �N��
			Runtime.getRuntime().exec("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe bicycle.html");
		} else {
			manual(args[0]);
		}
	}
	
	/**
	 * ������͂��邽�߂̃��\�b�h
	 */
	private static void manual(String fname) throws Exception {
		GPSRefiner g = new GPSRefiner();
		g.setGPSLogDirectory("gpslog/");
		g.addAlgorithm(new PrintPlotsFilter("--- initial data ---"));
		g.addAlgorithm(new VelocityPlotsFilter(42d));
		g.addAlgorithm(new PrintPlotsFilter("--- Velocity Cut ---"));
		g.addAlgorithm(new CutReturningCertainPlotsFilter());
		g.addAlgorithm(new PrintPlotsFilter("--- Cut Returning ---"));
		g.addAlgorithm(new CutSamePlotsFilter());
		g.addAlgorithm(new PrintPlotsFilter("--- Cut Same Plot ---"));
		g.addAlgorithm(new CutOutlierPlotsFilter(0.05d, 0.05d)); //0.05d));
		g.addAlgorithm(new PrintPlotsFilter("--- Cut Outlier ---"));
//		g.addAlgorithm(new MarkerPlotsFilter(1)); // �}�[�J�[������
		g.addAlgorithm(new ULMPlotsFilter(10d));
		g.addAlgorithm(new PrintPlotsFilter("--- ULM modifying ---"));
//		g.addAlgorithm(new AveragingPlotsFilter(15));
//		g.addAlgorithm(new PrintPlotsFilter("--- Averaging with 15 ---"));
//		g.addAlgorithm(new MedianPlotsFilter(15));
//		g.addAlgorithm(new PrintPlotsFilter("--- Median with 15 ---"));
		
		g.readGPSLog(fname);
		
		GPSDecorator d = new GPSDecorator();
		d.addPhotoDirectory("G:\\programs\\misc\\160922_������GPS�v���O��������\\photo\\");
		System.out.println("--- set stops ---");
		d.setStops(g);
		System.out.println("--- set photo file name ---");
		d.setPhotoFileName(g);
		System.out.println(d.getMetaInfoAsJson(g));
		// �t�@�C����������
		abdom.data.json.JsonArray j = g.getPlotsAsJson();
		
		PlotUtils.writeString("json/"+fname, ".json", j.toString());
		
		g.writeVelocity(fname);
		
		// Firefox �N��
		Runtime.getRuntime().exec("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe bicycle.html?fname="+fname.substring(0,fname.lastIndexOf('.'))+".json");
	}
}
