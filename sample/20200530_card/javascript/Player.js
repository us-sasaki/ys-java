/**
 * Player �� Bid, �܂��� Play���s����̂ł��B
 * GUI�ɘA�������l�A�R���s���[�^�A���S���Y���Ȃǂ����Ă͂܂�܂��B
 * Player �́A�e�X�P��Board�ƘA�����Ă���BBoardManager�̂���master board
 * �Ƃ̐������́A���̂Ƃ���Player�̂��肦�Ȃ��v���C���_�@�Ƃ��ďC�������B
 * �����I�ɂ� Board �̏�ԕύX���\�b�h���� hashCode ��p���Đ������m�������B
 *
 * @version		making		12, June 2020
 * @author		Yusuke Sasaki
 */
class Player {
	/** ���[�h�̏��Ԃ������萔�ŁA���[�_�[�i�P�Ԗځj�������܂��B */
	static LEAD		= Board.LEAD;
	
	/** ���[�h�̏��Ԃ������萔�ŁA�Z�J���h�n���h�i�Q�Ԗځj�������܂��B */
	static SECOND	= Board.SECOND;
	
	/** ���[�h�̏��Ԃ������萔�ŁA�T�[�h�n���h�i�R�Ԗځj�������܂��B */
	static THIRD	= Board.THIRD;
	
	/** ���[�h�̏��Ԃ������萔�ŁA�t�H�[�X�n���h�i�S�Ԗځj�������܂��B */
	static FORTH	= Board.FORTH;
	
	/** �v���C���[�̑��Έʒu�������萔(=0)�ŁA�����̈ʒu�������܂��B */
	static ME		= 0;
	
	/** �v���C���[�̑��Έʒu�������萔(=1)�ŁA�����̍��̐�(left hand)�������܂��B */
	static LEFT		= 1;
	
	/** �v���C���[�̑��Έʒu�������萔(=2)�ŁA�p�[�g�i�[�̈ʒu�������܂��B */
	static PARTNER	= 2;
	
	/** �v���C���[�̑��Έʒu�������萔(=3)�ŁA�����̉E�̐�(right hand)�������܂��B */
	static RIGHT	= 3;
	
	/**
	 * @type	{Board}	�{�[�h
	 */
	myBoard;

	/**
	 * @type	{number} ���Ȓ萔
	 */
	mySeat;
	
/*------------------
 * instance methods
 */
	/**
	 * ���̃v���C���[���Q�Ƃ���{�[�h��ݒ肵�܂��B
	 * �p���N���X�̃R���X�g���N�^�ȂǂŎg�p���܂��B
	 * �㏑���\�ł��B
	 * @param	{Board} board	�v���C���[���Q�Ƃ���{�[�h
	 */
	setBoard(board) {
		this.myBoard = board;
	}
	
	/**
	 * ���̃v���C���[�̍����Ă���ꏊ(Board.NORTH�Ȃ�)���w�肵�܂��B
	 * @param	{number} seat	�v���C���[�̍����Ă���ꏊ(���Ȓ萔)
	 */
	setMySeat(seat) {
		this.mySeat = seat;
	}
	
	/**
	 * ��ʃv���O��������R�[������郁�\�b�h�ŁA
	 * ���̃v���C���[�̃r�b�h�A�v���C��ԋp���܂��B
	 * @returns		{Bid|Card}	�v���C���e
	 */
	async play() {
		switch (this.myBoard.status) {
		
		case Board.BIDDING:
			while (true) {
				const b = await this.bid();
				if (this.myBoard.allows(b)) return b;
			}
			
		case Board.OPENING:
		case Board.PLAYING:
			while (true) {
				const c = await this.draw();
				if (this.myBoard.allows(c)) return c;
			}
			
		case Board.DEALING:
		case Board.SCORING:
			throw new Error("Player.play() �� DEALING/SCORING ��Ԃ̃{�[�h�ŌĂ΂�܂���");
			
		default:
			throw new Error("play() internal error");
		}
	}
	
/*
 * �T�u�N���X�ɒ񋟂���֗��֐�
 */
	/**
	 * �p�[�g�i�[�̍����Ă���ꏊ(Board.NORTH�Ȃǂ̍��Ȓ萔)��ԋp���܂��B
	 *
	 * @returns		{number}	�p�[�g�i�[�̍��Ȕԍ�
	 */
	getPartnerSeat() {
		return (this.mySeat + 2) % 4;
	}
	
	/**
	 * ���̃v���C���[�̂��n���h��ԋp���܂��B
	 * @returns		{Packet}	���̃v���C���[�̃n���h
	 */
	getMyHand() {
		return this.myBoard.getHand(this.mySeat);
	}
	
