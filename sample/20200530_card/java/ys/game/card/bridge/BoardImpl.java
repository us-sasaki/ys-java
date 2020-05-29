package ys.game.card.bridge;
/*
 * 2001/ 7/23  setName(), getName() �ǉ�
 */

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.PacketFactory;
import ys.game.card.CardOrder;
import ys.game.card.NaturalCardOrder;

/**
 * �u���b�W�ɂ�����P�{�[�h���p�b�N����B
 * BoardManager�ɑ΂���󓮓I�ȃI�u�W�F�N�g�ŁA��ԕω����N����
 * ���\�b�h��񋟂���B
 * Player�ɑ΂��Ă͏�ԎQ�Ƃ݂̂������B
 *
 * @version		a-release		23, July 2001
 * @author		Yusuke Sasaki
 */
public class BoardImpl implements Board {
	
/*---------------
 * Vulnerability
 */
	// O | - +   | - + O   - + O |   + O | -
	private static final int[] VUL = { 0, 1, 2, 3,  1, 2, 3, 0,
											2, 3, 0, 1, 3, 0, 1, 2 };
	
	/** ���� Board �̖��O */
	private String			name;
	
	/** �r�b�h�����Ȃǂ̃I�[�N�V���������̋@�\��񋟂���B */
	private BiddingHistory	bidding;
	
	/** �v���C�����̋@�\��񋟂���B */
	private PlayHistory		play;
	
	/** ���݂̃X�e�[�^�X */
	private int				status;
	
	/** ���̃{�[�h�� vulnerability */
	private int				vul;
	
	/** ���m�̃J�[�h */
	private Packet			openCards;
	
/*-------------
 * Constructor
 */
	/**
	 * �^����ꂽ �{�[�h�i���o�[�̏����{�[�h���쐬����B
	 *
	 * @param		num		�{�[�h�ԍ�(1�ȏ�)
	 */
	public BoardImpl(int num) {
		this((num - 1)%4, VUL[(num - 1)%16]);
	}
	
	/**
	 * �^����ꂽ�f�B�[���[�A�o���̏����{�[�h���쐬���܂��B
	 */
	public BoardImpl(int dealer, int vul) {
		// �������N���X�̐ݒ�
		bidding	= new BiddingHistoryImpl(dealer);
		play	= new PlayHistoryImpl();
		
		this.vul	= vul;
		status		= DEALING;
	}
	
