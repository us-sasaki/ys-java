package ys.game.card;

/**
 * CardImpl �̓C���^�[�t�F�[�X Card �̃f�t�H���g�̎�����񋟂��܂��B
 *
 * @version		a-release	29, April 2000
 * @author		Yusuke Sasaki
 */
public class CardImpl implements Card {
	
	/** ���̃J�[�h�̃o�����[ */
	private int		value;
	
	/** ���̃J�[�h�̃X�[�g */
	private int		suit;
	
	/**
	 * �\�������ǂ����������܂��B�������A�������ł��l�̎擾�Ȃǂ͉\�Ȃ��߁A
	 * �Ӗ��t���͓��ɂ���܂���B
	 * �l�̎擾���̂�s�\�ɂ������ꍇ�AUnspecified �̘g�g�݂��g�p���ĉ������B
	 */
	private boolean	isHead = true;
	
	/**
	 * ���� Card �̏������� holder �ł��B
	 * holder �́A���ׂĂ� Card �C���X�^���X���ێ����Ă��� Card �̏W�܂�
	 * (�ʏ�� 1 deck 52 ��)��ێ����Ă��܂��B
	 * holder �́APacketFactory �N���X�� provideDeck �֘A���\�b�h������
	 * ��������A���ƂŕύX���邱�Ƃ͂ł��܂���B
	 */
	private Packet holder;
	
/*-------------
 * Constructor
 */
	/**
	 * Unspecified Card ���쐬���܂��B
	 * Unspecified Card �͏�����ԂƂ��āA�������ɐݒ肳��܂��B
	 */
	public CardImpl() {
		value	= UNSPECIFIED;
		suit	= UNSPECIFIED;
		isHead	= false;
	}
	
	/**
	 * �w�肳�ꂽ�X�[�g�A�o�����[�� Card ���쐬���܂��B
	 * ������Ԃ͕\�����ł��B
	 *
	 * @param		suit		Card.JOKER, Card.SPADE, .., Card.CLUB
	 * @param		value		Card.JOKER, 1, .., 13(== Card.KING)
	 *
	 * @see			ys.game.card.Card#SPADE
	 * @see			ys.game.card.Card#HEART
	 * @see			ys.game.card.Card#DIAMOND
	 * @see			ys.game.card.Card#CLUB
	 * @see			ys.game.card.Card#JOKER
	 */
	public CardImpl(int suit, int value) {
		if ((suit < 0)||(suit > 4))
			throw new IllegalArgumentException(
						"�X�[�g�̒l�� " + suit + "�͎w��ł��܂���B");
		if ((value < 0)||(value > 13))
			throw new IllegalArgumentException(
						"�o�����[�̒l�� " + value + "�͎w��ł��܂���B");
		
		if (suit  == JOKER) value = JOKER;
		if (value == JOKER) suit  = JOKER;
		
		this.suit	= suit;
		this.value	= value;
		isHead		= true;
	}
	
	/**
	 * �w�肳�ꂽ�J�[�h�Ɠ���̓��e�� Card ��V�K�ɍ쐬���܂��B
	 * holder, �\���̏����R�s�[����܂��B
	 * 
	 * @param		card		�쐬�������J�[�h
	 */
	public CardImpl(Card card) {
		try {
			int suit	= card.getSuit();
			int value	= card.getValue();
			
			this.suit	= suit;
			this.value	= value;
		}
		catch (UnspecifiedException e) {
			value	= UNSPECIFIED;
			suit	= UNSPECIFIED;
		}
		isHead = card.isHead();
		setHolder(card.getHolder()); // Card �ɂ����āAequals �͓��e�̈�v�Ȃ̂ŁA
		// ���̃I�u�W�F�N�g���g�� holder �ɓo�^����Ă���C���X�^���X�łȂ��Ă��悢�B
	}
/*-------------------
 * implements (Card)
 */
	/**
	 * �o�����[�̓��e��Ԃ��B
	 * ���m�̃J�[�h�ł������ꍇ�AUnspecifiedException ���X���[�����B
	 *
	 * @return		Card.ACE(=1), Card.TWO(=2), �c�c, Card.KING(=13),
	 *				Card.JOKER(=0)
	 */
	public int getValue() {
		if (value == UNSPECIFIED)
			throw new UnspecifiedException("�J�[�h���e�����m�̂��߁AgetValue()�͍s���܂���");
		return value;
	}
	
