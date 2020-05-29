package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;
/**
 * Player �� Bid, �܂��� Play���s����̂ł��B
 * GUI�ɘA�������l�A�R���s���[�^�A���S���Y���Ȃǂ����Ă͂܂�܂��B
 * Player �́A�e�X�P��Board�ƘA�����Ă���BBoardManager�̂���master board
 * �Ƃ̐������́A���̂Ƃ���Player�̂��肦�Ȃ��v���C���_�@�Ƃ��ďC�������B
 * �����I�ɂ� Board �̏�ԕύX���\�b�h���� hashCode ��p���Đ������m�������B
 *
 * @version		a-release		11, May 2000
 * @author		Yusuke Sasaki
 */
public abstract class Player {
	/** ���[�h�̏��Ԃ������萔�ŁA���[�_�[�i�P�Ԗځj�������܂��B */
	protected static final int LEAD		= Board.LEAD;
	
	/** ���[�h�̏��Ԃ������萔�ŁA�Z�J���h�n���h�i�Q�Ԗځj�������܂��B */
	protected static final int SECOND	= Board.SECOND;
	
	/** ���[�h�̏��Ԃ������萔�ŁA�T�[�h�n���h�i�R�Ԗځj�������܂��B */
	protected static final int THIRD	= Board.THIRD;
	
	/** ���[�h�̏��Ԃ������萔�ŁA�t�H�[�X�n���h�i�S�Ԗځj�������܂��B */
	protected static final int FORTH	= Board.FORTH;
	
	/** �v���C���[�̑��Έʒu�������萔(=0)�ŁA�����̈ʒu�������܂��B */
	protected static final int ME		= 0;
	
	/** �v���C���[�̑��Έʒu�������萔(=1)�ŁA�����̍��̐�(left hand)�������܂��B */
	protected static final int LEFT		= 1;
	
	/** �v���C���[�̑��Έʒu�������萔(=2)�ŁA�p�[�g�i�[�̈ʒu�������܂��B */
	protected static final int PARTNER	= 2;
	
	/** �v���C���[�̑��Έʒu�������萔(=3)�ŁA�����̉E�̐�(right hand)�������܂��B */
	protected static final int RIGHT	= 3;
	
	private Board	myBoard;
	private int		mySeat;
	
/*------------------
 * instance methods
 */
	/**
	 * ���̃v���C���[���Q�Ƃ���{�[�h��ݒ肵�܂��B
	 * �p���N���X�̃R���X�g���N�^�ȂǂŎg�p���܂��B
	 * �㏑���\�ł��B
	 */
	public void setBoard(Board board) {
		myBoard = board;
	}
	
	/**
	 * ���̃v���C���[�̍����Ă���ꏊ(Board.NORTH�Ȃ�)���w�肵�܂��B
	 */
	public void setMySeat(int seat) {
		mySeat = seat;
	}
	
	/**
	 * ��ʃv���O��������R�[������郁�\�b�h�ŁA
	 * ���̃v���C���[�̃r�b�h�A�v���C��ԋp���܂��B
	 */
	public Object play() throws InterruptedException {
		switch (myBoard.getStatus()) {
		
		case Board.BIDDING:
			while (true) {
				Bid b = bid();
				if (myBoard.allows(b)) return b;
			}
			
		case Board.OPENING:
		case Board.PLAYING:
			while (true) {
				Card c = draw();
				if (myBoard.allows(c)) return c;
			}
			
		case Board.DEALING:
		case Board.SCORING:
			throw new IllegalStatusException();
			
		default:
			throw new InternalError();
		}
	}
	
/*
 * �T�u�N���X�ɒ񋟂���֗��֐�
 */
	/**
	 * ���̃v���C���[�̎Q�Ƃ���{�[�h��ԋp���܂��B
	 */
	public Board getBoard() {
		return myBoard;
	}
	
	/**
	 * ���̃v���C���[�̍����Ă�����Ȃ�ԋp���܂��B
	 */
	public int getMySeat() {
		return mySeat;
	}
	
