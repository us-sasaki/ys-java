package ys.game.card.bridge;

/**
 * Board �̋@�\�̂����A�I�[�N�V�����Ɋ֌W���镔���̎��ۂ̏������󂯎���
 * �N���X�ł��B
 *
 * @version		a-release	4, January 2001
 * @author		Yusuke Sasaki
 */
public class BiddingHistoryImpl implements BiddingHistory {
	
	/** �r�b�h�����̔z�� */
	private Bid[]	bid;
	
	/** �r�b�h�J�E���g */
	private int		bidCount;
	
	/** ���݂܂łŌ��܂��Ă���R���g���N�g */
	private Bid		contract;
	
	/** ���݂܂łŌ��܂��Ă���f�B�N���A���[ */
	private int		declarer;
	
	/** �f�B�[���[(�r�b�h���J�n����)�̈ʒu */
	private int		dealer;
	
	/** �r�b�h�V�[�P���X���I���������ǂ����̃t���O */
	private boolean	finished;
	
/*-------------
 * Constructor
 */
	/**
	 * �f�B�[���[���w�肵�� bidding history ���쐬���܂��B
	 *
	 * @param		dealer		�f�B�[���[
	 */
	public BiddingHistoryImpl(int dealer) {
		this.dealer = dealer;
		bid = new Bid[319];
		bidCount = 0;
		contract = null;
		declarer = -1;
		finished = false;
	}
	
