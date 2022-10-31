package ys.game.card;

/**
 * �f�b�L�A�n���h�Ƃ�������ʂ̃J�[�h�̏W�܂������킷�N���X�ł��B
 * Packet�́A�X�^�b�N�ƂȂ��Ă���FILO(First in Last out)�^�ł��B
 *
 * @version		a-release	3, December 2000
 * @author		Yusuke Sasaki
 */
public interface Packet extends Cloneable { // extends java.util.SortedSet ( after JDK 1.2 )
	
	/**
	 * �����̃J�[�h���܂܂�Ă��邩�J�E���g���܂��B
	 *
	 * @return		�܂�ł���J�[�h�̖���
	 */
	int size();
	
	/**
	 * ������UnspecifiedCard ���܂܂�Ă��邩�J�E���g���܂��B
	 *
	 * @return		�܂�ł��� UnspecifiedCard �̖���
	 */
	int countUnspecified();
	
	/**
	 * �w�肳�ꂽ�X�[�g���������邩�J�E���g���܂��B
	 * UnspecifiedCard �̓J�E���g���܂���B
	 *
	 * @return		�w�肳�ꂽ�X�[�g�̖���
	 */
	int countSuit(int suit);
	
	/**
	 * �e�X�[�c���������邩�J�E���g���A�z��̌`���ŕԂ��܂��B
	 * �Y�����ACard.SPADE �Ȃǂ̗v�f�ɖ������i�[����܂��B
	 * Joker ���܂߁A�v�f�� 5 �̔z�񂪕Ԃ�܂��B
	 * UnspecifiedCard �̓J�E���g���܂���B
	 *
	 * @return		�e�X�[�g�̖���
	 */
	int[] countSuits();
	
	/**
	 * �w�肵���J�[�h�̃C���f�b�N�X���擾���܂��B
	 * �擾���ꂽ�C���f�b�N�X�́Adraw(int)�Ȃǂ̃��\�b�h�Ŏg�p���܂��B
	 * �w�肳�ꂽ�J�[�h���܂܂Ȃ��ꍇ�A-1 ��Ԃ��܂��B
	 * �J�[�h�̈�v����́ACard �N���X�� equals() ���g�p���čs���Ă��܂��B
	 */
	int indexOf(Card c);
	
	/**
	 * �w�肳�ꂽ�X�[�g�A�o�����[�����J�[�h�̃C���f�b�N�X���擾���܂��B
	 * �Y��������̂����݂��Ȃ��ꍇ�A-1 ��Ԃ��܂��B
	 */
	int indexOf(int suit, int value);
	
	/**
	 * �w�肳�ꂽ�X�[�g�A�o�����[�����J�[�h�̃C���f�b�N�X���擾���܂��B
	 * ������ startIndex ����J�n���܂��B
	 * �Y��������̂����݂��Ȃ��ꍇ�A-1 ��Ԃ��܂��B
	 */
	int indexOf(int suit, int value, int startIndex);
	
	/**
	 * �w�肳�ꂽ�J�[�h���܂܂�Ă��邩�e�X�g���܂��B
	 */
	boolean contains(Card card);
	
	/**
	 * �w�肳�ꂽ�J�[�h���܂܂�Ă��邩�e�X�g���܂��B
	 */
	public boolean contains(int suit, int value);
	
	/**
	 * UnspecifiedCard ���܂܂�Ă��邩�e�X�g���܂��B
	 */
	boolean containsUnspecified();
	
	/**
	 * �w�肳�ꂽ�X�[�g�̃J�[�h���܂܂�Ă��邩�e�X�g���܂��B
	 */
	boolean containsSuit(int suit);
	
	/**
	 * �w�肳�ꂽ�o�����[�̃J�[�h���܂܂�Ă��邩�e�X�g���܂��B
	 */
	boolean containsValue(int value);
	
	/**
	 * �w�肳�ꂽ�J�[�h�̏W�܂�̂����ꂩ�̃J�[�h���܂܂�Ă��邩�e�X�g���܂��B
	 */
	boolean intersects(Packet packet);
	
	/**
	 * �w�肳�ꂽ�J�[�h�̏W�܂���܂�ł��邩�e�X�g���܂��B
	 */
	boolean contains(Packet packet);
	
	/**
	 * �J�[�h������Packet�̈�ԏ�ɒǉ����܂��B
	 * ���� holder �̃J�[�h�݂̂� add �\�ł��B����� holder �ɑ����Ă����
	 * ����ނ̃J�[�h���ǉ��ł��܂��B
	 * null �I�u�W�F�N�g��}�����悤�Ƃ���Ɖ����s���܂���B
	 *
	 * @param		c		�ǉ�����J�[�h
	 */
	void add(Card c);
	
	/**
	 * �w�肳�ꂽ Packet ������ Packet �̈�ԏ�ɒǉ����܂��B
	 *
	 * @param		packet	�ǉ����� Packet
	 */
	void add(Packet packet);
	
	/**
	 * �J�[�h������Packet�̎w�肳�ꂽ�ʒu�ɑ}�����܂��B
	 * insertAt(c, size()) �� add(c) �Ɠ����̓�����s���܂��B
	 * null �I�u�W�F�N�g��}�����悤�Ƃ���Ɖ����s���܂���B
	 */
	void insertAt(Card c, int index);
	
	/**
	 * ��ԏ�̃J�[�h�������܂��B�����ꂽ�J�[�h�͖{Packet����폜����܂��B
	 * ���̑���́Adraw(size() - 1) ��(�J�[�h���܂܂Ȃ��ꍇ��������)�����ł��B
	 * �J�[�h���܂܂Ȃ� Packet �ɑ΂��Ė{���\�b�h���R�[������ƁAIllegalStateException
	 * ���X���[����܂��B
	 *
	 * @return		��ԏ�̃J�[�h
	 */
	Card draw();
	
