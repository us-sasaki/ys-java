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
    /** @type {Player[]} */ player;
    /** @type {number} */ handno;
    /** @type {string} */ contractString;
	/** @type {Problem[]} */ problem;
    /** @type {Explanation} */ sumire;
	/** @type {Button} */ quit;
    /** @type {Button} */ dd;
	/** @type {Button} */ textWindow;
	/** @type {Thread} */ mainThread;
	/** @type {SelectDialog} */ dialog;
    /** @type {YesNoDialog} */ confirmDialog;
	/** @type {boolean} */ exitSignal;
	
	/**
	 * PlayMain �I�u�W�F�N�g�𐶐����܂��B
	 */
	constructor(canvasId) {
		this.canvasId = canvasId;
		this.problem = [];
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
	 */
	placeQuitButton() {
		this.quit = new Button(this.field, "���f");
		this.quit.setBounds(540, 30, 80, 24);
		this.field.add(this.quit);
	}

	/**
	 * �_�u���_�~�[�{�^��(this.dd)�𐶐��A�z�u���܂��B
	 * this.dd �ɂ� doubleDummy {boolean} �v���p�e�B������܂��B
	 */
	placeDDButton() {
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
	 */
	placeTextButton() {
		this.textWindow = new Button(this.field, "�e�L�X�g�\��"); // �e�L�X�g�\��
		this.field.add(this.textWindow);
		this.textWindow.setBounds(540, 86, 80, 24);
	}

	/**
	 * BridgeField, Board �����������܂��B
	 */
	initialize() {
		this.field = new Field(this.canvasId);
        
		// �{�^��
		this.placeQuitButton();
		this.placeDDButton();
		this.placeTextButton();
	}
	
	/**
	 * Board �̏��������s���܂��B
	 */
	start() {
		//dialog = new SelectDialog(display, board);
		//for (int i = 0; i < problem.size(); i++) {
		//	Problem prob = (Problem)(problem.elementAt(i));
		//	dialog.addChoice(prob.getTitle());
		//}
		this.field.draw();
		//try {
		//	dialog.newHand.select(handno);
		//} catch (Exception e) {
		//	handno = -1;
		//}
		//dialog.show();
		//dialog.requestFocus();	// added 2000/8/16
		//String result = dialog.result; // dialog.dispose() �ɂ���� result �������邽��
		const result = "test board";
		//dialog.disposeDialog();	// �����ōs�� added 2001/7/15
		//if (result == null) return;
		//if (result.equals("disposed")) {
		//	exitSignal = true;
		//	return;
		//}
		
		//mainThread = Thread.currentThread();
		//if ("Video".equals(result)) {
		//	makeVideohand();
		//	GuiedPacket east = (GuiedPacket)(board.getHand(Board.EAST));
		//	east.turn(true);
		//	GuiedPacket west = (GuiedPacket)(board.getHand(Board.WEST));
		//	west.turn(true);
		//	
		//	this.dd.doubleDummy = true;	// added 02/9/16
		//	this.dd.caption = "�ʏ�ɖ߂�";	// added 02/9/16
		//	
		//} else if ("Same Hand".equals(result)) {
		//	makeLasthand();
		//} else {
		//	this.handno = -1;
		//	for (int i = 0; i < problem.size(); i++) {
		//		Problem p = (Problem)(problem.elementAt(i));
		//		
		//		if (p.getTitle().equals(result)) {
		//			this.handno = i;
		//			makeNewhand();
		//			break;
		//		}
		//	}
		//	if (this.handno == -1) { return; }
		//}
		this.handno = 0;
		main();
	}
	
	/**
	 * �_�C�A���O�ŐV�����n���h��I�������Ƃ��̏����ł��B
	 */
	makeNewhand() {
		Problem prob = (Problem)(problem.elementAt(handno));
		prob.start();
		
		board = new GuiedBoard(new BoardImpl(1));
		board.setName(prob.getTitle());
		
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// Player �ݒ�
		//
		player = new Player[4];
		player[Board.NORTH] = new RandomPlayer(board, Board.NORTH);
		player[Board.SOUTH] = new HumanPlayer(board, field, Board.SOUTH);
		
		//
		// Computer Player �ݒ�
		//
		if ((prob.getThinker() == null)||(!prob.getThinker().equals("DoubleDummyPlayer"))) {
			player[Board.EAST ] = new SimplePlayer2(board, Board.EAST);
			player[Board.WEST ] = new SimplePlayer2(board, Board.WEST, prob.getOpeningLead());
		} else if (prob.getThinker().equals("DoubleDummyPlayer")) {
			player[Board.EAST ] = new ReadAheadPlayer(board, Board.EAST);
			player[Board.WEST ] = new ReadAheadPlayer(board, Board.WEST, prob.getOpeningLead());
		} else {
			player[Board.EAST ] = new NoRufPlayer(board, Board.EAST);
			player[Board.WEST ] = new NoRufPlayer(board, Board.WEST, prob.getOpeningLead());
		}
		
		//
		// �f�B�[��
		//
		Packet[] hand = prob.getHand();
		
		board.deal(hand);
		field.repaint();
		
		//
		// �r�b�h���s��
		//
		board.setContract(prob.getContract(), Board.SOUTH);
	}
	
	private void makeVideohand() {
		Board oldBoard = board;
		board = new GuiedBoard(new BoardImpl(1));
		board.setName(oldBoard.getName());
		
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// Player �ݒ�
		//
		player = new Player[4];
		player[Board.NORTH] = new VideoPlayer(board, oldBoard, Board.NORTH);
		player[Board.EAST ] = new VideoPlayer(board, oldBoard, Board.EAST );
		player[Board.SOUTH] = new VideoPlayer(board, oldBoard, Board.SOUTH);
		player[Board.WEST ] = new VideoPlayer(board, oldBoard, Board.WEST );
		
		//
		// �f�B�[��
		//
		Packet[] hand = BridgeUtils.calculateOriginalHand(oldBoard);
		
		board.deal(hand);
		field.repaint();
		
		//
		// �r�b�h���s��
		//
		board.setContract(oldBoard.getContract(), oldBoard.getDeclarer());
	}
	
	private void makeLasthand() {
		Problem prob = (Problem)(problem.elementAt(handno));
		Board oldBoard = board;
		board = new GuiedBoard(new BoardImpl(1));
		board.setName(oldBoard.getName());
		
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// Player �ݒ�
		//
		player = new Player[4];
		player[Board.NORTH] = new RandomPlayer(board, Board.NORTH);
		player[Board.SOUTH] = new HumanPlayer(board, field, Board.SOUTH);
		
		//
		// Computer Player �ݒ�
		//
		if ((prob.getThinker() == null)||(!prob.getThinker().equals("DoubleDummyPlayer"))) {
			player[Board.EAST ] = new SimplePlayer2(board, Board.EAST);
			player[Board.WEST ] = new SimplePlayer2(board, Board.WEST, prob.getOpeningLead());
		} else if (prob.getThinker().equals("DoubleDummyPlayer")) {
			player[Board.EAST ] = new ReadAheadPlayer(board, Board.EAST);
			player[Board.WEST ] = new ReadAheadPlayer(board, Board.WEST, prob.getOpeningLead());
		} else {
			player[Board.EAST ] = new NoRufPlayer(board, Board.EAST);
			player[Board.WEST ] = new NoRufPlayer(board, Board.WEST, prob.getOpeningLead());
		}
		//
		// �f�B�[��
		//
		Packet[] hand = BridgeUtils.calculateOriginalHand(oldBoard);
		
		board.deal(hand);
		field.repaint();
		
		//
		// �r�b�h���s��
		//
		board.setContract(oldBoard.getContract(), oldBoard.getDeclarer());
	}
	
	/**
	 * start()�Ƒ΂ɂȂ郁�\�b�h�ŁAstart()�ŏ������������\�[�X�̔j�����s���܂��B
	 * start() �ƕʂ̃X���b�h����Ă΂�܂��B
	 * �_�C�A���O�̃��\�[�X�ȂǏI���������K�v�Ȃ��̂̔j�����s���܂��B
	 * ��ʂ��疾���I�ɃR�[�����邱�Ƃɂ���ă_�C�A���O���c��o�O�͉�������܂��B
	 */
	public void stop() {
		if (field != null) {
			field.removeEntity(board);
		}
		if (dialog != null) dialog.disposeDialog();
		if (confirmDialog != null) confirmDialog.disposeDialog();
		if (player != null) {
			for (int i = 0; i < player.length; i++) {
				if ((player[i] != null)&&(player[i] instanceof HumanPlayer)) {
					((HumanPlayer)player[i]).dispose();
				}
			}
		}
	}
	
	public void dispose() {
		if (field != null) field.dispose();
	}
	
	/**
	 * �n�߂̂��݂�ɂ�������\������
	 */
	protected void explain() throws InterruptedBridgeException {
		
		Problem prob = (Problem)(problem.elementAt(handno));
		
		sumire = new Explanation(field, prob.getDescription());
		contractString = prob.getContractString();
		
		field.addEntity(sumire);
		field.repaint();
		try {
			waitClick(); // �N���b�N��҂B InterruptedException ���X���[���邩��
		} catch (InterruptedException e) {
			if (confirmQuit()) {
				field.removeEntity(sumire);
				throw new InterruptedBridgeException();
			}
		}
		field.removeEntity(sumire); // ����ɂ���Ă��݂�̃X���b�h���I������
		field.repaint();
	}
	
	/**
	 * ���C�����[�v
	 * @async
	 */
	async mainLoop() {
		if (dd.doubleDummy) {
			this.board.getHand(Board.EAST).turn(true);
			this.board.getHand(Board.WEST).turn(true);
		}
		
		while (true) {
			//
			// Spot ���w�肷��
			//
			this.field.setSpot(this.board.getTurn());
			this.field.draw();
			
			let c = null;
			while (c === null) {
				c = await player[this.board.getPlayer()].play(); // �u���b�N����
			}
			this.board.play(c);
			this.field.draw();
			
			if (this.board.getStatus() == Board.SCORING) break;
		}
	}
	
	/**
	 * ���̃n���h�̕\���A�X�R�A�̕\�����s��
	 */
	protected void displayScore() throws InterruptedBridgeException {
		field.removeSpot();
		field.repaint();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			if (confirmQuit()) throw new InterruptedBridgeException();
		}
		//
		// �J�[�h��������x�\������
		//
		Packet[] original = BridgeUtils.calculateOriginalHand(board);
		for (int i = 0; i < 4; i++) {
			GuiedPacket hand = (GuiedPacket)(board.getHand(i));
			for (int j = 0; j < original[i].size(); j++) {
				Card c = original[i].peek(j);
				c.turn(true);
				hand.add(c);
			}
		}

		board.layout();
		for (int i = 0; i < 4; i++) {
			GuiedPacket hand = (GuiedPacket)(board.getHand(i));
			hand.arrange();
			hand.layout();
		}
		
		//
		// �X�R�A�\��
		//
//		Score score = new Score();
		String msg = "���ʁF" + contractString + "  ";
		String msg2;
		// ���C�N��
		int win		= BridgeUtils.countDeclarerSideWinners(board);
		int up		= win - board.getContract().getLevel() - 6;
		int make	= win - 6;
		
		if (up >= 0) {
			// ���C�N
			msg += String.valueOf(make) + "���C�N";
			msg2 = "���߂łƂ��I�I";
		} else {
			// �_�E��
			msg += String.valueOf(-up) + "�_�E��";
			msg2 = "�c�O�B������x����΂��āI";
		}
		
		msg += "("+win+"�g���b�N)\nN-S���̃X�R�A�F"+Score.calculate(board, Board.SOUTH);
		msg += "\n \n" + msg2;
		
		sumire = new Explanation(field, msg);
		if (up >= 0) 
			sumire.animate(Explanation.DELIGHTED);
		else
			sumire.animate(Explanation.SAD);
		field.addEntity(sumire);
		field.repaint();
		try {
			waitClick();
		} catch (InterruptedException e) {
			if (confirmQuit()) {
				field.removeEntity(sumire);
				clearHands();
				throw new InterruptedBridgeException();
			}
		}
		field.removeEntity(sumire);
		field.repaint();
		clearHands();
	}
	
	protected void clearHands() {
		//
		// Board �̏�Ԃ𐳂������̂ɂ��邽�߁A�n���h���N���A����
		//
		for (int i = 0; i < 4; i++) {
			Packet hand = board.getHand(i);
			while (hand.size() > 0) {
				hand.draw();
			}
		}
	}
	
	protected boolean confirmQuit() {
		try {
			confirmDialog = new YesNoDialog(
								field.getCanvas(),
								"���̃{�[�h��j�����Ē��f���܂����H");
			confirmDialog.show();
			boolean yes = confirmDialog.isYes();
			confirmDialog.disposeDialog();
			return yes;
		} catch (Exception e) {
//			e.printStackTrace();
			return true;
		}
	}
	
	/**
	 * ���C�����\�b�h�ł��B
	 */
	main() {
		try {
			explain();
			mainLoop();
			displayScore();
		} catch (InterruptedBridgeException e) {
			field.removeSpot();
			field.repaint();
		}
	}
}

