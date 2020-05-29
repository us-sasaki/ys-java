package ys.game.card.bridge.gui;

import ys.game.card.bridge.*;
import ys.game.card.*;

/**
 * �u���b�W�V�~�����[�^�̃n���h�A�R���g���N�g���������肵�A
 * ���Ƃ���N���X�B�K�� South ���f�B�N���A���[�ɂȂ�܂��B
 *
 * @version		making		22, July 2001
 * @author		Yusuke Sasaki
 */
public class RandomProblem implements Problem {
	protected String	title;
	protected int		kind;
	protected int		level;
	protected int		denomination;
	
	/** NSEW �̃n���h */
	protected String	description;
	
	protected Packet[]	hand;
	
/*-------------
 * Constructor
 */
	public RandomProblem() {
	}
	
/*------------------
 * instance methods
 */
	public void start() {
		makeProblem();
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
		// 2. �t�B�b�g�̑����X�[�g��T��
		//
		int[][]	count = new int[4][4];
		
		// �e�X�[�g�ɂ��āA
		for (int suit = 1; suit < 5; suit++) {
			// ���ꂼ��̖����𐔂���
			for (int i = 0; i < 4; i++) {
				count[i][suit-1] = hand[i].countSuit(suit);
			}
		}
		// �����Ƃ��t�B�b�g�������Ă��鑤��T���A���̖������o���Ă���
		// �����t�B�b�g�������Ă���ꍇ�A���x���̍����X�[�g��D�悷��
		int maxFit	= 0;
		int maxSuit = 0;
		int declarer = -1;
		for (int suit = 1; suit < 5; suit++) {
			int ns = count[Board.NORTH][suit-1] + count[Board.SOUTH][suit-1];
			if ( (ns > maxFit)||( (ns == maxFit)&&(suit > maxSuit) ) ) {
				maxFit		= ns;
				maxSuit		= suit;
				declarer	= Board.NORTH; // �Ƃ肠���� North �Ƃ��Ă���
			}
			int ew = count[Board.EAST ][suit-1] + count[Board.WEST ][suit-1];
			if ( (ew > maxFit)||( (ew == maxFit)&&(suit > maxSuit) ) ) {
				maxFit		= ew;
				maxSuit		= suit;
				declarer	= Board.EAST; // �Ƃ肠���� East �Ƃ��Ă���
			}
		}
		//
		// 3. �R���g���N�g�� denomination �����߂�
		//
		if (maxFit == 7) denomination = Bid.NO_TRUMP;
		else denomination = maxSuit;
		
		if (denomination == Bid.NO_TRUMP) {
			//
			// NT�R���g���N�g�̎��A�P�ԋ����l�̃T�C�h�̃R���g���N�g�Ƃ���B
			//
			int maxHcp = 0;
			for (int i = 0; i < 4; i++) {
				int hcp = BridgeUtils.countHonerPoint(hand[i])[0];
				if (hcp > maxHcp) {
					declarer = i % 2;
					maxHcp = hcp;
				}
			}
		}
		
		//
		// 4. declarer �����߂�
		//
		int hcp			= BridgeUtils.countHonerPoint(hand[declarer  ])[0];
		int anotherHcp	= BridgeUtils.countHonerPoint(hand[declarer+2])[0];
		
		if (denomination == Bid.NO_TRUMP) {
			
			// NT �R���g���N�g�̎�
			// Honer Point �̍��������f�B�N���A���[�Ƃ���
			if (hcp < anotherHcp) declarer = declarer + 2;
		} else {
			// �X�[�g�R���g���N�g�̎�
			int decCount = count[declarer  ][denomination-1];
			int dumCount = count[declarer+2][denomination-1];
			if ( decCount < dumCount ) {
				// �p�[�g�i�[(South / West) �� declarer
				declarer = declarer + 2;
			} else if (decCount == dumCount) {
				// �����������ꍇ�� Honer Point �̍�����
				if (hcp < anotherHcp) declarer = declarer + 2;
			}
		}
		
		//
		// 5. �R���g���N�g�� level �����߂�
		//
		level = maxFit - 6; // �Ƃ肠����
		int totalPt = hcp + anotherHcp;
		if (denomination != Bid.NO_TRUMP) {
			totalPt += (maxFit - 8)*2;
		} else {
			level = 1;
		}
		
		if (totalPt > 36) {
			// �O�����h�X����
			level = 7;
		}
		if ( (totalPt > 32)&&(totalPt < 37) ) {
			// �X���[���X����
			level = 6;
		}
		if ( (totalPt > 28)&&(totalPt < 33)&&( (denomination == Bid.CLUB)||(denomination == Bid.DIAMOND) ) ) {
			// �}�C�i�[�Q�[��
			level = 5;
		}
		else if ( (totalPt > 24)&&(totalPt < 33) ) {
			// �Q�[��
			if (denomination == Bid.NO_TRUMP) {
				level = 3;
			} else if ( (denomination == Bid.HEART)||(denomination == Bid.SPADE) ) {
				level = 4;
			} else if ( (denomination == Bid.CLUB)||(denomination == Bid.DIAMOND) ) {
				denomination = Bid.NO_TRUMP;
				level = 3;
			}
		}
		
		//
		// 6. �_�u���󋵂�����
		//
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
