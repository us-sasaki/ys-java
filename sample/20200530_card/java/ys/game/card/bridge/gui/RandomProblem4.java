package ys.game.card.bridge.gui;

import ys.game.card.bridge.*;
import ys.game.card.*;
import ys.game.card.bridge.ta.*;

/**
 * �u���b�W�V�~�����[�^�̃n���h�A�R���g���N�g���������肵�A
 * ���Ƃ���N���X�B�K�� South ���f�B�N���A���[�ɂȂ�܂��B
 * RandomProblem�ł́A�_�u���h�R���g���N�g���N����₷���A�����₷�����߁A
 * ���ۂ̃R���g���N�g�ɂ��߂����̂ɕύX���Ă��܂��B
 * �Ֆʕ]���֐����g���A�f�m�~�l�[�V���������肵�A�|�C���g�ɂ���ăR���g���N�g��
 * ���x�������肵�܂��B�_�u���h�R���g���N�g�͋N����܂���B
 *
 * @version		making		31, January 2004
 * @author		Yusuke Sasaki
 */
public class RandomProblem4 implements Problem {
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
	
	/** �Ֆʕ]���֐��̌���(NS���̃g���b�N��) */
	private int[]		trick;	// [denomination-1]
	
	private int			declarer;
	
	/**
	 * �����̎���w�肵�܂��B
	 */
	private long		randomSeed;
	
/*-------------
 * Constructor
 */
	public RandomProblem4() {
		this(System.currentTimeMillis());
	}
	
	public RandomProblem4(long rseed) {
		count	= new int[4][4];
		hcp		= new int[4];
		pts		= new int[4];
		trick	= new int[6];
		
		randomSeed	= rseed;
	}
	
/*------------------
 * instance methods
 */
	public void start() {
		makeProblem();
	}
	
	public void setSeed(long rseed) {
		randomSeed = rseed;
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
		
		// �f�m�~�l�[�V�������Ƃ̃g���b�N�����v�Z����B
		for (int denomination = 1; denomination < 6; denomination++) {
			Board b = new BoardImpl(1);
			b.deal(hand);
			b.setContract(new Bid(Bid.BID, 1, denomination), Board.SOUTH);
			
			OptimizedBoard ob = new OptimizedBoard(b);
			trick[denomination-1] = 1300 - ob.calcApproximateTricks();
			// NS���̃g���b�N * 100 �Ƃ���
System.out.println(" denom : " + denomination + "  Tricks : " + trick[denomination-1]);
		}
	}
	
	/**
	 * �f�m�~�l�[�V�����A�f�B�N���A���[�����܂������
	 * �����O�X�|�C���g�A�_�~�[�|�C���g���v�Z����B
	 */
	private void calcPoints(int NSorEW) {
		//
		// �����O�X�|�C���g�Ȃǌv�Z����
		//
		for (int i = NSorEW; i < 4; i+=2) {
			pts[i] = hcp[i];
		}
		
		if (denomination != Bid.NO_TRUMP) {
			// �����O�X�|�C���g�����Z����
			// �����O�X�|�C���g�́ANT/Suit�R���g���N�g�� (�X�[�g�̖���)-4 �����̂Ƃ�
			// ���̐��l�����Z���܂�(5���X�[�g..1pts  6���X�[�g..2pts �c�c)
			// �g�����v�X�[�g�́A���̕]�����Ƃ���
			// FP�i�t�B�b�g�|�C���g�E�E�a���̑��ꁁ�i�t�B�b�g�����|8�j��1.5
			for (int i = NSorEW; i < 4; i+=2) {
				for (int suit = 1; suit < 5; suit++) {
					if (suit == denomination) continue;
					int cnt = count[i][suit-1];
					if (cnt > 4) pts[i] += (cnt - 4);
				}
			}
			int fit = count[NSorEW][denomination-1] + count[NSorEW+2][denomination-1];
			if (fit > 8) pts[NSorEW] += ((fit - 8) * 3 / 2);
			// NorE�݂̂ɉ��_���Ă��邪�A�g�����v�̂Ƃ���totalPoint�����]�����Ȃ�
			// �f�B�N���A���[�����߂�̂ɒ������g�p���Ă���̂�
		
			// �_�~�[�|�C���g
			int dummy = (declarer + 2) % 4;
			int dummyTrumps = count[dummy][denomination-1];
			for (int suit = 1; suit < 5; suit++) {
				int cnt = count[dummy][suit-1];
				switch (cnt) {
				
				case 0:	// void
					pts[dummy] += min(5, dummyTrumps*3);
					break;
				case 1: // singleton
					pts[dummy] += min(3, (dummyTrumps-1)*3);
					break;
				case 2: // doubleton
					pts[dummy] += min(1, (dummyTrumps-2)*3);
				default:	// fall through
				}
			}
		}
	}
	
