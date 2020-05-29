package ys.game.card;

/**
 * ���ۓI�ȃJ�[�h������킷�C���^�[�t�F�[�X�ł��B<BR>
 * <BR><B>Version�ɂ���</B></BR>
 * Version �́A�ȉ��̂悤�ɑJ�ڂ��܂��B<BR>
 * <TABLE BORDER=1>
 * <TR><TD>0.making</TD>
 * <TD>�\�[�X�R�[�h�����r���A�R���p�C���͂܂�</TD>
 * <TD>�R���p�C��������1</TD></TR>
 * <TR><TD>1.draft</TD>
 * <TD>�\�[�X�R�[�h�������A�R���p�C�����ʂ���</TD>
 * <TD>�P�̎���������2</TD></TR>
 * <TR><TD>2.unit tested</TD>
 * <TD>�P�́A�������͊�{�I�Ȍ����������ʂ���</TD>
 * <TD>��������������3</TD></TR>
 * <TR><TD>3.a-release</TD>
 * <TD>�����������ʂ���</TD>
 * <TD>�ȒP�ȏC����3�@��K�͂ȏC����4</TD></TR>
 * <TR><TD>4.remaking</TD>
 * <TD>�\�[�X�R�[�h�C����</TD>
 * <TD>�C���A�R���p�C��������1 or 2</TD></TR>
 * <TR><TD>5.release</TD>
 * <TD>3�̏�ԂŁA�����ɓn���ĕύX���Ȃ�����</TD>
 * <TD>�ȒP�ȏC����3�@��K�͂ȏC����4</TD></TR>
 * </TABLE>
 *
 * @version		a-release		17, April 2000
 * @author		Yusuke Sasaki
 */
public interface Card {

	// �X�[�c�̎��
	
	/**
	 * ���̃J�[�h�� Unspecified �ł��邱�Ƃ������萔�ł��B
	 * ���̒l���擾����邱�Ƃ͂���܂���B
	 */
	int UNSPECIFIED	= -1;
	
	/**
	 * �X�[�g�A�o�����[�ɓK�p�����A���̃J�[�h��
	 * �W���[�J�[�ł��邱�Ƃ������萔�ł��B
	 */
	int JOKER	= 0;
	
	/** �X�[�g�ɓK�p�����A�X�y�[�h�ł��邱�Ƃ������萔(=4)�ł��B */
	int SPADE	= 4;
	
	/** �X�[�g�ɓK�p�����A�n�[�g�ł��邱�Ƃ������萔(=3)�ł��B */
	int HEART	= 3;
	
	/** �X�[�g�ɓK�p�����A�_�C�A�����h�ł��邱�Ƃ������萔(=2)�ł��B */
	int DIAMOND = 2;
	
	/** �X�[�g�ɓK�p�����A�N���u�ł��邱�Ƃ������萔(=1)�ł��B */
	int CLUB	= 1;
	
	// ��
	/** �o�����[�ɓK�p�����A�G�[�X�ł��邱�Ƃ������萔(=1)�ł��B */
	int ACE		= 1;
	
	/** �o�����[�ɓK�p�����A�W���b�N�ł��邱�Ƃ������萔(=11)�ł��B */
	int JACK	= 11;
	
	/** �o�����[�ɓK�p�����A�N�C�[���ł��邱�Ƃ������萔(=12)�ł��B */
	int QUEEN	= 12;
	
	/** �o�����[�ɓK�p�����A�L���O�ł��邱�Ƃ������萔(=13)�ł��B */
	int KING	= 13;
	
	/**
	 * �o�����[�̓��e��Ԃ��܂��B
	 * ���m�̃J�[�h�ł������ꍇ�AUnspecifiedException ���X���[����܂��B
	 *
	 * @return		Card.ACE(=1), Card.TWO(=2), �c�c, Card.KING(=13),
	 *				Card.JOKER(=0)
	 * @see			#ACE
	 * @see			#KING
	 * @see			#QUEEN
	 * @see			#JACK
	 */
	int getValue();
	
	/**
	 * �X�[�g�̓��e��Ԃ��܂��B
	 * ���m�̃J�[�h�ł������ꍇ�AUnspecifiedException ���X���[����܂��B
	 *
	 * @return		Card.SPADE(=1), Card.HEART(=2), Card.DIAMOND(=3),
	 *              Card.CLUB(=4), Card.JOKER(=0)
	 * @see			#SPADE
	 * @see			#HEART
	 * @see			#DIAMOND
	 * @see			#CLUB
	 */
	int getSuit();
	
