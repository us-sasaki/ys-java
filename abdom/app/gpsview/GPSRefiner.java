package abdom.app.gpsview;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.text.SimpleDateFormat;

import java.util.Iterator;
import java.util.Date;

import abdom.data.json.*;
import abdom.location.*;
import abdom.location.filter.PlotsFilter;
import abdom.location.filter.CutReturningCertainPlotsFilter;
import abdom.location.filter.CutOutlierPlotsFilter;
import abdom.location.filter.CutSamePlotsFilter;
import abdom.location.filter.ULMPlotsFilter;
import abdom.location.filter.VelocityPlotsFilter;
import abdom.location.interval.Interval;
import abdom.location.interval.StopPicker;
import abdom.location.interval.StopPicker2;
import abdom.location.interval.IntervalDivider;

/**
 * �t�@�C����ǂ݁AList<Plot>, List<Interval> �ȂǍs���Ɋւ������␳�A
 * �ێ�����N���X�ł��B
 *
 * �ʐ^���Ƃ̌����ȂǁA�o�H���̕␳�ȊO�̕t�����l��t����͕̂ʂ̃N���X
 * (GPSDecorator)�ł��BInterval �����́A�␳�̖��ɗ����Ƃ�z�肵�Ă������
 * �N���X�ɑg�ݍ���ł��܂��B
 *
 * �A���S���Y���� add �� readGPSLog() �ŏC������A�Ƃ����̂���{�̎g�����ł��B
 * ���̃N���X�̕ێ����� plots, interval �͕K���Y�������Ή����邱�Ƃ�ۏ؂���
 * ���B���̂��߁A�ꕔ�� get ���\�b�h�ŕԂ�l�ɐ��񂪂���ꍇ������܂��B
 *
 * @see		abdom.app.gpsview.GPSDecorator
 */
public class GPSRefiner {
	protected String gpslogDir	= "gpslog/";
//	protected String jsonDir	= "json/";
	
	protected String filename;
	protected List<Plot> plots;
	protected List<Interval> interval;
	
	protected List<PlotsFilter> filter;
	
/*-------------
 * constructor
 */
	public GPSRefiner() {
		filter = new ArrayList<PlotsFilter>();
	}
	
/*------------------
 * instance methods
 */
	/**
	 * ���E�߂̃A���S���Y���Z�b�g��ݒ肵�܂��B
	 */
	public void setStandardAlgorithm() {
		// �S������_�Ƀ}�[�J�[�𗧂Ă�
		// ����_�́A
		// GPS(API)�̎d�l�ŉq����񂪎��Ȃ��ꍇ�ɁA
		// �@�@NW(Wifi)�ɂ��ʒu����
		// �@�A�O��Ɠ��ꌋ�ʂ�Ԃ�
		// �Ƃ��������̂��ߋN����A�Ƒz�肵�Ă���B
		// ����_�ƂȂ�m���͂قڂO�ł���A�O�񓯈�̏ꍇ���ʂ��Ȃ����ߍ폜�\
		
		// ���x 42m/s �ȏ�̓J�b�g
		filter.add(new VelocityPlotsFilter(42d));
		
		// ��̇@���J�b�g
		filter.add(new CutReturningCertainPlotsFilter());
		
		// ��̇A���J�b�g
		filter.add(new CutSamePlotsFilter());
		
		// Smirnov-Grubbs����ŁA���p����Ȃ��Ȃ�܂ŊO��l������
		filter.add(new CutOutlierPlotsFilter(0.05d));
		
		// �Ǐ��I�ɓ��������^�������肵�ĂȂ߂炩�ɂ���
//		filter.add(new ULMPlotsFilter(10d)); // 10�b�ȏ㗣�ꂽ��␳���Ȃ�
	}
	
	/**
	 * �C�ӂ̃A���S���Y����o�^���܂��B
	 */
	public void addAlgorithm(PlotsFilter pf) {
		filter.add(pf);
	}
	
