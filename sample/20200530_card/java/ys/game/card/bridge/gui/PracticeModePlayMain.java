package ys.game.card.bridge.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;
import ys.game.card.bridge.ta.ReadAheadPlayer;

/**
 * ���K���[�h�̃��C���v���O�����ł��B
 * PlayMain �ɑ΂��Čp�����g���Ă��܂��B
 *
 * @version		a-release		23, July 2001
 * @author		Yusuke Sasaki
 */
public class PracticeModePlayMain extends PlayMain {
	protected static final int[] VUL = { Board.VUL_NONE, Board.VUL_NS, Board.VUL_EW, Board.VUL_BOTH, Board.VUL_NS, Board.VUL_EW, Board.VUL_BOTH, Board.VUL_NONE };
	
	protected static final int BOARDS = 4;	// �W�ȉ��Ɏw�肷��K�v������
	
	protected GuiedBoard[]	boards	= new GuiedBoard[BOARDS];
	protected int			boardNo;
	protected Problem		prob	= new RandomProblem4();
	protected int			totalScore;
	protected boolean		finished = false;
	protected String		thinker;
	
	// 2002/10/5 �L�^���[�h�̂��߂̒ǉ�
	// �_�u���_�~�[�ɂ���Ɩ����ɂȂ�
	// �r����Ԃł�����
//	protected boolean		doubledummied = false;
//	protected boolean		recordIsValid = false;
	
//	protected YesNoDialog	retry;
	
/*-------------
 * Constructor
 */
	public PracticeModePlayMain(Container display) {
		super(display);
		this.thinker = "SimplePlayer2";
	}
	
	public PracticeModePlayMain(Container display, String thinker) {
		super(display);
		if (thinker == null) this.thinker = "SimplePlayer2";
		else this.thinker = thinker;
	}
	
	public boolean isFinished() { return finished; }
	
/*-----------
 * overrides
 */
	public void addProblem(Problem p) {
		throw new IllegalArgumentException("addProblem() �� PracticeModePlayMain �ł̓T�|�[�g���Ă��܂���");
	}
	
	/**
	 * Board �̏��������s���܂��B
	 */
	public void start() {
		mainThread = Thread.currentThread();
		totalScore = 0;
		Score score = new Score();
		
		for (boardNo = 0; boardNo < BOARDS; boardNo++) {
			if (display instanceof AppletCardImageHolder) {
				((AppletCardImageHolder)display).setBackImage(boardNo%4);
			}
			makeNewhand();
			main();
			super.stop();
			
			if (boards[boardNo] != null) {
				if (boards[boardNo].getStatus() != Board.SCORING) break;
			}
		}
		
		// 2002/6/29 �I�������A��ʍĕ`����s���Ă��Ȃ��������ߒǉ�
		// add start
		field.repaint();
		// add end
		
		if ( (boardNo == BOARDS)&&(boards[boardNo-1].getStatus() == Board.SCORING) ) {
			finished = false;
		} else {
			confirmDialog = new YesNoDialog(
								field.getCanvas(),
								"���K�p�A�v���b�g���I�����܂����H");
			confirmDialog.show();
			finished = confirmDialog.isYes();
			confirmDialog.dispose();
		}
	}
	
	public void stop() {
		super.stop();
//		if (retry != null) retry.disposeDialog();
		if (field != null) {
			for (int i = 0; i < boards.length; i++) {
				field.removeEntity(boards[i]);
			}
//			field.dispose();
		}
		for (int i = 0; i < boards.length; i++) {
			boards[i] = null;
		}
	}
	
	public void dispose() {
//System.out.println("practiceModePlayMain dispose() called field = "+field);
		if (field != null) field.dispose();
	}
	