class Problem {
	/** @type {string} */ title;
	/** @type {Bid} */ contract;
	/** @type {Packet[]} */ hands;
	/** @type {string} */ description;
	/** @type {string} */ openingLead;
	/** @type {string} */ thinker;

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
		for (let i = 0; i < 4; i++) draw(pile, hs[i], hands[i]);
		
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
	}

	/**
	 * ������ŗ^����ꂽ�n���h�������߂��� hand �ɐݒ肵�܂��B
	 * @param	{Packet} pile �J�[�h�̎R
	 * @param	{Packet} hand �ݒ��̃n���h
	 * @param	{string} str �n���h������
	 */
	draw(pile, hand, str) {
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
	 * @param	{string}	selectDialogId	html���̃_�C�A���O�ǉ��p div �v�f�� id
	 */
	constructor(selectName) {
		this.modalContent = document.getElementById('modal-content');
		this.modalOverlay = document.getElementById('modal-overlay');
		if (!this.modalContent || !this.modalOverlay)
			throw new Error("html error. without modal-content or modal-overlay");
		const s = document.getElementsByName('select');
		if (s.length != 1) throw new Error("html doesn't have select:"+selectName);
		this.select = s[0];
		// select �̎q�v�f���폜
		while (this.select.firstChild) { this.select.removeChild(this.select.firstChild); }
		const title = ['���u���b�W�̖��(1)', '���Ƃ肠�������O�̒������(2)', '�����(3)', '�����(4)'];
		for (let i = 0; i < title.length; i++) {
			const opt = document.createElement('option');
			opt.setAttribute('value', 'prob'+(i+1));
			opt.innerHTML = title[i];
			this.select.appendChild(opt);
		}
		this.startButton = document.getElementById('startButton');
		this.videoButton = document.getElementById('videoButton');
		this.replayButton = document.getElementById('replayButton');
	}
/*	constructor(selectDialogId) {
		const sd = document.getElementById(selectDialogId);
		// overlay �ǉ�
		const overlay = document.createElement('div');
		overlay.setAttribute('id', 'modal-overlay');
		sd.appendChild(overlay);
		// content �ǉ�
		const content = document.createElement('div');
		content.setAttribute('id', 'modal-content');
		// content �� select dialog �v�f�ǉ�
		const outerTable = document.createElement('table');
		outerTable.setAttribute('class', 'full');
		// �P�s�ځ@�^�C�g��
		const r1 = document.createElement('tr');
		r1.appendChild(document.createElement('td')).innerHTML = '�u���b�W�V�~�����[�^�[';
		outerTable.appendChild(r1);
		// �Q�s�ځ@�v���_�E���ƊJ�n�{�^���̃e�[�u��
		const innerTable = document.createElement('table');
		innerTable.setAttribute('class','full');
		const ir = document.createElement('tr');
		const pulldown = ir.appendChild(document.createElement('td')).appendChild(document.createElement('select'));

		const title = ['�u���b�W�̖��(1)', '�Ƃ肠�������O�̒������(2)', '���(3)', '���(4)'];
		for (let i = 0; i < title.length; i++) {
			const opt = document.createElement('option');
			opt.setAttribute('value', 'prob'+(i+1));
			opt.innerHTML = title[i];
			pulldown.appendChild(opt);
		}
		this.startButton = document.createElement('input');
		this.startButton.setAttribute('type', 'button');
		this.startButton.setAttribute('value', '�J�n����');
		ir.appendChild(document.createElement('td')).appendChild(this.startButton);
		innerTable.appendChild(ir);

		const r2 = document.createElement('tr');
		r2.appendChild(document.createElement('td')).appendChild(innerTable);
		outerTable.appendChild(r2);
		// �R�s�ځ@�v���C�����Đ�
		const r3 = document.createElement('tr');
		const i1 = document.createElement('input');
		i1.setAttribute('type', 'button');
		i1.setAttribute('value', '���̃v���C�������Đ�����');
		r3.appendChild(document.createElement('td')).appendChild(i1);
		outerTable.appendChild(r3);
		// �S�s�ځ@�����n���h
		const r4 = document.createElement('tr');
		const i2 = document.createElement('input');
		i2.setAttribute('type', 'button');
		i2.setAttribute('value', '�����n���h��������x�v���C����');
		r4.appendChild(document.createElement('td')).appendChild(i1);
		outerTable.appendChild(r4);

		content.appendChild(outerTable);
	}
*/
/*------------------
 * instance methods
 */
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
