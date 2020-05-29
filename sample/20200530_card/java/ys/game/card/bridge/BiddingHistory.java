package ys.game.card.bridge;

/**
 * Board �̋@�\�̂����A�I�[�N�V�����Ɋ֌W���镔���̎��ۂ̏������󂯎���
 * �N���X�ł��B
 *
 * @version		a-release	4, January 2001
 * @author		Yusuke Sasaki
 */
public interface BiddingHistory {
	
	/**
	 * �w�肳�ꂽ�r�b�h���A�r�b�f�B���O�V�[�P���X�㋖����邩�e�X�g���܂��B
	 * ���Ȋ֌W�A�r�b�h�̋����Ȃǂ����ׂ��܂��B
	 *
	 * @param		b		�e�X�g����r�b�h
	 * @return		�������r�b�h��
	 */
	boolean allows(Bid b);
	
	/**
	 * �r�b�h���s���C�r�b�f�B���O�V�[�P���X��i�߂܂��B
	 * �s�\�ȃr�b�h���s�����Ƃ���ƁAIllegalPlayException ���X���[����܂��B
	 * ���̃��\�b�h�� Board �������ŌĂяo���A�P�ƂŌĂԂƃX�e�[�^�X�ُ��
	 * �����N�����܂��B
	 *
	 * @param		newBid		�V���ɍs���r�b�h
	 */
	void bid(Bid newBid);
	
	/**
	 * �s��ꂽ���ׂẴr�b�h��z��`���ŕԋp���܂��B
	 * �z��̓Y�� 0 ���f�B�[���[�̃r�b�h�Ƃ��Ĉȍ~���ȏ��Ɋi�[����Ă��܂��B
	 * �����_�܂łŃr�b�h���ꂽ�񐔕��̗v�f���܂݂܂��B
	 *
	 * @return		���ׂẴr�b�h
	 */
	Bid[] getAllBids();
	
	/**
	 * ���Ƀr�b�h����Ȃ̔ԍ���Ԃ��܂��B
	 *
	 * @return		�Ȕԍ�
	 */
	int getTurn();
	
	/**
	 * �R���g���N�g�A�f�B�N���A���[���w�肵�܂��B
	 * �r�b�f�B���O�V�[�P���X��p�����ɃR���g���N�g�݂̂����肵�Ă���
	 * �Ƃ��Ɏg�p���܂��B
	 * ���̃��\�b�h�̓r�b�f�B���O�V�[�P���X����̏ꍇ�ɃR�[���ł��܂��B
	 * �r�b�h��̏ꍇ�AIllegalStatusException ���X���[����܂��B
	 *
	 * @param		contract		�R���g���N�g
	 * @param		declarer		�f�B�N���A���[
	 */
	void setContract(Bid contract, int declarer);
	
	/**
	 * (���݂܂łŊm�肵�Ă���)�R���g���N�g���擾���܂��B
	 *
	 * @return		�R���g���N�g
	 */
	Bid getContract();
	
	/**
	 * (���݂܂łŊm�肵�Ă���)�f�B�N���A���[�̐Ȕԍ����擾���܂��B
	 *
	 * @return		�f�B�N���A���[�̐Ȕԍ�
	 */
	int getDeclarer();
	
	/**
	 * �f�B�[���[�̐Ȕԍ����擾���܂��B
	 *
	 * @return		�f�B�[���[�̐Ȕԍ�
	 */
	int getDealer();
	
	/**
	 * �r�b�f�B���O�V�[�P���X���I���������ǂ����e�X�g���܂��B
	 *
	 * @return		�r�b�f�B���O�V�[�P���X���I��������
	 */
	boolean isFinished();
	
	void reset(int dealer);
	
	/**
	 * ���݂܂Ńr�b�h���ꂽ����Ԃ��܂��B
	 * �܂��N���r�b�h���Ă��Ȃ���Ԃł� 0 ���ԋp����܂��B
	 */
	int countBid();
	
	/**
	 * �P�O�̏�Ԃɖ߂��܂��B
	 * countBid() �� 0 ��Ԃ���Ԃ̂Ƃ��́AIllegalStatusException ���X���[���܂��B
	 */
	void undo();
}