	/**
	 * �w�肳�ꂽ�ԍ��̃J�[�h�������B�����ꂽ�J�[�h�͖{ Packet ����폜����܂��B
	 * �����Ƃ��Ė����Ȓl���w�肵���ꍇ�AIndexOutOfBoundsException ���X���[����܂��B
	 *
	 * @param		n		�ォ�牽�Ԗڂ̃J�[�h��������
	 * @return		�������J�[�h
	 */
	Card draw(int n);
	
	/**
	 * �w�肳�ꂽ�J�[�h�������܂��B(�����ꂽ�J�[�h).equals(�w��J�[�h) �ƂȂ�܂��B
	 * �C���X�^���X�̃J�e�S���[���قȂ��Ă���ꍇ�ȂǂŁA�u����́v�J�[�h����������
	 * �ꍇ�Adraw(int, int) �C���^�[�t�F�[�X���g�p���Ă��������B
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
	Card draw(Card c);
	
	/**
	 * �w�肳�ꂽ�J�[�h�������܂��B�����ꂽ�J�[�h�͖{ Packet ����폜����܂��B
	 */
	Card draw(int suit, int value);
	
	/**
	 * UnspecifiedCard �������܂��B�����ꂽ�J�[�h�͖{ Packet ����폜����܂��B
	 */
	Card drawUnspecified();
	
	/**
	 * ��ԏ�̃J�[�h��`���܂��B�`���ꂽ�J�[�h�͖{ Packet ����폜����܂���B
	 * ���̑���́A�J�[�h���܂܂�Ȃ��ꍇ�������� peek(size() - 1)�Ɠ����ł��B
	 * ���� Packet ����̏ꍇ�Anull ���Ԃ�܂��B
	 *
	 * @return		��ԏ�̃J�[�h�ւ̎Q��
	 */
	Card peek();
	
	/**
	 * �w�肳�ꂽ�ԍ��̃J�[�h��`���܂��B�`���ꂽ�J�[�h�͖{ Packet ����폜����܂���B
	 *
	 * @param		n		�ォ�牽�Ԗڂ̃J�[�h��`����
	 * @return		�������J�[�h�ւ̎Q��
	 */
	Card peek(int n);
	
	/**
	 * �w�肳�ꂽ�J�[�h�ւ̎Q�Ƃ��擾���܂��B
	 */
	Card peek(int suit, int value);
	
	/**
	 * Unspecified Card (�̂P��)�ւ̎Q�Ƃ��擾���܂��B
	 */
	Card peekUnspecified();
	
	/**
	 * �{ Packet �̎w�肳�ꂽ�X�[�c�𔲂��o���܂��B
	 * �����o���ꂽ�J�[�h�͖{�p�P�b�g����폜����܂���B
	 *
	 * @param		suit		���o�������X�[�c
	 * @return		��o���ꂽ Packet
	 *
	 */
	Packet subpacket(int suit);
	
	/**
	 * arrange()�ɂ���ĕ��ёւ���ۂ̃J�[�h�����K����ݒ肵�܂��B
	 *
	 * @param		order		���я��̋K��
	 */
	void setCardOrder(CardOrder order);
	
	/**
	 * ���� Packet �Ŏg�p���Ă���J�[�h�����K�����擾���܂��B
	 *
	 * @return		���я��̋K��
	 */
	CardOrder getCardOrder();
	
	/**
	 * �J�[�h�̕��ёւ����s���܂��B
	 * setCardOrder(CardOrder) �Ŏw�肳�ꂽ�J�[�h�����K���ɏ]���āA������̂��̂�
	 * ��ɗ���悤�ɕ��ёւ��܂��B
	 *
	 * @see			ys.game.card.Packet#setCardOrder
	 * @see			ys.game.card.CardOrder
	 */
	void arrange();
	
	/**
	 * �J�[�h�������_���ɃV���b�t�����܂��B
	 * �V���b�t���̎d���͎����N���X�ŋK�肳��܂��B
	 */
	void shuffle();
	
	/**
	 * �{ Packet �Ɋ܂܂�Ă��Ȃ��J�[�h�̏W��(�c��J�[�h)���擾���܂��B
	 */
	Packet complement();
	
	/**
	 * �w�肳�ꂽ Pakcet �Ɩ{ Packet �̋��ʕ������擾���܂��B
	 * ���ʂ� PacketImpl �̃C���X�^���X�ł���A������\������ Card �C���X�^���X��
	 * �{ Packet �̂��̂��g�p����܂��B
	 * 
	 * @param		target		���ʕ��������Ώ�
	 * @return		���� Packet �� target �̋��ʕ���
	 */
	Packet intersection(Packet target);

	/**
	 * �ꖇ����ׂɒ��o���܂��B
	 */
//	Card drawAtRandom();
	
	/**
	 * �ꖇ����ׂɉ{�����܂��B
	 */
//	Card peekAtRandom();
	
	/**
	 * ���������߂܂��B����� add(Packet) �Ŏ�������܂����B
	 */
//	Packet union(Packet target);
	
	/**
	 * �������߂܂��B
	 * this �Ɋ܂܂�Ă��� Packet ����w�� Packet �Ɋ܂܂����̂����������̂��ԋp����܂��B
	 */
	Packet sub(Packet target);
	
	/**
	 * �܂܂�Ă��邷�ׂẴJ�[�h�̕\�����t�ɂ��܂��B
	 */
	void turn();
	
	/**
	 * �܂܂�Ă��邷�ׂẴJ�[�h�̕\�����w�肵�܂��B
	 */
	void turn(boolean head);
}
