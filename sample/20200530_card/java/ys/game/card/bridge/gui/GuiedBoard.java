package ys.game.card.bridge.gui;
/*
 * 2001/ 7/23  setName(), getName() �ǉ�
 */
import java.awt.*;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;

/**
 * Board �� GUI �\���ł���AGuiedCard, GuiedPacket �̕`��̈��񋟂��܂��B
 * ���ۓI�� Board �ɒǉ������T�O�Ƃ��āA��ƂȂ��(���_�A����)������܂��B
 *
 * @version		a-release		23, July 2001
 * @author		Yusuke Sasaki
 */
public class GuiedBoard extends Entities implements Board {
	// O | - +   | - + O   - + O |   + O | -
	private static final int[] VUL = { 0, 1, 2, 3,  1, 2, 3, 0,
											2, 3, 0, 1, 3, 0, 1, 2 };
	
	public static final int WIDTH	= 640;
	public static final int HEIGHT	= 480;
	
	protected BoardImpl	impl;
	
/*----------------
 * GUI Components
 */
	protected GuiedPacket[] handGui; // BoardLayout���Q�Ƃ���B
	protected GuiedTrick	trickGui;
	protected WinnerGui		winnerGui;
	protected TableGui		tableGui;
	
/*-------------
 * Constructor
 */
	public GuiedBoard(Board board) {
		if (board instanceof BoardImpl) impl = (BoardImpl)board;
		else impl = new BoardImpl(board);
		
		direction = Entity.UPRIGHT;
		setSize(WIDTH, HEIGHT);
	}
	
/*------------------
 * instance methods
 */
	/**
	 * TableGui, WinnerGui ��V�K�ɍ쐬���APlayHistory �� Guied �ɕύX���܂��B
	 * �܂��AGuiedBoard �ł́AhandGui ��Ǝ��ɕێ����Ă���(GuiedPlayHistory
	 * �Ɠ����C���X�^���X)���߁A������擾���܂��B
	 * �܂��A������ Entity �Ƃ��� addEntity() ���܂��B
	 */
	private void setBoardGui() {
		entity = null;
		entities = 0;
		
		//
		tableGui = new TableGui(this);
		addEntity(tableGui);
		
		//
		winnerGui = new WinnerGui();
		addEntity(winnerGui);
		
		//
		handGui = new GuiedPacket[4];
		
		
		PlayHistory play = impl.getPlayHistory();
		if (!(play instanceof GuiedPlayHistory))
			impl.setPlayHistory(new GuiedPlayHistory(play));
		
		for (int i = 0; i < 4; i++) {
			handGui[i] = (GuiedPacket)(impl.getHand(i));
			addEntity(handGui[i]);
		}
		
		//
		trickGui = (GuiedTrick)(impl.getTrick());
		if (trickGui != null) addEntity(trickGui);
		
		setLayout(new BoardLayout());
	}
	
/*---------------------
 * Overrides(Entities)
 */

/*-------------------
 * Implements(Board)
 *
 * �قƂ�� impl �ɑ΂��ĈϏ����܂��B
 */
	public void setName(String name) { impl.setName(name); }
	public String getName() { return impl.getName(); }
	
	/**
	 * �n���h��z��܂��B
	 * GuiedBoard �ł́Adeal() ���s�����Ƃ��ɉ��ɕ\�������n���h���\������
	 * �Ȃ�܂��Bobserver (Open Cards �������Ȃ��l) �̏ꍇ������̐l(���炭SOUTH)
	 * �̃n���h�ɂ��ĕ\�����ɂ����̂ł͂Ȃ����Ƃ������O������܂����A
	 * ���ۂɂ͂��̃J�[�h�� Unspecified �̂��߁A��ʕ\����͗������Ɍ�����͂��ł��B
	 */
	public void deal() {
		impl.deal();
		
		//
		// Hand, Trick �� Guied �ɕύX����B
		//
		setBoardGui();
		
		//
		// ���ɕ\�������n���h��\�����ɂ���
		//
		Packet turned = getHand( (direction + 2)%4 );
		turned.turn(true);
	}
	
	/**
	 * �w�肳�ꂽ�n���h��z��܂��B
	 * GuiedBoard �ł́Adeal() ���s�����Ƃ��ɉ��ɕ\�������n���h���\������
	 * �Ȃ�܂��Bobserver (Open Cards �������Ȃ��l) �̏ꍇ������̐l(���炭SOUTH)
	 * �̃n���h�ɂ��ĕ\�����ɂ����̂ł͂Ȃ����Ƃ������O������܂����A
	 * ���ۂɂ͂��̃J�[�h�� Unspecified �̂��߁A��ʕ\����͗������Ɍ�����͂��ł��B
	 */
	public void deal(Packet[] hand) {
		impl.deal(hand);
		
		//
		// Hand, Trick �� Guied �ɕύX����B
		//
		setBoardGui();
		
		//
		// ���ɕ\�������n���h��\�����ɂ���
		//
		Packet turned = getHand( (direction + 2)%4 );
		for (int i = 0; i < turned.size(); i++) {
			turned.peek(i).turn(true);
		}
		
	}
	