	/**
	 * �w�肳�ꂽ Board �Ɠ���̏�Ԃ� BoardImpl �̃C���X�^���X��V�K�ɐ������܂��B
	 * BiddingHistory, PlayHistory �ɂ��ẮA�R�s�[���̂��̂̃C���X�^���X���g�p
	 * ����܂��B
	 */
	public BoardImpl(Board src) {
		name	= src.getName();
		bidding	= src.getBiddingHistory();
		play	= src.getPlayHistory();
		status	= src.getStatus();
		vul		= src.getVulnerability();
		openCards = src.getOpenCards();
	}
	
/*----------------------------------------------------------
 * ��ԕω����\�b�h(���J����Ȃ��p�b�P�[�W���x���̃��\�b�h)
 */
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		if (name == null) return "";
		return name;
	}
	
	public void deal() {
		if (status != DEALING)
			throw new IllegalStatusException("deal() �� DEALING ��Ԃ݂̂Ŏ��s�\�ł��B");
		
		Packet[] hand = new Packet[4];
		for (int i = 0; i < 4; i++) hand[i] = new PacketImpl();
		
		Packet pile = PacketFactory.provideDeck(PacketFactory.WITHOUT_JOKER);
		openCards = new PacketImpl();
		
		// holder ��ݒ肷��
		openCards.add(pile.peek());
		openCards.draw(pile.peek());
		
		// �J�[�h�𗠌����ɂ��Ĕz��
		for (int i = 0; i < pile.size(); i++) {
			Card card = pile.peek(i);
			card.turn(false);
		}
		pile.shuffle();
		PacketFactory.deal(pile, hand);
		for (int i = 0; i < 4; i++) hand[i].arrange();
		
		play.setHand(hand);
		
		status = BIDDING;
	}
	
	public void deal(Packet[] hand) {
		if (status != DEALING)
			throw new IllegalStatusException("deal() �� DEALING ��Ԃ݂̂Ŏ��s�\�ł��B");
		
		// �J�[�h�𗠌����ɂ��Ă���
		for (int i = 0; i < hand.length; i++) {
			for (int j = 0; j < hand[i].size(); j++) {
				hand[i].peek(j).turn(false);
			}
		}
		
		openCards = new PacketImpl();
		// holder ��ݒ肷��
		openCards.add(hand[0].peek());
		openCards.draw(hand[0].peek());
		
		play.setHand(hand);
		status = BIDDING;
	}
	
	/**
	 * ���� Board �Ŏg�p���Ă��� BiddingHistory ���擾���܂��B
	 *
	 * @return		BiddingHistory
	 */
	public BiddingHistory getBiddingHistory() {
		return bidding;
	}
	
	/**
	 * ���� Board �Ŏg�p���Ă��� PlayHistory ���擾���܂��B
	 *
	 * @return		PlayHistory
	 */
	public PlayHistory getPlayHistory() {
		return play;
	}
	
	/**
	 * ���� Board �Ŏg�p���� PlayHistory ���w�肵�܂��B
	 *
	 * @param		�ݒ肵���� playHistory
	 */
	public void setPlayHistory(PlayHistory playHistory) {
		this.play = playHistory;
	}
	
	public void setContract(Bid contract, int declarer) {
		bidding.setContract(contract, declarer); // may throw IllegalStatusException
		status = OPENING;
		this.play.setContract((getDeclarer() + 1)%4, getTrump() );
		
		reorderHand();
	}
	
	/**
	 * �r�b�h�A�v���C���s���B��Ԃ��ω�����B
	 */
	public void play(Object play) {
		if (!this.allows(play))
			throw new IllegalPlayException(play.toString() + "�͍s���܂���B");
		switch (status) {
		
		case BIDDING:
			if (!(play instanceof Bid))
				throw new IllegalPlayException("�r�b�h���Ȃ���΂Ȃ�܂���B" + play);
			bidding.bid((Bid)play);
			if (bidding.isFinished()) {
				if (getContract().getKind() == Bid.PASS) {
					//
					// Pass out
					//
					status = SCORING;
					break;
				}
				status = OPENING;
				this.play.setContract((getDeclarer() + 1)%4, getTrump() );
				
				reorderHand();
			}
			break;
			
		case OPENING:
		case PLAYING:
			if (!(play instanceof Card))
				throw new IllegalPlayException("�v���C���Ȃ���΂Ȃ�܂���B" + play);
			this.play.play((Card)play);
			openCards.add((Card)play);
			
			if (status == OPENING) dummyOpen();
			// �����l�b�g���[�N�ɑΉ�����ꍇ�A�}�X�^�[�ȊO�̃{�[�h�ł�
			// dummy �� Unspecified �ƂȂ��Ă���Bopening lead �Ɠ�����
			// dummy hand ��ʒm���AdummyOpen() �ȑO�Ƀ{�[�h�̃n���h��
			// specified �ɂ���ׂ��B
			//
			
			if (this.play.isFinished()) status = SCORING;
			else status = PLAYING;
			break;
			
		case DEALING:
		case SCORING:
			throw new IllegalPlayException(play.toString() + "�͍s���܂���B");
		
		default:
			throw new InternalError("Board.status ���s���Ȓl"
							+ status + "�ɂȂ��Ă��܂��B");
		}
	}
	
	/**
	 * �P�O�̏�Ԃɖ߂��܂��B
	 * ��ԑJ�ڂ��l����
	 *
	 * DEALING
	 *    |                           pass out
	 *    +----->BIDDING---------------------------------+
	 *    |              �_                              |
	 *    +----------------->OPENING                     |
	 *                          |                        |
	 *                          +-------->PLAYING        |
	 *                          O.L.         |           V
	 *                                       +------->SCORING
	 *                                       last play
	 */
	public void undo() {
		switch (status) {
		case DEALING:
			throw new IllegalStatusException("DEALING ��Ԃ� undo() �͂ł��܂���");
		
		case BIDDING:
			if (bidding.countBid() == 0) {
				status = DEALING;
				break;
			}
			bidding.undo();
			break;
		
		case OPENING:
			if (bidding.countBid() == 0) {
				// setContract ����Ă���
				status = DEALING;
				bidding.reset(bidding.getDealer());
				break;
			}
			status = BIDDING;
			bidding.undo();
			break;
		
		case SCORING:
			status = PLAYING;
			// fall through
			
		case PLAYING:
			//
			// �܂��APlayHistory �� undo() ����
			//
			Card lastPlay = play.undo(); // PLAYING �Ȃ̂ŁAException �͂łȂ��͂�
			int turn = play.getTurn();
			if (turn != getDummy()) lastPlay.turn(false);
			openCards.draw(lastPlay);
			
			// undo() �̌��ʁAOPENING �ɂȂ�ꍇ
			if ((getTricks() == 0)&&(play.getTrick() == null)) {
				// O.L.�ɂ��ǂ�
				dummyClose();
				status = OPENING;
			}
			break;
			
		default:
			throw new InternalError("status ���ُ�l " + status + " �ɂȂ��Ă��܂�");
		}
	}
	
	/**
	 * �r�b�h�I����ɁA�g�����v�X�[�g�����ɗ���悤�ɕ��ёւ��܂��B
	 */
	private void reorderHand() {
		CardOrder order; // stateless object
		
		switch (getTrump()) {
		case Bid.HEART:
			order = new NaturalCardOrder(NaturalCardOrder.SUIT_ORDER_HEART);
			break;
		case Bid.DIAMOND:
			order = new NaturalCardOrder(NaturalCardOrder.SUIT_ORDER_DIAMOND);
			break;
		case Bid.CLUB:
			order = new NaturalCardOrder(NaturalCardOrder.SUIT_ORDER_CLUB);
			break;
		default:
			order = new NaturalCardOrder(NaturalCardOrder.SUIT_ORDER_SPADE);
			break;
		}
		
		getHand(getDummy()).setCardOrder(order);
		getHand(getDummy()).arrange();
		getHand(getDeclarer()).setCardOrder(order);
		getHand(getDeclarer()).arrange();
	}
	
	/**
	 * �I�[�v�j���O���[�h�̌�ɌĂ΂�A�_�~�[�̎��\�����ɕύX���܂��B
	 * �܂��A��ɏo�Ă���J�[�h�Ƀ_�~�[�n���h��ǉ����܂��B
	 */
	protected void dummyOpen() {
		Packet dummy = getHand(getDummy());
		dummy.turn(true);
		openCards.add(dummy);
	}
	
	/**
	 * undo() ���� Opening Lead ��Ԃɂ��ǂ����߂̏������s���܂��B
	 * �_�~�[�̎�𗠌����ɕύX���܂��B
	 */
	protected void dummyClose() {
		Packet dummy = getHand(getDummy());
		dummy.turn(false);
		openCards.sub(dummy);
		
		if (openCards.size() != 0) throw new InternalError("openCards �����ɖ���������܂�");
	}
	