	private static int min(int a, int b) {
		if (a < b) return a;
		return b;
	}
	
	/**
	 * �����_���Ƀn���h��z��A���̓��e����R���g���N�g�Ȃǂ����肵�܂��B
	 */
	private void makeProblem() {
		//
		// 1. �܂��A�����_���Ƀn���h��z��
		//
		Packet pile = PacketFactory.provideDeck(PacketFactory.WITHOUT_JOKER);
		PacketImpl.setRandom(new java.util.Random(randomSeed));
		
		pile.shuffle();
		hand = PacketFactory.deal(pile, 4);
		
		for (int i = 0; i < 4; i++) hand[i].arrange();
		
		//
		// 2. �X�[�g���Ƃ̖����AHCP�ANS���̃g���b�N������b���Ƃ��Čv�Z����
		//
		calculateAttributes();
		
		//
		// 3. �f�m�~�l�[�V�����A�T�C�h���g���b�N�����猈�肷��
		//
		
		// �ő�ƍŏ��̕����v�Z����
		int maxNSTricks = -1;
		int maxDenom 	= -1;
		int minNSTricks = 1400;
		int minDenom	= -1;
		for (int i = 0; i < 5; i++) {
			if (trick[i] > maxNSTricks) {
				maxNSTricks = trick[i];
				if (i < 4) maxDenom = i + 1;
			}
			if (trick[i] < minNSTricks) {
				minNSTricks = trick[i];
				if (i < 4) minDenom = i + 1;
			}
		}
		int NSorEW = -1;
System.out.println("min " + minNSTricks + "  max " + maxNSTricks);
		if (maxNSTricks - minNSTricks <= 100) {
			this.denomination = Bid.NO_TRUMP;
			if (trick[Bid.NO_TRUMP] > 650) NSorEW = 0;	// NS���R���g���N�g
			else NSorEW = 1;
		} else {
			if (maxNSTricks > 1300 - minNSTricks) {
				NSorEW = 0;
				this.denomination = maxDenom;
			} else {
				NSorEW = 1;
				this.denomination = minDenom;
			}
		}
		
		//
		// 4. �f�B�N���A���[�����肷��
		//
		if (this.denomination == Bid.NO_TRUMP) {
			// HCP �̑傫�������f�B�N���A���[�Ƃ���
			if (hcp[NSorEW] > hcp[NSorEW + 2]) {
				this.declarer = NSorEW;
			} else {
				this.declarer = NSorEW + 2;
			}
		} else {
			// �X�[�g�R���g���N�g�̂Ƃ��́A�g�����v�̒�����
			if (count[NSorEW][this.denomination-1] > count[NSorEW+2][this.denomination-1]) {
				this.declarer = NSorEW;
			} else {
				this.declarer = NSorEW + 2;
			}
		}
		
		//
		// 5. �����O�X�|�C���g�A�_�~�[�|�C���g���v�Z����
		//
		calcPoints(NSorEW);
		
		//
		// 6. �_�������W�Ń��x�������߂�
		//
		int totalPt = pts[NSorEW] + pts[NSorEW+2];
System.out.println("Total Point : " + totalPt);
		if (totalPt > 36) {
			// �O�����h�X����
			level = 7;
		} else
		if ( (totalPt > 32)&&(totalPt < 37) ) {
			// �X���[���X����
			level = 6;
		} else
		if ( (totalPt > 29)&&(totalPt < 33 ) ) {
			// �T�̑�
			level = 5;
		} else
		if ( (totalPt > 26)&&(totalPt <30) ) {
			// �S�̑�
			level = 4;
		} else
		if ( (totalPt > 23)&&(totalPt < 27) ) {
			// �R�̑�
			level = 3;
		} else
		if ( (totalPt > 21)&&(totalPt < 24) ) {
			// �Q�̑�
			level = 2;
		} else {
			// �P�̑�
			level = 1;
		}
		kind = Bid.BID; // �_�u���Ȃ�
		
		int tr = trick[denomination-1]/100;
		if (tr < 7) tr = 13 - tr;
		if (level + 5 > tr) kind = Bid.DOUBLE;
		
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