	/**
	 * �X�[�g�̓��e��Ԃ��B
	 * ���m�̃J�[�h�ł������ꍇ�AUnspecifiedException ���X���[�����B
	 *
	 * @return		Card.SPADE(=1), Card.HEART(=2), Card.DIAMOND(=3),
	 *              Card.CLUB(=4), Card.JOKER(=0)
	 *
	 * @see			ys.game.card.Card#SPADE
	 * @see			ys.game.card.Card#HEART
	 * @see			ys.game.card.Card#DIAMOND
	 * @see			ys.game.card.Card#CLUB
	 * @see			ys.game.card.Card#JOKER
	 */
	public int getSuit() {
		if (suit == UNSPECIFIED)
			throw new UnspecifiedException("�J�[�h���e�����m�̂��߁AgetSuit()�͍s���܂���");
		return suit;
	}
	
	/**
	 * Unspecified�̃J�[�h�ɑ΂��ăX�[�g�ƃo�����[��ݒ肵�܂��B
	 * ���łɓ���� holder �Ɏw�肳�ꂽ�X�[�g�A�o�����[�̃J�[�h���܂܂��
	 * ����ꍇ�AAlreadySpecifiedException ���X���[����܂��B
	 *
	 * @param		suit		�X�[�g�̎w��(Card.SPADE �Ȃ�)
	 * @param		value		�o�����[�̎w��
	 * 
	 * @see			ys.game.card.Card#SPADE
	 * @see			ys.game.card.Card#HEART
	 * @see			ys.game.card.Card#DIAMOND
	 * @see			ys.game.card.Card#CLUB
	 * @see			ys.game.card.Card#JOKER
	 * @see			ys.game.card.Card#JACK
	 * @see			ys.game.card.Card#QUEEN
	 * @see			ys.game.card.Card#KING
	 */
	public void specify(int suit, int value) {
		if (this.value != UNSPECIFIED)
			throw new AlreadySpecifiedException(
						"���̃J�[�h��Specified�ł��B");
		
		int index = 0;
		while (index < holder.size()) {
			index = holder.indexOf(suit, value, index);
			if (index == -1)
				throw new AlreadySpecifiedException(
						"���łɂ��̃J�[�h���e�͎g�p����Ă��邩�Aholder �Ɋ܂܂�Ȃ����e�ł��B");
			if (!holder.peek(index).isHead()) break;
			index++;
		}
		
		this.suit	= suit;
		this.value	= value;
		
		holder.peek(index).turn(true);
	}
	
	/**
	 * Unspecified�̃J�[�h�ɑ΂��ăX�[�g�ƃo�����[��ݒ肵�܂��B
	 * ���łɓ���� holder �Ɏw�肳�ꂽ�X�[�g�A�o�����[�̃J�[�h���܂܂��
	 * ����ꍇ�AAlreadySpecifiedException ���X���[����܂��B
	 * 
	 * @param		card		�ݒ肷��J�[�h�̓��e
	 */
	public void specify(Card card) {
		specify(card.getSuit(), card.getValue());
	}
	
	/**
	 * ���̃J�[�h���w�肳�ꂽ�X�[�g�A�o�����[�� specify �\�ł��邩
	 * �e�X�g���܂��B
	 */
	public boolean isSpecifiableAs(int suit, int value) {
		if (this.value != UNSPECIFIED) return false;
		
		int index = 0;
		while (index < holder.size()) {
			index = holder.indexOf(suit, value, index);
			if (index == -1) return false;
			if (!holder.peek(index).isHead()) break;
			index++;
		}
		return true;
	}
	