	/**
	 * ���݃v���C���ԂƂȂ��Ă���n���h���擾���܂��B
	 * ���̃v���C���[���f�B�N���A���[�̏ꍇ�A���̃��\�b�h���g�p���邱�Ƃ�
	 * �v���C�ΏۂƂȂ��Ă��鎩���A�܂��̓_�~�[�̂����ꂩ�̃n���h���擾�ł��܂��B
	 * @returns		{Packet}	���݃v���C�ԂƂȂ��Ă���v���C���[�̃n���h
	 */
	getHand() {
		return this.myBoard.getHand(this.myBoard.getTurn());
	}
	
	/**
	 * @returns		{Packet}	�_�~�[�̃n���h
	 */
	getDummyHand() {
		return this.myBoard.getHand(this.myBoard.getDummy());
	}
	
	/**
	 * ���ݏ�ɏo�Ă���J�[�h���擾���܂��B
	 * @returns		{Trick}	���ݏ�ɏo�Ă���J�[�h
	 */
	getTrick() {
		return this.myBoard.getTrick();
	}
	
	/**
	 * ���[�h���ꂽ�J�[�h���擾���܂��B
	 * ���������[�h���s���Ԃł������ꍇ��ABoard �̏�Ԃ� Board.PLAYING
	 * �łȂ������ꍇ�Anull ���ԋp����܂��B
	 * @returns		{Card} ���[�h���ꂽ�J�[�h
	 */
	getLead() {
		const o = this.getPlayOrder();
		if ((o == Board.LEAD)||(o == -1)) return null;
		return this.getTrick().children[0];
	}
	
	/**
	 * ���݂̃v���C��(lead, 2nd, 3rd, 4th)��Ԃ��܂��B
	 * Board �̏�Ԃ� Board.OPENING, Board.PLAYING �ȊO�̏ꍇ�A -1 ���ԋp����܂��B
	 *
	 * @returns		{number} �v���C���������萔(LEAD, SECOND, THIRD, FORTH)
	 */
	getPlayOrder() {
		return this.myBoard.getPlayOrder();
	}
	
	/**
	 * �_�~�[�̎�������̑��Έʒu��Ԃ��܂��B
	 * �_�~�[���܂����肵�Ă��Ȃ��ꍇ�AError ���X���[����܂��B
	 *
	 * @returns		{number} �_�~�[�̑��Έʒu
	 * @see			ME
	 * @see			LEFT
	 * @see			PARTNER
	 * @see			RIGHT
	 */
	getDummyPosition() {
		const dummySeat = this.myBoard.getDummy();
		if (dummySeat == -1)
			throw new Error("�܂��R���g���N�g�����肵�Ă��܂���");
		
		return (dummySeat - this.mySeat + 4) % 4;
	}
	
	/**
	 * Leader �̎�������̑��Έʒu��Ԃ��܂��B
	 * �v���C�̏�ԂłȂ��Ƃ��AIllegalStatusException ���X���[����܂��B
	 *
	 * @returns		{number} Leader�̑��Έʒu
	 * @see			ME
	 * @see			LEFT
	 * @see			PARTNER
	 * @see			RIGHT
	 */
	getLeaderPosition() {
		if ( (this.myBoard.status != Board.OPENING) &&
				(this.myBoard.getStatus() != Board.PLAYING) )
			throw new Error("�v���C���J�n����Ă��܂���");
		if (this.myBoard.status == Board.SCORING)
			throw new Error("�{�[�h�͂��łɏI�����Ă��܂�");
		
		const t = this.myBoard.getTrick();
		return (t.leader - this.mySeat + 4) % 4;
	}
	
/*------------------
 * abstract methods
 */
	//async Bid bid();
	//async Card draw();
	
}


/**
 * �\�ȃv���C�������_���ɍs���R���s���[�^�v���C���[�ł��B
 * �r�b�h�͂˂Ƀp�X���܂��B�f�B�N���A���[�Ƃ��Ă��v���C�ł��܂��B
 *
 * @version		making		12, June 2020
 * @author		Yusuke Sasaki
 */
class RandomPlayer extends Player {
	
	/**
	 * 
	 * @param {?Board} board 
	 * @param {?number} seat 
	 */
	constructor(board, seat) {
		super();
		if (board !== void 0 && seat !== void 0) {
			this.setBoard(board);
			this.setMySeat(seat);
		}
	}
	
/*------------
 * implements
 */
	/**
	 * �p�X���܂��B
	 *
	 * @return		�p�X
	 */
	async bid() {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * �\�ȃv���C�������_���ɑI�����A�ԋp���܂��B
	 *
	 * @return		�����_���ȃv���C
	 */
	async draw() {
		const board = this.myBoard;
		const hand = this.getHand();
		
		// �v���C���ׂ��n���h��������
		hand.shuffle();
		let played = null;
		
		// ������ꂽ�n���h�̉����珇�Ƀv���C�\�ȃJ�[�h����������
		for (let i = 0; i < hand.children.length; i++) {
			played = hand.children[i];
			if (board.allows(played)) break;
		}
		if (played == null) throw new Error();
		
		// �n���h��߂��Ă���
		hand.arrange();
		
		return played;
	}
	
}
