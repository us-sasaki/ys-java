package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;

/**
 * �a���̍l�����f�B�t�F���_�[�v���C���s���N���X�ł��B
 * �r�b�h�͂˂Ƀp�X���܂��B
 *
 * @version		making		27, July 2002
 * @author		Yusuke Sasaki
 */
public class SimplePlayer2 extends Player {
	protected Card		lead;
	protected Board		board;
	protected Packet	hand;
	protected Packet	dummyHand;
	
	protected String	openingLead;
	
/*-------------
 * constructor
 */
	public SimplePlayer2(Board board, int seat) {
		setBoard(board);
		setMySeat(seat);
	}
	
	public SimplePlayer2(Board board, int seat, String ol) {
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
	 * �a���̍l�����f�B�t�F���_�[�v���C���s���܂��B
	 *
	 * @return		�a���̍l�����v���C
	 */
	public Card draw() throws InterruptedException {
		Thread.sleep(400); // �l�����U��
		return draw2();
	}
	
	public Card draw2() {
		board		= getBoard();
		hand		= getHand(); // = getMyHand()
		dummyHand	= getDummyHand();
		lead		= getLead();
		
		int order = getPlayOrder();
		if (order == LEAD)		return playIn1st();
		//
		// ���t�ł���Ƃ��́i�N���܂����t���Ă��Ȃ���΁j���[���t��
		//�i�ǉ��F���łɒN�������t���Ă���Ƃ��A
		//             �I�|�[�l���g�����t���Ă���Ƃ��A
		//                     �I�|�[�l���g��苭���g�����v������΃`�[�y�X�g�ɃI�[�o�[���t�A
		//                     �I�|�[�l���g��苭���g�����v���Ȃ���΃f�B�X�J�[�h����B
		//             �p�[�g�i�[���������t���Ă���Ƃ��̓f�B�X�J�[�h����B�j
		//				�p�[�g�i�[�������Ă���Ƃ��̓��t�����A�f�B�X�J�[�h����B
		//
		if (!hand.containsSuit(lead.getSuit())) { // �X�[�g�t�H���[�ł��Ȃ�
			int trump = board.getContract().getSuit();
			Packet pack = hand.subpacket(trump); // NT�̂Ƃ��́A��ɂȂ�
			if (pack.size() > 0) {
				// ���t���邱�Ƃ��ł���
				pack.arrange();
				if ((order == THIRD)&&(getDummyPosition()==LEFT)) {
					//--------------------------------
					// �O�Ԏ�ł��Ōオ�_�~�[�̏ꍇ
					//--------------------------------
					// ���t�ŏ��Ă�ꍇ�A�`�[�y�X�g���t�ŏ���
					// ���ĂȂ��A�܂��̓f�B�X�J�[�h�ŏ��Ă�(�p�[�g�i�[�������Ă���)
					// �ꍇ�A�f�B�X�J�[�h(discard()���I�񂾃J�[�h)����B
					//
					
					// ���Ă�J�[�h�������Ă��邩�H
					// �_�~�[�̂����Ƃ������J�[�h��I��
					Card dummyStrongest;
					Packet dFollow = dummyHand.subpacket(lead.getSuit());
					
					if (dFollow.size() == 0) {
						dFollow = dummyHand.subpacket(trump);
					}
					if (dFollow.size() == 0) {
						dummyStrongest = dummyHand.peek(); // �K���Ȃ���
					}
					else {
						// �t�H���[�ł���Ƃ��͂��̃X�[�g�̍ő�̂���
						// �ł��Ȃ��Ƃ��̓g�����v�̍ő�Ȃ���
						dFollow.arrange();
						dummyStrongest = dFollow.peek(0);
					}
					
					//
					// �������V�~�����[�g����(�_�~�[�n���h����̃v���C���s��)
					//
					
					// discard �ł����Ă�ꍇ�Adiscard���邽�߁A���ɒǉ�
					
					Packet pack2 = hand.subpacket(board.getContract().getSuit());
					pack2.arrange();
					pack2.add(discard());
					
					Card play = null;
					for (int i = 0; i < pack2.size(); i++) {
						Trick virtual = new TrickImpl(getTrick());
						virtual.add(pack2.peek(i));
						virtual.add(dummyStrongest);
						if (isItOurSide(virtual.getWinner())) play = pack2.peek(i);
					}
					// ���߂Ȃ̂Ńf�B�X�J�[�h
					if (play == null) return discard();
					
					return play;
				}
				if (order == THIRD) {
					//--------------------------------------
					// �O�Ԏ�ōŌ�̈�l�̓_�~�[�łȂ��ꍇ
					//--------------------------------------
					Packet winner = getWinners2();
					if ( (winner.contains(lead))
							&&(isItOurSide(board.getTrick().getWinner())) ) {
						// �p�[�g�i�[�̓E�B�i�[���v���C���A���ꂪ�����Ă���Ƃ��Ƀf�B�X�J�[�h
						return discard();
					}
					// ���[���t
					pack.arrange();
					return pack.peek();
				}
				if (order == FORTH) {
					//--------------
					// �l�Ԏ�̏ꍇ
					//--------------
					// �p�[�g�i�[�����łɃv���C���Ă���
					if (isItOurSide(board.getTrick().getWinner())) {
						// ���������i���̏ꍇ�p�[�g�i�[�j�̏���
						
						
						// �f�B�X�J�[�h����
						return discard();
					}
				}
				//--------------------------------------------
				// ��Ԏ�̏ꍇ�A�l�Ԏ�ł܂������Ă��Ȃ��ꍇ
				//--------------------------------------------
				// �p�[�g�i�[���v���C���Ă��Ȃ����A�����Ă��Ȃ�
				// (�`�[�y�X�g�ɃI�[�o�[)���t�����݂�
				for (int i = pack.size()-1; i >= 0; i--) {
					Card c = pack.peek(i);
					Trick virtual = new TrickImpl(board.getTrick());
					virtual.add(c);
					if (isItOurSide(virtual.getWinner()))	return c;
				}
				return discard();
			} else {
				// �f�B�X�J�[�h����
				return discard();
			}
		}
		
		//--------------------------
		// �X�[�g�t�H���[�ł���ꍇ
		//--------------------------
		
		if (order == SECOND)	return playIn2nd();
		if (order == THIRD)		return playIn3rd();
		if (order == FORTH)		return playIn4th();
		
		throw new InternalError("Play Order ���ُ�l("+order+")�ɂȂ��Ă��܂�");
	}
	
	/**
	 * �f�B�X�J�[�h���܂��B
	 */
	private Card discard() {
		Packet winners = getWinners(); // �����̃n���h�̃E�B�i�[
		boolean winnerIsInOnlyOneSuit = false;
		for (int i = 0; i < 4; i++) {
			int s = winners.size();
			if ((s > 0)&&(s == winners.countSuit(i+1))) winnerIsInOnlyOneSuit = true;
		}
		
		// �X�[�g��I��
		int trump = board.getContract().getSuit();
		
		// �g�����v���������Ă��Ȃ��Ƃ�
		// �d���Ȃ����[�G�X�g�g�����v���f�B�X�J�[�h
		if (hand.size() == hand.subpacket(trump).size()) return hand.peek();
		
		// ���
		Packet p = new PacketImpl(); // winner �������
		Packet w = new PacketImpl(); // winner ������
		for (int i = 0; i < hand.size(); i++) {
			Card c = hand.peek(i);
			if (c.getSuit() == trump) continue;	// �g�����v�͌��Ƃ��Ȃ�
			w.add(c);
			if ((!winnerIsInOnlyOneSuit)&&winners.contains(c))
				continue;	// �E�B�i�[�������Ă���X�[�g���Q�ȏ�̂Ƃ��A�E�B�i�[�����Ƃ��Ȃ�
			p.add(c);
		}
		int suit;
		if (p.size() == 0) {
			// �E�B�i�[���������Ă��Ȃ�
			w.shuffle();
			suit = w.peek().getSuit();
		} else {
			p.shuffle();
			suit = p.peek().getSuit();
		}
		Packet p2 = hand.subpacket(suit);
		p2.arrange();
		return p2.peek(); // ���[�G�X�g
	}
	
	//*********************************************************************//
	//  �P�Ԏ�̃v���C�i���ʕ����j                                         //
	//*********************************************************************//
	
	/**
	 * ���[�h�̈ʒu�ɂ���Ƃ��̎���l���܂��B
	 * �I�[�v�j���O���[�h���ǂ����A�R���g���N�g���m�s���ǂ����łS�ʂ�̊֐��ɕ��򂵂܂��B
	 */
	private Card playIn1st() {
		if (board.getStatus() == Board.OPENING) {
			// �I�[�v�j���O���[�h
			
			//
			// �w�肪����ꍇ�͂��̃J�[�h
			//
			if (openingLead != null) {
				Card play = null;
				int suit	= -1;
				int value	= -1;
				
				try {
					switch (openingLead.charAt(0)) {
					case 'S': suit = Card.SPADE;	break;
					case 'H': suit = Card.HEART;	break;
					case 'D': suit = Card.DIAMOND;	break;
					case 'C': suit = Card.CLUB;		break;
					}
					switch (openingLead.charAt(1)) {
					case 'A': value = Card.ACE;		break;
					case 'K': value = Card.KING;	break;
					case 'Q': value = Card.QUEEN;	break;
					case 'J': value = Card.JACK;	break;
					case 'T': value = 10;			break;
					default: value = openingLead.charAt(1) - '0';
					}
				} catch (Exception e) {
				}
				if ( (suit != -1)&&(value != -1) ) {
					// �w�肳�ꂽ�X�[�g�ƃo�����[���Ƃ��ɗL��
					Card ol = getMyHand().peek(suit, value);
					if (ol != null) return ol;
				}
				if ((suit != -1)&&(hand.containsSuit(suit))) {
					// �X�[�g�݂̂��L��
					if (board.getContract().getSuit() == Bid.NO_TRUMP) return ntOpening(suit);
					return suitOpening(suit);
				}
			}
			
			if (board.getContract().getSuit() == Bid.NO_TRUMP) return ntOpening();
			return suitOpening();
		}
		if (board.getContract().getSuit() == Bid.NO_TRUMP) return ntLead();
		return suitLead();
	}
	
	//*********************************************************************//
	//  �m�s�I�[�v�j���O���[�h�̃v���C                                     //
	//*********************************************************************//
	
	/**
	 * �m�s�R���g���N�g�̏ꍇ�̃I�[�v�j���O���[�h���l���܂�
	 */
	private Card ntOpening() {
		//
		// ��Ԓ����X�[�g��I��(���ꖇ���̂Ƃ��̓����N�̍����X�[�g)
		//
		int suit = -1;
		int max  = -1;
		
		for (int i = 0; i < 4; i++) {
			int c = hand.countSuit(i+1);
			if (c > max) {
				max = c;
				suit = i+1;
			}
		}
		
		return ntOpening(suit);
	}
	
	/**
	 * �m�s�R���g���N�g�̃I�[�v�j���O���[�h�ŃX�[�g�܂ł��܂��Ă���ꍇ
	 */
	private Card ntOpening(int suit) {
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
		if ( (suitPat.startsWith("KQ"))&&(hand.countSuit(suit) == 3) ) value = Card.KING; // 2015/8/15 added
		if (suitPat.startsWith("AQJT"))	value = Card.QUEEN;
		if (suitPat.startsWith("AQJ9"))	value = Card.QUEEN;
		if (suitPat.startsWith("QJT"))	value = Card.QUEEN;
		if (suitPat.startsWith("QJ9"))	value = Card.QUEEN;
		if ( (suitPat.startsWith("QJ"))&&(hand.countSuit(suit) == 3) ) value = Card.QUEEN; // 2015/8/15 added
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
	
	//*********************************************************************//
	//  �X�[�g�R���g���N�g�ł̃I�[�v�j���O���[�h�̃v���C                   //
	//*********************************************************************//
	
	/**
	 * �X�[�c�R���g���N�g�̃I�[�v�j���O���[�h���l����B
	 */
	private Card suitOpening() {
		int max  = -1;
		Card play = null;
		
		for (int i = 0; i < 4; i++) {
			if ( (i+1) == board.getContract().getSuit() ) continue; // �g�����v�͏��O
			
			String suitPat = BridgeUtils.valuePattern(hand, i+1);
			
			// AK �̂���X�[�g (10 �_)
			if ( (suitPat.startsWith("AK"))&&(max < 10) ) {
				max = 10;
				play = hand.peek(i+1, Card.KING);
			}
			// KQ �̂���X�[�g ( 9 �_)
			if ( (suitPat.startsWith("KQ"))&&(max < 9) ) {
				max = 9;
				play = hand.peek(i+1, Card.KING);
			}
			// �V���O���g�� ( 8 �_)
			if ( (suitPat.length() == 1)&&(max < 8) ) {
				max = 8;
				play = hand.subpacket(i+1).peek();
			}
			// QJ �̂���X�[�g (7 �_)
			if ( (suitPat.startsWith("QJ"))&&(max < 7) ) {
				max = 7;
				play = hand.peek(i+1, Card.QUEEN);
			}
			// �_�u���g�� (6 �_)
			if ( (suitPat.length() == 2)&&(max < 6) ) {
				max = 6;
				Packet p = hand.subpacket(i+1);
				p.arrange();
				play = p.peek(0);
			}
		}
		if (play != null) return play;
		
		//
		// ���܂�Ȃ�����(�K���ȃX�[�g�𗐐��őI��)
		//
		for (int i = 0; i < 20; i++) {
			int suit = (int)(Math.random() * 4) + 1;
			if (suit == board.getContract().getSuit()) continue;
			
			if (hand.containsSuit(suit)) return suitOpening(suit);
		}
		//
		// �n���h���g�����v�X�[�g�݂̂���Ȃ��Ă���ȂǋH�ȏꍇ
		//
		return hand.peek();
	}
	
	/**
	 * �X�[�c�R���g���N�g�ŃI�[�v�j���O���[�h�̃X�[�g�����܂����Ƃ�
	 */
	private Card suitOpening(int suit) {
//		if (suit == Board.getContract().getSuit()) return 0;
		
		String suitPat = BridgeUtils.valuePattern(hand, suit);
//System.out.println("suitOpening(suit) . suitPat = " + suitPat);
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
	
	//*********************************************************************//
	//  �m�s�ł̂P�Ԏ�̃v���C                                             //
	//*********************************************************************//
	
	/**
	 * �m�s�R���g���N�g�ł̃��[�h
	 *
	 *
	 */
	private Card ntLead() {
// �R�D�f�B�t�F���_�[���������Ƃ��̃��[�h
// �E�m�s�̏ꍇ
// �@�X�[�g�̌��ߕ����̏��ʁF
// �@�i�O�j�����̎�̃E�B�i�[�̐��{�p�[�g�i�[�̃E�B�i�[�̐���
// �@�@�@�@�R���g���N�g�𗎂Ƃ��̂ɏ\���ȂƂ��A�����̃E�B�i�[���L���b�V��
// �@�i�P�j�p�[�g�i�[�ɃE�B�i�[�̂���X�[�g
// �@�i�Q�j�n�D�k�D�Ɠ����X�[�g
// �@�i�R�j���܂Ńf�B�t�F���_�[���������Ƃ��Ƀ��[�h�����X�[�g�i�ŋ߂��珇�Ɂj
// �@�i�S�j�O�`�R�̃X�[�g���Ȃ��Ƃ�
// �@�@�@�@�k�g�n�i�f�B�N���A���[�̍���j�F
//                �_�~�[�̃A�i�[�i�g�b�o�Ŕ��f�j�̑����X�[�g
// �@�@�@�@�q�g�n�i�f�B�N���A���[�̉E��j�F
// �@�@�@�@�@�@�@�@�_�~�[�̃A�i�[�i�g�b�o�Ŕ��f�j�̏��Ȃ��X�[�g�@�@
// 
// �@�X�[�g���ł̃J�[�h�̌��ߕ������̏��ʂŌ��߂�@�@�@�@�@�@�@�@
// �@�@�i�P�j���̃X�[�g�̒��ŁA�g�b�v���E�B�i�[�Ȃ�L���b�V��
//�@�@�@�@�@�`�j����͂j�A���͏�
// �@�@�i�Q�j�j�p����j
// �@�@�i�R�j�p�i����p
// �@�@�i�S�j�j�i�s�C�i�s����i
// �@�@�i�T�j�j�s�X�C�p�s�X�A�s�X����s
// �@�@�i�U�j���̑��F���݂Q���F��
// �@�@�@�@�@�@�@�@�@���݂R���F�R����
// �@�@�@�@�@�@�@�@�@���݂S���ȏと�S���ځ@
		//
		// (0) �����ƃp�[�g�i�[�̃E�B�i�[�̐����R���g���N�g�𗎂Ƃ��̂ɏ\���ȂƂ��A
		//     �����̃E�B�i�[���L���b�V��
		Packet winners = getWinnersInNTLead();
		
		// �f�B�t�F���_�[���̂Ƃ����g���b�N
		int win = board.getTricks() - BridgeUtils.countDeclarerSideWinners(board); 
		if ( (winners.size() + win) > 7 - board.getContract().getLevel() ) {
			// �R���g���N�g�𗎂Ƃ���
			for (int i = 0; i < winners.size(); i++) {
				if (hand.contains(winners.peek(i))) return winners.peek(i);
			}
		}

		//
		// (1) �p�[�g�i�[�ɃE�B�i�[�̂���X�[�g
		//		�i�����̎����Ă���X�[�g�j
		//
//		for (int i = 0; i < winners.size(); i++) {
//			Card c = winners.peek(0);
//			if (!hand.contains(c)) {
//				int suit = c.getSuit();
//				Packet p = hand.subpacket(suit);
//				p.arrange();
//				return p.peek(); // ���[�G�X�g
//			}
//		}
		
		int suit = chooseSuitInNTLead();
		return choosePlayInNTLead(suit);
		
	}
	
	/**
	 * NT�R���g���N�g�̃��[�h�X�[�g��I�т܂��B
	 */
// �@�X�[�g�̌��ߕ����̏��ʁF
// �@�i�P�j�p�[�g�i�[�ɃE�B�i�[�̂���X�[�g
// �@�i�Q�j�n�D�k�D�Ɠ����X�[�g
// �@�i�R�j���܂Ńf�B�t�F���_�[���������Ƃ��Ƀ��[�h�����X�[�g�i�ŋ߂��珇�Ɂj
// �@�i�S�j�O�`�R�̃X�[�g���Ȃ��Ƃ�
// �@�@�@�@�k�g�n�i�f�B�N���A���[�̍���j�F
//                �_�~�[�̃A�i�[�i�g�b�o�Ŕ��f�j�̑����X�[�g
// �@�@�@�@�q�g�n�i�f�B�N���A���[�̉E��j�F
// �@�@�@�@�@�@�@�@�_�~�[�̃A�i�[�i�g�b�o�Ŕ��f�j�̏��Ȃ��X�[�g�@�@
	private int chooseSuitInNTLead() {
		//
		// (1) �p�[�g�i�[�ɃE�B�i�[�̂���X�[�g
		//     �� �����(2)�Ɠ��l�ł��邪�ꉞ�������Ă���
		//
		Packet winners = getWinnersInNTLead();
		for (int i = 0; i < winners.size(); i++) {
			Card c = winners.peek(i);
			if (!hand.contains(c)) { // �p�[�g�i�[�������Ă���
				if (hand.containsSuit(c.getSuit())) return c.getSuit();
			}
		}
		
		//
		// (2) O.L. �Ɠ����X�[�g
		//
		if (board.getTricks() >= 1) {
			Card c = board.getAllTricks()[0].peek(0);
			if (hand.containsSuit(c.getSuit())) return c.getSuit();
		}
		
		//
		// (3) ���܂Ńf�B�t�F���_�[���������g���b�N�̃��[�h(�ŋ߂��珇��)
		//
		Trick[] trick = board.getAllTricks();
		for (int i = board.getTricks()-2; i >= 0; i--) {
			if (isItOurSide(trick[i].getWinner())) { // ���������̏���
				int suit = trick[i+1].getLead().getSuit();
				if (hand.containsSuit(suit)) return suit;
			}
		}
		
		//
		// (4) �O�`�R�̃X�[�g���Ȃ��Ƃ�
		//�@�@�@�@�k�g�n�i�f�B�N���A���[�̍���j�F
		//            �_�~�[�̃A�i�[�i�g�b�o�Ŕ��f�j�̑����X�[�g
		//�@�@�@�@�q�g�n�i�f�B�N���A���[�̉E��j�F
		//    �@�@�@�@�_�~�[�̃A�i�[�i�g�b�o�Ŕ��f�j�̏��Ȃ��X�[�g
		//
		int[] dummyHonerPoint = BridgeUtils.countHonerPoint(dummyHand);
		
		if (getDummyPosition() == LEFT) { // �����͂k�g�n
			int maxHcpSuit = 0;
			int maxHcpVal  = -1;
			for (int i = 1; i < 5; i++) {
				if (!dummyHand.containsSuit(i)) continue; // �����ĂȂ��X�[�g�͏��O
				if (!hand.containsSuit(i)) continue;
				if (dummyHonerPoint[i] >= maxHcpVal) { // ���� HCP �ł� Major ��D�悳����
					maxHcpVal  = dummyHonerPoint[i];
					maxHcpSuit = i;
				}
			}
			if (maxHcpVal > -1) return maxHcpSuit;
		} else { // �����͂q�g�n
			int minHcpSuit = 0;
			int minHcpVal  = 100;
			for (int i = 1; i < 5; i++) {
				if (!dummyHand.containsSuit(i)) continue;
				if (!hand.containsSuit(i)) continue;
				if (dummyHonerPoint[i] <= minHcpVal) { // ���� HCP �ł� Major ��D�悳����
					minHcpVal  = dummyHonerPoint[i];
					minHcpSuit = i;
				}
			}
			if (minHcpVal < 100) return minHcpSuit;
		}
		// �����ĂȂ��X�[�g���Y���X�[�g�������ꍇ�A�K����
		hand.shuffle();
		int suit = hand.peek(0).getSuit();
		hand.arrange();
		return suit;
	}
	
	/**
	 * NT �R���g���N�g�̏ꍇ�ŁA���[�h����X�[�g�����܂����ꍇ�̃v���C���s���܂��B
	 *�@�X�[�g���ł̃J�[�h�̌��ߕ������̏��ʂŌ��߂�@�@�@�@�@�@�@�@
	 *�@�@�i�P�j���̃X�[�g�̒��ŁA�g�b�v���E�B�i�[�Ȃ�L���b�V��
	 *�@�@�i�Q�j�j�p����j
	 *�@�@�i�R�j�p�i����p
	 *�@�@�i�S�j�j�i�s�C�i�s����i
	 *�@�@�i�T�j�j�s�X�C�p�s�X�A�s�X����s
	 *�@�@�i�U�j���̑��F���݂Q���F��
	 *�@�@�@�@�@�@�@�@�@���݂R���F�R����
	 *�@�@�@�@�@�@�@�@�@���݂S���ȏと�S���ځ@
	 */
	public Card choosePlayInNTLead(int suit) {
		Packet candidacy = hand.subpacket(suit);
		if (candidacy.size() == 0)
			throw new InternalError("choosePlayInNTLead �Ŏw�肳�ꂽ�X�[�g("+suit+")�������Ă��܂���");
		candidacy.arrange();
		
		// �i�P�j���̃X�[�g�̒��ŁA�g�b�v���E�B�i�[�Ȃ�L���b�V��
		Packet winner = getWinners(); //getWinnersInNTLead();
		Card top = candidacy.peek(0);
		if (winner.contains(top.getSuit(), top.getValue())) return top;
		
		// �i�Q�j�j�p����j
		if (BridgeUtils.patternMatch(hand, "KQ*", suit)) {
			return hand.peek(suit, Card.KING);
		}
		
		// �i�R�j�p�i����p
		if (BridgeUtils.patternMatch(hand, "QJ*", suit)) {
			return hand.peek(suit, Card.QUEEN);
		}
		
		// �i�S�j�j�i�s�C�i�s����i
		if (BridgeUtils.patternMatch(hand, "KJT*", suit)) {
			return hand.peek(suit, Card.JACK);
		}
		if (BridgeUtils.patternMatch(hand, "JT*", suit)) {
			return hand.peek(suit, Card.JACK);
		}
		
		// �i�T�j�j�s�X�C�p�s�X�A�s�X����s
		if (BridgeUtils.patternMatch(hand, "KT9*", suit)) {
			return hand.peek(suit, 10);
		}
		if (BridgeUtils.patternMatch(hand, "QT9*", suit)) {
			return hand.peek(suit, 10);
		}
		if (BridgeUtils.patternMatch(hand, "T9*", suit)) {
			return hand.peek(suit, 10);
		}
		
		// �i�U�j���̑��F���݂Q���F��
		// �@�@�@�@�@�@�@���݂R���F�R����
		// �@�@�@�@�@�@�@���݂S���ȏと�S���ځ@
		switch (candidacy.size()) {
		case 1:
		case 2:
			return candidacy.peek(0);
		case 3:
			return candidacy.peek(2);
		default:
			return candidacy.peek(3);
		}
	}
	
	//*********************************************************************//
	//  �X�[�g�R���g���N�g�ł̂P�Ԏ�̃v���C                               //
	//*********************************************************************//
	
	//�E�X�[�c�̏ꍇ
	//�@�X�[�g�̌��ߕ����̏��ʁF
	//�@�i�O�j�����̎�̃E�B�i�[�̐��{�p�[�g�i�[�̃E�B�i�[�̐���
	//�@�@�@�@�R���g���N�g�𗎂Ƃ��̂ɏ\���ȂƂ��A���ׂăL���b�V��
	//
	//�@�i�P�j���t���X�̃X�[�g�����O�i���ꂵ���Ȃ���΂������Ȃ��j
	//�@�@�@�@���t���X�̃X�[�g�Ƃ́F
	//�@�@�@�@�_�~�[�ɂ��f�B�N���A���[�ɂ��g�����v���c���Ă���󋵂�
	//�@�@�@�@�@�@�@�@�i���݂O���Ɣ������Ă��Ȃ����Ɓj�@�@�@�@
	//�@�@�@�@�_�~�[���f�B�N���A���[�������݂O���Ɣ������Ă���X�[�g�@�@�@�@�@�@�@
	//�@�@�@�@�@�@�@�@�@
	//�@�i�Q�j�p�[�g�i�[�̃g�����v�X�[�g�����݂O���Ɗm�肵�Ȃ��Ƃ��A����
	//�@�@�@�@�p�[�g�i�[�Ɍ��݂O���Ɗm�肵�Ă���T�C�h�X�[�g������Ƃ��A���̃X�[�g
	//�@�@�@�@�i���t������j
	//
	//�@�i�R�j�p�[�g�i�[�ɃE�B�i�[�̂���X�[�g
	//�@�i�S�j�n�D�k�D�Ɠ����X�[�g
	//�@�i�T�j���܂܂Ńf�B�t�F���_�[���������Ƃ��Ƀ��[�h�����X�[�g�i�ŋ߂��珇�Ɂj
	//�@�i�U�j�ȏ�̃X�[�g���Ȃ��Ƃ�
	//�@�@�@�@�k�g�n�i�f�B�N���A���[�̍���j�F
	//�@�@�@�@�@�@�@�_�~�[�̃A�i�[�i�g�b�o�Ŕ��f�j�̑����X�[�g
	//�@�@�@�@�q�g�n�i�f�B�N���A���[�̉E��j�F�@�@�@�@�@�@�@
	//�@�@�@�@�@�@�@�_�~�[�̃A�i�[�i�g�b�o�Ŕ��f�j�̏��Ȃ��X�[�g�@�@�@�@�@�@
	//
	//�@�X�[�g���ł̃J�[�h�̌��ߕ�
	//�@���̏��ʂŌ��߂�@�@�@�@�@�@�@�@
	//�@�@�i�P�j���̃X�[�g�̒��ŁA�g�b�v���E�B�i�[�Ȃ�L���b�V��
	//�@�@�i�Q�j�j�p����j
	//�@�@�i�R�j�p�i����p
	//�@�@�i�S�j�j�i�s�C�i�s����i
	//�@�@�i�T�j�j�s�X�C�p�s�X�A�s�X����s
	//�@�@�i�U�j���̑��F���݂Q���F��
	//�@�@�@�@�@�@�@�@�@���݂R���F�R����
	//�@�@�@�@�@�@�@�@�@���݂S���ȏと�S����
	private Card suitLead() {
		//
		// (0) �����ƃp�[�g�i�[�̃E�B�i�[�̐����R���g���N�g�𗎂Ƃ��̂ɏ\���ȂƂ��A
		//     �����̃E�B�i�[���L���b�V��
		Packet winners = getWinnersInSuitLead();
		
		// �f�B�N���A���[���̂Ƃ����g���b�N
		int win = board.getTricks() - BridgeUtils.countDeclarerSideWinners(board);
		if ( (winners.size() + win) > 7 - board.getContract().getLevel() ) {
			// �R���g���N�g�𗎂Ƃ���
			// ���̂Ƃ��A���[�h���ꂽ�񐔂̏��Ȃ��X�[�g�����i���������j
			for (int i = 0; i < winners.size(); i++) {
				if (hand.contains(winners.peek(i))) return winners.peek(i);
			}
		}
		
		//�@�i�P�j���t���X�̃X�[�g�����O�i���ꂵ���Ȃ���΂������Ȃ��j
		//�@�@�@�@���t���X�̃X�[�g�Ƃ́F
		//�@�@�@�@�_�~�[�ɂ��f�B�N���A���[�ɂ��g�����v���c���Ă���󋵂�
		//�@�@�@�@�@�@�@�@�i���݂O���Ɣ������Ă��Ȃ����Ɓj�@�@�@�@
		//�@�@�@�@�_�~�[���f�B�N���A���[�������݂O���Ɣ������Ă���X�[�g�@�@�@�@�@�@�@
		
		//
		// �i�P�j���ǂ̂悤�Ɏ������邩
		//
		//       ���ꂼ��̎����̒��ŁA���߂��X�[�g�����t���X�������ꍇ�A���_�̃X�[�g�ɕύX
		//       ���邱�ƂƂ���(���܂肫�ꂢ�ł͂Ȃ����A(2)�ȍ~�͂��ꂼ����I�Ȃ�肩����
		//       �̂ŁA���_���ŃX�[�g��o�^����������V���v���ɂȂ肻���Ȃ̂�)
		//
		
		int suit = chooseSuitInSuitLead();
		
		return choosePlayInSuitLead(suit);
	}
	
	/**
	 * �X�[�c�R���g���N�g�̏ꍇ�̃��[�h�X�[�g��I�т܂��B
	 */
	private int chooseSuitInSuitLead() {
		// (int[4][4][2])�ŁA[����][�X�[�g][�ő�(1) or �ŏ�(0)]
		int[][][] dist = ThinkingUtils.countDistribution(board, getMySeat());
		int trump = board.getContract().getSuit();
		
		//
		// (2) �p�[�g�i�[�̃g�����v�X�[�g�����݂O���Ɗm�肵�Ȃ��Ƃ��A����
		//�@�@�@�p�[�g�i�[�Ɍ��݂O���Ɗm�肵�Ă���T�C�h�X�[�g������Ƃ��A���̃X�[�g
		//�@�@�@�i���t������j
		//
		
		// �g�����v�X�[�g�����݂O���Ɗm�肵�Ȃ��Ƃ��A
		if (dist[ getPartnerSeat() ][ trump-1 ][ ThinkingUtils.MAX ] > 0) {
			int sideSuit;
			for (sideSuit = 1; sideSuit < 5; sideSuit++) {
				if (sideSuit == trump) continue;
				if (!hand.containsSuit(sideSuit)) continue;
				// (2) �ł̓��t���X�͒P���ɏ��O����
				if (isRuflis(dist, sideSuit)) continue;
				
				// �p�[�g�i�[�Ɍ��݂O���Ɗm�肵�Ă���T�C�h�X�[�g������Ƃ�
				if (dist[ getPartnerSeat() ][ sideSuit-1 ][ ThinkingUtils.MAX ] == 0) break;
			}
			if (sideSuit < 5) return sideSuit;
		}
		
		//
		// (3)�p�[�g�i�[�ɃE�B�i�[�̂���X�[�g
		//    �����(4)�Ɠ��`�Ȃ̂ŁA�������Ȃ�  �� �������Ă�������
		//    ����͓���̂Ŗ������B�����ɂk�g�n�̏ꍇ�͂n�k�Ɠ����X�[�g
		if ((board.getTricks() >= 1)&&(getDummyPosition() == LEFT)) {
			Card c = board.getAllTricks()[0].peek(0);
			int suit = c.getSuit();
			if ( (hand.containsSuit(suit))
				&& (!isRuflis(dist, suit)) ) return suit;
		}
		
		
		// (4) O.L. �Ɠ����X�[�g  ����߂�
		//     �������A���ꂪ���t���X�̏ꍇ�X�L�b�v���� 
		//
//		if (board.getTricks() >= 1) {
//			Card c = board.getAllTricks()[0].peek(0);
//			int suit = c.getSuit();
//			if (suit == trump) 
//			if ( (hand.containsSuit(suit))
//				&& (!isRuflis(dist, suit)) ) return suit;
//		}
		
		//
		//�@�i�T�j���܂܂Ńf�B�t�F���_�[���������Ƃ��Ƀ��[�h�����X�[�g�i�ŋ߂��珇�Ɂj����߂�
		//
		//        ���t���X�̏ꍇ�A�X�L�b�v����
		//
//		Trick[] trick = board.getAllTricks();
//		for (int i = board.getTricks()-2; i >= 0; i--) {
//			if (isItOurSide(trick[i].getWinner())) { // ���������̏���
//				int suit = trick[i+1].getLead().getSuit();
//				if (isRuflis(dist, suit)) continue;
//				if (!hand.containsSuit(suit)) continue;
//				return suit;
//			}
//		}
		
		//�@�i�U�j�ȏ�̃X�[�g���Ȃ��Ƃ�
		//�@�@�@�@�k�g�n�i�f�B�N���A���[�̍���j�F
		//�@�@�@�@�@�@�@�_�~�[�̃A�i�[�i�g�b�o�Ŕ��f�j�̑����X�[�g�@�@��߂�
		//�@�@�@�@�q�g�n�i�f�B�N���A���[�̉E��j�F�@�@�@�@�@�@�@
		//�@�@�@�@�@�@�@�_�~�[�̃A�i�[�i�g�b�o�Ŕ��f�j�̏��Ȃ��X�[�g�@��߂�
		//�@�@�@�@�������A���t���X�̃X�[�g�ƃg�����v�͏��O����@�@�@�@��߂Ȃ�
		//
		//�@�@�@�@���_�~�[�̃I���W�i���n���h��Ώ� �ɂ���悤�ɕύX���Ă�������
		//        ���k�g�n�́A�_�~�[�̃I���W�i���̃A�i�[�̖������P�����ŗD��A
		//         �Q�������D��A�����O���ƂȂ�B
		//        ���q�g�n�́A�A�i�[�̖������O�����ŗD��A�P�������D��A�ƂȂ�
		int[] dummyHonerPoint = BridgeUtils.countHonerPoint(dummyHand);
		Packet dummyOriginal = BridgeUtils.calculateOriginalHand(board)[board.getDummy()];
//System.out.println("Dummy Original = " + dummyOriginal);
		
		if (getDummyPosition() == LEFT) { // �����͂k�g�n
			int honers = -1;
			int honerSuit = -1;
			for (int i = 1; i < 5; i++) {
				if (!dummyHand.containsSuit(i)) { // �_�~�[�����t�ł���X�[�g�͏��O
					if (dummyHand.containsSuit(trump))
						continue;
				}
				if (!hand.containsSuit(i)) continue;	// �����ĂȂ��X�[�g�͏��O
				if (i == trump) continue;				// �g�����v�͏��O
				int h = BridgeUtils.countHoners(dummyOriginal, i);
				if (h > 2) continue;
				if (honers == -1) {
					honers = h;
					honerSuit = i;
					continue;
				}
				if (honers == 0) {
					if (h > 0) {
						honers = h;
						honerSuit = i;
					}
					continue;
				}
				if (honers == 2) {
					if (h == 1) {
						honers = h;
						honerSuit = i;
					}
					continue;
				}
				if (h != 1) { // h = 0 or 2
					honers = h;
					honerSuit = i;
				}
			}
			if (honers > -1) return honerSuit;
			
//			int maxHcpSuit = 0;
//			int maxHcpVal  = -1;
//			for (int i = 1; i < 5; i++) {
//				if (!dummyHand.containsSuit(i)) {
//					if (dummyHand.containsSuit(trump))
//						continue; // Dummy�Ƀ��t�����X�[�g�͏��O
//				}
//				if (!hand.containsSuit(i)) continue;	// �����������ĂȂ��X�[�g�͏��O
//				if (isRuflis(dist, i)) continue; // ���t���X�̃X�[�g�͏��O(�����ɂ͂��Ȃ�)
//				if (i == trump) continue;	// �g�����v�͏��O
//				if (dummyHonerPoint[i] >= maxHcpVal) { // ���� HCP �ł� Major ��D�悳����
//					maxHcpVal  = dummyHonerPoint[i];
//					maxHcpSuit = i;
//				}
//			}
//			if (maxHcpSuit > 0)	return maxHcpSuit;
			// ���t���X�̃X�[�g���������Ă��Ȃ��ꍇ�܂��̓g�����v�����Ȃ�
			// �����ɂ��āA���ɔ�����
			
		} else { // �����͂q�g�n
			int honers = -1;
			int honerSuit = -1;
			for (int i = 1; i < 5; i++) {
				if (!dummyHand.containsSuit(i)) { // �_�~�[�����t�ł���X�[�g�͏��O
					if (dummyHand.containsSuit(trump))
						continue;
				}
				if (!hand.containsSuit(i)) continue;	// �����ĂȂ��X�[�g�͏��O
				if (i == trump) continue;				// �g�����v�͏��O
				int h = BridgeUtils.countHoners(dummyOriginal, i);
				if (h > 1) continue;
				if (honers == -1) {
					honers = h;
					honerSuit = i;
					continue;
				}
				if (honers == 1) {
					if (h == 0) {
						honers = h;
						honerSuit = i;
						continue;
					}
				}
			}
			if (honers > -1) return honerSuit;
//			int minHcpSuit = 0;
//			int minHcpVal  = 100;
//			for (int i = 1; i < 5; i++) {
//				if (!dummyHand.containsSuit(i)) {
//					if (dummyHand.containsSuit(trump))
//						continue; // Dummy�Ƀ��t�����X�[�g�͏��O
//				}
//				if (!hand.containsSuit(i)) continue;	// �����������Ă��Ȃ��X�[�g�͏��O
//				if (isRuflis(dist, i)) continue; // ���t���X�̃X�[�g�͏��O(�����ɂ͂��Ȃ�)
//				if (i == trump) continue; // �g�����v�͏��O
//				if (dummyHonerPoint[i] <= minHcpVal) { // ���� HCP �ł� Major ��D�悳����
//					minHcpVal  = dummyHonerPoint[i];
//					minHcpSuit = i;
//				}
//			}
//			if (minHcpSuit > 0) return minHcpSuit;
			// ���t���X�̃X�[�g���������Ă��Ȃ��ꍇ�܂��̓g�����v�����Ȃ�
			//�����ɂ��āA���ɔ�����
		}
		//
		// (7)�����ɃE�B�i�[�̑����X�[�g �ǉ�(2002/09/21)
		//
		int maxWinner = -1;
		int maxWinnerSuit = -1;
		Packet winner = getWinners();
		
		for (int i = 1; i < 5; i++) {
			int winnerCount = winner.countSuit(i);
			if (winnerCount > maxWinner) {
				maxWinner		= winnerCount;
				maxWinnerSuit	= i;
			}
		}
		if (maxWinner > 0) return maxWinnerSuit;
		
		//
		// ���t���X�̃X�[�g���������Ă��Ȃ��ꍇ�܂��̓g�����v�����Ȃ�
		//
		Packet p = getMyHand();
		p.shuffle();
		int suit = p.peek(0).getSuit(); // �����Ă���C�ӂ̃J�[�h�̃X�[�g
		p.arrange();
		
		return suit;
	}
	
	//
	// (1) ���t���X�̃X�[�g�����O
	//�@�@�@�@���t���X�̃X�[�g�Ƃ́F
	//�@�@�@�@�_�~�[�ɂ��f�B�N���A���[�ɂ��g�����v���c���Ă���󋵂�
	//�@�@�@�@�@�@�@�@�i���݂O���Ɣ������Ă��Ȃ����Ɓj�@�@�@�@
	//�@�@�@�@�_�~�[���f�B�N���A���[�������݂O���Ɣ������Ă���X�[�g�@�@�@�@�@�@�@
	//
	private boolean isRuflis(int[][][] dist, int suit) {
		int declarer	= board.getDeclarer();
		int dummy		= board.getDummy();
		
		int trump = board.getContract().getSuit();
		// �f�B�N���A���[�Ƀg�����v���m���Ɏc���Ă��Ȃ��ꍇ�A���t���X�ł͂Ȃ�
		if (dist[ declarer ][ trump-1 ][ ThinkingUtils.MAX ] == 0) return false;
		
		// �_�~�[�Ɋm���Ƀg�����v���m���Ɏc���Ă��Ȃ��ꍇ�A���t���X�ł͂Ȃ�
		if (dist[ dummy    ][ trump-1 ][ ThinkingUtils.MAX ] == 0) return false;
		
		// �f�B�N���A���[�������Ă���\��������ꍇ�A���t���X�ł͂Ȃ�
		if (dist[ declarer ][ suit-1 ][ ThinkingUtils.MAX ] > 0) return false;
		
		// �_�~�[�������Ă���\��������ꍇ�A���t���X�ł͂Ȃ�
		if (dist[ dummy    ][ suit-1 ][ ThinkingUtils.MAX ] > 0) return false;
		
		return true;
	}
	
	/**
	 * �X�[�c�R���g���N�g�̏ꍇ�ŁA���[�h����X�[�g�����܂����ꍇ�̃v���C���s���܂��B
	 *�@�X�[�g���ł̃J�[�h�̌��ߕ������̏��ʂŌ��߂�@�@�@�@�@�@�@�@
	 *�@�@�i�P�j���̃X�[�g�̒��ŁA�g�b�v���E�B�i�[�Ȃ�L���b�V��
	 *�@�@�i�Q�j�j�p����j
	 *�@�@�i�R�j�p�i����p
	 *�@�@�i�S�j�j�i�s�C�i�s����i
	 *�@�@�i�T�j�j�s�X�C�p�s�X�A�s�X����s
	 *�@�@�i�U�j���̑��F�g�b�v�I�u�i�b�V���O
	 *                  ���݂Q���F��
	 *�@�@�@�@�@�@�@�@�@���݂R���F�R����
	 *�@�@�@�@�@�@�@�@�@���݂S���ȏと�S���ځ@
	 */
	public Card choosePlayInSuitLead(int suit) {
		Packet candidacy = hand.subpacket(suit);
		if (candidacy.size() == 0)
			throw new InternalError("choosePlayInSuitLead �Ŏw�肳�ꂽ�X�[�g("+suit+")�������Ă��܂���");
		candidacy.arrange();
		
		// �i�P�j���̃X�[�g�̒��ŁA�g�b�v���E�B�i�[�Ȃ�L���b�V��
		Packet winner = getWinners(); //getWinnersInSuitLead();
//System.out.println("choosePlayInSuitLead(suit). winner = " + winner);
		Card top = candidacy.peek(0);
		if (winner.contains(top.getSuit(), top.getValue())) return top;
		
		// �i�Q�j�j�p����j
		if (BridgeUtils.patternMatch(hand, "KQ*", suit)) {
			return hand.peek(suit, Card.KING);
		}
		
		// �i�R�j�p�i����p
		if (BridgeUtils.patternMatch(hand, "QJ*", suit)) {
			return hand.peek(suit, Card.QUEEN);
		}
		
		// �i�S�j�j�i�s�C�i�s����i
		if (BridgeUtils.patternMatch(hand, "KJT*", suit)) {
			return hand.peek(suit, Card.JACK);
		}
		if (BridgeUtils.patternMatch(hand, "JT*", suit)) {
			return hand.peek(suit, Card.JACK);
		}
		
		// �i�T�j�j�s�X�C�p�s�X�A�s�X����s
		if (BridgeUtils.patternMatch(hand, "KT9*", suit)) {
			return hand.peek(suit, 10);
		}
		if (BridgeUtils.patternMatch(hand, "QT9*", suit)) {
			return hand.peek(suit, 10);
		}
		if (BridgeUtils.patternMatch(hand, "T9*", suit)) {
			return hand.peek(suit, 10);
		}
		
		// �i�U�j���̑��F���݂Q���F��
		// �@�@�@�@�@�@�@���݂R���F�R����
		// �@�@�@�@�@�@�@���݂S���ȏと�S����
		// ���̃X�[�g�̑���ڂ̃��[�h�̂Ƃ���
		// �A�i�[�̂Ȃ��Ƃ��̓g�b�v�I�u�i�b�V���O
		// �g�b�v���A�i�[�̂Ƃ��͂��̂܂܂ł悢�B
		
		if (suitIsFirstTime(suit)) {
			if (bridgeValue(candidacy.peek(0)) < 10)
				return candidacy.peek(0);
		}
		
		switch (candidacy.size()) {
		case 1:
		case 2:
			return candidacy.peek(0);
		case 3:
			return candidacy.peek(2);
		default:
			return candidacy.peek(3);
		}
	}
	
	private boolean suitIsFirstTime(int suit) {
		for (int i = 0; i < board.getTricks(); i++) {
			Trick t = board.getAllTricks()[i];
			if (t.size() == 0) continue;
			if (t.peek(0).getSuit() == suit) return false;
		}
		return true;
	}
	
	//*********************************************************************//
	//  �Q�Ԏ�̃v���C(�X�[�g�t�H���[�ł���ꍇ)                           //
	//*********************************************************************//
	
	
	/**
	 * �Q�Ԏ�ł́A
	 * �E�E�B�i�[������Ώo���i��������Ή�����j
	 * �E�Ȃ���΃��[�G�X�g
	 */
	private Card playIn2nd() {
		int suit = lead.getSuit();
		if (getDummyPosition() == LEFT) {
			// LHO
			Packet follow = hand.subpacket(suit);
			follow.arrange();
			if (follow.size() == 0)
				throw new InternalError("playIn2nd() �ŁALHO �̓X�[�g�t�H���[�ł��Ȃ��Ȃ��Ă��܂�");
			int trump = board.getContract().getSuit();
			
			//
			// �a���A���S���Y��
			//
			Packet dummyFollow = dummyHand.subpacket(suit);
			if ( dummyFollow.size() == 0 ) {
				// �_�~�[���t�H���[�ł��Ȃ�
				if ( (trump != Bid.NO_TRUMP)&& // �X�[�c�R���g���N�g
						(dummyHand.subpacket(trump).size() > 0)&& // �_�~�[�Ƀg�����v������
						(bridgeValue(lead) <= 10))
					return follow.peek(); // ���[�G�X�g
				else
					return getCheepestWinner(follow, lead);
			}
			dummyFollow.arrange();
			if ( bridgeValue(dummyFollow.peek()) > bridgeValue(follow.peek(0)) )
				// �_�~�[�̃��[�G�X�g �� �����̃n�C�G�X�g --> ���[�G�X�g
				return follow.peek();
			else if (bridgeValue(lead) > bridgeValue(dummyFollow.peek(0)))
				// ���[�h �� �_�~�[�̃n�C�G�X�g --> ���[�h�Ƀ`�[�y�X�g�ɏ���
				return getCheepestWinner(follow, lead);
			else if (dummyFollow.size() == 1)
				// �_�~�[�̃J�[�h���P��
				return getCheepestWinner(follow, dummyFollow.peek());
			else if (bridgeValue(follow.peek(0)) > bridgeValue(dummyFollow.peek(0)))
				// �����̃n�C�G�X�g���_�~�[�̃n�C�G�X�g
				return getCheepestWinner(follow, dummyFollow.peek(0));
			else if ((bridgeValue(lead) > bridgeValue(dummyFollow.peek()))&&
						(bridgeValue(lead) >= 10))
				// ���[�h�����_�~�[�̃��[�G�X�g�j���i���[�h���P�O�ȏ�j
				return getCheepestWinner(follow, lead);
			else
				return follow.peek(); // ���[�G�X�g
		} else {
			// RHO
			Packet winner = getWinners();
			
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
	}
	
	//*********************************************************************//
	//  �R�Ԏ�̃v���C(�X�[�g�t�H���[�ł���ꍇ)                           //
	//*********************************************************************//
	
	/**
	 * �R�Ԏ�ł́A
	 * �E�q�g�n�̏ꍇ�A�n�C�G�X�g���o���i�������_�~�[�Ǝ��������킹���J�[�h��
	 *   �V�[�N�G���X�ƂȂ鎞�͂��̓��ōŉ��ʂ��o���j
	 */
	private Card playIn3rd() {
		if (getDummyPosition() == LEFT) {

			//
			// LHO
			//
			
			Packet follow = hand.subpacket(lead.getSuit());
			// follow �ł��Ȃ��ꍇ�͂��łɃ��t or �f�B�X�J�[�h���Ă���̂ŁA
			// ���� if �ł� 0 �ɂȂ邱�Ƃ͂Ȃ��B
			if (follow.size() <= 1) return follow.peek();
			follow.arrange();
			
			int trump = board.getContract().getSuit(); // NT(==5)�̂��Ƃ�����
			
			Card declarerPlay = board.getTrick().peek(1);
//System.out.println("declarerPlay = " + declarerPlay);
			if ( (lead.getSuit() != trump)&&(declarerPlay.getSuit() == trump) )
				// �f�B�N���A���[�����t����
				return getSignal();
			
			Packet dummyFollow = dummyHand.subpacket(lead.getSuit());
			dummyFollow.arrange();
			
			if ( compare(declarerPlay, lead) > 0 ) {
				// �f�B�N���A���[���v���C���āA���ꂪ�����Ă���
				if (dummyFollow.size() == 0) {
					// �_�~�[�̓t�H���[�ł��Ȃ�
					if ( compare(follow.peek(0), declarerPlay) > 0 )
						// �����̃n�C�G�X�g �� �f�B�N���A���[�̃v���C
						return getCheepestWinner(follow, declarerPlay);
					else
						return getSignal();
				} else {
					// �_�~�[�̓t�H���[�ł���
					if ( (compare(dummyFollow.peek(), follow.peek(0)) > 0)
							||(compare(declarerPlay, follow.peek(0)) > 0) ) {
						// �_�~�[�̃��[�G�X�g�������̃n�C�G�X�g
						//  or �f�B�N���A���[�v���C�������̃n�C�G�X�g
						return getSignal();
					} else if ( compare(follow.peek(0), dummyFollow.peek(0)) > 0){
						return getCheepestWinner(follow,
									getStronger(declarerPlay, dummyFollow.peek(0)) );
					} else {
						//�_�~�[�̃n�C�G�X�g�������̃n�C�G�X�g���_�~�[�̃��[�G�X�g
						//�������̃n�C�G�X�g���f�B�N���A���[
						//�_�~�[���P���̂Ƃ��͂��肦�Ȃ�
						//  �� getcheepestwinner�i�����̎�,�i�_�~�[��getcheepestwinner�i�_�~�[
						// �̎�A�����̃n�C�G�X�g�j�̎��ɒႢ�J�[�h�j�ƃf�B�N���A���[�̑傫�����j
//System.out.println("������������������ʂ̏ꍇ�ɂȂ�������������������");
//System.out.println("follow = " + follow);
//System.out.println("dummyFollow = " + dummyFollow);
//System.out.println("declarerPlay = " + declarerPlay);
//System.out.println("getCheepestWinner(dummyFollow, follow.peek(0)) = " + getCheepestWinner(dummyFollow, follow.peek(0)));
//System.out.println("getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))) = " + getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))));
//System.out.println("getStronger(getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))),declarerPlay) = "+getStronger(getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))),declarerPlay));
//System.out.println(getCheepestWinner(follow, getStronger(getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))), declarerPlay)));
						return 
						getCheepestWinner(follow, 
						getStronger(
							getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))),
							 declarerPlay
						)
						);
					}
				}
			} else {
				// ���[�h���f�B�N���A���[
				if (dummyFollow.size() == 0) {
					return getSignal();
				} else if (dummyFollow.size() == 1) {
					if ( compare(lead, dummyFollow.peek()) > 0) return getSignal();
					else return getCheepestWinner(follow, dummyFollow.peek());
				} else {
					//if   ���[�h���_�~�[�̃n�C�G�X�g OR
					// �_�~�[�̃n�C�G�X�g�����[�h�������̃n�C�G�X�g
					//         ��getsignal
					if (compare(lead, dummyFollow.peek(0)) > 0) return getSignal();
					if ((compare(dummyFollow.peek(0), lead) > 0)
							&&(compare(lead, follow.peek(0)) >0 )) return getSignal();
					
					//�_�~�[�̃n�C�G�X�g�����[�h�� �����̃n�C�G�X�g�����[�h
					if (compare(follow.peek(0), dummyFollow.peek(0)) > 0 )
						return getCheepestWinner(follow, dummyFollow.peek(0));
					//�_�~�[�̃n�C�G�X�g�������̃n�C�G�X�g
					if (compare(	getCheepestWinner(dummyFollow, lead),
									getCheepestWinner(dummyFollow, follow.peek(0) )) >= 0)
						return getSignal();
					
					return getCheepestWinner(follow, getNextLowerCard(dummyFollow, getCheepestWinner(dummyFollow, follow.peek(0))));
				}
			}
		} else {
			//
			// RHO(RHO�R�Ԏ�t�H���[�̐헪)
			//
			Packet pack = hand.subpacket(lead.getSuit());
			pack.arrange();
			
			// �P�������Ȃ��ꍇ�͂��̃J�[�h���o��
			if (pack.size() == 1) return pack.peek();
			
			// �����Ă���l�A�J�[�h��I��ł���
			Card wc = board.getTrick().getWinnerCard();
			int wcs = board.getTrick().getWinner();
			
			//
			Card max = lead;
			Card dummyPlay = board.getTrick().peek(1);
			if (compare(lead, dummyPlay) < 0) max = dummyPlay;
			
			Card highest = pack.peek(0);
			if (compare(highest, max) < 0) {
				// �����̃n�C�G�X�g��Max(���[�h�A�_�~�[�̃v���C�����J�[�h)
				return getSignal();
			} else {
				// �����̃n�C�G�X�g��Max(���[�h�A�_�~�[�̃v���C�����J�[�h)
				Packet o = new PacketImpl(board.getOpenCards());
				o.add(hand);	// o = �_�~�[�̃J�[�h�A�v���C���ꂽ�J�[�h�A�����̃n���h
				Card cardA = getBottomOfSequence(o, highest);
				
				if (compare(dummyPlay, lead) < 0) {
					// �_�~�[�̃v���C�����J�[�h �� ���[�h (�� �����̃n�C�G�X�g)
					if (compare(cardA, lead) <= 0) return getSignal();
					return getCheepestWinner(hand, cardA);	// 3rd hand high
				}
				if (compare(cardA, dummyPlay) <= 0)
					return getCheepestWinner(hand, dummyPlay);
				return getCheepestWinner(hand, cardA);
			}
		}
	}
	
	//*********************************************************************//
	//  �S�Ԏ�̃v���C(�X�[�g�t�H���[�ł���ꍇ)                           //
	//*********************************************************************//
	/**
	 * �S�Ԏ�ł́A��Ԉ����������[�G�X�g
	 */
	private Card playIn4th() {
		Packet pack = hand.subpacket(lead.getSuit());
		pack.arrange();
		Card play = null;
		
		for (int i = 0; i < pack.size(); i++) {
			Trick virtual = new TrickImpl(getTrick());
			virtual.add(pack.peek(i));
			if (isItOurSide(virtual.getWinner())) play = pack.peek(i);
		}
		if (play == null) play = pack.peek();
		return play;
	}
	
	/**
	 * �w�肵���V�[�g�ԍ������������T�C�h�̏ꍇ�Atrue
	 */
	private boolean isItOurSide(int seat) {
		return (((seat ^ getMySeat()) & 1) == 0);
	}
	
/*==========================================================
 *                  ��  ��  ��  ��  �Q
 *==========================================================
 */
	/**
	 * �����̃n���h�̒��ŃE�B�i�[�ƂȂ��Ă���J�[�h�𒊏o���� Packet ��ԋp���܂��B
	 * �E�B�i�[�ł��邱�Ƃ́A�e�X�[�g�ɂ����č��v���C����Ă��Ȃ��J�[�h�̂��������Ƃ�
	 * �����J�[�h�ł��邱�ƂŔ��f���܂��B
	 * �s�m��ȏ��͎g�p���܂���B
	 * 
	 * @return		winner
	 */
	private Packet getWinners() {
		boolean afterDummy = (getDummyPosition() == RIGHT);
		
		Packet result = new PacketImpl();
		
		//
		// ���ݎc���Ă���J�[�h(winner�̌��)�𒊏o����
		// winner �̌��́A���݃v���C����Ă��Ȃ��J�[�h�ƍ���ɏo�Ă���J�[�h�ł���
		// �������A���̃I�u�W�F�N�g���q�g�n�ł������_�~�[���v���C���A���̃I�u�W�F�N�g��
		// �v���C���Ă��Ȃ��Ƃ��Awinner �̌�₩��_�~�[�̃n���h�������B
		//
		Packet rest = board.getOpenCards().complement();
		rest.add(getTrick());
		
		if ( (afterDummy)&&(board.getTrick().size() > 0) ) {
			// RHO �ŁA���[�h�łȂ��ꍇ(�_�~�[�̃n���h������)
		} else {
			// LHO �ł��邩�ARHO �����������烊�[�h����ꍇ
			rest.add(getDummyHand());
		}
		
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
	
	/**
	 * ���t�ł���Ƃ��A�_�~�[�̂��Ƃ̂R�Ԏ�̂Ƃ��ɂ́A�p�[�g�i�[���E�B�i�[�������Ă�����
	 * ���t���Ȃ��A���������邽�߂̃E�B�i�[���o�֐��B
	 */
	private Packet getWinners2() {
		boolean afterDummy = (getDummyPosition() == RIGHT);
		Packet result = new PacketImpl();
		
		//
		// �c��̃J�[�h(Winner �̌��)�𒊏o����
		//
		Packet rest = board.getOpenCards().complement();
		rest.add(getTrick());
		
		if (!afterDummy) rest.add(getDummyHand());
		
		// �c��̃J�[�h���e�X�[�g�ɕ�����
		Packet[] suits = new Packet[4];
		
		for (int i = 0; i < 4; i++) {
			suits[i] = rest.subpacket(i+1);
			suits[i].arrange();	// �����J�[�h���������C���f�b�N�X��
		}
		
		Packet hand2 = new PacketImpl(hand);
		hand2.add(lead); // �p�[�g�i�[�̃��[�h��ǉ����Ă���
		
		// �e�X�[�c�̃E�B�i�[�𒊏o����
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < suits[i].size(); j++) {
				Card winner = suits[i].peek(j);
				if (hand2.contains(winner)) {
					result.add(winner); // �ō��ʂ������Ă���ꍇ�A���̈ʂ����ׂ�
				} else {
					break;	// �V�[�N�G���X���؂ꂽ�ꍇ�A�I��
				}
			}
		}
		
		result.arrange();
		return result;
	}
	
	/**
	 * �m�s�R���g���N�g�̏ꍇ�̎��������̎����Ă����ΓI�ȃE�B�i�[���l���܂��B
	 * �����̎�͂��ׂĕ]���ΏۂƂȂ�܂����A�p�[�g�i�[�̎�̓I�[�v�j���O���[�h��
	 * ���܂肩�琄�肳�����݂̂̂��ΏۂƂȂ�܂��B
	 */
	private Packet getWinnersInNTLead() {
		Packet result = new PacketImpl();
		
		// opened = ���łɌ����Ă���J�[�h
		//        = (�_�~�[�n���h) �� (����܂Ńv���C���ꂽ�g���b�N)
		// ���� rest �̂��߂Ɏ擾���Ă��܂��B
		Packet opened = board.getOpenCards();
		
		// rest = �܂��v���C����Ă��Ȃ��J�[�h(�_�~�[�n���h���܂�)
		Packet rest = opened.complement();
		rest.add(getDummyHand());
		
		// ours = {���������̃E�B�i�[�ɂȂ�\��������J�[�h}
		//      := (�����̃n���h) �� (O.L.������҂����p�[�g�i�[�̌��݂̃n���h)
		Packet ours = new PacketImpl();
		ours.add(hand); // �����̃n���h
		ours.add(getExpectedCardsInNT()); // �p�[�g�i�[�������Ă���Ɗ��҂����J�[�h
		
		// rest �̃J�[�h�S�̂ŁA�e�X�[�g�ɂ��ďォ�珇�� ours �ɓ����Ă�����̂�
		// NT �ɂ�����E�B�i�[�ƂȂ�܂��B
		for (int suit = 1; suit < 5; suit++) {
			Packet restOfSuit = rest.subpacket(suit);
			restOfSuit.arrange(); // �ォ�珇�Ԃ�
			for (int i = 0; i < restOfSuit.size(); i++) {
				Card c = restOfSuit.peek(i);
				if (ours.contains(c))
					result.add(c);
				else break; // ours �ɂ���V�[�N�F���X���r�؂ꂽ
			}
		}
		
		//
		// ����̉ۑ�Ƃ��āA
		// �m�s�R���g���N�g�ł̓��[�J�[�h�̃E�B�i�[���d�v�ŁA������J�E���g�������B
		//
		
		//
		// ��L�Ō����Ă��邱�Ƃ́A�f�B�N���A���[�ƃ_�~�[�ŃV���E�A�E�g�����X�[�g��
		// �E�B�i�[�Ƃ��ăJ�E���g�������A�p�[�g�i�[�̂S�����x�X�g���[�h�Ȃǂ̃V�O�i��
		// �ɂ���ĕ����閇�������g���ă����O�X�[�g�̃��[�J�[�h�̃E�B�i�[���J�E���g
		// �������A�Ƃ������e���H
		//
		
		return result;
	}
	
	private Packet getWinnersInSuitLead() {
		Packet result = new PacketImpl();
		
		// opened = ���łɌ����Ă���J�[�h
		//        = (�_�~�[�n���h) �� (����܂Ńv���C���ꂽ�g���b�N)
		// ���� rest �̂��߂Ɏ擾���Ă��܂��B
		Packet opened = board.getOpenCards();
//System.out.println("getWinnersInSuitLead . opened = " + opened);
		
		// rest = �܂��v���C����Ă��Ȃ��J�[�h(�_�~�[�n���h���܂�)
		Packet rest = opened.complement();
		rest.add(getDummyHand());
		
		// ours = {���������̃E�B�i�[�ɂȂ�\��������J�[�h}
		//      := (�����̃n���h) �� (O.L.������҂����p�[�g�i�[�̌��݂̃n���h)
		Packet ours = new PacketImpl();
		ours.add(hand); // �����̃n���h
		ours.add(getExpectedCardsInTrump()); // �p�[�g�i�[�������Ă���Ɗ��҂����J�[�h
		
		// sideSuits
		int[][][] sideSuits = ThinkingUtils.countDistribution(board, getMySeat());
		
		// rest �̃J�[�h�S�̂ŁA�e�X�[�g�ɂ��ďォ�珇�� ours �ɓ����Ă�����̂�
		// Suit Contract �ɂ�����E�B�i�[�ƂȂ�܂��B
		// �������ASuit Contract �ł́A�_�~�[�ƃf�B�N���A���[�ɂ��ăg�����v���c����
		// ����\���������Ԃł́A�T�C�h�X�[�g��(�ő�)���܂ł����E�B�i�[��F�߂܂���B
		
		// �g�����v���c���Ă��Ȃ��ꍇ�A�������͂���
		int trump = board.getTrump();
		// �_�~�[
		if (sideSuits[board.getDummy()][trump-1][ThinkingUtils.MAX] == 0) {
//System.out.println("getWinnersInSuitLead . dummy Trump is empty.");
			for (int suit = 1; suit < 5; suit++)
				if (suit != trump)
					sideSuits[board.getDummy()][suit-1][ThinkingUtils.MAX] = 13;
		}
		// �f�B�N���A���[
		if (sideSuits[board.getDeclarer()][trump-1][ThinkingUtils.MAX] == 0) {
			for (int suit = 1; suit < 5; suit++)
				if (suit != trump)
					sideSuits[board.getDeclarer()][suit-1][ThinkingUtils.MAX] = 13;
		}
//System.out.println("getWinnersInSuitLead . rest = " + rest);
//System.out.println("getWinnersInSuitLead . ours = " + ours);

		for (int suit = 1; suit < 5; suit++) {
			Packet restOfSuit = rest.subpacket(suit);
			restOfSuit.arrange(); // �ォ�珇�Ԃ�
			
			// �E�B�i�[�ƃJ�E���g�ł���ő吔���v�Z���܂�
			int maxWinnersOfSuit = restOfSuit.size();
			if (suit != trump) { // suit �̓T�C�h�X�[�g
				int tmp = sideSuits[board.getDummy()][suit-1][ThinkingUtils.MAX];
				if (maxWinnersOfSuit > tmp) maxWinnersOfSuit = tmp;
				tmp = sideSuits[board.getDeclarer()][suit-1][ThinkingUtils.MAX];
				if (maxWinnersOfSuit > tmp) maxWinnersOfSuit = tmp;
			}
//System.out.println("getWinnersInSuitLead.suit="+suit+"  maxWinnersOfSuit="+maxWinnersOfSuit);
			for (int i = 0; i < maxWinnersOfSuit; i++) {
				Card c = restOfSuit.peek(i);
				if (ours.contains(c))
					result.add(c);
				else break; // ours �ɂ���V�[�N�F���X���r�؂ꂽ
			}
		}
//System.out.println("getWinnersInsuitLead.result="+result);
		return result;
	}
	
	private static final String[][] NT_EXPECTED_PATTERN = {
		{ "T9*" }, // T lead
		{ "JT*" },	// J lead
		{ "QJ9*", "QJT*" },	// Q lead
		{ "KQT*", "KQJ*", "AKJT*", "AKQ*" }, // K lead
		{ "AKJTx*", "AKQxx*" } }; // A lead
	/**
	 * �m�s�R���g���N�g�̏ꍇ�̃I�[�v�j���O���[�h���琄�肳���p�[�g�i�[�̎�����o���܂��B
	 * �p�[�g�i�[���I�[�v�j���O���[�_�[�ł������ꍇ�ɁA���̃X�[�g�͈��̃��[��
	 * �ɂ���Ă���D�揇�ʂɊ�Â��ăn���h�p�^�[�������肳��܂��B
	 * ���[�h���ꂽ�J�[�h�̃o�����[�Ɛ��肳���n���h�p�^�[���͎��̒ʂ�ł��B
	 * �擪�̂��̂����D�揇�ʂ������ݒ肳��Ă��܂��B
	 *
	 *	{ "T9*" }, // T lead
	 *	{ "JT*" },	// J lead
	 *	{ "QJ9*", "QJT*" },	// Q lead
	 *	{ "KQT*", "KQJ*", "AKJT*", "AKQ*" }, // K lead
	 *	{ "AKJTx*", "AKQxx*" } }; // A lead
	 *
	 * @param		�p�[�g�i�[�������Ă���Ɛ��肳���J�[�h
	 */
	private Packet getExpectedCardsInNT() {
		return getExpectedCardsImpl(NT_EXPECTED_PATTERN);
	}
	
	/**
	 * �p�[�g�i�[�̍s�����I�[�v�j���O���[�h���琄�肳���
	 * ���݂̃p�[�g�i�[�n���h��ԋp���܂��B���݂́A�Ƃ̓I�[�v�j���O���[�h����
	 * ���肳���n���h�ł��łɃv���C���ꂽ���̂͏��O����A�Ƃ����Ӗ��ł��B
	 * �I���W�i���n���h�ōl���āA�����Ŏ������p�^�[��������ɂ��Ă͂܂����
	 * �����o����܂��B
	 */
	private Packet getExpectedCardsImpl(String[][] pattern) {
		Packet result = new PacketImpl();
		
		Trick opening = board.getPlayHistory().getTrick(0); // null �͂��肦�Ȃ�
		
		// �������I�[�v�j���O���[�_�[�̏ꍇ�A���͂Ȃ����߁A
		// ��� Packet ��ԋp����B
		if (opening.getLeader() == getMySeat()) return result;
		
		// �p�[�g�i�[���I�[�v�j���O���[�_�[�ł���A�����̔ԂɂȂ��Ă��邽�߁A
		// ���łɃI�[�v�j���O���[�h�͍s���Ă���͂�
		Card openingLead = opening.getLead();
		
		int value = openingLead.getValue();
		if ((value <= 9)&&(value >= 2)) return result; // ���[�J�[�h�̃��[�h�͉������҂ł��Ȃ�
		
		int suit = openingLead.getSuit();
		
		int index = value - 10; // T=0, J=1, Q=2, K=3, A=4
		if (index < 0) index = 4; // ACE �� value == 1 �ƂȂ��Ă��邽��
		String [] handPattern = pattern[index];
		
		// �D�揇�ʂ̍������̂��珇�ɐ���
		int handPatternIndex = 0;
		
		//
		// �p�[�g�i�[�ƃf�B�N���A���[�̎�� Union �����߂�B
		// ���̃A���S���Y���ł͂�����p�[�g�i�[�̎��������Ƃ݂Ȃ��B
		//
		// open = {��ɏo���J�[�h(�܃_�~�[)} �� (���݂̎����̃n���h)
		//  i.e. �������F���ł��Ă��邷�ׂẴJ�[�h
		Packet open = new PacketImpl(board.getOpenCards());
		open.add(getMyHand());
		
		// ����ɂ���܂Ńv���C�����p�[�g�i�[�̎�����킹�����̂� Union
		//
		// rest = ��open
		// i.e. �������猩�Ė��m�̂��ׂẴJ�[�h ( = �p�[�g�i�[ �� �f�B�N���A���[ )
		Packet rest = open.complement();
		
		// ����܂ł̃g���b�N�̒��ŁA�p�[�g�i�[���o�������̂��ׂĂ� rest �ɉ�����
		// i.e. rest �� �p�[�g�i�[�̏����n���h �� ���݂̃f�B�N���A���[�n���h
		//           �� �p�[�g�i�[�̏����n���h
		Trick[] trick = board.getAllTricks();
		for (int i = 0; i < board.getTricks(); i++) {
			for (int j = 0; j < trick[i].size(); j++) {
				int seat = (trick[i].getLeader() + j)%4;
				if (( (seat - getMySeat() + 6)%4 ) == 0) rest.add(trick[i].peek(j));
			}
		}
//System.out.println("expected card (NT/Suit) rest : " + rest);
		
		// �p�[�g�i�[�̏����n���h�Ƃ��Ă��肤����̂� handPattern ����T��
		for (handPatternIndex = 0; handPatternIndex < handPattern.length; handPatternIndex++) {
			if (BridgeUtils.patternMatch(rest, handPattern[handPatternIndex], suit)) break;
		}
		
		if (handPatternIndex == handPattern.length) return result; // �Y���Ȃ��B��p�P�b�g�ԋp
		
		// �Y������̂��߁A�p�^�[��������� result �ɉ�����(High Card �̂�)
		String toAdd = handPattern[handPatternIndex];
		for (int i = 0; i < toAdd.length(); i++) {
			char c = toAdd.charAt(i);
			
			// open �Ɋ܂܂�Ă�����̂� add ���Ȃ� (���łɃp�[�g�i�[���v���C��������)
			switch (c) {
			case 'A':
				if (!open.contains(suit, Card.ACE))
					result.add(rest.peek(suit, Card.ACE));
				break;
			case 'K':
				if (!open.contains(suit, Card.KING))
					result.add(rest.peek(suit, Card.KING));
				break;
			case 'Q':
				if (!open.contains(suit, Card.QUEEN))
					result.add(rest.peek(suit, Card.QUEEN));
				break;
			case 'J':
				if (!open.contains(suit, Card.JACK))
					result.add(rest.peek(suit, Card.JACK));
				break;
			case 'T':
				if (!open.contains(suit, 10))
					result.add(rest.peek(suit, 10));
				break;
			default:
			}
		}
		return result;
	}
	
	private static final String[][] SUIT_EXPECTED_PATTERN = {
		{ "T9*", "KT9*", "QT9*" }, // T lead
		{ "JT*", "KJT*" },	// J lead
		{ "QJ*" },	// Q lead
		{ "KQ*" }, // K lead
		{ "A*" } }; // A lead
	
	/**
	 * �X�[�c�R���g���N�g�̏ꍇ�̃I�[�v�j���O���[�h���琄�肳���p�[�g�i�[�̎�����o���܂��B
	 * �p�[�g�i�[���I�[�v�j���O���[�_�[�ł������ꍇ�ɁA���̃X�[�g�͈��̃��[��
	 * �ɂ���Ă���D�揇�ʂɊ�Â��ăn���h�p�^�[�������肳��܂��B
	 * ���[�h���ꂽ�J�[�h�̃o�����[�Ɛ��肳���n���h�p�^�[���͎��̒ʂ�ł��B
	 * �擪�̂��̂����D�揇�ʂ������ݒ肳��Ă��܂��B
	 *
		{ "T9*", "KT9*", "QT9" }, // T lead
		{ "JT*", "KJT*" },	// J lead
		{ "QJ*" },	// Q lead
		{ "KQ*" }, // K lead
		{ "A*" } }; // A lead
	 *
	 * @param		�p�[�g�i�[�������Ă���Ɛ��肳���J�[�h
	 */
	private Packet getExpectedCardsInTrump() {
		return getExpectedCardsImpl(SUIT_EXPECTED_PATTERN);
	}
	
	/**
	 * �w�肳�ꂽ���J�[�h�̏W�܂�̒�����A�w�肳�ꂽ�J�[�h�ɏ��Ă�l���`�[�y�X�g��
	 * �J�[�h���擾����B�ǂ����Ă����ĂȂ��ꍇ�A���[�G�X�g��Ԃ��B
	 * �X�[�g�t�H���[�ɂ��Ă͍l�����Ă��炸�A�l�ɂ��]���������Ă��Ȃ��B
	 */
	private Card getCheepestWinner(Packet candidacy, Card target) {
		Packet p = candidacy.subpacket(target.getSuit());
		if (target == null) return p.peek();
		p.arrange();
		if (p.contains(target)) return target;
		
		Card stronger = null;
		for (int i = 0; i < p.size(); i++) {
			Card c = p.peek(i);
			if (bridgeValue(c) > bridgeValue(target)) stronger = c;
		}
		if (stronger == null) return p.peek(); // ���[�G�X�g
		return stronger;
	}
	
	/**
	 * �w�肳�ꂽ Packet �̒��́A�w�肳�ꂽ�J�[�h�Ɠ����̍Œ�̃J�[�h���o��
	 */
	private Card getBottomOfSequence(Packet candidacy, Card base) {
		Packet p = candidacy.subpacket(base.getSuit());
		p.arrange();
		Card c = base;
		for (int i = 1; i < p.size(); i++) {
			Card c2 = p.peek(i);
			if (bridgeValue(c) - bridgeValue(c2) == 1)	c = c2;
		}
		return c;
	}
	
	private Card getBottomOfSequence(Packet candidacy) {
		if (candidacy.size() == 0) return null;
		Packet p = new PacketImpl(candidacy);
		p.arrange();
		Card c = p.peek(0);
		for (int i = 1; i < p.size(); i++) {
			Card c2 = p.peek(i);
			if (bridgeValue(c) - bridgeValue(c2) == 1)	c = c2;
		}
		return c;	
	}
	
	private Card getBottomOfSequence(Packet candidacy, int suit) {
		return getBottomOfSequence(candidacy.subpacket(suit));
	}
	
	private Card getNextLowerCard(Card c) {
		Packet p = hand.subpacket(c.getSuit());
		p.arrange();
		int index = p.indexOf(c);
		if ((!(index == -1))||(index == p.size()-1))
			throw new IllegalStateException("�n���h�� " + c + "���܂܂�Ă��܂���");
		return p.peek(index+1);
	}
	
	private Card getNextLowerCard(Packet h, Card c) {
		Packet p = h.subpacket(c.getSuit());
		p.arrange();
//System.out.println("getNext..p = " + p);
//System.out.println("c = " + c);
		int index = p.indexOf(c);
		if (index == -1)
			throw new IllegalStateException("�Ώ� Packet �� " + c + "���܂܂�Ă��܂���");
		if (index == p.size()-1) return null;
		return p.peek(index+1);
	}
	
	/**
	 * �ǂ̈ʒu�ł��Ă΂��
	 */
	private Card getSignal() {
		// �����̃n���h����A���[�h�Ɠ����X�[�g�̃J�[�h�𒊏o����
		Packet follow = hand.subpacket(lead.getSuit());
		
		if (follow.size() == 0) return null;
		if (follow.size() == 1) return follow.peek();
		
		follow.arrange();
		Card card1 = board.getTrick().peek(0); // == lead
		Card card2 = board.getTrick().peek(1);
		int trump = board.getContract().getSuit();
		
//�I�[�o�[�e�C�N�̏ꍇ��I�o		
		if ((card1.getSuit() == trump)||(card2.getSuit() != trump)) {
			// �f�B�N���A���[�̓��t���Ă��Ȃ�
			if ((compare(follow.peek(0), card1) > 0)&&(compare(card1, card2) > 0)) {
				// �t�H���[�̃n�C�G�X�g��card1��card2
				Packet p = board.getOpenCards().complement();
				p.add(dummyHand);
				p = p.complement().subpacket(lead.getSuit()); // p = ���܂Ńv���C���ꂽ�J�[�h
				// ���̂Q�s�͂��łɃv���C����Ă���̂ŁAp�Ɋ܂܂�Ă���͂�
				//p.add(card1);
				//if (card2.getSuit() == card1.getSuit()) p.add(card2);
				
				Card high = follow.peek(0);
				Card low;
				if (follow.size() == 2) low = follow.peek(1);
				else low = follow.peek(2);
				
				int i;
				for (i = bridgeValue(low); i <= bridgeValue(high); i++) {
					int j = i;
					if (j == 14) j = 1;
					if (!p.containsValue(j)) break;
				}
				if (i > bridgeValue(high)) {
					Card c = follow.peek(0);
					for (int j = 1; j < follow.size(); j++) {
						Card c2 = follow.peek(j);
						if (bridgeValue(c) - bridgeValue(c2) == 1)	c = c2;
					}
					//�I�[�o�[�e�C�N���Ă����̂Ȃ��󋵂ł̓I�[�o�[�e�C�N����
					return c;
				}
			}
		}
		return follow.peek(); // ���[�G�X�g 
/*
//�E�J�����V�O�i���̏ꍇ
if  ���܂܂ł��̃X�[�g�����[�h���ꂽ���Ƃ͂Ȃ�//���߂Ă��̃X�[�g�����[�h����
��
    ���i�ipacket�ɂ`�܂��͂j���܂ށj  �n�q  �icard1=K & packet�ɂp���܂ށj�j
        if packet�ɂX�ȉ��̃J�[�h������
             ���X�ȉ��̃J�[�h�̒��ň�ԍ�������
         else //10�ȏサ�������Ă��Ȃ�
             �����[�G�X�g��Ԃ�

//�E�A���u���b�N�i�`�V����͂`�������ȂǁjLHO�͂��Ȃ��i�����q�g�n�ɂ͂�����
���j
//�E�J�E���g�V�O�i�� �i����Ȃ烍�[�A�������Ȃ�n�C�j���͂��Ȃ�
//�E�X�[�c�v���t�@�����X�V�O�i���i���[�h�̂Ƃ��ɏo�����������̂ŁA�Ȃ��j
else
��  ���[�G�X�g��Ԃ�
*/
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
	
	/**
	 * lead �ɑ΂���X�[�g�t�H���[�A�g�����v�X�[�g���l�����ĂQ���̃J�[�h�̋������r���܂�
	 * �������A�Q���Ƃ������X�[�g�̃f�B�X�J�[�h�̏ꍇ�A�l���傫�����������Ƃ݂Ȃ��A
	 * �Ⴄ�X�[�g�̃f�B�X�J�[�h�̏ꍇ�� 0 ��ԋp���Ă��܂�
	 *
	 * @param		a		��r�Ώۂ̃J�[�h
	 * @param		b		��r�Ώۂ̃J�[�h
	 * @return		����( (1) a > b  (-1) a < b  (0) a = b )
	 */
	private int compare(Card a, Card b) {
		if ((a == null)&&(b == null)) return 0;
		if (b == null) return 1;
		if (a == null) return -1;
		
		// �����X�[�g�̏ꍇ
		// (�Q���Ƃ������X�[�g�̃f�B�X�J�[�h�̏ꍇ�A�l�̑傫�������������ƂȂ��Ă���j
		if (a.getSuit() == b.getSuit()) {
			int av = bridgeValue(a);
			int bv = bridgeValue(b);
			if (av > bv) return 1;
			if (av == bv) return 0;
			return -1;
		}
		int trump = board.getContract().getSuit();
		
		// ���t�̏ꍇ
		if (a.getSuit() == trump) return 1;
		if (b.getSuit() == trump) return -1;
		
		// �X�[�g�t�H���[������
		if (a.getSuit() == lead.getSuit()) return 1;
		if (b.getSuit() == lead.getSuit()) return -1;
		
		//
		return 0;
	}
	
	/**
	 * �w�肳�ꂽ�Q�J�[�h�̂����A��������ԋp���܂��B
	 * �����̔���ɂ� compare(a,b) ���g�p���܂��B
	 *
	 * @param		a		���`
	 * @param		b		���a
	 * @return		�Q���̂����A�����J�[�h
	 */
	private Card getStronger(Card a, Card b) {
		if (compare(a, b) > 0) return a;
		else return b;
	}
}