	public void readGPSLog(String filename) throws IOException {
		this.filename = filename;
		plots = PlotUtils.readGPSLog(gpslogDir, filename);
		
		//
		// �_�P�ʂ̕␳���s��
		//
		for (PlotsFilter f : filter) {
			plots = f.apply(plots);
		}
		
		//
		// ��ԕ��͂��s��
		//
		
		// �ؗ���Ԃ����o����
		StopPicker2 t = new StopPicker2(plots);
		interval = t.divideByStop();
		
		// ���x�Ɋւ��A���덷(���U)���������Ȃ�悤�ɋ�Ԃ𕪊�����
		// ���x�X�����ς��Ƃ���ŕ��������͂�
		//interval = new IntervalDivider(plots).divideByVelocity(interval);
		
	}
	
	/**
	 * �Q�Ƃ�Ԃ����AList ��ύX���Ă͂Ȃ�Ȃ�(interval�Ƃ̐����������)�B
	 * �v�f�ɑ΂���ύX�͉B
	 */
	public List<Plot> getPlots() {	return plots;	}
	
	/**
	 * �Q�Ƃ�Ԃ����AList ��ύX���Ă͂Ȃ�Ȃ�(plots�Ƃ̐����������)�B
	 * �v�f�ɑ΂���ύX�͉B
	 */
	public List<Interval> getInterval() {	return interval;	}
	
//	public void setJsonDirectory(String d) {	jsonDir = d;	}
	
	public void setGPSLogDirectory(String d) {	gpslogDir = d;	}
	
	/**
	 * �w�肳�ꂽ�t�@�C�����Ŋ���̃f�B���N�g���� json �t�@�C���������o���܂��B
	 * �����R�[�h�� UTF-8 ���g�p����܂��B
	 */
//	public void writeJson(String fname) throws IOException {
//		PlotUtils.writeJson(jsonDir, fname, plots);
//	}
	public void writeVelocity(String fname) throws IOException {
		PlotUtils.writeVelocity(fname, plots);
	}
	public JsonArray getPlotsAsJson() {
		return new JsonArray(Plot.toJson(plots.toArray(new Plot[0])));
	}
	public String getFileName() { return filename; }
	
	/**
	 * plots, interval �̐�����ۂ��A�w�肳�ꂽ plot ��}������B
	 *
	 * @param	index	�}���ʒu(���̈ʒu�ȍ~�� plot ����(�E)�ɂ����)
	 * @param	p		�}������ plot
	 */
	public void addPlot(int index, Plot p) {
		if (index < 0 || index >= plots.size()) throw new IndexOutOfBoundsException("index �� 0�ȏ�A"+plots.size()+"�����łȂ���΂Ȃ�܂���B�w��l:"+index);
		plots.add(index, p);
		// interval �� index �����炷
		// �}�����ꂽ Plot �͂��Ƃ��Ǝw�肳�ꂽ index ���܂� interval ��
		// �܂܂��悤�� interval �̒������g������
		boolean needsShift = false;
		for (Interval i : interval) {
			if (i.sind <= index && index <= i.eind) {
				i.eind++;
				needsShift = true;
			} else if (needsShift) {
				i.sind++;
				i.eind++;
			}
		}
	}
	
	/**
	 * plots, interval �̐�����ۂ��A�w�肳�ꂽ�ʒu�� plot ���폜����B
	 * �ȍ~�� plot �͑O(��)�ɂ����B
	 *
	 * @param	index	�폜���� plot �̈ʒu
	 * @return	�폜���ꂽ plot
	 */
	public Plot removePlot(int index) {
		if (index < 0 || index >= plots.size()) throw new IndexOutOfBoundsException("index �� 0�ȏ�A"+plots.size()+"�����łȂ���΂Ȃ�܂���B�w��l:"+index);
		Plot p = plots.remove(index);
		boolean needsShift = false;
		Interval disappear = null;
		for (Interval i : interval) {
			if (i.sind <= index && index <= i.eind) {
				i.eind--;
				if (i.eind < i.sind) {
					// interval ����
					disappear = i; // ��ō폜
				}
				needsShift = true;
			} else if (needsShift) {
				i.sind--;
				i.eind--;
			}
		}
		// assert �g���Ă݂�
		assert disappear != null : "disappear is null";
		
		if (!interval.remove(disappear)) throw new InternalError("interval doesn\'t contain disappear.");
		return p;
	}
	
