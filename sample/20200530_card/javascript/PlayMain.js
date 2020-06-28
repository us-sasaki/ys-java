/**
 * �A�h�z�b�N�ȃ��C���v���O�����ł��B���񂾂�{�i�I�ɂȂ��Ă��܂����B
 *
 * @version		a-release		19, June 2020
 * @author		Yusuke Sasaki
 */
class PlayMain {
    /** @type {string} */ canvasId;
    /** @type {Field} */ field;
    /** @type {Board} */ board;
    /** @type {Player[]} */ players;
    /** @type {number} */ handno;
    /** @type {string} */ contractString;
	/** @type {Problem[]} */ problems;
    /** @type {Sumire} */ sumire;
	/** @type {Button} */ quit;
    /** @type {Button} */ dd;
	/** @type {Button} */ textWindow;
	/** @type {SelectDialog} */ dialog;
    /** @type {YesNoDialog} */ confirmDialog;
	/** @type {boolean} */ exitSignal;
	
	/**
	 * PlayMain �I�u�W�F�N�g�𐶐����܂��B
	 */
	constructor(canvasId) {
		this.canvasId = canvasId;
		this.problems = [];

		this.dialog = new SelectDialog();
		this.field = new Field(this.canvasId);
        
		// �{�^��
		this._placeQuitButton_();
		this._placeDDButton_();
		this._placeTextButton_();
	}
	
/*-----------------------------
 * implements (ActionListener)
 */
    /**
     * dd �{�^�����������Ƃ��̏���
     */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == quit) {
			if (mainThread != null) mainThread.interrupt();
		}
		
		// �ȉ��Aadded 03/6/2
		if (e.getSource() == textWindow) {
			if (board != null) {
				TextInfoWindow.getInstance(board.toText());
			}
		}
		// �ȏ�Aadded 03/6/2
	}
	
