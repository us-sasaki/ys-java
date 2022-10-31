package ys.game.card;
/*
 * 2001/ 7/22  shuffle �Ŏg�p���闐�����N���X�ϐ��ɕύX
 */

import java.util.Random;

/**
 * �f�b�L�A�n���h�Ƃ�������ʂ̃J�[�h�̏W�܂������킵�܂��B
 * PacketImpl �́A�X�^�b�N�ƂȂ��Ă���AFILO�^(First In Last Out)�ł��B
 *
 * @version		a-release	22, July 2001
 * @author		Yusuke Sasaki
 */
public class PacketImpl implements Packet {
	protected static Random random;
	
	/** �Ō�� index ����ԏ� */
	protected Card[]	content;
	protected int		size;
	protected CardOrder	cardOrder;
	protected Packet	holder;
	
/*-------------
 * Constructor
 */
	public PacketImpl() {
		content		= new Card[0];
		size		= 0;
		cardOrder	= new NaturalCardOrder();
		holder		= null;
	}
	
	/**
	 * �w�肳�ꂽ Packet �Ɠ���̓��e�̃J�[�h��ێ����� PacketImpl ��
	 * �C���X�^���X�𐶐����܂��B�ێ�����J�[�h�̃C���X�^���X�̓R�s�[����
	 * Packet �̂��̂��g�p����܂��B(Impl �J�e�S���[�̃C���X�^���X�ɕϊ����Ȃ�)
	 * ���������āAholder ���R�s�[���̂��̂Ɠ���ƂȂ�܂��B
	 * CardOrder �ɂ��Ă��R�s�[���s���܂��B
	 *
	 * @param		packet		�R�s�[���� Packet
	 */
	public PacketImpl(Packet packet) {
		this();
		int size = packet.size();
		for (int i = 0; i < size; i++) {
			Card card = packet.peek(i);
			add(card);
		}
		cardOrder = packet.getCardOrder();
	}
	
/*---------------
 * class methods
 */
	/**
	 * shuffle() �Ŏg�p���闐�����ݒ肵�܂��B
	 * ��܂����n��� Random �I�u�W�F�N�g��ݒ肷�邱�Ƃɂ���āAshuffle() �̌��ʂ�
	 * �Č������������邱�Ƃ��ł��܂��B
	 *
	 * @param		r		����g�p���� Random �I�u�W�F�N�g
	 */
	public static void setRandom(Random r) {
		random = r;
	}
	
/*------------------
 * instance methods
 */
	
	/**
	 * �����̃J�[�h���܂܂�Ă��邩�J�E���g���܂��B
	 *
	 * @return		�܂�ł���J�[�h�̖���
	 */
	public int size() {
		return size;
	}
	
	/**
	 * ������UnspecifiedCard ���܂܂�Ă��邩�J�E���g���܂��B
	 *
	 * @return		�܂�ł��� UnspecifiedCard �̖���
	 */
	public int countUnspecified() {
		int cnt = 0;
		for (int i = 0; i < size; i++) {
			if (content[i].isUnspecified()) cnt++;
		}
		return cnt;
	}
	
	/**
	 * �w�肳�ꂽ�X�[�g���������邩�J�E���g���܂��B
	 * UnspecifiedCard �̓J�E���g���܂���B
	 
	 * @return		�w�肳�ꂽ�X�[�g�̖���
	 */
	public int countSuit(int suit) {
		int cnt = 0;
		for (int i = 0; i < size; i++) {
			try {
				if (content[i].getSuit() == suit) cnt++;
			} catch (UnspecifiedException ignored) { }
		}
		return cnt;
	}
	
	/**
	 * �e�X�[�c���������邩�J�E���g���A�z��̌`���ŕԂ��܂��B
	 * �Y�����ACard.SPADE �Ȃǂ̗v�f�ɖ������i�[����܂��B
	 * Joker ���܂߁A�v�f�� 5 �̔z�񂪕Ԃ�܂��B
	 * UnspecifiedCard �̓J�E���g���܂���B
	 *
	 * @return		�e�X�[�g�̖���
	 */
	public int[] countSuits() {
		int[] result = new int[5];
		for (int i = 0; i < size; i++) {
			try {
				result[content[i].getSuit()]++;
			} catch (UnspecifiedException ignored) { }
		}
		return result;
	}
	
