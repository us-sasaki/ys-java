package ys.game.card.bridge.ta;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.bridge.Player;
import ys.game.card.bridge.Board;
import ys.game.card.bridge.Bid;
import ys.game.card.bridge.BridgeUtils;
import ys.game.card.bridge.IllegalStatusException;
import ys.game.card.bridge.SimplePlayer2;

/**
 * �_�u���_�~�[��ԂŁA�Ō�܂œǂ݂����čőP���łv���C���[�B
 *
 * 2015/8/12 �R���s���[�^�̐��\�A�b�v�ɔ����A��ǂݐ[��
 *
 * @version		making		20 October, 2002
 * @author		Yusuke Sasaki
 */
public class ReadAheadPlayer extends Player {
	protected SimplePlayer2	base;
	protected boolean		openingLeadSpecified;
	
	private byte[][] paths = new byte[5000][13];
	
/*
 * 
 */
	public ReadAheadPlayer(Board board, int seat) {
		setBoard(board);
		setMySeat(seat);
		
		base = new SimplePlayer2(board, seat);
	}
	
	public ReadAheadPlayer(Board board, int seat, String ol) {
		setBoard(board);
		setMySeat(seat);
		
		base = new SimplePlayer2(board, seat, ol);
		if ((ol != null)&&(!ol.equals(""))) openingLeadSpecified = true;
	}
	
/*------------
 * implements
 */
	/**
	 * �p�X���܂��B
	 *
	 * @return		�p�X
	 */
	public Bid bid() throws InterruptedException {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * OptimizedBoard �̍őP��T���A���S���Y�����g�p�����v���C���s���܂��B
	 * �I�[�v�j���O���[�h�ɂ��Ă͎w�肪����ꍇ�A�w��ɏ]���܂��B
	 *
	 * @return		�őP��
	 */
	public Card draw() throws InterruptedException {
		// �v���C�̊Ԋu���Ȃ�ׂ����ɂ��邽��
		long t0 = System.currentTimeMillis();
		
		Board board = getBoard();
		
		if (board.getStatus() == Board.OPENING) {
			// �I�[�v�j���O���[�h�̏ꍇ�� SimplePlayer2 �̃A���S���Y�����g��
			if (openingLeadSpecified) return base.draw();
		}
		
		// �őP��v���C�̏W�����擾���܂��B
		Packet playOptions = getPlayOptions();
		
		Card play = choosePlay(playOptions);
		
		// �v���C�Ԋu���̂���
		long t = System.currentTimeMillis();
		try { if ((t - t0) < 700) Thread.sleep(700 - (t - t0)); // 700msec �ɂȂ�܂ōl����ӂ�
		} catch (InterruptedException ignored) { }
		
		return play;
	}
	
	/**
	 * OptimizedBoard ���g�p���āA�őP��̌��������܂��B
	 * �{�N���X���� Optimized �֘A�����ƂȂ�܂��B
	 *
	 * @return		�őP��̌��
	 */
	protected Packet getPlayOptions() {
		OptimizedBoard b = new OptimizedBoard(getBoard());
		//
		// �g���b�N���ɂ���ǂ݂̐[���ύX
		//
		
		// depthBorder �l���w��
		// depthBorder �́A�Œ��ǂ݃v���C���ŁA���ۂɂ͂���ȍ~�̍ŏ��̃��[�h���
		// �܂Ő�ǂ݂��s���܂��B�Ⴆ�΂O���w�肵�A���łɃ��[�h��Ԃɂ������ꍇ�A
		// ��ǂ݂͍s���܂���B
		//                        
		int[] depth = new int[] {   8,   8,   8,   8,
								    8,   8,   9,  10,
								  100, 100, 100, 100, 100, 100 };


// ���ꂾ�Ǝ��Ԃ������肷���Ƃ������ƂŁA�����������炷
// 2016/3/27
//
//		int[] depth = new int[] {   9,   9,   9,   9,
//								    9,   9,  10,  100,
//								  100, 100, 100, 100, 100, 100 };

// Pentium 3(700MHz)����(2015�܂ł��̒l���̗p���Ă���)
//		int[] depth = new int[] {   5,   5,   5,   5,
//								    5,   5,   6,   6,
//								  100, 100, 100, 100, 100, 100 };
		b.setDepthBorder(depth[getBoard().getTricks()]);
		
		int[] bps = b.getBestPlay();
		
		//
		// �f�o�b�O�p�o��
		//
/*
for (int i = 0; i < bps.length; i++) {
	if (bps[i] == -1) break;
	System.out.print(" " + i + ":" + OptimizedBoard.getCardString(bps[i]));
}
System.out.println();
*/
		
		//------------------
		// ���i�J�[�h�̒��o
		//------------------
		// �v���C����Ă��Ȃ��J�[�h = 0 // �܂��͍���ɏo�Ă���J�[�h
		// �����Ă���J�[�h         = 1
		// �w�肳�ꂽ�J�[�h			= 2
		// �v���C���ꂽ�J�[�h       = 3
		// 0 �� delimiter �Ƃ��āAtoken ����؂�A2 ���܂܂�Ă��� token ��
		// 1 �� 2 �ɕύX����B2 �ƂȂ��Ă���J�[�h��ԋp����
		
		int[] tmp = new int[56];
		
		// �v���C���ꂽ�J�[�h(3)�̐ݒ�
		// �܂��R�ɖ߂��Ă��Ȃ��J�[�h(disposed)
		//   = { open cards } - { ���o�Ă���J�[�h }
		Board board = getBoard();
		Packet disposed = board.getOpenCards().sub(board.getTrick()).sub(getDummyHand());
		
		for (int i = 0; i < disposed.size(); i++) {
			Card c = disposed.peek(i);
			tmp[ OptimizedBoard.getCardNumber(c) ] = 3;
		}
		
		// �����Ă���J�[�h(1)�̐ݒ�
		Packet h = getHand();
		
		for (int i = 0; i < h.size(); i++) {
			Card c = h.peek(i);
			tmp[ OptimizedBoard.getCardNumber(c) ] = 1;
		}
		
		// �w�肳�ꂽ�J�[�h(2)�̐ݒ�
		for (int i = 0; i < bps.length; i++) {
			if (bps[i] == -1) break;
//if (tmp[ bps[i] ] != 1) System.out.println("asserted in tmp != 1");
			tmp[ bps[i] ] = 2;
		}
		
		// token ���Ƃ̏���
		int tokenStartIndex = 0;
		int resultCount = 0;
		
		while (true) {
			// delimiter �łȂ��C���f�b�N�X��T�� --> tokenStartIndex
			for (; tokenStartIndex < 56; tokenStartIndex++) {
				if (tmp[tokenStartIndex] != 0) break;
			}
			if (tokenStartIndex == 56) break;
			
			int tokenEndIndex;
			boolean containsTargetCard = false;
			for (tokenEndIndex = tokenStartIndex; tokenEndIndex < 56; tokenEndIndex++) {
				if (tmp[tokenEndIndex] == 2)
					containsTargetCard = true;
				else if (tmp[tokenEndIndex] == 0) break;
			}
			
			if (containsTargetCard) {
				for (int i = tokenStartIndex; i < tokenEndIndex; i++) {
					if (tmp[i] != 3) {
						tmp[i] = 2;
						resultCount++;
					}
				}
			}
			tokenStartIndex = tokenEndIndex + 1;
			if (tokenStartIndex >= 56) break;
		}
		
		//
		// ���ʐ���
		//
		Packet result = new PacketImpl();
		
		for (int i = 0; i < tmp.length; i++) {
			if (tmp[i] != 2) continue;
			
			int value	= (i % 14) + 2;
			if (value == 14) value = Card.ACE;
			int suit	= (i / 14) + 1;
			
			result.add(getHand().peek(suit, value));
		}
System.out.println("���i�J�[�h�܂߂��őP�v���C���:" + result);
		return result;
	}
	
	/**
	 * �͂��߂̕��� SimplePlayer2 ��D�悳���邱�Ƃ��ł���
	 */
	static final boolean[] SPL_IS_SUPERIOR = new boolean[]
							 { true, true, true, false, false,
							 false, false, false, false, false,
							 false, false, false };
	
	/**
	 * �w�肳�ꂽ�v���C��₩��A���[�h�K���Ȃǂɏ]���v���C��I�т܂��B
	 * �e�v���C���ɂ��āApoint �t�����s���A�ő� point �̃v���C��ԋp���܂��B
	 *
	 * @return		�����v���C
	 */
	protected Card choosePlay(Packet option) throws InterruptedException {
		if (SPL_IS_SUPERIOR[getBoard().getTricks()]) {
			Card simplePlayer2Play = base.draw2(); // �l�����U���wait�Ȃ�
System.out.println("SimplePlayer2 �̈ӌ���D�� : " + simplePlayer2Play);
			return simplePlayer2Play;
		}
		
		//
		// point�t��������
		//
		int[] point = new int[option.size()];
		
		//
		// �I�[�v�j���O���[�h�̏ꍇ�̋K��
		//
		if (getBoard().getStatus() == Board.OPENING) {
			Packet p = leadSignal();
System.out.println("Lead Signal : " + p);
			for (int i = 0; i < option.size(); i++) {
				Card c = option.peek(i);
				if (p.contains(c)) point[i] += 100; //point[i] = 100;
			}
		}
		
		if (getBoard().getTurn() != getBoard().getDummy()) {
			// Dummy�ł�SimplePlayer2���@�\���Ȃ����߁A�X�L�b�v
			
			//
			// SimplePlayer2 �őI�񂾎�
			//
			Card simplePlayer2Play = base.draw2(); // �l�����U���wait�Ȃ�
System.out.println("SimplePlayer2 �̈ӌ� : " + simplePlayer2Play);
			int index = option.indexOf(simplePlayer2Play);
			if (index >= 0) point[index] += 50;
			
			//
			// ���[�h�̏ꍇ�ASimplePlayer2 �̃X�[�g���Ƃ̑I�񂾎���]��
			//
			if (getPlayOrder() == LEAD) {
				for (int suit = 1; suit < 5; suit++) {
					if (getMyHand().countSuit(suit) == 0) continue;
					Card sp;
					if (getBoard().getContract().getSuit() == Bid.NO_TRUMP) {
						sp = base.choosePlayInNTLead(suit);
					} else {
						sp = base.choosePlayInSuitLead(suit);
					}
System.out.println("SimplePlayer2 �̃X�[�g���Ƃ̈ӌ��F" + sp);
					int ind = option.indexOf(sp);
					if (ind >= 0) point[ind] += 10;
				}
			}
		}
		//
		// �f�B�X�J�[�h�̍ہA�X�N�C�Y�ϐ��𑝂₷����(2015/8/15�ǉ�)
		// �`�������邽�߁ASimplePlayer2 �łȂ� ReadAheadPlayer �ɋL�q
		// �@����̃T�C�h�X�[�c���G�X�^�u���b�V�������Ȃ����߁A�ȉ���
		// �@�A���h������ point[] �����炵�܂�
		// �@�@�@1) �p�[�g�i�[�Ɩ����������������ꍇ
		//   �@�@2) ����̒����T�C�h�X�[�g
		//
		if  ( (getPlayOrder() != LEAD)&& // ���[�h�łȂ�
			  (!option.containsSuit(getLead().getSuit()))&& // ���[�h�X�[�c���Ȃ�
			  (!getMyHand().containsSuit(getBoard().getTrump())) ) { // �g�����v���Ȃ�
			
System.out.println("�f�B�X�J�[�h�p�����J�n");
			//
			// ����̒����T�C�h�X�[�g�A�����A���Ȃ����o����
			//
			int s1 = (getMySeat() + 1)%4;
			int s2 = (getMySeat() + 3)%4;
			Packet hand1 = getBoard().getHand()[s1];
			Packet hand2 = getBoard().getHand()[s2];
			
			int longSideSuitSeat	= -1;
			int longSideSuit		= -1;
			int longSideSuitCount	= -1; // �����l
			
			for (int suit = 1; suit < 5; suit++) {
				if (suit == getBoard().getTrump()) continue;
				// �g�����v�͏��O, No Trump���͏��O�ΏۂȂ��ƂȂ�
				if (hand1.countSuit(suit) > longSideSuitCount) {
					longSideSuitCount	= hand1.countSuit(suit);
					longSideSuit		= suit;
					longSideSuitSeat	= s1;
				}
				if (hand2.countSuit(suit) > longSideSuitCount) {
					longSideSuitCount	= hand2.countSuit(suit);
					longSideSuit		= suit;
					longSideSuitSeat	= s2;
				}
			}
			if (longSideSuitCount == -1)
				throw new InternalError("ReadAheadPlayer �f�B�X�J�[�h�����ŁA�z��O��Ԃ����o���܂���");
			
System.out.println("����̒����T�C�h�X�[�g : " + BridgeUtils.suitString(longSideSuit));
			//
			// 1) 2) �̃A���h�����ƂȂ�X�[�g�����
			//
			int myCount  = getMyHand().countSuit(longSideSuit);
			int prdCount = getBoard().getHand(getPartnerSeat()).countSuit(longSideSuit);
			if ( (myCount >= prdCount)&&(myCount <= longSideSuitCount) ) {
				//	�����ɍ����̂ŁApoint[] �����_
				for (int i = 0; i < option.size(); i++) {
					if (option.peek(i).getSuit() == longSideSuit)
						// SimplePlayer2 ���D��
						point[i] -= 75;
				}
			}
		}
		
		//
		// �ő�̂��̂�I��
		//
		int maxPoint = point[0];
		int maxIndex = 0;
		for (int i = 1; i < option.size(); i++) {
			if (point[i] > maxPoint) {
				maxPoint = point[i];
				maxIndex = i;
			}
		}
		
		return option.peek(maxIndex);
	}
	
	private Packet leadSignal() {
		if (getBoard().getContract().getSuit() == Bid.NO_TRUMP) {
			return leadSignalInNoTrump();
		} else {
			return leadSignalInTrump();
		}
	}
	
	private Packet leadSignalInNoTrump() {
		Packet result = new PacketImpl();
		
		for (int suit = Card.CLUB; suit <= Card.SPADE; suit++) {
			if (getHand().countSuit(suit) == 0) continue;
			result.add(ntOpening(suit));
		}
		return result;
	}
	
	private Card ntOpening(int suit) {
		Packet hand = getMyHand();
		
		String suitPat = BridgeUtils.valuePattern(hand, suit);
		int value = -1;
		
		//
		// ����̃n���h�p�^�[���ɍ��v���邩
		//
		if (suitPat.startsWith("AKQ"))	{
			if (hand.countSuit(suit) >= 5) value = Card.ACE;
			else value = Card.KING;
		}
		if (suitPat.startsWith("KQJ"))	value = Card.KING;
		if (suitPat.startsWith("KQT"))	value = Card.KING;
		if (suitPat.startsWith("AQJT"))	value = Card.QUEEN;
		if (suitPat.startsWith("AQJ9"))	value = Card.QUEEN;
		if (suitPat.startsWith("QJT"))	value = Card.QUEEN;
		if (suitPat.startsWith("QJ9"))	value = Card.QUEEN;
		if (suitPat.startsWith("AKJT"))	{
			if (hand.countSuit(suit) >= 5) value = Card.ACE;
			else value = Card.KING;
		}
		if (suitPat.startsWith("AJT"))	value = Card.JACK;
		if (suitPat.startsWith("KJT"))	value = Card.JACK;
		if (suitPat.startsWith("JT"))	value = Card.JACK;
		if (suitPat.startsWith("AKT9"))	value = 10;
		if (suitPat.startsWith("AT9"))	value = 10;
		if (suitPat.startsWith("KT9"))	value = 10;
		if (suitPat.startsWith("QT9"))	value = 10;
		if (suitPat.startsWith("AQT9"))	value = 10;
		if (suitPat.startsWith("T9"))	value = 10;
		
		if (value > -1) return hand.peek(suit, value);
		
		Packet p = hand.subpacket(suit);
		p.arrange();
		if ( bridgeValue(p.peek(0)) < 10 ) {
			return p.peek(0); // �g�b�v�I�u�i�b�V���O
		}
		
		//
		// �S�����x�X�g���o���邩
		//
		int size = p.size();
		
		if (size >= 4) return p.peek(3);
		
		//
		// �S�����x�X�g���o���Ȃ�
		//
		if (size == 3) return p.peek(2);
		return p.peek(0);
	}
	
	private Packet leadSignalInTrump() {
		Packet result = new PacketImpl();
		
		for (int suit = Card.CLUB; suit <= Card.SPADE; suit++) {
			if (getHand().countSuit(suit) == 0) continue;
			
			result.add(suitOpening(suit));
		}
		return result;
	}
	
	//
	// AK �_�u���g������ K ���o�Ă��邯�ǂn�j�H
	//
	private Card suitOpening(int suit) {
		Packet hand = getMyHand();
//		if (suit == Board.getContract().getSuit()) return 0;
		
		String suitPat = BridgeUtils.valuePattern(hand, suit);
//System.out.println("suitOpening(suit) . suitPat = " + suitPat);
		if (suitPat.equals("AK")) return hand.peek(suit, Card.ACE);
		if (suitPat.startsWith("AK")) return hand.peek(suit, Card.KING);
		if (suitPat.startsWith("A")) return hand.peek(suit, Card.ACE);
		if (suitPat.startsWith("KQ")) return hand.peek(suit, Card.KING);
		if (suitPat.startsWith("QJ")) return hand.peek(suit, Card.QUEEN);
		if (suitPat.startsWith("KJT")) return hand.peek(suit, Card.JACK);
		if (suitPat.startsWith("JT")) return hand.peek(suit, Card.JACK);
		if (suitPat.startsWith("KT9")) return hand.peek(suit, 10);
		if (suitPat.startsWith("QT9")) return hand.peek(suit, 10);
		if (suitPat.startsWith("T9")) return hand.peek(suit, 10);
		if (suitPat.charAt(0) <= '9') return hand.peek(suit, suitPat.charAt(0) - '0');
		
		Packet p = hand.subpacket(suit);
		p.arrange();
		if ( bridgeValue(p.peek(0)) < 10 ) {
			return p.peek(0); // �g�b�v�I�u�i�b�V���O
		}
		if (p.size() >= 4) return p.peek(3);
		if (p.size() == 3) return p.peek(2);
		return p.peek(0);
	}
	
	/**
	 * Ace��14�ɕϊ����܂��B
	 */
	private int bridgeValue(int value) {
		if (value == 1) return 14;
		else return value;
	}
	
	/**
	 * Ace��14�Ƃ��āA�w��J�[�h�̒l��ǂݎ��܂�
	 */
	private int bridgeValue(Card target) {
		return bridgeValue(target.getValue());
	}
	
}
