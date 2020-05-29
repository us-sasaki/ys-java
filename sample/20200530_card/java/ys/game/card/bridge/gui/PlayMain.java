package ys.game.card.bridge.gui;

/*
 * 2001/ 7/15  �I�����Ƀ_�C�A���O���\�����ꂽ�܂܂ɂȂ�o�O��FIX�B
 * 2001/ 7/23  private ���\�b�h�A�ϐ��� protected ��(PracticeModePlayMain�̂���)
 */

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;
import ys.game.card.bridge.ta.ReadAheadPlayer;

/**
 * �A�h�z�b�N�ȃ��C���v���O�����ł��B���񂾂�{�i�I�ɂȂ��Ă��܂����B
 *
 * @version		a-release		23, July 2001
 * @author		Yusuke Sasaki
 */
public class PlayMain implements MouseListener, ActionListener {
	protected Container	display;
	protected BridgeField	field;
	protected GuiedBoard	board;
	protected Player[]	player;
	protected int			handno;
	protected String		contractString;
	
	protected Vector		problem;
	protected Explanation	sumire;
	
	protected Button		quit;
	protected Button		dd;			// added 02/9/16
	protected Button		textWindow;	// added 03/6/2
	protected boolean		doubleDummy = false; // added 02/9/16
	
	protected Thread		mainThread;
	
	protected SelectDialog	dialog;
	protected YesNoDialog	confirmDialog;
	
	/** Select Dialog �� X �{�^�����������Ƃ��� true �ɂȂ�܂� */
	public boolean			exitSignal = false;
	
/*-------------
 * Constructor
 */
	/**
	 * PlayMain �I�u�W�F�N�g�𐶐����܂��B
	 */
	public PlayMain(Container display) {
		this.display	= display;
		problem = new Vector();
	}
	
/*----------------------------
 * implements (MouseListener)
 */
	/**
	 * �}�E�X���N���b�N���ꂽ�ꍇ�A���̃I�u�W�F�N�g�� wait() ���J�����܂��B
	 */
	public void mouseClicked(MouseEvent me) {
		synchronized (this) {
			notifyAll();
		}
	}
	
	public void mousePressed(MouseEvent me) { }
	public void mouseReleased(MouseEvent me) { }
	public void mouseEntered(MouseEvent me) { }
	public void mouseExited(MouseEvent me) { }
	
/*-----------------------------
 * implements (ActionListener)
 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == quit) {
			if (mainThread != null) mainThread.interrupt();
		}
		
		// �ȉ��Aadded 02/9/16
		if (e.getSource() == dd) {
			doubleDummy = !doubleDummy;
			if (!doubleDummy) {
				dd.setLabel("�_�u���_�~�[");
				if (board.getStatus() == Board.PLAYING) {
					board.getHand(Board.EAST).turn(false);
					board.getHand(Board.WEST).turn(false);
					field.repaint();
				}
			} else {
				dd.setLabel("�ʏ�ɖ߂�");
				if (board.getStatus() == Board.PLAYING) {
					board.getHand(Board.EAST).turn(true);
					board.getHand(Board.WEST).turn(true);
					field.repaint();
				}
			}
		}
		// �ȏ�Aadded 02/9/16
		
		// �ȉ��Aadded 03/6/2
		if (e.getSource() == textWindow) {
			if (board != null) {
				TextInfoWindow.getInstance(board.toText());
			}
		}
		// �ȏ�Aadded 03/6/2
	}
	
/*------------------
 * instance methods
 */
	/**
	 * �͂��߂ɕ\�������_�C�A���O�ɖ���ǉ����܂��B
	 * valid �łȂ����͒ǉ�����܂���B
	 */
	public void addProblem(Problem p) {
		if (p.isValid()) problem.addElement(p);
	}
	