	/**
	 * �w�肳�ꂽ�J�[�h�̃C���f�b�N�X���擾���܂��B
	 * �w�肳�ꂽ�J�[�h���܂܂Ȃ��ꍇ�A-1 ��Ԃ��܂��B
	 * �J�[�h�̈�v����́ACard �N���X�� equals() ���g�p���čs���Ă��܂��B
	 *
	 * @param		c		�C���f�b�N�X���擾�������J�[�h(not Unspecified)
	 */
	public int indexOf(Card c) {
		Card unspecified = null;
		
		for (int i = 0; i < size; i++) {
			try {
				if (content[i].equals(c)) return i;
			}
			catch (UnspecifiedException e) {
				unspecified = content[i];
			}
		}
		if (unspecified != null)
			if (unspecified.isSpecifiableAs(c))
				throw new UnspecifiedException();
		
		return -1;
	}
	
	/**
	 * �w�肳�ꂽ�X�[�g�A�o�����[�̃J�[�h�̃C���f�b�N�X���擾���܂��B
	 * �Y��������̂����݂��Ȃ��ꍇ�A-1 ��Ԃ��܂��B
	 */
	public int indexOf(int suit, int value) {
		Card unspecified = null;
		
		for (int i = 0; i < size; i++) {
			try {
				if ( (content[i].getValue() == value)
						&& (content[i].getSuit() == suit) ) return i;
			}
			catch (UnspecifiedException e) {
				unspecified = content[i];
			}
		}
		if (unspecified != null)
			if (unspecified.isSpecifiableAs(suit, value))
				throw new UnspecifiedException();
		
		return -1;
	}
	
	/**
	 * �w�肳�ꂽ�X�[�g�A�o�����[�����J�[�h�̃C���f�b�N�X���擾���܂��B
	 * ������ startIndex ����J�n���܂��B
	 * �Y��������̂����݂��Ȃ��ꍇ�A-1 ��Ԃ��܂��B
	 */
	public int indexOf(int suit, int value, int startIndex) {
		if (startIndex < 0)
			throw new IndexOutOfBoundsException("startIndex " + startIndex + " < 0");
		
		Card unspecified = null;
		
		for (int i = startIndex; i < size; i++) {
			try {
				if ( (content[i].getValue() == value)
						&& (content[i].getSuit() == suit) ) return i;
			}
			catch (UnspecifiedException e) {
				unspecified = content[i];
			}
		}
		if (unspecified != null)
			if (unspecified.isSpecifiableAs(suit, value))
				throw new UnspecifiedException();
		
		return -1;
	}
	
	/**
	 * �w�肳�ꂽ�J�[�h���܂܂�Ă��邩�e�X�g���܂��B
	 */
	public boolean contains(Card card) {
		return (indexOf(card) > -1);
	}
	
	/**
	 * �w�肳�ꂽ�J�[�h���܂܂�Ă��邩�e�X�g���܂��B
	 */
	public boolean contains(int suit, int value) {
		return (indexOf(suit, value) > -1);
	}
	
	/**
	 * UnspecifiedCard ���܂܂�Ă��邩�e�X�g���܂��B
	 */
	public boolean containsUnspecified() {
		for (int i = 0; i < size; i++) {
			if (content[i].isUnspecified()) return true;
		}
		return false;
	}
	