/*------------------
 * instance methods
 */
	/**
	 * �͂��߂ɕ\�������_�C�A���O�ɖ���ǉ����܂��B
	 * valid �łȂ����͒ǉ�����܂���B
     * @param   {Problem} p     �ǉ�������
	 */
	addProblem(p) {
		if (p.isValid()) this.problem.push(p);
	}
	
	/**
	 * ���f�{�^��(this.quit)�𐶐��A�z�u���܂��B
	 * @private
	 */
	_placeQuitButton_() {
		this.quit = new Button(this.field, "���f");
		this.quit.setBounds(540, 30, 80, 24);
		this.field.add(this.quit);
		this.quit.setListener( () => {
			if (window.confirm('���̃{�[�h��j�����Ē��f���܂�')) {
				this.field.interrupt(); // OK ����
			}
		});
		}

	/**
	 * �_�u���_�~�[�{�^��(this.dd)�𐶐��A�z�u���܂��B
	 * this.dd �ɂ� doubleDummy {boolean} �v���p�e�B������܂��B
	 * @private
	 */
	_placeDDButton_() {
		const dd = new Button(this.field, "�_�u���_�~�["); // �_�u���_�~�[
		dd.doubleDummy = false;
		this.field.add(dd);
		dd.setBounds(540, 58, 80, 24);
		dd.setListener( () => {
			if (this.board.status != Board.PLAYING) return;
			//
			dd.doubleDummy = !dd.doubleDummy;
			dd.caption = dd.doubleDummy?'�ʏ�ɖ߂�':'�_�u���_�~�[';
			this.board.getHand(Board.EAST).turn(dd.doubleDummy);
			this.board.getHand(Board.WEST).turn(dd.doubleDummy);
			this.field.draw();
		});
		this.dd = dd;
	}

	/**
	 * �e�L�X�g�\���{�^��(this.textWindow)�𐶐��A�z�u���܂��B
	 * @private
	 */
	_placeTextButton_() {
		this.textWindow = new Button(this.field, "�e�L�X�g�\��"); // �e�L�X�g�\��
		this.field.add(this.textWindow);
		this.textWindow.setBounds(540, 86, 80, 24);
	}
	
	/**
	 * Board �̏��������s���܂��B
	 * @async
	 */
	async start() {
		const titles = [];
		this.problems.forEach( prob => titles.push(prob.title));
		this.dialod.setPulldown(titles);
		this.field.draw();
		const result = await this.dialog.show();
		
		if ("video" ==  result) {
			this._makeVideohand_();
		} else if ("replay" == result) {
			this._makeLasthand_();
		} else {
			this.handno = parseInt(result.substring(4));
			this._makeNewhand_();
		}
		// field �� board ��ǉ�
		this.field.add(this.board);
		this.board.setPosition(0, 0);
		this.board.setDirection(0);
		this.field.draw();
		
		this.handno = 0;
		await this.main();

		// field ���� board ���폜
		this.field.pull(this.board);

	}
	
	/**
	 * �_�C�A���O�ŐV�����n���h��I�������Ƃ��̏����ł��B
	 * @private
	 */
	_makeNewhand_() {
		const prob = this.problems[handno];
		
		this.board = new Board(1);
		this.board.setName(prob.title);
		
		// Player �ݒ�
		_setPlayers(prob)_
		
		// �f�B�[��
		const hands = prob.createHands();
		
		this.board.deal(hands);
		
		// �R���g���N�g�ݒ���s��
		this.board.setContract(prob.contract, Board.SOUTH);
	}

	/**
	 * �v���C�ɂ�����v���C���[�� thinker �Ɋ�Â��ݒ肵�܂��B
	 * @param	{Problem} prob ���
	 */
	_setPlayers_(prob) {
		this.players = [];
		this.players[Board.NORTH] = new RandomPlayer(board, Board.NORTH);
		this.players[Board.SOUTH] = new HumanPlayer(board, field, Board.SOUTH);
		// Computer Player �ݒ�
		if ( !prob.thinker || prob.thinker != "DoubleDummyPlayer") {
			this.players[Board.EAST ] = new SimplePlayer2(board, Board.EAST);
			this.players[Board.WEST ] = new SimplePlayer2(board, Board.WEST, prob.openingLead);
		} else if (prob.thinker == "DoubleDummyPlayer") {
			this.players[Board.EAST ] = new ReadAheadPlayer(board, Board.EAST);
			this.players[Board.WEST ] = new ReadAheadPlayer(board, Board.WEST, prob.openingLead);
		} else {
			this.players[Board.EAST ] = new NoRufPlayer(board, Board.EAST);
			this.players[Board.WEST ] = new NoRufPlayer(board, Board.WEST, prob.openingLead);
		}
	}
	
	/**
	 * �O�̃v���C�������Đ�����v���C���[��ݒ肵�܂��B
	 * @private
	 */
	_makeVideohand_() {
		const oldBoard = this.board;
		this.board = new Board(1);
		this.board.setName(oldBoard.name);
		
		// Player �ݒ�
		this.players = [];
		for (let i = 0; i < 4; i++)
			this.players.push(new VideoPlayer(this.board, oldBoard, i));
		
		// �f�B�[��
		const hands = BridgeUtils.calculateOriginalHand(oldBoard);
		
		this.board.deal(hands);
		
		// �R���g���N�g�ݒ���s��
		this.board.setContract(oldBoard.getContract(), oldBoard.getDeclarer());

		// �r�f�I���[�h�̓I�[�v�����
		const east = this.board.getHand(Board.EAST);
		east.turn(true);
		const west = this.board.getHand(Board.WEST);
		west.turn(true);
		
		this.dd.doubleDummy = true;
		this.dd.caption = "�ʏ�ɖ߂�";
		
	}
	
	/**
	 * �O��Ɠ����n���h��ݒ肵�܂��B
	 * @private
	 */
	_makeLasthand_() {
		const prob = problems[handno];
		const oldBoard = this.board;
		this.board = new Board(1);
		this.board.name = oldBoard.name;
		
		// Player �ݒ�
		_setPlayers_(prob);

		// �f�B�[��
		const hands = BridgeUtils.calculateOriginalHand(oldBoard);
		
		this.board.deal(hands);
		
		// �R���g���N�g�ݒ���s��
		this.board.setContract(oldBoard.getContract(), oldBoard.getDeclarer());
	}
	
	/**
	 * �n�߂̂��݂�ɂ�������\������
	 * @async
	 * @throws	QuitInterruptException ���f���I�����ꂽ
	 */
	async explain() {
		const prob = this.problem[this.handno];
		
		this.sumire = new Sumire(field, prob.description);
		this.contractString = prob.getContractString();
		await this.sumire.animate(Sumire.NORMAL);
	}
	
	/**
	 * ���C�����[�v
	 * @async
	 * @throws	QuitInterruptException ���f���I�����ꂽ
	 */
	async mainLoop() {
		if (this.dd.doubleDummy) {
			this.board.getHand(Board.EAST).turn(true);
			this.board.getHand(Board.WEST).turn(true);
		}
		
		while (true) {
			// Spot ���w�肷��
			this.field.setSpot(this.board.getTurn());
			this.field.draw();
			
			let c = null;
			while (c === null) {
				c = await player[this.board.getPlayer()].play(); // �u���b�N����
			}
			this.board.play(c);
			this.field.draw();
			
			if (this.board.status == Board.SCORING) break;
		}
	}
	
	/**
	 * ���̃n���h�̕\���A�X�R�A�̕\�����s��
	 * @async
	 * @throws	QuitInterruptException ���f���I�����ꂽ
	 */
	async displayScore() {
		this.field.spot = -1; // spot ������
		this.field.draw();
		
		await this.field.sleep(500);

		// �J�[�h��������x�\������
		const original = BridgeUtils.calculateOriginalHand(this.board);
		for (let i = 0; i < 4; i++) {
			const hand = this.board.getHand(i);
			for (let j = 0; j < original[i].children.length; j++) {
				const c = original[i].children[j];
				c.turn(true);
				hand.add(c);
			}
		}

		this.board.layout();
		this.board.getHand().forEach( hand => { hand.arrange(); hand.layout(); });
		
		// �X�R�A�\��
		let msg = "���ʁF" + this.contractString + "  ";
		let msg2;
		// ���C�N��
		const win = BridgeUtils.countDeclarerSideWinners(this.board);
		const up = win - this.board.getContract().level - 6;
		const make = win - 6;
		
		if (up >= 0) {
			// ���C�N
			msg += make + "���C�N";
			msg2 = "���߂łƂ��I�I";
		} else {
			// �_�E��
			msg += (-up) + "�_�E��";
			msg2 = "�c�O�B������x����΂��āI";
		}
		
		msg += "("+win+"�g���b�N)\nN-S���̃X�R�A�F"+Score.calculate(this.board, Board.SOUTH);
		msg += "\n \n" + msg2;
		
		this.sumire = new Sumire(field, msg);
		if (up >= 0) 
			await this.sumire.animate(Sumire.DELIGHTED);
		else
			await this.sumire.animate(Sumire.SAD);
		this.field.draw();
		this.board.getHand().forEach( h => { while (h.children.length > 0) h.pull(); });
	}
	
	/**
	 * ���C�����\�b�h�ł��B
	 * @async
	 */
	async main() {
		try {
			await this.explain();
			await this.mainLoop();
			await this.displayScore();
			this.field.pull(this.board);
		} catch (e) {
			this.field.spot = -1;
			this.field.draw();
		}
	}
}

