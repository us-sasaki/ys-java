package ys.game.card.bridge.gui;

import ys.game.card.bridge.*;
import ys.game.card.*;

/**
 * �u���b�W�V�~�����[�^�̃n���h�A�R���g���N�g���������肵�A
 * ���Ƃ���N���X�B�K�� South ���f�B�N���A���[�ɂȂ�܂��B
 * RandomProblem�ł́A�_�u���h�R���g���N�g���N����₷���A�����₷�����߁A
 * ���ۂ̃R���g���N�g�ɂ��߂����̂ɕύX���Ă��܂��B
 *
 * @version		making		24, January 2004
 * @author		Yusuke Sasaki
 */
public class RandomProblem3 implements Problem {
	/** ���̃^�C�g�� */
	protected String	title;
	
	// �ȉ��̂R�ϐ���NS/EW�̏ꍇ�̑z��R���g���N�g�����肷��ۂ�
	// ���[�N�Ƃ��Ă����p����邪�AmakeProblem() �̌��ʁA�ŏI�R���g���N�g��
	// �����l�ɐݒ肳���B
	protected int		kind;
	protected int		level;
	protected int		denomination;
	
	/** NSEW �̃n���h */
	protected String	description;
	
	protected Packet[]	hand;
	
	// �����v�Z�p
	/** �e�n���h�̃X�[�g���Ƃ̖��� */
	private int[][]		count; // [seat][suit-1]
	
	/** �e�n���h�� High Card Point */
	private int[]		hcp;
	
	/** �e�n���h�� point */
	private int[]		pts;
	
	/** NS/EW ���ꂼ��Ō����Ƃ��̃r�b�h */
	private Bid[]		bid;	// NS(0) / EW(1)
	
	/** NS/EW ���ꂼ��Ō����Ƃ��̃f�B�N���A���[ */
	private int[]		declarer;	// NS(0) / EW(1)
	
/*-------------
 * Constructor
 */
	public RandomProblem3() {
		count	= new int[4][4];
		hcp		= new int[4];
		pts		= new int[4];
		bid		= new Bid[2];
		declarer= new int[2];
	}
	
/*------------------
 * instance methods
 */
	public void start() {
		makeProblem();
	}
	
	/**
	 * �X�[�g���Ƃ̖����AHCP���v�Z���܂�
	 * hand �ϐ��̒l���g�p���܂��B
	 */
	private void calculateAttributes() {
		// �X�[�g���Ƃ̖������J�E���g����
		// �e�X�[�g�ɂ��āA
		for (int suit = 1; suit < 5; suit++) {
			// ���ꂼ��̖����𐔂���
			for (int i = 0; i < 4; i++) {
				count[i][suit-1] = hand[i].countSuit(suit);
			}
		}
		
		// HCP���v�Z����
		for (int i = 0; i < 4; i++) {
			hcp[i] = BridgeUtils.countHonerPoint(hand[i])[0];
		}
	}
	