	/**
	 * �w�肳�ꂽ�X�[�g�̃J�[�h���܂܂�Ă��邩�e�X�g���܂��B
	 */
	public boolean containsSuit(int suit) {
		boolean containsUnspecified = false;
		if ( (suit < 0)||(suit > 4) ) return false;
		
		for (int i = 0; i < size; i++) {
			if (content[i].isUnspecified()) containsUnspecified = true;
			else if (content[i].getSuit() == suit) return true;
		}
		
		if (containsUnspecified) {
			if (holder == null) return false;
			//
			// holder �Ɏw�肳�ꂽ�X�[�c�̂��̂Ŏg�p����ĂȂ����̂�
			// ����ꍇ�CUnspecifiedException
			//
			for (int i = 0; i < holder.size(); i++) {
				Card c = holder.peek(i);
				if ( (c.getSuit() == suit)
						&& (!c.isHead()) ) throw new UnspecifiedException();
			}
		}
		
		return false;
	}
	
	/**
	 * �w�肳�ꂽ�o�����[�̃J�[�h���܂܂�Ă��邩�e�X�g����B
	 */
	public boolean containsValue(int value) {
		boolean containsUnspecified = false;
		if ( (value < 0)||(value > 13) ) return false;
		
		for (int i = 0; i < size; i++) {
			if (content[i].isUnspecified()) containsUnspecified = true;
			else if (content[i].getValue() == value) return true;
		}
		
		if (containsUnspecified) {
			if (holder == null) return false;
			//
			// holder �Ɏw�肳�ꂽ�X�[�c�̂��̂Ŏg�p����ĂȂ����̂�
			// ����ꍇ�CUnspecifiedException
			//
			for (int i = 0; i < holder.size(); i++) {
				Card c = holder.peek(i);
				if ( (c.getValue() == value)
						&& (!c.isHead()) ) throw new UnspecifiedException();
			}
		}
		
		return false;
	}
	
	/**
	 * �w�肳�ꂽ�J�[�h�̏W�܂�̂����ꂩ�̃J�[�h���܂܂�Ă��邩�e�X�g����B
	 */
	public boolean intersects(Packet packet) {
		boolean mayContainUnspecified = false;
		for (int i = 0; i < packet.size(); i++) { // packet �� synchronized ���������H
			Card card = packet.peek(i);
			try {
				if (contains(card)) return true;
			}
			catch (UnspecifiedException e) {
				mayContainUnspecified = true;
			}
		}
		if (mayContainUnspecified)
			throw new UnspecifiedException();
		
		return false;
	}
	
	/**
	 * �w�肳�ꂽ�J�[�h�̏W�܂���܂�ł��邩�e�X�g����B
	 */
	public boolean contains(Packet packet) {
		int containsUnspecified = 0;
		
		if (size < packet.size()) return false;
		
		for (int i = 0; i < packet.size(); i++) {
			Card card = packet.peek(i);
			try {
				if (!contains(card)) return false;
			}
			catch (UnspecifiedException e) {
				containsUnspecified++;
			}
		}
		if ( (containsUnspecified > 0)
				&&(countUnspecified() >= containsUnspecified) )
			throw new UnspecifiedException();
		
		return true;
	}
	
	/**
	 * �J�[�h������Packet�̈�ԏ�ɒǉ����܂��B
	 * ���� holder �̃J�[�h�݂̂� add �\�ł��B����� holder �ɑ����Ă����
	 * ����ނ̃J�[�h���ǉ��ł��܂��B
	 *
	 * @param		c		�ǉ�����J�[�h
	 */
	public void add(Card c) {
		if ( (!c.isUnspecified())&&(contains(c)) ) return;
		
		if (holder != null) {
			if ( holder != c.getHolder() ) {
				throw new CardRuntimeException(
					"holder �̈قȂ�J�[�h�͒ǉ��ł��܂���B");
			}
		}
		else {
			holder = c.getHolder();
		}
		
		if (content.length == size) {		// < �̂Ƃ��͂��肦�Ȃ�
			int newSize = content.length * 2 + 1;
			Card[] tmp = new Card[newSize];
			System.arraycopy(content, 0, tmp, 0, content.length);
			content = tmp;
		}
		content[size] = c;
		size++;
		
	}
	
	/**
	 * �w�肳�ꂽ Packet ������ Packet �̈�ԏ�ɒǉ����܂��B
	 *
	 * @param		packet	�ǉ����� Packet
	 */
	public void add(Packet packet) {
		for (int i = 0; i < packet.size(); i++) {
			add(packet.peek(i)); // �x������
		}
	}
	