class Problem {
	/** @type {string} ���^�C�g�� */ title;
	/** @type {Bid} �R���g���N�g */ contract;
	/** @type {Packet[]} 4�l�̃n���h */ hands;
	/** @type {string} �������� */ description;
	/** @type {string} O.L. */ openingLead;
	/** @type {string} �v�l���[�`�� */ thinker;

	/**
	 * 4  SXX �Ȃǂ̃R���g���N�g�������ԋp���܂��B
	 * @returns		{string} �R���g���N�g������
	 */
	getContractString() {
		if (!this.contract) return "null";
		return this.contract.toString().substring(1, 7);
	}

	estimateValidity() {
		//
	}

	/**
	 * �w�肳�ꂽ�p�����[�^�Ŗ����쐬���܂��B
	 * 
	 * @param {string} title ���̕\��
	 * @param {number} kind �r�b�h���(Bid.PASS�Ȃ�
	 * @param {number} level �r�b�h���x��
	 * @param {number} denomination Bid.DOUBLE, Bid.REDOUBLE �Ȃ�
	 * @param {string[]} hands �n���h������
	 * @param {string} description ���̐���
	 * @param {string} openingLead �I�[�v�j���O���[�h�w��(�w��Ȃ��� null)
	 * @param {string} thinker �v�l�A���S���Y����
	 * @returns	{Problem} ���
	 */
	static regular(title, kind, level, denomination, hands, description, openingLead, thinker) {
		let p = new Problem();
		p.title = title;
		p.description = description;
		p.openingLead = openingLead;
		p.thinker = thinker;
		p.contract = new Bid(kind, level, denomination);

		hs = [];
		for (let i = 0; i < 4; i++) hs.push(new Packet());
		
		const pile = Packet.provideDeck();
		for (let i = 0; i < 4; i++) Problem.draw(pile, hs[i], hands[i]);
		
		// �c��̓����_���ɔz��
		if (pile.children.length > 0) {
			pile.shuffle();
			for (let i = 0; i < 4; i++) {
				for (let j = hs[i].children.length; j < 13; j++) {
					hs[i].add(pile.pull());
					// �r���� pile ���s���邱�Ƃ�����
					// ���̏ꍇ�́AisValid() �� false ��Ԃ����ƂɂȂ�
				}
			}
		}
		hs.forEach( h => h.arrange() );
		p.hands = hs;
		return p;
	}