	/**
	 * plots �ŁAplots[index-1].time <= time < plots[index].time
	 * �𖞂��� index ��ԋp���܂��B
	 * �񕪒T���ɂ�茟�����܂��B
	 */
	public int getIndexOf(long time) {
		if (time >= plots.get(plots.size()-1).time) return plots.size();
		// 2���T������
		int l = 0;
		int r = plots.size() -1;
		while (true) {
			int i = (l+r)/2;
			long t = plots.get(i).time;
			if (t == time) return i+1;
			if (t > time) {
				if (r == i) return r;
				r = i;
			} else if (t < time) {
				if (l == i) return r;
				l = i;
			}
		}
	}
	
	/**
	 * �w�肳�ꂽ interval �ԍ��� interval �̓��e���A�w�肳�ꂽ List<Plot> ��
	 * �u�������܂��Binterval �̑��̑����͕ύX���܂���B
	 */
	public void replaceInterval(int index, List<Plot> p) {
		if (index < 0 || index >= interval.size() )
			throw new IndexOutOfBoundsException("index �� 0�ȏ�A"+interval.size()+"�����łȂ���΂Ȃ�܂���B�w��l:"+index);
		//List<Interval> left = interval.subList(0, index);
		List<Interval> right = interval.subList(index+1, interval.size());
		Interval replaced = interval.get(index);
		
		//
		// plots �̑���
		//
		
		// eind - sind + 1 �� Plot ���폜
		for (int j = 0; j < replaced.eind - replaced.sind + 1; j++) {
			plots.remove(replaced.sind); // �����ʒu�������΂悢
		}
		plots.addAll(replaced.sind, p); // �}��
		
		//
		// interval �̑���
		//
		int newEind = replaced.sind + p.size() - 1;
		int move = newEind - replaced.eind;
		replaced.eind = newEind;
		// ��� interval �� index �����炷
		for (Interval i : right) {
			i.sind += move;
			i.eind += move;
		}
	}
	
	/**
	 * �w�肳�ꂽ interval �̓��e���A�w�肳�ꂽ List<Plot> �ɒu�������܂��B
	 * interval �̑��̑����͕ύX���܂���B
	 * �w�肳�ꂽ interval ���܂܂�Ȃ��ꍇ�ARuntimeException ���X���[���܂��B
	 */
	public void replaceInterval(Interval i, List<Plot> p) {
		int index = interval.indexOf(i);
		if (index == -1) throw new RuntimeException("Interval���܂܂�܂���");
		replaceInterval(index, p);
	}
	
	/**
	 * �w�肳�ꂽ interval �ԍ��ɑΉ����� List<Plot> ��ԋp���܂��B
	 * ���ʂ́AList.subList() �I�y���[�V�����ɂ�萶������邽�߁A
	 * getPlots() ���l�AList ��ύX���Ă͂Ȃ�܂���B
	 */
	public List<Plot> getPlotsOfInterval(int index) {
		Interval i = interval.get(index);
		
		return plots.subList(i.sind, i.eind + 1);
	}
	
	/**
	 * �w�肳�ꂽ interval �ɑΉ����� List<Plot> ��ԋp���܂��B
	 * ���ʂ́AList.subList() �I�y���[�V�����ɂ�萶������邽�߁A
	 * getPlots() ���l�AList ��ύX���Ă͂Ȃ�܂���B
	 */
	public List<Plot> getPlotsOfInterval(Interval i) {
		return plots.subList(i.sind, i.eind + 1);
	}
	
	
}