	/**
	 * Unspecified�̃J�[�h�ɑ΂��ăX�[�g�ƃo�����[��ݒ肵�܂��B
	 * ���̌��ʁA�J�[�h�͎����I�ɕ\�����ɂȂ�܂��B
	 * ���łɓ���� holder �Ɏw�肳�ꂽ�X�[�g�A�o�����[�̃J�[�h���܂܂��
	 * ����ꍇ�AAlreadySpecifiedException ���X���[����܂��B
	 *
	 * @param		suit		�ݒ肷��X�[�g
	 * @param		value		�ݒ肷��o�����[
	 * 
	 * @see			#SPADE
	 * @see			#HEART
	 * @see			#DIAMOND
	 * @see			#CLUB
	 * @see			#JOKER
	 * @see			#JACK
	 * @see			#QUEEN
	 * @see			#KING
	 * @see			#invalidate()
	 */
	void specify(int suit, int value);
	
	/**
	 * Unspecified�̃J�[�h�ɑ΂��ăX�[�g�ƃo�����[��ݒ肵�܂��B
	 * ���̌��ʁA�J�[�h�͎����I�ɕ\�����ɂȂ�܂��B
	 * ���łɓ���� holder �Ɏw�肳�ꂽ�X�[�g�A�o�����[�̃J�[�h���܂܂��
	 * ����ꍇ�AAlreadySpecifiedException ���X���[����܂��B
	 *
	 * @param		card		�ݒ肷��J�[�h�̓��e
	 * @see			#invalidate()
	 */
	void specify(Card card);
	
	/**
	 * �{�I�u�W�F�N�g�� Unspecified �J�[�h�ɕύX���܂��B
	 *
	 * @see			#specify(int,int)
	 * @see			#specify(ys.game.card.Card)
	 */
	void invalidate();
	
	/**
	 * ���̃J�[�h���w�肳�ꂽ�X�[�g�A�o�����[�� specify �\�ł��邩
	 * �e�X�g���܂��B
	 *
	 * @param		suit		�e�X�g�������X�[�g�̎w��
	 * @param		value		�e�X�g�������l�̎w��
	 * @return		specifiable �Ȃ� true
	 */
	boolean isSpecifiableAs(int suit, int value);
	
	/**
	 * ���̃J�[�h���w�肳�ꂽ�J�[�h�̓��e�� specify �\�ł��邩
	 * �e�X�g���܂��B
	 *
	 * @param		card		�e�X�g�������J�[�h(�̎��)�̎w��
	 * @return		specifiable �Ȃ� true
	 */
	boolean isSpecifiableAs(Card card);
	
	/**
	 * ���\���Ђ�����Ԃ��܂��B
	 *
	 * @see		#isHead()
	 */
	void turn();
	
	/**
	 * ���\���w�肵�܂��B
	 *
	 * @param		head		�\�ɂ���ꍇ true, ���ɂ���ꍇ false
	 *							���w�肵�܂��B
	 * @see		#isHead()
	 */
	void turn(boolean head);
	
	/**
	 * �\�ł��邩�e�X�g���܂��B
	 *
	 * @return		�\�̏ꍇ true, ���̏ꍇ false ���Ԃ�܂��B
	 * @see			#turn()
	 * @see			#turn(boolean)
	 */
	boolean isHead();
	
	/**
	 * �X�[�g�A�o�����[���ݒ肳��Ă��邩�e�X�g����B
	 *
	 * @return		�X�[�g�A�o�����[���w�肳��Ă��Ȃ��ꍇ true ���Ԃ�܂��B
	 */
	boolean isUnspecified();
	
	/**
	 * ���̃J�[�h�̏�������f�b�L��Ԃ��܂��B
	 *
	 * @return		���� Card �̏������� holder
	 */
	Packet getHolder();
	
	/**
	 * ���̃J�[�h�̏�������f�b�L���w�肵�܂��B
	 * �J�[�h�͐�����A��x�����{���\�b�h���Ă΂�A�����f�b�L���w�肳��܂��B
	 * 
	 * @param		holder		��������f�b�L
	 */
	void setHolder(Packet holder);

}
