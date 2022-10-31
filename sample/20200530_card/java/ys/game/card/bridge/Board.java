package ys.game.card.bridge;
/*
 * 2001/ 7/23  setName(), getName() ��ǉ�
 */
import ys.game.card.Packet;

/**
 * �u���b�W�ɂ�����P�{�[�h���p�b�N����I�u�W�F�N�g�ł��B
 * BoardManager�ɑ΂��Ă͎󓮓I�ȃI�u�W�F�N�g�ŁA��ԕω����N����
 * ���\�b�h��񋟂��܂��B
 * Player�ɑ΂��Ă͏�ԎQ�Ƃ�񋟂��܂��B
 *
 * @version		a-release		23, July 2001
 * @author		Yusuke Sasaki
 */
public interface Board {
	
	/**
	 * ���Ȓ萔�Ƃ��Ďg�p����ANorth(=0) �ł��邱�Ƃ������܂��B
	 */
	int NORTH = 0;
	
	/**
	 * ���Ȓ萔�Ƃ��Ďg�p����AEast(=1) �ł��邱�Ƃ������܂��B
	 */
	int EAST  = 1;
	
	/**
	 * ���Ȓ萔�Ƃ��Ďg�p����ASouth(=2) �ł��邱�Ƃ������܂��B
	 */
	int SOUTH = 2;
	
	/**
	 * ���Ȓ萔�Ƃ��Ďg�p����AWest(=3) �ł��邱�Ƃ������܂��B
	 */
	int WEST  = 3;
	
	String[] STATUS_STRING	= {"Dealing", "Bid", "Opening Lead", "Playing", "Scoring"};
	String[] VUL_STRING		= {"none", "N-S", "E-W", "Both"};
	String[] SEAT_STRING	= {"North", "East ", "South", "West "};
	
	/**
	 * getVulnerability() ���\�b�h�̕ԋp�l�Ƃ��Ďg�p�����A
	 * NS-EW �̂�������o���łȂ�����(None Vul.)�������萔�ł��B
	 *
	 * @see		#getVulnerability()
	 */
	int VUL_NONE	= 0;
	
	/**
	 * getVulnerability() ���\�b�h�̕ԋp�l�Ƃ��Ďg�p�����A
	 * NS ���o���ł��邱�Ƃ������萔�ł��B
	 *
	 * @see		#getVulnerability()
	 */ 
	int VUL_NS		= 1;
	
	/**
	 * getVulnerability() ���\�b�h�̕ԋp�l�Ƃ��Ďg�p�����A
	 * EW ���o���ł��邱�Ƃ������萔�ł��B
	 *
	 * @see		#getVulnerability()
	 */ 
	int VUL_EW		= 2;
	
	/**
	 * getVulnerability() ���\�b�h�̕ԋp�l�Ƃ��Ďg�p�����A
	 * NS-EW �̗������o���ł��邱��(Both Vul.)�������萔�ł��B
	 *
	 * @see		#getVulnerability()
	 */ 
	int VUL_BOTH	= 3;
	
	/**
	 * �P�{�[�h���A�{�[�h���V�K�ɍ쐬����A�܂��v���C���[�ɃJ�[�h��
	 * �f�B�[������Ă��Ȃ���Ԃ������萔�ł��B
	 * deal() ���\�b�h���R�[�����邱�Ƃ� BIDDING ��ԂɈڍs���܂��B
	 *
	 * @see		#getStatus()
	 * @see		#deal()
	 * @see		#deal(ys.game.card.Packet[])
	 * @see		#BIDDING
	 */
	int DEALING = 0;
	
	/**
	 * �P�{�[�h���A�r�b�h���s���Ă����Ԃ������萔�ł��B
	 * �r�b�h�� play(Object) ���\�b�h�ɑ΂��� Bid �I�u�W�F�N�g��
	 * �^���邱�Ƃɂ���Đi�s���܂��B
	 * �r�b�f�B���O�V�[�P���X�̒��ŁA
	 * Pass Out �̏ꍇ�� SCORING ��ԂɈڍs���A
	 * �r�b�h�� Pass ���R�񑱂����ꍇ�� OPENING ��ԂɈڍs���܂��B
	 *
	 * @see		ys.game.card.bridge.Bid
	 * @see		#play(Object)
	 * @see		#getStatus()
	 * @see		#OPENING
	 * @see		#SCORING
	 */
	int BIDDING = 1;
	