	/**
	 * BridgeField, Board �����������܂��B
	 *
	 * @param		display		GUI���\�[�X�Ɛڑ�����Ă��� Container(Applet�܂���Frame)
	 */
	public void initialize() {
		field = new BridgeField(display); // ���̒��� display.createImage() ���s������peer���K�v
		Canvas canvas = field.getCanvas();
		
		display.setLayout(null);
		
		quit = new Button("���f");
		quit.setVisible(false);
		display.add(quit);
		quit.setBounds(540, 30, 80, 24);
		quit.addActionListener(this);
		
		// �ȉ��A added 02/9/16
		dd = new Button("�_�u���_�~�[");
		dd.setVisible(false);
		display.add(dd);
		dd.setBounds(540, 58, 80, 24);
		dd.addActionListener(this);
		// �ȏ�A// added 02/9/16
		
		// �ȉ��Aadded 03/6/2
		textWindow = new Button("�e�L�X�g�\��");
		textWindow.setVisible(false);
		display.add(textWindow);
		textWindow.setBounds(540, 86, 80, 24);
		textWindow.addActionListener(this);
		// �ȏ�Aadded 03/6/2
		
		display.add(canvas);
		
		if (display instanceof Frame) {
			Frame f = (Frame)display;
			f.pack();
			f.show();
		}
		canvas.requestFocus();
		
		try {
			System.gc();
		} catch (Throwable t) {
			System.out.println("System.gc() is not allowed:"+t);
		}
	}
	
	/**
	 * Board �̏��������s���܂��B
	 */
	public void start() {
		dialog = new SelectDialog(display, board);
		for (int i = 0; i < problem.size(); i++) {
			Problem prob = (Problem)(problem.elementAt(i));
			dialog.addChoice(prob.getTitle());
		}
		field.repaint();
		try {
			dialog.newHand.select(handno);
		} catch (Exception e) {
			handno = -1;
		}
		dialog.show();
		dialog.requestFocus();	// added 2000/8/16
		String result = dialog.result; // dialog.dispose() �ɂ���� result �������邽��
		dialog.disposeDialog();	// �����ōs�� added 2001/7/15
		if (result == null) return;
		if (result.equals("disposed")) {
			exitSignal = true;
			return;
		}
		
		mainThread = Thread.currentThread();
		// �ȉ��Aadded 02/9/16
		doubleDummy = false;
		dd.setLabel("�_�u���_�~�[");
		// �ȏ�Aadded 02/9/16
		if ("Video".equals(result)) {
			makeVideohand();
			GuiedPacket east = (GuiedPacket)(board.getHand(Board.EAST));
			east.turn(true);
			GuiedPacket west = (GuiedPacket)(board.getHand(Board.WEST));
			west.turn(true);
			
			doubleDummy = true;	// added 02/9/16
			dd.setLabel("�ʏ�ɖ߂�");	// added 02/9/16
			
		} else if ("Same Hand".equals(result)) {
			makeLasthand();
		} else {
			this.handno = -1;
			for (int i = 0; i < problem.size(); i++) {
				Problem p = (Problem)(problem.elementAt(i));
				
				if (p.getTitle().equals(result)) {
					this.handno = i;
					makeNewhand();
					break;
				}
			}
			if (this.handno == -1) { return; }
		}
		main();
	}
	
	/**
	 * �_�C�A���O�ŐV�����n���h��I�������Ƃ��̏����ł��B
	 */
	protected void makeNewhand() {
		Problem prob = (Problem)(problem.elementAt(handno));
		prob.start();
		
		board = new GuiedBoard(new BoardImpl(1));
		board.setName(prob.getTitle());
		
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// Player �ݒ�
		//
		player = new Player[4];
		player[Board.NORTH] = new RandomPlayer(board, Board.NORTH);
		player[Board.SOUTH] = new HumanPlayer(board, field, Board.SOUTH);
		
		//
		// Computer Player �ݒ�
		//
		if ((prob.getThinker() == null)||(!prob.getThinker().equals("DoubleDummyPlayer"))) {
			player[Board.EAST ] = new SimplePlayer2(board, Board.EAST);
			player[Board.WEST ] = new SimplePlayer2(board, Board.WEST, prob.getOpeningLead());
		} else if (prob.getThinker().equals("DoubleDummyPlayer")) {
			player[Board.EAST ] = new ReadAheadPlayer(board, Board.EAST);
			player[Board.WEST ] = new ReadAheadPlayer(board, Board.WEST, prob.getOpeningLead());
		} else {
			player[Board.EAST ] = new NoRufPlayer(board, Board.EAST);
			player[Board.WEST ] = new NoRufPlayer(board, Board.WEST, prob.getOpeningLead());
		}
		
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
	}
	