	/**
	 * ���̖��̃n���h�𐶐����܂��B
	 * (�����I�Ɏ��� Packet �� shallow copy �����܂�)
	 * @returns	{Packet[]} ���̖��̃n���h
	 */
	createHands() {
		const result = [];
		for (let i = 0; i < 4; i++) {
			const h = new Packet();
			this.hands[i].children.forEach(c => h.add(c));
			result.push(h);
		}
		return result;
	}

	/**
	 * ������ŗ^����ꂽ�n���h�������߂��� hand �ɐݒ肵�܂��B
	 * @param	{Packet} pile �J�[�h�̎R
	 * @param	{Packet} hand �ݒ��̃n���h
	 * @param	{string} str �n���h������
	 */
	static draw(pile, hand, str) {
		let suit = Card.SPADE;
		
		for (let i = 0; i < str.length; i++) {
			const c = str.charAt(i);
			
			if (c == 'S') {
				suit = Card.SPADE;
			} else if (c == 'H') {
				suit = Card.HEART;
			} else if (c == 'D') {
				suit = Card.DIAMOND;
			} else if (c == 'C') {
				suit = Card.CLUB;
			} else if (c == 'K') {
				hand.add(pile.pull(suit, Card.KING));
			} else if (c == 'Q') {
				hand.add(pile.pull(suit, Card.QUEEN));
			} else if (c == 'J') {
				hand.add(pile.pull(suit, Card.JACK));
			} else if (c == 'T') {
				hand.add(pile.pull(suit, 10));
			} else if (c == 'A') {
				hand.add(pile.pull(suit, Card.ACE));
			} else if ( (c >= '2')&&(c <= '9') ) {
				hand.add(pile.pull(suit, parseInt(c) ));
			}
		}
	}
}