	/**
	 * ���̃J�[�h���w�肳�ꂽ�J�[�h�̓��e�� specify �\�ł��邩
	 * �e�X�g���܂��B
	 */
	public boolean isSpecifiableAs(Card card) {
		return isSpecifiableAs(card.getSuit(), card.getValue());
	}
	
	/**
	 * ���̃J�[�h�� Unspecified �ɂ��܂��B
	 */
	public void invalidate() {
		int index = 0;
		while (index < holder.size()) {
			index = holder.indexOf(suit, value, index);
			if (index == -1)
			throw new InternalError(
					"holder �̏�ԕs���ł��Bholder�ɂ��̃J�[�h���܂܂�Ă��܂���B");
			if (holder.peek(index).isHead()) break;
			index++;
		}
		
		suit  = UNSPECIFIED;
		value = UNSPECIFIED;
		
		holder.peek(index).turn(false);
	}
	
	/**
	 * ���\���Ђ�����Ԃ�
	 */
	public void turn() {
		isHead = !isHead;
	}
	
	/**
	 * ���\���w�肵�܂��B
	 */
	public void turn(boolean head) {
		isHead = head;
	}
	
	/**
	 * �\�ł��邩�e�X�g���܂��B
	 */
	public boolean isHead() {
		return isHead;
	}
	
	/**
	 * Unspecified �J�[�h�ł��邩�e�X�g���܂��B
	 */
	public boolean isUnspecified() {
		return (suit == UNSPECIFIED);
	}
	
	/**
	 * ���̃J�[�h�̏�������f�b�L��Ԃ��܂��B
	 *
	 * @return		���� Card �̏������� holder
	 */
	public Packet getHolder() {
		return holder;
	}
	
	/**
	 * ���̃J�[�h�̏�������f�b�L���w�肵�܂��B
	 * �J�[�h�͐�����A��x�����{���\�b�h���Ă΂�A�����f�b�L���w�肳��܂��B
	 */
	public void setHolder(Packet holder) {
		if (this.holder == null) this.holder = holder;
	}
	
/*-----------
 * overrides
 */
	/**
	 * ������ނ̃J�[�h�ł��邩�e�X�g���܂��B
	 * Unspecified Card �ɑ΂��ẮAspecifiable �ł���Ƃ� UnspecifiedException
	 * ���X���[����܂��B
	 */
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof Card)) return false;
		
		Card c = (Card)o;
		
		if (this.isUnspecified()) {
			if (c.isUnspecified())
				throw new UnspecifiedException();
			
			if (this.isSpecifiableAs(c))
				throw new UnspecifiedException();
			
			return false;
		}
		if (c.isUnspecified()) {
			if (c.isSpecifiableAs(this))
				throw new UnspecifiedException();
			
			return false;
		}
		
		return ( (getValue() == c.getValue())&&(getSuit() == c.getSuit() ) );
	}
	
	/**
	 * ������\�����擾���܂��B
	 */
	public String toString() {
		String s;
		if (this.isHead()) s = "/"; else s = "_";
		if (this.isUnspecified()) return s+"??";
		
		switch (getSuit()) {
			case UNSPECIFIED:
				s += "*";
				break;
			case SPADE:
				s += "S";
				break;
			case HEART:
				s += "H";
				break;
			case DIAMOND:
				s += "D";
				break;
			case CLUB:
				s += "C";
				break;
			default:
				s += "Jo";
		}
		switch (getValue()) {
			case UNSPECIFIED:
			case JOKER:
				break;
			case ACE:
				s = s + "A";
				break;
			case 10:
				s = s + "T";
				break;
			case JACK:
				s = s + "J";
				break;
			case QUEEN:
				s = s + "Q";
				break;
			case KING:
				s = s + "K";
				break;
			default:
				s = s + getValue();
		}
		return s; // + "]";
	}
}