	/**
	 * �r�b�h�A�f�B�N���A���[��NS, EW�̎w�肳�ꂽ�����������Ɖ��肵�Č��肵�܂�
	 * �r�b�h�͉\�ȍō����x���̂��̂�ݒ肵�܂�
	 */
	private void assuming(int NSorEW) {
		// �t�B�b�g�̑����X�[�g��T��
		
		// �����Ƃ��t�B�b�g�������Ă��鑤��T���A���̖������o���Ă���
		// �����t�B�b�g�������Ă���ꍇ�A���x���̍����X�[�g��D�悷��
		int maxFit	= 0;
		int maxSuit = 0;
		int declarer = -1;
		for (int suit = 1; suit < 5; suit++) {
			int ns = count[NSorEW][suit-1] + count[NSorEW+2][suit-1];
			if ( (ns > maxFit)||( (ns == maxFit)&&(suit > maxSuit) ) ) {
				maxFit		= ns;
				maxSuit		= suit;
				declarer	= NSorEW; // �Ƃ肠���� North or East �Ƃ��Ă���
			}
		}
System.out.println("NSorEW:"+NSorEW+"  maxFit:"+maxFit+"  maxSuit:"+maxSuit);
		
		//
		// �R���g���N�g�� denomination �����߂�
		//
		if (maxFit == 7) denomination = Bid.NO_TRUMP;
		else denomination = maxSuit;
		
		//
		// 4. declarer �����߂�
		//
		if (denomination == Bid.NO_TRUMP) {
			// NT �R���g���N�g�̎�
			// Honer Point �̍��������f�B�N���A���[�Ƃ���
			if (hcp[declarer] < hcp[declarer+2]) declarer = declarer + 2;
		} else {
			// �X�[�g�R���g���N�g�̎�
			int decCount = count[declarer  ][denomination-1];
			int dumCount = count[declarer+2][denomination-1];
			if ( decCount < dumCount ) {
				// �p�[�g�i�[(South / West) �� declarer
				declarer = declarer + 2;
			} else if (decCount == dumCount) {
				// �����������ꍇ�� Honer Point �̍�����
				if (hcp[declarer] < hcp[declarer+2]) declarer = declarer + 2;
			}
		}
		
		//
		// �����O�X�|�C���g�Ȃǌv�Z����
		//
		
		// �����O�X�|�C���g�����Z����
		// �����O�X�|�C���g�́ANT/Suit�R���g���N�g�� (�X�[�g�̖���)-4 �����̂Ƃ�
		// ���̐��l�����Z���܂�(5���X�[�g..1pts  6���X�[�g..2pts �c�c)
		for (int i = NSorEW; i < 4; i+=2) {
			pts[i] = hcp[i];
			for (int suit = 1; suit < 5; suit++) {
				int cnt = count[i][suit-1];
				if (cnt > 4) pts[i] += (cnt - 4);
			}
		}
		
		int[] ptsBkup = new int[2];
		if (denomination != Bid.NO_TRUMP) {
			// �f�B�X�g���r���[�V�����|�C���g
			// �X�[�g�R���g���N�g�̏ꍇ�ɁA�ȉ��̓_�����Z����
			// �{�C�h..3pts  �V���O���g��..2pts  �_�u���g��..1pts
			// �܂��A���ɁA4-3-3-3 �u���C�N�̂Ƃ��P�_����
			ptsBkup[0] = pts[0];
			ptsBkup[1] = pts[1]; // minor�t�B�b�g��NT�ɖ߂��Ƃ���pts�����̒l�ɂ���
			
			for (int i = NSorEW; i < 4; i+=2) {
				// �g�����v�������ĂȂ��Ƃ��͉��Z���Ȃ�
				if (count[i][denomination-1] == 0) continue;
				
				boolean f4333 = true;
				for (int suit = 1; suit < 5; suit++) {
					int cnt = count[i][suit-1];
					if ((cnt != 3)&&(cnt != 4)) f4333 = false;
					switch (cnt) {
					
					case 0: // void
						pts[i] += 0; //3;
						break;
					case 1: // singleton
						pts[i] += 0; //2;
						break;
					case 2: // doubleton
						pts[i] += 0; //1;
					default:	// fall through
					}
				}
				// 4-3-3-3 �̂Ƃ��͂P�_�Ђ�
				if (f4333) pts[i]--;
			}
			
			// �_�~�[�|�C���g
			// �X�[�g�R���g���N�g�̃_�~�[�ŁA�g�����v�T�|�[�g���S���ȏ�̂Ƃ��A
			// �ȉ��̕␳��������
			// �{�C�h..+2pts  �V���O���g��..+1pts
			int dummy = (declarer + 2) % 4;
			if (count[dummy][denomination-1] >= 4) {
				for (int suit = 1; suit < 5; suit++) {
					int cnt = count[dummy][suit-1];
					switch (cnt) {
					
					case 0:	// void
						pts[dummy] += 5;
						break;
					case 1: // singleton
						pts[dummy] += 3;
						break;
					default:	// fall through
					}
				}
			}
		}
		//
		// 5. �R���g���N�g�� level �����߂�
		//
		level = maxFit - 6; // �Ƃ肠����
		if (denomination == Bid.NO_TRUMP) level = 1;
		
		int totalPt = pts[NSorEW] + pts[NSorEW+2];
		
		if (totalPt > 36) {
			// �O�����h�X����
			if (has1stControl(NSorEW)) {
				level = 7;
			} else {
				level = 6;
			}
		}
		if ( (totalPt > 32)&&(totalPt < 37) ) {
			// �X���[���X����
			level = 6;
		}
		if ( (totalPt > 29)&&(totalPt < 33) ) {
			if ( (denomination != Bid.NO_TRUMP)&&(level < 5) )
				level = 5;
			else if ( (denomination == Bid.NO_TRUMP) )
				 level = 5; // NT �͖������ɂT�Ƃ���
		}
		if ( (totalPt > 24)&&(totalPt < 33) ) {
			// �Q�[��
			if (denomination == Bid.NO_TRUMP) {
				if (level < 5) level = 3; // �o�����X�n���h�̏ꍇ�Ȃ̂ŁA�X�g�b�p�[�͖���
			} else if ( (denomination == Bid.HEART)||(denomination == Bid.SPADE) ) {
				if (level < 4) level = 4;
			} else if ( (denomination == Bid.CLUB)||(denomination == Bid.DIAMOND) ) {
				// �X�g�b�p�[�����邩�ǂ���
				if (hasStopper(NSorEW)) {
					denomination = Bid.NO_TRUMP; // �}�C�i�[�A���o�����X�n���h�ɂ��NT�ɕύX
					level = 3;
					// pts��NT�p�ɒ���
					pts[0] = ptsBkup[0];
					pts[1] = ptsBkup[1];
					
				} else {
					if (level < 4) level = 4; // 4 level minor
				}
			}
		} else if ( (totalPt > 21)&&(totalPt < 25) ) {
			if (denomination == Bid.NO_TRUMP) level = 2; // 2NT
		}
		
		// ���ʂ̐ݒ�
		bid[NSorEW] = new Bid(Bid.BID, level, denomination);
		this.declarer[NSorEW] = declarer;
System.out.println("HCP[NSorEW] : " + hcp[NSorEW] + "  HCP[another] : " + hcp[NSorEW+2]);
System.out.println("pts[NSorEW] : " + pts[NSorEW] + "  pts[another] : " + pts[NSorEW+2]);
System.out.println("BID : " + bid[NSorEW] + "  declarer : " + declarer);
		
	}
	