	/**
	 * �J�[�h������Packet�̎w�肳�ꂽ�ʒu�ɑ}�����܂��B
	 * insertAt(c, size()) �� add(c) �Ɠ����̓�����s���܂��B
	 */
	public void insertAt(Card c, int index) {
		if (index < 0) throw new IndexOutOfBoundsException("�l�͈͊O�ł�:"+index);
		if (index > size) index = size;
		
		Card[] src = content;
		Card[] dst;
		
		if (holder != null) {
			if ( holder != c.getHolder() ) {
				throw new CardRuntimeException(
					"holder �̈قȂ�J�[�h�͒ǉ��ł��܂���B");
			}
		}
		else {
			holder = c.getHolder();
		}
		
		if (content.length == size) {
			int newSize = content.length * 2;
			dst = new Card[newSize];
			if (index > 0)
				System.arraycopy(content, 0, dst, 0, index);
		}
		else {
			dst = content;
		}
		if (size > index)
			System.arraycopy(src, index, dst, index + 1, size - index);
		dst[index] = c;
		
		content = dst;
		size++;
	}
	
	/**
	 * ��ԏ�̃J�[�h�������܂��B�����ꂽ�J�[�h�͖{Packet����폜����܂��B
	 * ���̑���́Adraw(size() - 1) ��(�J�[�h���܂܂Ȃ��ꍇ��������)�����ł��B
	 * �J�[�h���܂܂Ȃ� Packet �ɑ΂��Ė{���\�b�h���R�[������ƁAIllegalStateException
	 * ���X���[����܂��B
	 *
	 * @return		��ԏ�̃J�[�h
	 */
	public Card draw() {
		if (size == 0)
			throw new IllegalStateException("Empty Packet �ɑ΂��� draw �����s���܂����B");
		
		Card drawn = content[size - 1];
		content[size - 1] = null;
		size--;
		
		return drawn;
	}
	
	/**
	 * �w�肳�ꂽ�ԍ��̃J�[�h�������B�����ꂽ�J�[�h�͖{ Packet ����폜����܂��B
	 * �����Ƃ��Ė����Ȓl���w�肵���ꍇ�AIndexOutOfBoundsException ���X���[����܂��B
	 *
	 * @param		n		�ォ�牽�Ԗڂ̃J�[�h��������
	 * @return		�������J�[�h
	 */
	public Card draw(int n) {
		if ( (n < 0)||(n >= size) )
			throw new IndexOutOfBoundsException("draw �̈����͖����ł��B");
		
		Card drawn = content[n];
		
		System.arraycopy(content, n+1, content, n, size - n - 1);
		
		content[size - 1] = null;
		size--;
		
		return drawn;
	}
	
	/**
	 * �w�肳�ꂽ�J�[�h(�Ɠ���ނ̃J�[�h)�������܂��B
	 * �����ꂽ�J�[�h�͖{ Packet ����폜����܂��B
	 * ����ނ̃J�[�h���Ȃ��ꍇ�Anull ���Ԃ���܂��B
	 * �ԋp����� Card �̃C���X�^���X�͈����Ɏw�肵�� Card �̃C���X�^���X�ƈ��
	 * �ɈقȂ�܂��B
	 * �ԋp����� Card �͂˂ɂ��� Packet �Ɋ܂܂��C���X�^���X�ł���̂ɑ΂��A
	 * �����Ɏw�肷�� Card �͂��̌���ł͂Ȃ�����ł��B
	 *
	 * @param		c		���������J�[�h
	 * @return		���� Packet �Ɋ܂܂��w�肳�ꂽ�J�[�h�Ɠ���̃J�[�h�ւ̎Q��
	 */
	public Card draw(Card c) {
		int index = indexOf(c);
		if (index == -1) return null;
		return draw(index);
	}
	
