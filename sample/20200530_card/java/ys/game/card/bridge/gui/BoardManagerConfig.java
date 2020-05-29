package ys.game.card.bridge.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;

/**
 * GUI オプション、プレイ内容制御機能としては、次を含められるようにしたい
 * ・プレイナビゲーション(解説。あるイベントですみれが説明する。)
 * ●中断
 * ●ダブルダミーモード
 * ・視点の設定
 * ・部分的オープンモード
 * ・カードの裏の模様設定
 * ・点滅時間など、wait の設定
 * ・Undo
 * ・これらの機能の有効化、無効化
 */
public class BoardManagerConfig {
	protected boolean	ddAvailable	= false;
	protected boolean	dd			= false;
	protected String	desc;
	protected String	contStr;
	protected String	title;
	
	public boolean doubleDummyIsAvailable() {
		return ddAvailable;
	}
	
	public void setDoubleDummy(boolean b) {
		dd = b;
	}
	
	public boolean doubleDummy() {
		if (!ddAvailable) return false;
		return dd;
	}
	
	public String getDescription() {
		return desc;
	}
	
	public String getContractString() {
		return contStr;
	}
	
	public String getTitle() {
		return title;
	}
}