	/**
	 * �p�[�g�i�[�̍����Ă���ꏊ(Board.NORTH�Ȃǂ̍��Ȓ萔)��ԋp���܂��B
	 *
	 * @since		2002/5
	 */
	public int getPartnerSeat() {
		return (mySeat + 2) % 4;
	}
	
	/**
	 * ���̃v���C���[�̂��n���h��ԋp���܂��B
	 */
	public Packet getMyHand() {
		return myBoard.getHand(mySeat);
	}
	
	/**
	 * ���݃v���C���ԂƂȂ��Ă���n���h���擾���܂��B
	 * ���̃v���C���[���f�B�N���A���[�̏ꍇ�A���̃��\�b�h���g�p���邱�Ƃ�
	 * �v���C�ΏۂƂȂ��Ă��鎩���A�܂��̓_�~�[�̂����ꂩ�̃n���h���擾�ł��܂��B
	 */
	public Packet getHand() {
		return myBoard.getHand(myBoard.getTurn());
	}
	
	public Packet getDummyHand() {
		return myBoard.getHand(myBoard.getDummy());
	}
	
	/**
	 * ���ݏ�ɏo�Ă���J�[�h���擾���܂��B
	 */
	public Trick getTrick() {
		return myBoard.getTrick();
	}
	
	/**
	 * ���[�h���ꂽ�J�[�h���擾���܂��B
	 * ���������[�h���s���Ԃł������ꍇ��ABoard �̏�Ԃ� Board.PLAYING
	 * �łȂ������ꍇ�Anull ���ԋp����܂��B
	 */
	public Card getLead() {
		int o = getPlayOrder();
		if ((o == Board.LEAD)||(o == -1)) return null;
		return getTrick().getLead();
	}
	
	/**
	 * ���݂̃v���C��(lead, 2nd, 3rd, 4th)��Ԃ��܂��B
	 * Board �̏�Ԃ� Board.OPENING, Board.PLAYING �ȊO�̏ꍇ�A -1 ���ԋp����܂��B
	 *
	 * @return		�v���C���������萔(LEAD, SECOND, THIRD, FORTH)
	 */
	public int getPlayOrder() {
		return myBoard.getPlayOrder();
	}
	
	/**
	 * �_�~�[�̎�������̑��Έʒu��Ԃ��܂��B
	 * �_�~�[���܂����肵�Ă��Ȃ��ꍇ�AIllegalStatusException ���X���[����܂��B
	 *
	 * @return		�_�~�[�̑��Έʒu
	 * @see			ME
	 * @see			LEFT
	 * @see			PARTNER
	 * @see			RIGHT
	 */
	public int getDummyPosition() {
		int dummySeat = myBoard.getDummy();
		if (dummySeat == -1)
			throw new IllegalStatusException("�܂��R���g���N�g�����肵�Ă��܂���");
		
		return (dummySeat - mySeat + 4) % 4;
	}
	
	/**
	 * Leader �̎�������̑��Έʒu��Ԃ��܂��B
	 * �v���C�̏�ԂłȂ��Ƃ��AIllegalStatusException ���X���[����܂��B
	 *
	 * @return		Leader�̑��Έʒu
	 * @see			ME
	 * @see			LEFT
	 * @see			PARTNER
	 * @see			RIGHT
	 */
	public int getLeaderPosition() {
		if ( (myBoard.getStatus() != Board.OPENING) && (myBoard.getStatus() != Board.PLAYING) )
			throw new IllegalStatusException("�v���C���J�n����Ă��܂���");
		if (myBoard.getStatus() == Board.SCORING)
			throw new IllegalStatusException("�{�[�h�͂��łɏI�����Ă��܂�");
		
		Trick t = myBoard.getTrick();
		return (t.getLeader() - mySeat + 4) % 4;
	}
	
/*------------------
 * abstract methods
 */
	public abstract Bid bid() throws InterruptedException;
	public abstract Card draw() throws InterruptedException;
	
}