	/**
	 * �w�肳�ꂽ�J�[�h�������܂��B�����ꂽ�J�[�h�͖{ Packet ����폜����܂��B
	 */
	public Card draw(int suit, int value) {
		int index = indexOf(suit, value);
		if (index == -1) return null;
		return draw(index);
	}
	
	/**
	 * UnspecifiedCard �������܂��B
	 * ���� Packet �� UnspecifiedCard ���܂܂Ȃ��ꍇ�Anull ���Ԃ�܂��B
	 *
	 * @return		���� Packet �Ɋ܂܂�� Unspecified Card (�̂ЂƂ�)
	 */
	public Card drawUnspecified() {
		for (int i = 0; i < size; i++) {
			if (content[i].isUnspecified()) return draw(i);
		}
		
		return null;
	}
	
	/**
	 * ��ԏ�̃J�[�h��`���܂��B�`���ꂽ�J�[�h�͖{ Packet ����폜����܂���B
	 * ���̑���́A�J�[�h���܂܂�Ȃ��ꍇ�������� peek(size() - 1)�Ɠ����ł��B
	 * ���� Packet ����̏ꍇ�Anull ���Ԃ�܂��B
	 *
	 * @return		��ԏ�̃J�[�h�ւ̎Q��
	 */
	public Card peek() {
		if (size == 0) return null;
		return content[size - 1];
	}
	
	/**
	 * �w�肳�ꂽ�ԍ��̃J�[�h��`���܂��B�`���ꂽ�J�[�h�͖{ Packet ����폜����܂���B
	 *
	 * @param		n		�ォ�牽�Ԗڂ̃J�[�h��`����
	 * @return		�������J�[�h�ւ̎Q��
	 */
	public Card peek(int n) {
		if ( (n < 0) || (n >= size) )
			throw new IndexOutOfBoundsException(
						"���� Packet �ɂ�" + size + "���̃J�[�h���܂܂�A"
						+ n + "�Ԗڂ͎w��ł��܂���B");
		
		return content[n];
	}
	
	/**
	 * �w�肳�ꂽ�X�[�g�A�o�����[�����J�[�h���擾���܂��B
	 * �Y��������̂��Ȃ��ꍇ�Anull ���Ԃ�܂��B
	 */
	public Card peek(int suit, int value) {
		int index = indexOf(suit, value);
		if (index == -1) return null;
		return content[index];
	}
	
	/**
	 * UnspecifiedCard �ւ̎Q�Ƃ��擾���܂��B
	 * ���� Packet �� UnspecifiedCard ���܂܂Ȃ��ꍇ�Anull ���Ԃ�܂��B
	 *
	 * @return		���� Packet �Ɋ܂܂�� Unspecified Card (�̂ЂƂ�)
	 */
	public Card peekUnspecified() {
		for (int i = 0; i < size; i++) {
			if (content[i].isUnspecified()) return peek(i);
		}
		
		return null;
	}
	
	/**
	 * �{ Packet �̎w�肳�ꂽ�X�[�c�𔲂��o���܂��B
	 * �����o���ꂽ�J�[�h�͖{�p�P�b�g����폜����܂���B
	 * Unspecified Card �͔����o���Ώۂ��珜�O����܂��B
	 *
	 * @param		suit		���o�������X�[�c
	 * @return		��o���ꂽ Packet
	 *
	 */
	public Packet subpacket(int suit) {
		Packet result = PacketFactory.newPacket();
		for (int i = 0; i < size; i++) {
			try {
				if (content[i].getSuit() == suit)
					result.add(content[i]);
			}
			catch (UnspecifiedException ignored) {
			}
		}
		
		return result;
	}
	
	/**
	 * arrange()�ɂ���ĕ��ёւ���ۂ̃J�[�h�����K����ݒ肵�܂��B
	 *
	 * @param		order		���я��̋K��
	 */
	public void setCardOrder(CardOrder order) {
		cardOrder = order;
	}
	
	/**
	 * ���� Packet �Ŏg�p���Ă���J�[�h�����K�����擾���܂��B
	 *
	 * @return		���я��̋K��
	 */
	public CardOrder getCardOrder() {
		return cardOrder;
	}
	