/*------------------------------
 * ��ԎQ�ƃ��\�b�h(���J�����)
 */
	/**
	 * �v���C�\�ł��邩�e�X�g����B
	 *
	 * @return		true�F�\    false:�s�\
	 */
	public boolean allows(Object play) {
		switch (status) {
		
		case BIDDING:
			if (play instanceof Bid) return bidding.allows((Bid)play);
			return false;
			
		case OPENING:
		case PLAYING:
			if (play instanceof Card) return this.play.allows((Card)play);
			return false;
			
		case DEALING:
		case SCORING:
			return false;
			
		default:
			throw new InternalError("Board.status ���s���Ȓl"
							+ status + "�ɂȂ��Ă��܂��B");
		}
	}
	
	public int getStatus() {
		return status;
	}
	
	/**
	 * �X�e�[�^�X�� Board.OPENING, Board.PLAYING �̏ꍇ�Ƀv���C����
	 * �����萔��ԋp���܂��B
	 *
	 * @return		�v���C���������萔
	 */
	public int getPlayOrder() {
		switch (status) {
		
		case OPENING:
		case PLAYING:
			return getTrick().size();
		
		case DEALING:
		case BIDDING:
		case SCORING:
			return -1;
		
		default:
			throw new InternalError("Board.status ���s���Ȓl"
							+ status + "�ɂȂ��Ă��܂��B");
		}
	}
	
	/**
	 * Vulnerability ���擾���܂��B
	 *
	 * @return		���̃{�[�h�� vulnerability�B
	 *				(VUL_NONE, VUL_NS, VUL_EW, VUL_BOTH)
	 */
	public int getVulnerability() {
		return vul;
	}
	
	/**
	 * ����̔Ԃł��邩��Ȕԍ��ŕԋp����B
	 * DEALING, SCORING �̃X�e�[�^�X�ł� -1 ���ԋp�����B
	 */
	public int getTurn() {
		switch (status) {
		
		case BIDDING:
			return bidding.getTurn();
			
		case OPENING:
		case PLAYING:
			return play.getTurn();
			
		case DEALING:
		case SCORING:
			return -1;
			
		default:
			throw new InternalError("Board.status ���s���Ȓl"
							+ status + "�ɂȂ��Ă��܂��B");
		}
	}
	
	/**
	 * ���ꂪ�v���C����Ԃ��擾���܂��B
	 * getTurn() �Ƃ̈Ⴂ�́A�v���C�̂Ƃ��A�_�~�[�̔ԂŃf�B�N���A���[��
	 * �Ȕԍ����Ԃ����_�ł��B
	 */
	public int getPlayer() {
		int seat = getTurn();
		
		switch (status) {
		
		case OPENING:
		case PLAYING:
			if (seat == getDummy()) return getDeclarer();
			return seat;
			
		case BIDDING:
			return seat;
			
		case DEALING:
		case SCORING:
		default:
			return -1;
		}
	}
	
	/**
	 * (���݂܂ł�)�ŏI�R���g���N�g���擾����B
	 * �r�b�h���܂��s���Ă��Ȃ��ꍇ�Anull ���Ԃ�B
	 */
	public Bid getContract() {
		return bidding.getContract();
	}
	
	/**
	 * (���݂܂ł�)�ŏI�g�����v���擾���܂��B
	 * �r�b�h���܂��s���Ă��Ȃ��ꍇ�A-1 ���Ԃ�܂��B
	 */
	public int getTrump() {
		Bid contract = getContract();
		if (contract == null) return -1;
		return contract.getSuit();
	}
	
	/**
	 * (���݂܂łŌ��肵�Ă���)�f�B�N���A���[�̐Ȕԍ����擾����B
	 * �r�b�h���܂��s���Ă��Ȃ��ꍇ�A-1 ���Ԃ�B
	 */
	public int getDeclarer() {
		return bidding.getDeclarer();
	}
	
	/**
	 * (���݂܂łŌ��肵�Ă���)�_�~�[�̐Ȕԍ����擾����B
	 * �r�b�h���܂��s���Ă��Ȃ��ꍇ�A-1 ���Ԃ�B
	 */
	public int getDummy() {
		int dec = bidding.getDeclarer();
		if (dec == -1) return -1;
		return (dec + 2) % 4;
	}
	
	/**
	 * (�r�b�h���J�n����)�f�B�[���[�̐Ȕԍ����擾����B
	 * �r�b�h���܂��s���Ă��Ȃ��ꍇ�A-1 ���Ԃ�B
	 */
	public int getDealer() {
		return bidding.getDealer();
	}
	
	/**
	 * �n���h�̏����擾����BUnspecifiedCard���܂܂�邱�Ƃ�����B
	 */
	public Packet getHand(int seat) {
		return play.getHand(seat);
	}
	
	/**
	 * ���ׂẴn���h�̏�Ԃ��擾����B
	 */
	public Packet[] getHand() {
		return play.getHand();
	}
	
	/**
	 * ���݂܂łɃv���C���ꂽ�g���b�N�����擾����B
	 */
	public int getTricks() {
		return play.getTricks();
	}
	
	/**
	 * ���ݏ�ɏo�Ă���g���b�N���擾����B
	 */
	public Trick getTrick() {
		return play.getTrick();
	}
	
	/**
	 * �v���C���ꂽ�ߋ��̃g���b�N���ׂĂ��擾���܂��B
	 * �{�����́APlayHistory.getAllTricks() �ֈϏ����Ă��܂��B
	 *
	 * @see		ys.game.card.bridge.PlayHistory#getAllTricks()
	 */
	public Trick[] getAllTricks() {
		return play.getAllTricks();
	}
	
	/**
	 * �w�肳�ꂽ���Ȃ��o���ł��邩���肵�܂��B
	 */
	public boolean isVul(int seat) {
		int mask = 0;
		if ( (seat == NORTH)||(seat == SOUTH) ) mask = 1;
		else mask = 2;
		
		if ((vul & mask) > 0) return true;
		return false;
	}
	
	/**
	 * �v���C���ꂽ�J�[�h�A�_�~�[�n���h�Ƃ��������m�̃J�[�h��Ԃ��܂��B
	 * �v�l���[�`���Ŏg�p����邱�Ƃ����҂��Ă��郁�\�b�h�ł��B
	 * openCards �Ƃ��ĔF���������̂́A���łɏ�ɏo���J�[�h�ƃ_�~�[�̃n���h�ł��B
	 *
	 * @return		���m�̃J�[�h����Ȃ� Packet
	 */
	public Packet getOpenCards() {
		return openCards;
	}
	
	/**
	 * ������\���𓾂�B
	 */
	public String toString() {
		String result = "---------- Board Information ----------";
		result += "\n  [    Status     ]  : " + STATUS_STRING[status];
		result += "\n  [ Vulnerability ]  : " + VUL_STRING[vul];
		result += "\n  [    Dealer     ]  : " + SEAT_STRING[getDealer()];
		result += "\n  [   Declarer    ]  : ";
		if (getDeclarer() == -1) result += "none";
		else result += SEAT_STRING[getDeclarer()];
		result += "\n\n  [Bidding History]\n";
		result += bidding.toString();
		result += "\n  [  Table Info   ]\n";
		result += play.toString() + "\n";
		
		return result;
	}
	
	public void reset(int num) {
		reset((num - 1)%4, VUL[(num - 1)%16]);
	}
	
	public void reset(int dealer, int vul) {
		bidding.reset(dealer);
		play.reset();
		
		this.vul = vul;
		status = DEALING;
	}
	
	public String toText() {
		StringBuffer s = new StringBuffer();
		String nl = "\n";
		
		s.append(name);
		s.append(nl);
		s.append("----- �R���g���N�g -----");
		s.append(nl);
		s.append("contract�F");	s.append(getContract().toString());
		s.append(" by " + SEAT_STRING[getDeclarer()]);	s.append(nl);
		s.append("vul     �F");	s.append(VUL_STRING[getVulnerability()]);	s.append(nl);
		
		s.append(nl);
		s.append("----- �I���W�i���n���h -----");
		s.append(nl);
		
		Packet[] hands = BridgeUtils.calculateOriginalHand(this);
		
		// NORTH
		for (int suit = 4; suit >= 1; suit--) {
			s.append("               ");
			s.append(getHandString(hands, 0, suit));
			s.append(nl);
		}
		
		// WEST, EAST
		for (int suit = 4; suit >= 1; suit--) {
			String wstr = getHandString(hands, 3, suit) + "               ";
			wstr = wstr.substring(0, 15);
			s.append(wstr);
			switch (suit) {
			case 4:
			s.append("    N          "); break;
			case 3:
			s.append("W       E      "); break;
			case 2:
			s.append("               "); break;
			case 1:
			s.append("    S          "); break;
			default:
			}
			
			s.append(getHandString(hands, 1, suit));
			s.append(nl);
		}
		// SOUTH
		for (int suit = 4; suit >= 1; suit--) {
			s.append("               ");
			s.append(getHandString(hands, 2, suit));
			s.append(nl);
		}
		
		// �r�b�h�o��
//		s.append(bidding.toString());
//		s.append(nl);
		
		// �v���C���C��
		s.append(nl);
		s.append("----- �v���C -----");
		s.append(nl);
		
		s.append("    1  2  3  4  5  6  7  8  9 10 11 12 13");
		s.append(nl);
		
		Trick[] trick = getAllTricks();
		StringBuffer[] nesw = new StringBuffer[4];
		for (int i = 0; i < 4; i++) {
			nesw[i] = new StringBuffer();
			nesw[i].append("NESW".substring(i,i+1));
			nesw[i].append(' ');
			if (getTricks() > 0) {
				int leaderSeat = trick[0].getLeader();
				if (i == leaderSeat) nesw[i].append('-');
				else nesw[i].append(' ');
			}
		}
		
		for (int i = 0; i < getTricks(); i++) {
			int leaderSeat = trick[i].getLeader();
			int winnerSeat = trick[i].getWinner();
			for (int j = 0; j < 4; j++) {
				int seat = (j + leaderSeat) % 4;
				nesw[seat].append(trick[i].peek(j).toString().substring(1));
				if (seat == winnerSeat) nesw[seat].append('+');
				else nesw[seat].append(' ');
			}
		}
		for (int i = 0; i < 4; i++) {
			s.append(nesw[i]);
			s.append(nl);
		}
//				
//		for (int i = 0; i < getTricks(); i++) {
//			if (i < 13) {
//				s.append("[");
//				if (i < 9) s.append(" ");
//				s.append(String.valueOf(i+1));
//				s.append("]"+getAllTricks()[i]);
//				s.append("  win:"+getAllTricks()[i].getWinnerCard());
//				s.append(" " + Board.SEAT_STRING[getAllTricks()[i].getWinner()]+"\n");
//			}
//		}
//		s.append(nl);
//		
		if (getStatus() == SCORING) {
			s.append(nl);
			s.append("----- ���� -----");
			s.append(nl);
			// ���C�N��
			int win		= countWinners();
			int up		= win - getContract().getLevel() - 6;
			int make	= win - 6;
			
			if (up >= 0) {
				// ���C�N
				s.append(String.valueOf(make) + "���C�N  ");
			} else {
				// �_�E��
				s.append(String.valueOf(-up) + "�_�E��  ");
			}
			
			s.append("("+win+"�g���b�N)\nN-S���̃X�R�A�F"+Score.calculate(this, SOUTH));
			s.append(nl);
		}
		return s.toString();
	}
	
	private String getHandString(Packet[] hand, int seat, int suit) {
		StringBuffer s = new StringBuffer();
		s.append("CDHS".substring(suit-1, suit));
		s.append(':');
		Packet oneSuit = hand[seat].subpacket(suit);
		oneSuit.arrange();
		for (int i = 0; i < oneSuit.size(); i++) {
			Card c = oneSuit.peek(i);
			if ((c.isHead())||(getOpenCards().contains(c))) {
				s.append(c.toString().substring(2));
			}
		}
		return s.toString();
	}
	
	private int countWinners() {
		Trick[]	tr			= getAllTricks();
		if (tr == null) return 0;
		
		int		win			= 0;
		int		declarer	= getDeclarer();
		
		// winner �𐔂���(Board �ɂ������ق����֗�)
		for (int i = 0; i < tr.length; i++) {
			int winner = tr[i].getWinner();
			if ( ((winner ^ declarer) & 1) == 0 ) win++;
		}
		
		return win;
	}
}