	/**
	 * �_�C�A���O�ŐV�����n���h��I�������Ƃ��̏����ł��B
	 */
	protected void makeNewhand() {
		prob.start();
		
		board = new GuiedBoard(new BoardImpl(Board.NORTH, VUL[boardNo]));
		boards[boardNo] = board;
		
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// Player �ݒ�
		//
		player = new Player[4];
		player[Board.NORTH] = new RandomPlayer(board, Board.NORTH);
		if (thinker.equals("DoubleDummyPlayer"))
			player[Board.EAST ] = new ReadAheadPlayer(board, Board.EAST);
		else
			player[Board.EAST ] = new SimplePlayer2(board, Board.EAST);
		player[Board.SOUTH] = new HumanPlayer(board, field, Board.SOUTH);
		if (thinker.equals("DoubleDummyPlayer"))
			player[Board.WEST ] = new ReadAheadPlayer(board, Board.WEST, prob.getOpeningLead());
		else
			player[Board.WEST ] = new SimplePlayer2(board, Board.WEST, prob.getOpeningLead());
		
		//
		// �f�B�[��
		//
		Packet[] hand = prob.getHand();
		
		board.deal(hand);
		field.repaint();
		
		//
		// �r�b�h���s��
		//
		board.setContract(prob.getContract(), Board.SOUTH);
		
		//
		// �^�C�g����ݒ肷��
		//
		board.setName("Board " + (boardNo+1));
	}
	
	/**
	 * �n�߂̂��݂�ɂ�������\������
	 */
	protected void explain() throws InterruptedBridgeException {
		
		sumire = new Explanation(field, prob.getDescription());
		contractString = prob.getContractString();
		
		field.addEntity(sumire);
		field.repaint();
		try {
			waitClick(); // �N���b�N��҂B InterruptedException ���X���[���邩��
		} catch (InterruptedException e) {
			if (confirmQuit()) {
				field.removeEntity(sumire);
				throw new InterruptedBridgeException();
			}
		}
		field.removeEntity(sumire);
		field.repaint();
	}
	
	/**
	 * ���̃n���h�̕\���A�X�R�A�̕\�����s��
	 */
	protected void displayScore() throws InterruptedBridgeException {
		field.removeSpot();
		field.repaint();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			if (confirmQuit()) throw new InterruptedBridgeException();
		}
		
		//
		// �J�[�h��������x�\������
		//
		Packet[] original = calculateOriginalHand(board);
		for (int i = 0; i < 4; i++) {
			GuiedPacket hand = (GuiedPacket)(board.getHand(i));
			for (int j = 0; j < original[i].size(); j++) {
				Card c = original[i].peek(j);
				c.turn(true);
				hand.add(c);
			}
		}
		board.layout();
		for (int i = 0; i < 4; i++) {
			GuiedPacket hand = (GuiedPacket)(board.getHand(i));
			hand.arrange();
			hand.layout();
		}
		
		//
		// �X�R�A�\��
		//
//		Score score = new Score();
//System.out.println(Converter.serialize(board));
		String msg = "���ʁF" + contractString + "  ";
		String msg2;
		// ���C�N��
		int win		= countWinners(board);
		int up		= win - board.getContract().getLevel() - 6;
		int make	= win - 6;
		
		if (up >= 0) {
			// ���C�N
			msg += String.valueOf(make) + "���C�N";
			msg2 = "���߂łƂ��I�I";
		} else {
			// �_�E��
			msg += String.valueOf(-up) + "�_�E��";
			msg2 = "�c�O�B������x����΂��āI";
		}
		
		msg += "("+win+"�g���b�N)\nN-S���̃X�R�A�F"+Score.calculate(board, Board.SOUTH);
		
		totalScore += Score.calculate(board, Board.SOUTH); // override �ǉ���
		msg += "\n�X�R�A�ݐρF"+totalScore;
		
		msg += "\n" + msg2;
		
		sumire = new Explanation(field, msg);
		if (up >= 0) 
			sumire.animate(Explanation.DELIGHTED);
		else
			sumire.animate(Explanation.SAD);
		
		field.addEntity(sumire);
		field.repaint();
		
		try {
			waitClick();
		} catch (InterruptedException e) {
			if (confirmQuit()) {
				field.removeEntity(sumire);
				clearHands();
				throw new InterruptedBridgeException();
			}
		}
		field.removeEntity(sumire);
		field.repaint();
		clearHands();
	}
	
	protected boolean confirmQuit() {
		try {
			confirmDialog = new YesNoDialog(
								field.getCanvas(),
								"����܂ł̃v���C���ʂ�j�����Ē��f���܂����H");
			confirmDialog.show();
			boolean yes = confirmDialog.isYes();
			confirmDialog.dispose();
			return yes;
		} catch (Exception e) {
//			e.printStackTrace();
			return true;
		}
	}
	
	// IE��JavaVM 4.0 �Ή�
	private static int countWinners(Board board) {
		return BridgeUtils.countDeclarerSideWinners(board);
	}
	
	public Board getBoard(int boardNo) {
		return boards[boardNo];
	}

}