	/**
	 * �J�[�h�̕��ёւ����s���܂��B
	 * setCardOrder(CardOrder) �Ŏw�肳�ꂽ�J�[�h�����K���ɏ]���āA������̂��̂�
	 * ��ɗ���悤�ɕ��ёւ����܂��B
	 *
	 * @see			ys.game.card.Packet#setCardOrder
	 * @see			ys.game.card.CardOrder
	 */
	public void arrange() {
		for (int i = 0; i < size - 1; i++) {
			for (int j = i + 1; j < size; j++) {
				if (cardOrder.compare(content[i], content[j]) < 0) {
					Card tmp = content[i];
					content[i] = content[j];
					content[j] = tmp;
				}
			}
		}
	}

	/**
	 * �J�[�h�������_���ɃV���b�t������B
	 * PacketImpl �ł́Ajava.util.Random �ɂ��R���s���[�^�V���b�t�����s���B
	 */
	public void shuffle() {
		if (size == 0) return;
		
		if (random == null) random = new Random();
		Random r = random;
		
		Card[] tmp = new Card[size];
		
		for (int i = 0; i < size; i++) {
			int index;
			do {
				index = (r.nextInt() & 0x7fffffff) % size;
			} while ( tmp[index] != null );
			tmp[index] = content[i];
		}
		content = tmp;
	}
	
	/**
	 * �{ Packet �Ɋ܂܂�Ă��Ȃ��J�[�h�̏W��(�c��J�[�h)���擾����B
	 */
	public Packet complement() {
		Packet result	= new PacketImpl();
		Packet copy		= (Packet)clone();
		
		for (int i = 0; i < holder.size(); i++) {
			Card c = holder.peek(i);
			if (copy.contains(c)) copy.draw(c); // holder �ɂ͓���̃J�[�h���Q���ȏ゠�邱�Ƃ�����
			else result.add(c);
		}
		return result;
	}
	
	public void turn() {
		for (int i = 0; i < size; i++)
			content[i].turn();
	}
	
	public void turn(boolean head) {
		for (int i = 0; i < size; i++)
			content[i].turn(head);
	}
	
	/**
	 * �w�肳�ꂽ Pakcet �Ɩ{ Packet �̋��ʕ������擾���܂��B
	 * ���ʂ� PacketImpl �̃C���X�^���X�ł���A������\������ Card �C���X�^���X��
	 * �{ Packet �̂��̂��g�p����܂��B
	 * 
	 * @param		target		���ʕ��������Ώ�
	 * @return		���� Packet �� target �̋��ʕ���
	 */
	public Packet intersection(Packet target) {
		PacketImpl result = new PacketImpl();
		
		for (int i = 0; i < size; i++) {
			Card c = content[i];
			if (target.contains(c)) result.add(c);
		}
		
		return result;
	}
	
	public Packet sub(Packet target) {
		PacketImpl result = new PacketImpl();
		
		for (int i = 0; i < size; i++) {
			Card c = content[i];
			if (!target.contains(c)) result.add(c);
		}
		return result;
	}
	
/*-----------
 * overrides
 */
	/**
	 * ���� PacketImpl �̃R�s�[���쐬���܂��B
	 * �z��ɂ��Ă͐V���ɍ쐬����܂����A�ێ����Ă���Card�̃C���X�^���X
	 * �̃R�s�[�͍s���܂���B
	 *
	 * @return		����Packet�̃R�s�[�I�u�W�F�N�g
	 */
	public Object clone() {
		PacketImpl copy = new PacketImpl();
		copy.content	= new Card[size];
		
		System.arraycopy(content, 0, copy.content, 0, size);
		
		copy.size		= size;
		copy.cardOrder	= cardOrder;
		copy.holder		= holder;
		
		return copy;
	}
		
	/**
	 * ���� PacketImpl �̕�����\����Ԃ��܂��B
	 *
	 * @return		���� Packet �̕�����\��
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < size; i++) {
			s.append(content[i].toString());
		}
		return "{" + s.toString() + "}";
	}

}
