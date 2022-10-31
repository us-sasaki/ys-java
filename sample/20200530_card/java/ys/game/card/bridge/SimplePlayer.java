package ys.game.card.bridge;

/*
 * 22, July 2001  countHonerPoint �֘A�� static �֐��Ƃ��� BridgeUtils �ɐ؂�o����
 */
import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;

/**
 * �a���̍l������{�I�ȃf�B�t�F���_�[�v���C���s���N���X�ł��B
 * �r�b�h�͂˂Ƀp�X���܂��B
 *
 * @version		a-release		22, July 2001
 * @author		Yusuke Sasaki
 */
public class SimplePlayer extends Player {
	protected Card		lead;
	protected Board		board;
	protected Packet	hand;
	protected Packet	dummyHand;
	
	protected String	openingLead;
	
/*-------------
 * constructor
 */
	public SimplePlayer(Board board, int seat) {
		setBoard(board);
		setMySeat(seat);
	}
	
	public SimplePlayer(Board board, int seat, String ol) {
		this(board, seat);
		openingLead = ol;
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
	 * �a���̍l������{�I�ȃf�B�t�F���_�[�v���C���s���܂��B
	 *
	 * @return		�a���̍l������{�I�ȃv���C
	 */
	public Card draw() throws InterruptedException {
		Thread.sleep(400); // �l�����U��
		
		board		= getBoard();
		hand		= getHand(); // = getMyHand()
		dummyHand	= getDummyHand();
		
		// ���[�h�̎�
		if (getPlayOrder() == LEAD) return playInLeading();
		
		//
		// ���t�ł���Ƃ��́i�N���܂����t���Ă��Ȃ���΁j���[���t��
		//�i�ǉ��F���łɒN�������t���Ă���Ƃ��A
		//             �I�|�[�l���g�����t���Ă���Ƃ��A
		//                     �I�|�[�l���g��苭���g�����v������΃`�[�y�X�g�ɃI�[�o�[���t�A
		//                     �I�|�[�l���g��苭���g�����v���Ȃ���΃f�B�X�J�[�h����B
		//             �p�[�g�i�[���������t���Ă���Ƃ��̓f�B�X�J�[�h����B�j
		//				�p�[�g�i�[�������Ă���Ƃ��̓��t�����A�f�B�X�J�[�h����B
		//            
		// �f�B�X�J�[�h����D�揇�ʂ� Winner, Kx, Qxx �͒Ⴂ�B(������)
		//
		lead = getLead(); // lead �łȂ��̂� not null
		
		if (!hand.containsSuit(lead.getSuit())) { // �X�[�g�t�H���[�ł��Ȃ�
			int trump = board.getContract().getSuit();
			Packet pack = hand.subpacket(trump);
			if (pack.size() > 0) {
				pack.arrange();
				return pack.peek(); // ���[�G�X�g���t
			} else {
				hand.shuffle();
				int suit = hand.peek().getSuit();
				hand.arrange();
				
				Packet pack2 = hand.subpacket(suit);
				pack2.arrange();
				return pack2.peek(); // ���[�G�X�g�f�B�X�J�[�h
			}
		}
		
		if (getPlayOrder() == SECOND)	return playIn2nd();
		if (getPlayOrder() == THIRD)	return playIn3rd();
		if (getPlayOrder() == FORTH)	return playIn4th();
		
		throw new InternalError("playOrder ���ُ�Ȓl�ł��B:"+getPlayOrder());
	}
	
	/**
	 * ���[�h�̎��́A
	 *   �`�j�E�B�i�[������΃L���b�V��
	 *   �a�j�E�B�i�[���Ȃ���
	 *      �k�g�n�́[���_�~�[�̃X�[�g�̂����A�g�����v�ȊO�ŃA�i�[�_�������A�W�_�ȉ���
	 *                  �Ƃ�������B�����Ƃ���D��B�Z���Ƃ���D��B
	 *      �q�g�n�́|�����݃p�[�g�i�[�ƃf�B�N���A���[�̎����Ă���_�̑����X�[�c��łB
	 */
	private Card playInLeading() {
		if ((board.getStatus() == Board.OPENING)&&(openingLead != null)&&(openingLead.length()==2)) {
			int suit = Card.SPADE;
			int value = 2;
			
			switch (openingLead.charAt(0)) {
			case 'S': suit = Card.SPADE; break;
			case 'H': suit = Card.HEART; break;
			case 'D': suit = Card.DIAMOND; break;
			case 'C': suit = Card.CLUB; break;
			}
			switch (openingLead.charAt(1)) {
			case 'A': value = Card.ACE; break;
			case 'K': value = Card.KING; break;
			case 'Q': value = Card.QUEEN; break;
			case 'J': value = Card.JACK; break;
			case 'T': value = 10; break;
			default: value = openingLead.charAt(1) - '0';
			}
			Card ol = getMyHand().peek(suit, value);
			if (ol != null) return ol;
		}
		
		if ((board.getStatus() != Board.OPENING)||(openingLead == null)||(openingLead.length()!=1)) {
//System.out.println("playInLeading()");
			Packet winner = getWinners(false); // before dummy
//System.out.println("playInLeading() winner = " + winner);
			if (winner.size() > 0) {	// �E�B�i�[�������
				winner.shuffle(); // ���̂����̈�������_���ɑI�����ĕԂ�
				return winner.peek(0);
			}
		}
		// �E�B�i�[���Ȃ�
//System.out.println("playInLeading(): no winner");
		if (getDummyPosition() == LEFT) {
//System.out.println("playInLeading(): no winner LHO");
			//-----
			// LHO
			//
			int determinedSuit = 0;
			if ((board.getStatus() == Board.OPENING)&&(openingLead != null)&&(openingLead.length()==1)) {
				switch (openingLead.charAt(0)) {
				case 'S': determinedSuit = Card.SPADE; break;
				case 'H': determinedSuit = Card.HEART; break;
				case 'D': determinedSuit = Card.DIAMOND; break;
				case 'C': determinedSuit = Card.CLUB; break;
				}
				// �����Ă��Ȃ��ꍇ�A�ʏ�̃X�[�g�̑I�ѕ��ɂ��������B
				if (!hand.containsSuit(determinedSuit)) determinedSuit = 0;
			}
			if (determinedSuit == 0) {
				int[] honerPts = BridgeUtils.countHonerPoint(dummyHand);
				
				int[] preference = new int[4]; // �ǂ̃X�[�c��ł��������̕]���_
				for (int i = 0; i < 4; i++) {
					preference[i] = 0;
					if (!hand.containsSuit(i + 1)) { // �����ĂȂ��X�[�c�͖��O
						preference[i] = Integer.MIN_VALUE;
						continue;
					}
					// �g�����v�̗D�揇�ʂ͒Ⴍ����
					if (board.getContract().getSuit() == i + 1) continue;
					
					if (honerPts[i + 1] <= 8) preference[i] = honerPts[i + 1] * 1000;
					
					preference[i] -= dummyHand.countSuit(i + 1) * 100;
				}
				
				// �X�[�g�����肷��B(�ׂ����O��FCLUB���D��)
				int pref = Integer.MIN_VALUE;
				for (int i = 0; i < 4; i++) {
					if (preference[i] > pref) {
						pref = preference[i];
						determinedSuit = i + 1;
					}
				}
			}
			
			// �X�[�g���聨���[�h�̋K���ɂ��������ăJ�[�h��I��
			// ���[�h�̋K�� 
			// �A�i�[�i�`�j�p�i�P�O�j���Ȃ��Ƃ��́A�g�b�v�I�u�i�b�V���O
			// �A�i�[���܂݁A������
			// �P��������
			// �Q������
			// �R���ȏと�A�i�[�V�[�N�G���X�i�`�j����P�O�X�܂Łj�����邩����
			//                                 �i�C���e���A�V�[�N�G���X���܂ށj
			//           �V�[�N�G���X���聨�V�[�N�G���X�̈�ԏ�
			//           �V�[�N�G���X�Ȃ����S���ȏと�S����
			//                             �R��    ���A�i�[���P���Ȃ�R����
			//                                     ���A�i�[�Q���ȏ�Ȃ�Q����
			Packet pack = hand.subpacket(determinedSuit);
			pack.arrange();
			if (pack.size() == 1) return pack.peek(); // �P���̂Ƃ��A����
			if (pack.size() == 2) return pack.peek(0); // �Q���̂Ƃ��A��
			if (pack.peek(0).getValue() < 10) return pack.peek(0); // �g�b�v�I�u�i�b�V���O
			
			// �R���ȏ�
			// �V�[�P���X��T��
			Card top = null;
			int v1 = pack.peek(0).getValue();
			for (int i = 1; i < pack.size(); i++) {
				int v2 = pack.peek(i).getValue();
				if ((v1 - v2 == 1)&&(v2 > 8)) {
					top = pack.peek(i-1);
					break;
				}
				v1 = v2;
				if (v1 < 10) break;
			}
			if (top != null) return top; // �g�b�v�I�u�V�[�P���X
			
			if (pack.size() >= 4) return pack.peek(3); // �S���ȏ�
			// �R���̂Ƃ�
			int honers = 0;
			for (int i = 0; i < pack.size(); i++) {
				if (pack.peek(i).getValue() > 9) honers++;
			}
			if (honers == 1) return pack.peek(2);
			return pack.peek(1);
			
		} else {
//System.out.println("playInLeading(): no winner RHO");
			//------------
			// RHO �̂͂�
			//
			Packet knownCards = board.getOpenCards(); // dummy + played
			knownCards.add(hand);
			
			Packet unknownCards = knownCards.complement(); // declarer + pard
			
			int[] honerPts = BridgeUtils.countHonerPoint(unknownCards);
//System.out.println("playInLeading(): honerPts = " + honerPts[0] + "," + honerPts[1] + "," + honerPts[2] + "," + honerPts[3] + "," + honerPts[4]);
			
			int[] preference = new int[4]; // �ǂ̃X�[�c��ł��������̕]���_
			for (int i = 0; i < 4; i++) {
				preference[i] = 0;
				if (!hand.containsSuit(i + 1)) { // �����ĂȂ��X�[�c�͖��O
					preference[i] = Integer.MIN_VALUE;
					continue;
				}
				// �g�����v�̗D�揇�ʂ�Ⴍ
				if (board.getContract().getSuit() == i + 1) continue;
				preference[i] = honerPts[i + 1] * 1000;
				
				preference[i] += dummyHand.countSuit(i + 1) * 100;
			}
			
			// �X�[�g�����肷��B(�ׂ����O��FCLUB���D��)
			int determinedSuit = 0;
			int pref = Integer.MIN_VALUE;
			for (int i = 0; i < 4; i++) {
				if (preference[i] > pref) {
					pref = preference[i];
					determinedSuit = i + 1;
				}
			}
			
			// ���[�G�X�g������
			Packet pack = getMyHand().subpacket(determinedSuit);
			pack.arrange();
			
			return pack.peek();
		}
	}
	
	/**
	 * �Q�Ԏ�ł́A
	 * �E�E�B�i�[������Ώo���i��������Ή�����j
	 * �E�Ȃ���΃��[�G�X�g
	 */
	private Card playIn2nd() {
//System.out.println("playIn2nd()");
		int suit = lead.getSuit();
		
		boolean afterDummy = (getDummyPosition() == RIGHT);
		
		Packet winner = getWinners(afterDummy);
		
		Packet pack = hand.subpacket(suit);
		// win = pack.intersection(winner); �̂悤�Ȏ������Ȍ��B
		
		winner.arrange();
		for (int i = winner.size() - 1; i >= 0; i--) {
			Card c = winner.peek(i);
			if ( (pack.contains(c))&&(c.getSuit() == suit) ) return c;
		}
		
		// �Ȃ�
		pack.arrange();
		
		return pack.peek(); // ���[�G�X�g
	}
	
	/**
	 * �R�Ԏ�ł́A
	 * �E�k�g�n�̏ꍇ�A�_�~�[�ƃv���C���ꂽ�J�[�h�����āA���Ă邩�l����B
	 *   ���Ă�Ȃ�`�[�y�X�g�ɏ��B���߂Ȃ烍�[�G�X�g�B
	 * �E�q�g�n�̏ꍇ�A�n�C�G�X�g���o���i�������_�~�[�Ǝ��������킹���J�[�h��
	 *   �V�[�N�G���X�ƂȂ鎞�͂��̓��ōŉ��ʂ��o���j
	 */
	private Card playIn3rd() {
//System.out.println("playIn3rd()");
		if (getDummyPosition() == LEFT) {
//System.out.println("playIn3rd() : LHO");

			//
			// LHO
			//
			
			// ���Ă�J�[�h�������Ă��邩�H
			// �_�~�[�̂����Ƃ������J�[�h��I��
			Card dummyStrongest;
			Packet dFollow = dummyHand.subpacket(lead.getSuit());
			
			if (dFollow.size() == 0) {
				dummyStrongest = dummyHand.peek(); // �K���Ȃ���(trump��������Ȃ���)
			}
			else {
				dFollow.arrange();
				dummyStrongest = dFollow.peek(0);
			}
			
			//
			// �������V�~�����[�g����
			//
			Packet pack = hand.subpacket(lead.getSuit());
			pack.arrange();
			
			Card play = null;
			for (int i = 0; i < pack.size(); i++) {
				Trick virtual = new TrickImpl(getTrick());
				virtual.add(pack.peek(i));
				virtual.add(dummyStrongest);
				if (((virtual.getWinner() ^ getMySeat()) & 1) == 0) play = pack.peek(i);
			}
			if (play == null) play = pack.peek();
			
			return play;
		} else {
//System.out.println("playIn3rd() : RHO");
			//
			// RHO(�P�Ƀn�C�G�X�g��I�ԁA������J�[�h�����Ȃ��ꍇ�A���[�G�X�g) 
			//                   �����܂��@�\���ĂȂ�����
			//
			Packet pack = hand.subpacket(lead.getSuit());
			pack.arrange();
			
			Card c = pack.peek(0); // �n�C�G�X�g
			
			if (pack.size() == 1) return c;
			
			Trick virtual = new TrickImpl(getTrick());
			virtual.add(c);
			virtual.add(pack.peek(1));
			if (virtual.getWinner() == getMySeat()) return c;
			return pack.peek();
		}
	}
	
	/**
	 * �S�Ԏ�ł́A��Ԉ����������[�G�X�g
	 */
	private Card playIn4th() {
//System.out.println("playIn4th()");
		Packet pack = hand.subpacket(lead.getSuit());
		pack.arrange();
		Card play = null;
		
		for (int i = 0; i < pack.size(); i++) {
			Trick virtual = new TrickImpl(getTrick());
			virtual.add(pack.peek(i));
			if (((virtual.getWinner() ^ getMySeat()) & 1) == 0) play = pack.peek(i);
		}
		if (play == null) play = pack.peek();
		return play;
	}
	
	/**
	 * �����̃n���h�̒��ŃE�B�i�[�ƂȂ��Ă���J�[�h�𒊏o���� Packet ��ԋp���܂��B
	 * �E�B�i�[�ł��邱�Ƃ́A�e�X�[�g�ɂ����č��v���C����Ă��Ȃ��J�[�h�̂��������Ƃ�
	 * �����J�[�h�ł��邱�ƂŔ��f���܂��B
	 * �s�m��ȏ��͎g�p���܂���B
	 * 
	 * @return		winner
	 */
	private Packet getWinners(boolean afterDummy) {
		Packet result = new PacketImpl();
		
		//
		// �c��̃J�[�h�𒊏o����
		//
		Packet rest = board.getOpenCards().complement();
		rest.add(getTrick());
		
		if (!afterDummy) rest.add(getDummyHand());
//System.out.println("getWinners() : rest = " + rest);
		
		// �c��̃J�[�h���e�X�[�g�ɕ�����
		Packet[] suits = new Packet[4];
		
		for (int i = 0; i < 4; i++) {
			suits[i] = rest.subpacket(i+1);
			suits[i].arrange();	// �����J�[�h���������C���f�b�N�X��
		}
		
		// �e�X�[�c�̃E�B�i�[�𒊏o����
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < suits[i].size(); j++) {
				Card winner = suits[i].peek(j);
//System.out.println("getWinners() : winner = " + winner);
				if (hand.contains(winner)) {
					result.add(winner); // �ō��ʂ������Ă���ꍇ�A���̈ʂ����ׂ�
				} else {
					break;	// �V�[�N�G���X���؂ꂽ�ꍇ�A�I��
				}
			}
		}
		
		result.arrange();
		return result;
	}
	
}