/**
 * �Q�[���I���_�C�A���O�𐶐����܂��B
 * ���̃N���X�� bridge.css �ƂƂ��Ɏg���K�v������܂��B
 */
class SelectDialog {
	modalContent;
	modalOverlay;
	select;
	startButton;
	videoButton;
	replayButton;

	/**
	 * document �ɁA�_�C�A���O��ǉ����܂��B
	 */
	constructor(titles) {
		this.modalContent = document.getElementById('modal-content');
		this.modalOverlay = document.getElementById('modal-overlay');
		if (!this.modalContent || !this.modalOverlay)
			throw new Error("html error. without modal-content or modal-overlay");
		const s = document.getElementsByName('select');
		if (s.length != 1) throw new Error("html doesn't have select");
		this.select = s[0];
		this.startButton = document.getElementById('startButton');
		this.videoButton = document.getElementById('videoButton');
		this.replayButton = document.getElementById('replayButton');
	}
/*------------------
 * instance methods
 */
	/**
	 * �v���_�E���Ɏw�肳�ꂽ�������ݒ肵�܂��B
	 * @param	{string[]} titles �v���_�E���ɐݒ肷�镶����
	 */
	setPulldown(titles) {
		// select �̎q�v�f���폜
		while (this.select.firstChild) { this.select.removeChild(this.select.firstChild); }
		for (let i = 0; i < titles.length; i++) {
			const opt = document.createElement('option');
			opt.setAttribute('value', 'prob'+(i+1));
			opt.innerHTML = titles[i];
			this.select.appendChild(opt);
		}
	}

	/**
	 * ���[�_���̑I����ʂ�\�����A�{�^��������҂��܂��B
	 * async �Ƃ��Ă̕Ԃ�l�͕�����ŁA"prob{num}", "video", "replay" �̂����ꂩ�ł��B
	 * �����ɑI����ʏ�̊e�{�^���̃n���h����o�^���܂��B
	 * @async
	 * @returns	{Promise<string>} "prob{num}", "video", "replay"
	 */
	show() {
		let startListener, videoListener, replayListener;
		return new Promise( (res) => {
			startListener = () => {	res('prob'+this.select.selectedIndex); };
			videoListener = () => {	res('video'); };
			replayListener = () => { res('replay'); };
				// �C�x���g�n���h����o�^���Ă���
			this.startButton.addEventListener('click', startListener);
			this.replayButton.addEventListener('click', replayListener);
			this.videoButton.addEventListener('click', videoListener);

			// ���[�_����ʂ�\������
			this.modalContent.style.display = 'inline';
			this.modalOverlay.style.display = 'inline';
		}).then( (val) => new Promise( (res) => {
			this.startButton.removeEventListener('click', startListener);
			this.videoButton.removeEventListener('click', videoListener);
			this.replayButton.removeEventListener('click', replayListener);
			// ���[�_����ʂ��B��
			this.modalContent.style.display = 'none';
			this.modalOverlay.style.display = 'none';
			res(val);
		}));
	}
}

/**
 * �u���b�W�V�~�����[�^�ɂ�����J�n���̐����̊G�𐶐����� Entity �ł��B
 * ���� Entity �ɂ́A�R���g���N�g�̓��e�Ȃǂ̐����������\������܂��B
 *
 */
class Sumire extends Entity {
	static NORMAL = 0;
	static DELIGHTED = 1;
	static SAD = 2;
	
	static FONT = 'normal 14px SanSerif';
	/** @type {number} ���̍s�܂ł̃X�e�b�v */
	static Y_STEP = 20;
	static MSG_COLOR = 'rgb(255, 255, 200)';
	static BACK_COLOR = 'rgba(200, 255, 200, 0.5)';
	
	/** @type {Field} */
	field;
	/** @type {string[]} */
	lines;
	/** @type {number} */
	picNumber;
	/** @type {number} */
	face;
	