	public void play(Object c) {
		int oldStatus = impl.getStatus();
		impl.play(c);
		
		if (impl.getStatus() == Board.PLAYING) {
			TrickAnimation animation = new TrickAnimation(this);
			animation.start();
			
			if (oldStatus == Board.OPENING) {
				trickGui = (GuiedTrick)getTrick();
				addEntity(trickGui);
				//------------
				// �����̐ݒ�
				//------------
				int d = (4 + trickGui.getLeader() - trickGui.getDirection() ) % 4;
				Entity ent = trickGui.getEntity(0);
				ent.setDirection((10 - d )%4);
				trickGui.layout();
				setDummyLayout();
			}
		} else if (impl.getStatus() == Board.SCORING) {
			TrickAnimation animation = new TrickAnimation(this);
			animation.start();
		}
	}
	
	public void undo() {
		throw new InternalError("�������I");
	}
	
	private void setDummyLayout() {
		GuiedPacket dummyHand = (GuiedPacket)getHand(getDummy());
		dummyHand.setLayout(new DummyHandLayout());
		dummyHand.layout();
		layout();
	}
	
	// undo() �p
	private void removeDummyLayout() {
		GuiedPacket dummyHand = (GuiedPacket)getHand(getDummy());
		dummyHand.setLayout(new CardHandLayout());
		dummyHand.layout();
		layout();
	}
	
	public BiddingHistory getBiddingHistory() { return impl.getBiddingHistory(); }
	public PlayHistory getPlayHistory() { return impl.getPlayHistory(); }
	public void setPlayHistory(PlayHistory playHistory) {
		if (!(playHistory instanceof GuiedPlayHistory)) {
			playHistory = new GuiedPlayHistory(playHistory);
		}
		impl.setPlayHistory(playHistory);
	}
		
	public void setContract(Bid contract, int declarer) { impl.setContract(contract, declarer); }
	public boolean allows(Object play) { return impl.allows(play); }
	public int getVulnerability() { return impl.getVulnerability(); }
	public boolean isVul(int seat) { return isVul(seat); }
	public int getStatus() { return impl.getStatus(); }
	public int getPlayOrder() { return impl.getPlayOrder(); }
	public int getTurn() { return impl.getTurn(); }
	public int getPlayer() { return impl.getPlayer(); }
	public Bid getContract() { return impl.getContract(); }
	public int getTrump() { return impl.getTrump(); }
	public int getDeclarer() { return impl.getDeclarer(); }
	public int getDummy() { return impl.getDummy(); }
	public int getDealer() { return impl.getDealer(); }
	public Packet getHand(int seat) { return impl.getHand(seat); }
	public Packet[] getHand() { return impl.getHand(); }
	public int getTricks() { return impl.getTricks(); }
	public Trick getTrick() { return impl.getTrick(); }
	public Trick[] getAllTricks() { return impl.getAllTricks(); }
	
	/**
	 * �{���\�b�h�͎v�l���[�`���Ŏg�p����邱�Ƃ����҂��Ă��邽�߁A
	 * GuiedBoard �ɂ����Ă��ԋp����� Packet �̃C���X�^���X�� Guied
	 * �ł��邱�Ƃ͕ۏ؂���܂���B
	 *
	 * @return		���m�̃J�[�h����Ȃ� Packet
	 */
	public Packet getOpenCards() { return impl.getOpenCards(); }
	
	public void reset(int num) {
		reset((num - 1)%4, VUL[(num - 1)%16]);
	}
	
	/**
	 * GuiedBoard �ł́Areset() �̂Ƃ��Ɋe��GUI�R���|�[�l���g��؂藣���܂��B
	 * �؂藣����� GUI �R���|�[�l���g�Ƃ��� TableGui ���܂܂�邽�߁A
	 * ���̌�� field.repaint() �����s����ƃe�[�u���̉�ʂ���������܂��B
	 * TrickAnimation(not a thread) �ȂǂƂ̊ԂŔr����������s���ׂ��ł����A
	 * ���̃I�u�W�F�N�g�ł͍s����
	 * ���Ȃ��̂ŁA��ʃA�v���P�[�V�����Ő��䂷��K�v������܂��B
	 * �����e�� GUI �R���|�[�l���g�� deal �ŐV��������܂��B
	 * (private void setBoardGui() �ō쐬)
	 */
	public void reset(int dealer, int vul) {
		removeEntity(tableGui);		tableGui	= null;
		removeEntity(winnerGui);	winnerGui	= null;
		if (handGui != null) {
			for (int i = 0; i < 4; i++) {
				removeEntity(handGui[i]);
			}
		}
		removeEntity(trickGui);		trickGui	= null;
		
		impl.reset(dealer, vul);
	}
	
	public String toString() {
		return impl.toString();
	}
	
	public String toText() {
		return impl.toText();
	}
}