	/**
	 * �O�����h�X�����̏ꍇ�Ɏg�p����A�S�X�[�g�ɃR���g���[�������邩��
	 * �`�F�b�N����֐��ł�
	 */
	private boolean has1stControl(int NSorEW) {
		int pard = NSorEW + 2;
		for (int suit = 1; suit < 5; suit++) {
			// A�������Ă��邩�A�{�C�h�ł����OK
			if ( (!BridgeUtils.patternMatch(hand[NSorEW], "A*", suit))
				&& (!BridgeUtils.patternMatch(hand[pard], "A*", suit))
				&& (count[NSorEW][suit-1] > 0)
				&& (count[pard]  [suit-1] > 0) )
				return false;
		}
		return true;
	}
	
	/**
	 * �m�[�g�����v�R���g���N�g�̏ꍇ�Ɏg�p����A�S�X�[�g�ɃX�g�b�p�[��
	 * ���邩���`�F�b�N����֐��ł�
	 */
	private boolean hasStopper(int NSorEW) {
		int pard = NSorEW + 2;
		for (int suit = 1; suit < 5; suit++) {
			if ( (!BridgeUtils.patternMatch(hand[NSorEW], "A?*", suit)) // A?*�̓X�g�b�p�[
				&& (!BridgeUtils.patternMatch(hand[pard], "A?*", suit))
				&& (!BridgeUtils.patternMatch(hand[NSorEW], "K?*", suit)) // K?*�̓X�g�b�p�[
				&& (!BridgeUtils.patternMatch(hand[pard],   "K?*", suit))
				&& (!BridgeUtils.patternMatch(hand[NSorEW], "Q??*", suit)) // Q??*�̓X�g�b�p�[
				&& (!BridgeUtils.patternMatch(hand[pard],   "Q??*", suit))
				&& (!BridgeUtils.patternMatch(hand[NSorEW], "J???*", suit)) // J???*�̓X�g�b�p�[
				&& (!BridgeUtils.patternMatch(hand[pard],   "J???*", suit))
				&& (!BridgeUtils.patternMatch(hand[NSorEW], "????*", suit)) // ????*�̓X�g�b�p�[
				&& (!BridgeUtils.patternMatch(hand[pard],   "????*", suit)) )
					return false;
		}
		return true;
	}
	