	private void makeVideohand() {
		Board oldBoard = board;
		board = new GuiedBoard(new BoardImpl(1));
		board.setName(oldBoard.getName());
		
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// Player �ݒ�
		//
		player = new Player[4];
		player[Board.NORTH] = new VideoPlayer(board, oldBoard, Board.NORTH);
		player[Board.EAST ] = new VideoPlayer(board, oldBoard, Board.EAST );
		player[Board.SOUTH] = new VideoPlayer(board, oldBoard, Board.SOUTH);
		player[Board.WEST ] = new VideoPlayer(board, oldBoard, Board.WEST );
		
		//
		// �f�B�[��
		//
		Packet[] hand = calculateOriginalHand(oldBoard);
		
		board.deal(hand);
		field.repaint();
		
		//
		// �r�b�h���s��
		//
		board.setContract(oldBoard.getContract(), oldBoard.getDeclarer());
	}
	
	private void makeLasthand() {
		Problem prob = (Problem)(problem.elementAt(handno));
		Board oldBoard = board;
		board = new GuiedBoard(new BoardImpl(1));
		board.setName(oldBoard.getName());
		
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// Player �ݒ�
		//
		player = new Player[4];
		player[Board.NORTH] = new RandomPlayer(board, Board.NORTH);
		player[Board.SOUTH] = new HumanPlayer(board, field, Board.SOUTH);
		
		//
		// Computer Player �ݒ�
		//
		if ((prob.getThinker() == null)||(!prob.getThinker().equals("DoubleDummyPlayer"))) {
			player[Board.EAST ] = new SimplePlayer2(board, Board.EAST);
			player[Board.WEST ] = new SimplePlayer2(board, Board.WEST, prob.getOpeningLead());
		} else if (prob.getThinker().equals("DoubleDummyPlayer")) {
			player[Board.EAST ] = new ReadAheadPlayer(board, Board.EAST);
			player[Board.WEST ] = new ReadAheadPlayer(board, Board.WEST, prob.getOpeningLead());
		} else {
			player[Board.EAST ] = new NoRufPlayer(board, Board.EAST);
			player[Board.WEST ] = new NoRufPlayer(board, Board.WEST, prob.getOpeningLead());
		}
//		player[Board.NORTH] = new RandomPlayer(board, Board.NORTH);
//		player[Board.EAST ] = new ReadAheadPlayer(board, Board.EAST);
//		player[Board.SOUTH] = new HumanPlayer(board, field, Board.SOUTH);
//		player[Board.WEST ] = new ReadAheadPlayer(board, Board.WEST, prob.getOpeningLead());
		
		//
		// �f�B�[��
		//
		Packet[] hand = calculateOriginalHand(oldBoard);
		
		board.deal(hand);
		field.repaint();
		
		//
		// �r�b�h���s��
		//
		board.setContract(oldBoard.getContract(), oldBoard.getDeclarer());
	}
	
	/**
	 * start()�Ƒ΂ɂȂ郁�\�b�h�ŁAstart()�ŏ������������\�[�X�̔j�����s���܂��B
	 * start() �ƕʂ̃X���b�h����Ă΂�܂��B
	 * �_�C�A���O�̃��\�[�X�ȂǏI���������K�v�Ȃ��̂̔j�����s���܂��B
	 * ��ʂ��疾���I�ɃR�[�����邱�Ƃɂ���ă_�C�A���O���c��o�O�͉�������܂��B
	 */
	public void stop() {
		if (field != null) {
			field.removeEntity(board);
//			field.dispose();
		}
		if (dialog != null) dialog.disposeDialog();
		if (confirmDialog != null) confirmDialog.disposeDialog();
		if (player != null) {
			for (int i = 0; i < player.length; i++) {
				if ((player[i] != null)&&(player[i] instanceof HumanPlayer)) {
					((HumanPlayer)player[i]).dispose();
				}
			}
		}
	}
	
