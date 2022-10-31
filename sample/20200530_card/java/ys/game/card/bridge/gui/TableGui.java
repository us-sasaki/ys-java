package ys.game.card.bridge.gui;
/*
 * 2001/ 7/23  左上のコントラクト表示を変更
 */

import java.awt.Panel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;

import ys.game.card.bridge.*;
import ys.game.card.gui.*;

/**
 * NSEW, コントラクトなどを表示する Entity です。
 * 一番下に配置されるべきです。
 *
 * @version		a-release		23, July 2001
 * @author		Yusuke Sasaki
 */
public class TableGui extends Entity {
	protected static final int WIDTH  = 640;
	protected static final int HEIGHT = 400;
	
	protected static final Color NAVY = new Color(0, 0, 96);
	
	protected static final Color CENTER = new Color(20, 160, 40); //, 128);
	protected static final Color CENTER2 = new Color(224, 255, 224); //, 128);
	protected static final Color CENTER3 = new Color(170, 0, 0);
	
	protected static final Color CONTRACT_BACK = new Color(0, 32, 0);
	protected static final Color CONTRACT_FIELD = new Color(0, 160, 0);
	
	protected GuiedBoard board;
	
/*-------------
 * Constructor
 */
	/**
	 * TableGui を作成する.
	 */
	public TableGui(GuiedBoard board) {
		this.board = board;
		setSize(WIDTH, HEIGHT);
	}
	
	public void draw(Graphics g) {
		drawContract(g);
		drawDirection(g);
	}
	
	private void drawContract(Graphics g) {
		Bid contract = board.getContract();
		
		// わく
		g.setColor(CONTRACT_BACK);
		g.fillRect(18, 32, 172, 72);
		g.setColor(CONTRACT_FIELD);
		g.fillRect(10, 24, 172, 72);
		//
		// コントラクトがあれば、左上に表示する
		//
		final String[] DIRECTIONS = new String[] {"N", "E", "S", "W"};
		final String[] DIRECTION = new String[] {"North", "East", "South", "West"};
		if (contract != null) {
			
			// 文字列
			g.setFont(new Font("Serif", Font.BOLD, 16));
			
			int kind = contract.getKind();
			String contractStr;
			if (kind == Bid.PASS) {
				contractStr = "Pass Out";
			} else {
				contractStr = "Contract " + String.valueOf(contract.getLevel());
				
				final String[] SUIT = new String[] {"?", " C", " D", " H", " S", " NT"};
				contractStr += SUIT[contract.getSuit()];
				
				final String[] DOUBLE = new String[] {"", "p.o.", "X", "XX"};
				contractStr += DOUBLE[kind];
				contractStr += " by ";
				
				contractStr += DIRECTIONS[board.getDeclarer()];
			}
			
			g.setColor(NAVY);
			g.drawString(contractStr, 20, 85);
			
			g.setColor(Color.white);
			g.drawString(contractStr, 18, 83);
		} else {
			// ディーラー
			String dealerStr = "Dlr: ";
			dealerStr += DIRECTION[board.getDealer()];
			g.setColor(NAVY);
			g.drawString(dealerStr, 20, 85);
			g.setColor(Color.white);
			g.drawString(dealerStr, 18, 83);
		}
		// バル
		final String[] VUL_STR = new String[] {"Vul: Neither", "Vul: N-S", "Vul: E-W", "Vul: Both"};
		g.setColor(NAVY);
		g.drawString(VUL_STR[board.getVulnerability()], 20, 67);
		g.setColor(Color.white);
		g.drawString(VUL_STR[board.getVulnerability()], 18, 65);
		
		// タイトル
		g.setFont(new Font("Serif", Font.BOLD + Font.ITALIC, 14));
		g.setColor(NAVY);
		g.drawString(board.getName(), 20, 46);
		g.setColor(Color.white);
		g.drawString(board.getName(), 18, 44);
		
		g.drawLine(18, 48, 170, 48);
	}
	
	private void drawDirection(Graphics g) {
		//
		// 真ん中の NESW
		//
		
		// 枠線
		g.setColor(CENTER2);
		g.fillRect(220, 140, 200, 8);
		g.fillRect(220, 140, 8, 200);
		g.fillRect(412, 140, 8, 200);
		g.fillRect(220, 332, 200, 8);
		
		// VULを示す赤線
		int vul = board.getVulnerability();
		g.setColor(CENTER3);
		if ( (vul & Board.VUL_NS) != 0) {
			g.fillRect(228, 148, 184, 32);
			g.fillRect(228, 300, 184, 32);
		}
		if ( (vul & Board.VUL_EW) != 0) {
			g.fillRect(228, 148, 32, 184);
			g.fillRect(380, 148, 32, 184);
		}
		
		// N E S W の線
		g.setFont(new Font("SanSerif", Font.PLAIN, 28));
		g.setColor(CENTER2);
		g.drawString("N", 314, 178);
		g.drawString("E", 384, 250);
		g.drawString("S", 314, 326);
		g.drawString("W", 238, 250);
	}
}