	/**
	 * �����_���Ƀn���h��z��A���̓��e����R���g���N�g�Ȃǂ����肵�܂��B
	 */
	private void makeProblem() {
		//
		// 1. �܂��A�����_���Ƀn���h��z��
		//
		Packet pile = PacketFactory.provideDeck(PacketFactory.WITHOUT_JOKER);
		pile.shuffle();
		hand = PacketFactory.deal(pile, 4);
		
		for (int i = 0; i < 4; i++) hand[i].arrange();
		
		//
		// 2. �X�[�g���Ƃ̖����AHCP����b���Ƃ��Čv�Z����
		//
		calculateAttributes();
		
		//
		// 3. NS/EW���ꂼ��Ŕ������Ƃ��čō��r�b�h�����߂�
		//
		assuming(Board.NORTH);
		assuming(Board.EAST);
		
		//
		// 4. ���荇���󋵂��`�F�b�N���A���[�Y�i�u���ȃR���g���N�g�܂ŗ��Ƃ�
		//
		int declarer = -1;
		int NSorEW   = -1;
		int another  = -1;
		if (bid[Board.NORTH].isBiddableOver(bid[Board.EAST])) {
			declarer	= this.declarer[Board.NORTH];
			NSorEW		= Board.NORTH;
			another		= Board.EAST;
		} else {
			declarer	= this.declarer[Board.EAST];
			NSorEW		= Board.EAST;
			another		= Board.NORTH;
		}
		
		denomination	= bid[NSorEW].getSuit();
		level			= bid[NSorEW].getLevel();
		kind			= bid[NSorEW].getKind();
		
		switch (denomination) {
		
		case Bid.NO_TRUMP:
			// NT�̏ꍇ�́A4NT or 5NT �̏ꍇ��3NT�܂ŗ��Ƃ���Η��Ƃ�
			if ( (level > 3)&&(level < 6) ) {
				if (bid[another].getLevel() < 4) {
					level = 3;
				} else {
					level = bid[another].getLevel();
				}
			}
			break;
		
		case Bid.SPADE:
		case Bid.HEART:
			// Major�̏ꍇ�́A5M�̏ꍇ��4M�ɁA3M�̏ꍇ��2�ȏ�̗��Ƃ���Ƃ���܂ŗ��Ƃ�
			switch (level) {
			
			case 5:
				if (bid[another].getSuit() > denomination) { // ���� S ������ H �̂Ƃ�
					if (bid[another].getLevel() < 4) { // 3S �܂łȂ�4H�ɂł���
						level = 4; // 4H
					}
				} else {
					if (bid[another].getLevel() < 5) { // 4x �܂łȂ�4M�ɂł���
						level = 4;
					}
				}
				break;
			
			case 3:
//			case 2:
				if (bid[another].getSuit() > denomination) {
					level = bid[another].getLevel() + 1;
				} else {
					level = bid[another].getLevel();
					if (level == 1) level++;
				}
				break;
			default:	// fall through
			}
			break;
		
		case Bid.DIAMOND:
		case Bid.CLUB:
			// Minor�̏ꍇ�́A4m, 3m �̏ꍇ��2�ȏ�̗��Ƃ���Ƃ���܂ŗ��Ƃ�
			switch (level) {
			case 4:
			case 3:
				if (bid[another].getSuit() > denomination) {
					level = bid[another].getLevel() + 1;
				} else {
					level = bid[another].getLevel();
					if (level == 1) level++;
				}
				break;
			default:	// fall through
			}
			break;
		
		default: // fall through
		}
		
		//
		// 6. �_�u���󋵂�����
		//
		int totalPt = pts[NSorEW] + pts[NSorEW+2];
		kind = Bid.BID;
		if (totalPt < 13) {
			kind = Bid.DOUBLE;
		} else if (totalPt < 17) {
			if (level > 1) kind = Bid.DOUBLE;
		} else if (totalPt < 21) {
			if (level > 2) kind = Bid.DOUBLE;
		} else if (totalPt < 24) {
			if (level > 3) kind = Bid.DOUBLE;
		} else if (totalPt < 27) {
			if (level > 4) kind = Bid.DOUBLE;
		} else if (totalPt < 34) {
			if (level > 5) kind = Bid.DOUBLE;
		} else if (totalPt < 37) {
			if (level > 6) kind = Bid.DOUBLE;
		}
		
		//
		// 7. �f�B�N���A���[�� SOUTH �ɂȂ�悤�Ƀn���h����]������
		//
		for (int i = 0; i < ((declarer - Board.SOUTH) + 4) % 4; i++) {
			Packet tmp = hand[0];
			for (int j = 0; j < 3; j++) {
				hand[j] = hand[j+1];
			}
			hand[3] = tmp;
		}
		
		//
		// 8. ������������
		//
		description = "���Ȃ��� " + getContractString() + " ��B\n�؂�D��";
		switch (denomination) {
		case Bid.NO_TRUMP:	description += "����܂���B";	break;
		case Bid.SPADE:		description += "�X�y�[�h�A";	break;
		case Bid.HEART:		description += "�n�[�g�A";		break;
		case Bid.DIAMOND:	description += "�_�C�A�����h�A";break;
		case Bid.CLUB:		description += "�N���u�A";		break;
		default:			description += "�Ȃ�ł��傤�B";break;
		}
		
		description += "\n13�g���b�N�̂����A" + (level + 6) + "�g���b�N�ȏ�Ƃ��Ă�";
	}
	
	public String getTitle() {
		return "���K���[�h";
	}
	
	public Bid getContract() {
		return new Bid(kind, level, denomination);
	}
	
	/**
	 * �n���h��Ԃ��܂�
	 */
	public Packet[] getHand() {
		return hand;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getContractString() {
		String result = String.valueOf(level);
		
		switch (denomination) {
		
		case Bid.NO_TRUMP:
			result += "NT";
			break;
		case Bid.SPADE:
			result += "S";
			break;
		case Bid.HEART:
			result += "H";
			break;
		case Bid.DIAMOND:
			result += "D";
			break;
		case Bid.CLUB:
			result += "C";
		}
		
		if (kind == Bid.DOUBLE) result += "�_�u��";
		if (kind == Bid.REDOUBLE) result += "���_�u��";
		
		return result;
	}
	
	public String getOpeningLead() {
		return null;
	}
	
	public String getThinker() {
		return null;
	}
	
	public boolean isValid() {
		return true; // �������� valid
	}
}