	/**
	 * �P�{�[�h���A�I�[�v�j���O���[�h��҂��Ă����Ԃ������萔�ł��B
	 * �I�[�v�j���O���[�h�� play(Object) �ɑ΂��� Card �I�u�W�F�N�g��
	 * �^���邱�Ƃɂ���Đi�s���܂��B
	 * �I�[�v�j���O���[�h���s����ƃ_�~�[�n���h���I�[�v������A
	 * PLAYING ��ԂɈڍs���܂��B
	 *
	 * @see		ys.game.card.Card
	 * @see		#play(Object)
	 * @see		#getStatus()
	 * @see		#PLAYING
	 */
	int OPENING = 2;
	
	/**
	 * �P�{�[�h���A�v���C���ł��邱�Ƃ������萔�ł��B
	 * �v���C����������� SCORING ��ԂɈڍs���܂��B
	 * �{��ԂɂȂ����{�[�h�� reset(int,int) ���\�b�h�� DEALING
	 * ��ԂɈڍs���܂��B
	 *
	 * @see		#getStatus()
	 * @see		#reset(int,int)
	 * @see		#DEALING
	 */
	int PLAYING = 3;
	
	/**
	 * �P�{�[�h���I���������Ƃ������萔�ł��B
	 * �v���C����������� SCORING ��ԂɈڍs���܂��B
	 *
	 * @see		#getStatus()
	 * @see		#SCORING
	 */
	int SCORING = 4;
	
// �v���C����
	int LEAD	= 0;
	int SECOND	= 1;
	int THIRD	= 2;
	int FORTH	= 3;
	
/*----------------------------------------------------------
 * ��ԕω����\�b�h(���J����Ȃ��p�b�P�[�W���x���̃��\�b�h)
 */
	/**
	 * �r�b�h�A�܂��̓v���C���s���܂��B��Ԃ��K�X�ω����܂��B
	 *
	 * @param		c		�r�b�h(Bid �̃C���X�^���X)�܂���
	 *						�v���C(Card �̃C���X�^���X)
	 */
	void play(Object c);
	
	/**
	 * �����_���ȃn���h��ݒ肵�A�I�[�N�V�����ł�����(BIDDING)�Ɉڍs���܂��B
	 */
	void deal();
	
	/**
	 * �w�肳�ꂽ�n���h��ݒ肵�A�I�[�N�V�����ł�����(BIDDING)�Ɉڍs���܂��B
	 * �{���\�b�h���R�[������ƁA�n���h�͗��Ԃ��̏�ԂƂȂ�܂��BtoText()��
	 * �s���ꍇ�A�\������Ȃ��Ȃ�̂ŁA���ӂ��K�v�ł��B
	 */
	void deal(Packet[] hand);
	
	// void undo();
	
/*------------------------------
 * ��ԎQ�ƃ��\�b�h(���J�����)
 */
	/**
	 * ���� Board �̖��O��ݒ肵�܂��B
	 */
	void setName(String name);
	
	/**
	 * ���� Board �̖��O���擾���܂��B�ݒ肳��Ă��Ȃ��ꍇ�A�󕶎����Ԃ���܂��B
	 */
	String getName();
	
	/**
	 * ���� Board �Ŏg�p���Ă��� BiddingHistory ���擾���܂��B
	 *
	 * @return		BiddingHistory
	 */
	BiddingHistory getBiddingHistory();
	
	/**
	 * ���� Board �Ŏg�p���Ă��� PlayHistory ���擾���܂��B
	 *
	 * @return		PlayHistory
	 */
	PlayHistory getPlayHistory();
	