	/** @type {number} �`����J�n���邘���W */
	x0;
	/** @type {number} �`����J�n���邙���W */
	y0;
	
	
	/** @type {number} ���������̕� */
	width;
	/** @type {number} ���������̍��� */
	height;
	
	/** @type {number} �ӂ������̕� */
	mw;
	/** @type {number} �ӂ������̍��� */
	mh;
	
	/** @type {number[]} �ӂ������̒��_x���W */
	xp;
	/** @type {number[]} �ӂ������̒��_y���W */
	yp;
	
/*-------------
 * Constructor
 */
	/**
	 * �w�肵���R���g���N�g�ł��邱�Ƃ�������� Entity ���쐬���܂��B
	 * @param	{Field} field field
	 * @param	{string} msg �\�����郁�b�Z�[�W
	 */
	constructor(field, msg) {
		super();
		this.field = field;
		
		// �^����ꂽ����������s�ŋ�؂�A�z��ɕϊ�����
		this.lines = msg.split('\n');
		const lines = this.lines.length;
		
		// �傫�������肷��
		const ctx = field.ctx;
		ctx.font = Sumire.FONT;
		this.width = 0;
		
		for (let i = 0; i < lines; i++) {
			this.width = Math.max(this.width, ctx.measureText(this.lines[i]));
		}
		this.width += 20; // as a margin
		this.height	= Sumire.Y_STEP * lines + 20;
		
		this.setBounds(140, 120, 360, 240);
		this.x0 = 130 + 40;
		this.y0 = Math.floor(90 + 12 + Sumire.Y_STEP + 100 - Sumire.Y_STEP * lines * 2 / 3);
		const msgy0 = Math.floor(100 + 100 - Sumire.Y_STEP * lines * 2 / 3);
		this.mw = 380 - 40;
		this.mh = Sumire.Y_STEP * lines + 20;
		this.xp = [this.x0-20, this.x0-20+this.mw, this.x0-20+this.mw, 410, 405, 390, this.x0-20];
		this.yp = [msgy0, msgy0, msgy0+this.mh, msgy0+this.mh, msgy0+this.mh+10, msgy0+this.mh, msgy0+this.mh];
		
		this.picNumber = 0;
	}
	
/*-----------
 * Overrides
 */
	/**
	 * @override
	 * @param {Context} ctx �O���t�B�b�N�R���e�L�X�g
	 */
	draw(ctx) {
		ctx.fillStyle = Sumire.BACK_COLOR;
		ctx.fillRect(this.x, this.y, this.w, this.h);
		ctx.strokeStyle = 'rgb(0,0,0)';
		ctx.fillStyle = Sumire.MSG_COLOR;
		ctx.beginPath();
		ctx.moveTo(this.xp[0], this.yp[0]);
		for (let i = 1; i < this.xp.length; i++) {
			ctx.lineTo(this.xp[i], this.yp[i]);
		}
		ctx.closePath();
		ctx.fill();
		ctx.stroke();
		
		ctx.fillStyle = 'rgb(0,0,0)';
		ctx.font = Sumire.FONT;
		let y = this.y0;
		
		for (let i = 0; i < this.lines.length; i++) {
			ctx.fillText(this.lines[i], this.x0, y);
			y += Sumire.Y_STEP;
		}
		
		ctx.drawImage(CardImageHolder.SUMIRE[this.picNumber], 400, 260);
	}
	
/*------------------
 * instance methods
 */
	/**
	 * ��������A��������̃A�j���[�V������\�����A�u���b�N���܂��B
	 * �N���b�N�����m����ƃA�j���[�V�������������܂��B
	 * @async
	 * @param�@{number} face ��(1..�΂���, 2..������)
	 */
	async animate(face) {
		this.field.add(this);
		this.face = face;
		while (true) {
			this.picNumber ^= this.face;
			this.field.draw();
			if (this.picNumber > 0) {
				if (await this.field.waitClick(500)) break;
			} else if (await this.field.waitClick(1000)) break;
		}
		this.field.pull(this); // remove
	}
}