	/**
	 * �w�肳�ꂽ BiddingHistory �Ɠ���̏�Ԃ� BiddingHistoryImpl
	 * �̃C���X�^���X��V�K�ɐ������܂��B
	 * Bid �̃C���X�^���X�̓R�s�[���̃C���X�^���X���g�p����܂��B
	 *
	 * @param		src		�R�s�[���� BiddingHistory
	 */
	public BiddingHistoryImpl(BiddingHistory src) {
		this(src.getDealer());
		
		Bid[] bids = src.getAllBids();
		for (int i = 0; i < bids.length; i++) {
			bid(bids[i]);
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * �w�肳�ꂽ�r�b�h���A�r�b�f�B���O�V�[�P���X�㋖����邩�e�X�g���܂��B
	 * ���Ȋ֌W�A�r�b�h�̋����Ȃǂ����ׂ��܂��B
	 *
	 * @param		b		�e�X�g����r�b�h
	 * @return		�������r�b�h��
	 */
	public boolean allows(Bid b) {
		if (finished) return false;
		
		if (contract == null) {
			switch (b.getKind()) {
			
			case Bid.PASS:
			case Bid.BID:
				return true;
			
			case Bid.DOUBLE:
			case Bid.REDOUBLE:
				return false;
				
			default:
				throw new InternalError(
							"Bid instance status error: " + b);
			}
		}
		
		// double, redouble
		switch (b.getKind()) {
		
		case Bid.DOUBLE:
			if (contract.getKind() != Bid.BID) return false;
			if ( (contract.getSuit() != b.getSuit())||
				(contract.getLevel() != b.getLevel()) ) return false;
			if (((declarer ^ bidCount ^ dealer) & 1) != 1) return false;
			break;
		
		case Bid.REDOUBLE:
			if (contract.getKind() != Bid.DOUBLE) return false;
			if ( (contract.getSuit() != b.getSuit())||
				(contract.getLevel() != b.getLevel()) ) return false;
			if (((declarer ^ bidCount ^ dealer) & 1) != 0) return false;
			break;
			
		}
		
		// ���x���ɂ�锻��
		if ((b.getKind() != Bid.PASS)&&
			(!b.isBiddableOver(contract))) return false;
		
		return true;
	}
	
	/**
	 * �r�b�h���s���C�r�b�f�B���O�V�[�P���X��i�߂܂��B
	 * �s�\�ȃr�b�h���s�����Ƃ���ƁAIllegalPlayException ���X���[����܂��B
	 *
	 * @param		newBid		�V���ɍs���r�b�h
	 */
	public void bid(Bid newBid) {
		// �r�b�h�ł��邩�̃`�F�b�N���s��.
		if (!this.allows(newBid))
			throw new IllegalPlayException("Illegal bid:" + newBid.toString());
		
		// �r�b�h�ł���̂ŁA�r�b�h�����ɉ�����.
		bid[bidCount++] = newBid;
		
		// declarer, contract �X�V, �p�X������������
		switch (newBid.getKind()) {
		case Bid.PASS:
			// �p�X����������
			int passCount = 0;
			for (int i = bidCount - 1; i >= 0; i--) {
				if (bid[i].getKind() == Bid.PASS) passCount++;
				else break;
			}
			if (passCount < 3) break;
			if ((passCount == 3)&&(contract == null)) break;
			
			// Pass out ?
			finished = true;
			if (passCount == 4) {
				contract = new Bid(Bid.PASS, 0, 0);
				declarer = dealer;
				return;
			}
			break;
		case Bid.DOUBLE:
		case Bid.REDOUBLE:
			contract = newBid;
			break;
		
		case Bid.BID:
			contract = newBid;
			
			// declarer ��������.
			int n;
			for (n = (1 - (bidCount & 1)); n < bidCount; n += 2) {
				Bid b = bid[n];
				if ((b.getKind() == Bid.BID)&&
					(b.getSuit() == newBid.getSuit())) break;
			}
			declarer = (n + dealer)%4;
			break;
		}
		
	}
	
	public void undo() {
		if (bidCount == 0)
			throw new IllegalStatusException("�r�b�h����Ă��Ȃ��̂ŁAundo() �ł��܂���");
		contract		= null;
		bid[--bidCount]	= null;
		declarer		= -1;
		finished		= false;
		
		// contract ��������
		int lastBidCount = 0;
		for (int i = bidCount-1; i >= 0; i--) {
			if (bid[i].getKind() == Bid.BID) {
				contract = bid[i];
				lastBidCount = i+1;
				break;
			}
		}
		
		// declarer ��������
		int n;
		for (n = (1 - (lastBidCount & 1)); n < lastBidCount; n += 2) {
			Bid b = bid[n];
			if ((b.getKind() == Bid.BID)&&
				(b.getSuit() == contract.getSuit())) break;
		}
		declarer = (n + dealer)%4;
	}
	
	/**
	 * �s��ꂽ���ׂẴr�b�h��z��`���ŕԋp���܂��B
	 * �z��̓Y�� 0 ���f�B�[���[�̃r�b�h�Ƃ��Ĉȍ~���ȏ��Ɋi�[����Ă��܂��B
	 * �����_�܂łŃr�b�h���ꂽ�񐔕��̗v�f���܂݂܂��B
	 *
	 * @return		���ׂẴr�b�h
	 */
	public Bid[] getAllBids() {
		Bid[] result = new Bid[bidCount];
		System.arraycopy(bid, 0, result, 0, bidCount);
		
		return result;
	}
	
	/**
	 * ���Ƀr�b�h����Ȃ̔ԍ���Ԃ��܂��B
	 *
	 * @return		�Ȕԍ�
	 */
	public int getTurn() {
		return (dealer + bidCount) % 4;
	}
	
	public void setContract(Bid contract, int declarer) {
		if ( (bidCount != 0)||(finished) )
			throw new IllegalStatusException("���łɃr�b�h����Ă��邽�߃R���g���N�g���w��ł��܂���B");
		if ( (declarer < Board.NORTH)||(declarer > Board.WEST) )
			throw new IllegalArgumentException("declarer �̒l���s���ł��B");
		
		this.contract = contract;
		this.declarer = declarer;
		finished = true;
	}
	
	/**
	 * (���݂܂łŊm�肵�Ă���)�R���g���N�g���擾���܂��B
	 *
	 * @return		�R���g���N�g
	 */
	public Bid getContract() {
		return contract;
	}
	
	/**
	 * (���݂܂łŊm�肵�Ă���)�f�B�N���A���[�̐Ȕԍ����擾���܂��B
	 *
	 * @return		�f�B�N���A���[�̐Ȕԍ�
	 */
	public int getDeclarer() {
		return declarer;
	}
	
	/**
	 * �f�B�[���[�̐Ȕԍ����擾���܂��B
	 *
	 * @return		�f�B�[���[�̐Ȕԍ�
	 */
	public int getDealer() {
		return dealer;
	}
	
	/**
	 * �r�b�f�B���O�V�[�P���X���I���������ǂ����e�X�g���܂��B
	 *
	 * @return		�r�b�f�B���O�V�[�P���X���I��������
	 */
	public boolean isFinished() {
		return finished;
	}
	
	public void reset(int dealer) {
		this.dealer = dealer;
		reset();
	}
	
	private void reset() {
		for (int i = 0; i < 319; i++) {
			bid[i] = null;
		}
		bidCount = 0;
		contract = null;
		declarer = -1;
		finished = false;
	}
	
	public int countBid() {
		return bidCount;
	}
	
/*-----------
 * overrides
 */
	/**
	 * ������\���𓾂�.
	 */
	public String toString() {
		if ( (bidCount == 0)&&(finished) )
			return "Bidding Sequence Unknown";
		String result = "   N       E       S       W\n";
		for (int i = 0; i < dealer; i++) {
			result += "        ";
		}
		int seat = dealer;
		
		for (int i = 0; i < bidCount; i++) {
			result += bid[i];
			seat++;
			if (seat == 4) {
				result += "\n";
				seat = 0;
			}
		}
		return result + "\n";
	}
}