	public void dispose() {
		if (field != null) field.dispose();
	}
	
	/**
	 * �^����ꂽ�{�[�h�ɂ�����͂��߂̃n���h���v�Z���܂��B
	 */
	protected Packet[] calculateOriginalHand(Board board) {
		return BridgeUtils.calculateOriginalHand(board);
	}
	
	/**
	 * �N���b�N�����̂�҂��܂��B
	 */
	protected void waitClick() throws InterruptedException {
		synchronized (this) {
			Canvas canvas = field.getCanvas();
			canvas.addMouseListener(this);
			try {
				wait();
				canvas.removeMouseListener(this);
			} catch (InterruptedException e) {
				canvas.removeMouseListener(this);
				throw new InterruptedException();
			}
		}
	}
	
	/**
	 * �n�߂̂��݂�ɂ�������\������
	 */
	protected void explain() throws InterruptedBridgeException {
		
		Problem prob = (Problem)(problem.elementAt(handno));
		
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
		field.removeEntity(sumire); // ����ɂ���Ă��݂�̃X���b�h���I������
		field.repaint();
	}
	
	/**
	 * ���C�����[�v
	 */
	protected void mainLoop() throws InterruptedBridgeException {
		// �ȉ��Cadded 02/9/16
		if (doubleDummy) {
			board.getHand(Board.EAST).turn(true);
			board.getHand(Board.WEST).turn(true);
		}
		// �ȏ�Aadded 02/9/16
		
		while (true) {
			//
			// Spot ���w�肷��
			//
			field.setSpot(board.getTurn());
			field.repaint();
			
			Object c = null;
			while (c == null) {
				try {
					c = player[board.getPlayer()].play(); // �u���b�N����
				} catch (InterruptedException e) {
					if (confirmQuit()) throw new InterruptedBridgeException();
				}
			}
			board.play(c);
			field.repaint();
			
			if (board.getStatus() == Board.SCORING) break;
		}
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
		msg += "\n \n" + msg2;
		
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
	
	protected void clearHands() {
		//
		// Board �̏�Ԃ𐳂������̂ɂ��邽�߁A�n���h���N���A����
		//
		for (int i = 0; i < 4; i++) {
			Packet hand = board.getHand(i);
			while (hand.size() > 0) {
				hand.draw();
			}
		}
	}
	
	protected boolean confirmQuit() {
		try {
			confirmDialog = new YesNoDialog(
								field.getCanvas(),
								"���̃{�[�h��j�����Ē��f���܂����H");
			confirmDialog.show();
			boolean yes = confirmDialog.isYes();
			confirmDialog.disposeDialog();
			return yes;
		} catch (Exception e) {
//			e.printStackTrace();
			return true;
		}
	}
	
	/**
	 * ���C�����\�b�h�ł��B
	 */
	public void main() {
		if (Thread.interrupted()); // interrupt�X�e�[�^�X�N���A
		quit.setVisible(true);
		dd.setVisible(true);	// added 02/9/16
		textWindow.setVisible(true); // added 03/6/2
		try {
			explain();
			mainLoop();
			displayScore();
		} catch (InterruptedBridgeException e) {
			field.removeSpot();
			field.repaint();
		}
		quit.setVisible(false);
		dd.setVisible(false);	// added 02/9/16
		textWindow.setVisible(false); // added 03/6/2
	}
	
	/**
	 * VM �����x���������Ă΂�Ȃ����Ƃ��ۏ؂���Ă��邽�߁A���̃��\�b�h��
	 * �v���O��������Ăяo�����Ƃ͂��Ȃ����ƂƂ���B
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		stop();
	}
	
	// IE��JavaVM 4.0 �Ή�
	private static int countWinners(Board board) {
		Trick[]	tr			= board.getAllTricks();
		if (tr == null) return 0;
		
		int		win			= 0;
		int		declarer	= board.getDeclarer();
		
		// winner �𐔂���(Board �ɂ������ق����֗�)
		for (int i = 0; i < tr.length; i++) {
			int winner = tr[i].getWinner();
			if ( ((winner ^ declarer) & 1) == 0 ) win++;
		}
		
		return win;
	}

}