	/**
	 * ���� Board �Ŏg�p���� PlayHistory ���w�肵�܂��B
	 *
	 * @param		�ݒ肵���� playHistory
	 */
	void setPlayHistory(PlayHistory playHistory);
	
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
	 * �w�肳�ꂽ�r�b�h�A�������̓v���C�\�ł��邩�e�X�g���܂��B
	 * �{�I�u�W�F�N�g�ł́A���̂悤�ȃ`�F�b�N���s���܂��B<BR>
	 * (1) Board�̐i�s��ԂƎw�肳�ꂽ�I�u�W�F�N�g�̐�����<BR>
	 * (2) BIDDING �̏ꍇ�A�r�b�f�B���O�V�[�P���X�Ƃ��ċ�����邱��<BR>
	 * (3) PLAYING �̏ꍇ�ARevoke ���s���Ă��Ȃ�����<BR>
	 * (4) PLAYING �̏ꍇ�A�X�[�g�t�H���[���s���Ă��邱��<BR>
	 *
	 * @param		play		����Ώۂ̃r�b�h�A�܂��̓v���C
	 * @return		true�F�\    false:�s�\
	 */
	boolean allows(Object play);
	
	/**
	 * Board�̐i�s��Ԃ��擾���܂��B
	 *
	 * @return		Board �̐i�s���(DEALING,BIDDING,OPENING,PLAYING,SCORING)
	 * @see			#DEALING
	 * @see			#BIDDING
	 * @see			#OPENING
	 * @see			#PLAYING
	 * @see			#SCORING
	 */
	int getStatus();
	
	/**
	 * �X�e�[�^�X�� OPENING, PLAYING �̏ꍇ�Ƀv���C����
	 * �����萔��ԋp���܂��B
	 * �X�e�[�^�X�����̒l�̏ꍇ�A -1 ���ԋp����܂��B
	 *
	 * @return		�v���C���������萔(LEAD, SECOND, THIRD, FORTH)
	 * @see			#LEAD
	 * @see			#SECOND
	 * @see			#THIRD
	 * @see			#FORTH
	 */
	int getPlayOrder();
	
	/**
	 * Vulnerability ���擾���܂��B
	 *
	 * @return		���̃{�[�h�� vulnerability�B
	 *				(0:VUL_NONE, 1:VUL_NS, 2:VUL_EW, 3:VUL_BOTH)
	 */
	int getVulnerability();
	
	/**
	 * �r�b�h�A�܂��̓v���C�����݂���̔Ԃł��邩�����Ȓ萔�Ŏ擾���܂��B
	 * DEALING, SCORING �ł� -1 ���ԋp����܂��B
	 *
	 * @see			#NORTH
	 * @see			#EAST
	 * @see			#SOUTH
	 * @see			#WEST
	 * @see			#DEALING
	 * @see			#SCORING
	 */
	int getTurn();
	
	/**
	 * ���݂��ꂪ�v���C����Ԃ������Ȓ萔�Ŏ擾���܂��B
	 * getTurn() �Ƃ̈Ⴂ�́A�v���C�̂Ƃ��A�_�~�[�̔ԂŃf�B�N���A���[��
	 * ���Ȓ萔���Ԃ����_�ł��B
	 *
	 * @see			#NORTH
	 * @see			#EAST
	 * @see			#SOUTH
	 * @see			#WEST
	 * @see			#DEALING
	 * @see			#SCORING
	 */
	int getPlayer();
	
	/**
	 * (���݂܂ł�)�ŏI�R���g���N�g���擾���܂��B
	 * �܂��r�b�h���s���Ă��Ȃ��ꍇ�Anull ���ԋp����܂��B
	 */
	Bid getContract();
	
	/**
	 * (���݂܂ł�)�ŏI�g�����v���X�[�g�萔�Ŏ擾���܂��B
	 * �r�b�h���܂��s���Ă��Ȃ��ꍇ�A-1 ���Ԃ�܂��B
	 *
	 * @return	�X�[�g�萔(SPADE, HEART, DIAMOND, CLUB)
	 * @see		ys.game.card.Card#SPADE
	 * @see		ys.game.card.Card#HEART
	 * @see		ys.game.card.Card#DIAMOND
	 * @see		ys.game.card.Card#CLUB
	 */
	int getTrump();
	
	/**
	 * (���݂܂łŌ��肵�Ă���)�f�B�N���A���[�����Ȓ萔�Ŏ擾���܂��B
	 * �܂����肵�Ă��Ȃ��ꍇ�A-1 ���ԋp����܂��B
	 *
	 * @return	�f�B�N���A���[�̍��Ȓ萔�A�܂��� -1
	 */
	int getDeclarer();
	
	/**
	 * (���݂܂łŌ��肵�Ă���)�_�~�[�����Ȓ萔�Ŏ擾���܂��B
	 * �܂����肵�Ă��Ȃ��ꍇ�A-1 ���ԋp����܂��B
	 *
	 * @return	�_�~�[�̍��Ȓ萔�A�܂��� -1
	 */
	int getDummy();
	
	/**
	 * (�r�b�h���J�n����)�f�B�[���[�����Ȓ萔�Ŏ擾���܂��B
	 *
	 * @return	�f�B�[���[�̍��Ȓ萔�A�܂��� -1
	 */
	int getDealer();
	
	/**
	 * �w�肳�ꂽ���Ȃ̃n���h���擾���܂��B
	 * ���̃{�[�h���}�X�^�[�łȂ��ꍇ�A���m�̃n���h�ɂ�
	 * UnspecifiedCard ���܂܂��\��������܂��B
	 */
	Packet getHand(int seat);
	
	/**
	 * �v���C���[�S���̃n���h���擾���܂��B�z��̓Y�����͍��Ȓ萔�ł��B
	 * ���̃{�[�h���}�X�^�[�łȂ��ꍇ�A���m�̃n���h�ɂ�
	 * UnspecifiedCard ���܂܂��\��������܂��B
	 */
	Packet[] getHand();
	
	/**
	 * ���݂܂łɃv���C���ꂽ�g���b�N�����擾���܂��B
	 * ����́Afinished() �ƂȂ����g���b�N�̐��������Ă���A���Ƃ���
	 * �͂��߂̂P�g���b�N���I�����Č��݂Q�g���b�N�ڂɂ���ꍇ�ɂ� 1
	 * ���ԋp����܂��B
	 * �P�R�g���b�N���ׂĂ�����������Ԃł� 13 ���ԋp����܂��B
	 *
	 * @return		�v���C���ꂽ�g���b�N��
	 * @see			#getAllTricks()
	 * @see			#getTrick()
	 */
	int getTricks();
	
	/**
	 * ���ݏ�ɏo�Ă���g���b�N���擾���܂��B
	 * �܂��R���g���N�g�����肵�Ă��Ȃ��ꍇ�Anull ���ԋp����܂��B
	 * OPENING, PLAYING ��Ԃł� null ��Ԃ���邱�Ƃ͂Ȃ��A
	 * ��� finished() �ł͂Ȃ� Trick �̃C���X�^���X���ԋp����܂��B
	 * SCORING ��Ԃł́A�ŏI Trick �i�ʏ�Afinished() �̏�ԁj
	 * ���ԋp����܂��B
	 */
	Trick getTrick();
	
	/**
	 * �v���C���ꂽ�ߋ��̃g���b�N���ׂĂ��擾���܂��BDEALING, BIDDING 
	 * �̏ꍇ�� null ���ԋp����܂��B
	 */
	Trick[] getAllTricks();
	
	/**
	 * �w�肳�ꂽ���Ȃ��o���ł��邩���肵�܂��B
	 *
	 * @param		seat		���肷�����(N/E/S/W)
	 * @see		#NORTH
	 * @see		#EAST
	 * @see		#SOUTH
	 * @see		#WEST
	 */
	boolean isVul(int seat);
	
	/**
	 * ���m�̃J�[�h(�v���C���ꂽ�J�[�h�A�_�~�[�n���h)�̏W����Ԃ��܂��B
	 * �v�l���[�`���Ŏg�p����邱�Ƃ����҂��Ă��郁�\�b�h�ł��B
	 *
	 * @return		���m�̃J�[�h����Ȃ� Packet
	 */
	Packet getOpenCards();
	
	/**
	 * ���� Board �̏�Ԃ���������̏�ԂɃ��Z�b�g���܂��B
	 * 
	 * @param		boardNum	�{�[�h�ԍ�
	 */
	void reset(int boardNum);
	
	/**
	 * ���� Board �̏�Ԃ���������̏�ԂɃ��Z�b�g���܂��B
	 * 
	 * @param		dealer		�f�B�[���[�̔ԍ�
	 * @param		vul			�o���l���r���e�B
	 */
	void reset(int dealer, int vul);
	
	/**
	 * ���� Board �̏�Ԃ��P�O�̏�Ԃɖ߂��܂��B
	 * DEALING ��Ԃł��̃��\�b�h���ĂԂ� IllegalStatusException ���X���[����܂�
	 *
	 * @see		#DEALING
	 * @see		ys.game.card.bridge.IllegalArgumentException
	 * @exception	IllegalArgumentException	DEALING ��Ԃł�����
	 */
	void undo();
	
	String toText();
}
