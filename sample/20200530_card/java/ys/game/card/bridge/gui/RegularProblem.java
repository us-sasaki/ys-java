package ys.game.card.bridge.gui;

import ys.game.card.bridge.*;
import ys.game.card.*;

/**
 * �u���b�W�V�~�����[�^�̃n���h�Ȃǂ̃f�[�^���i�[����N���X�B
 *
 * @version		release		5, September 2000
 * @author		Yusuke Sasaki
 */
public class RegularProblem implements Problem {
	protected String	title;
	protected int		kind;
	protected int		level;
	protected int		denomination;
	
	/** NSEW �̃n���h */
	protected String[]	handStr;
	
	protected String	description;
	
	protected String	openingLead;
	
	/** �v�l���[�`�������������񂪊i�[����܂� */
	protected String	thinker;
	
/*-------------
 * Constructor
 */
	public RegularProblem(	String	title,
					int		kind,
					int		level,
					int		denomination,
					String[] hand,
					String	description,
					String	openingLead) {
		this.title	= title;
		this.kind	= kind;
		this.level	= level;
		this.denomination = denomination;
		this.handStr	= hand;
		this.description = description;
		this.openingLead = openingLead;
	}
	
	public RegularProblem(	String	title,
					int		kind,
					int		level,
					int		denomination,
					String[] hand,
					String	description,
					String	openingLead,
					String	thinker) {
		this(title, kind, level, denomination, hand, description, openingLead);
		this.thinker = thinker;
	}
/*------------------
 * instance methods
 */
	public void start() {
		// ���͌Œ�I�Ȃ̂ŁA���ɏ����͍s��Ȃ��B
	}
	
	public String getTitle() {
		return title;
	}
	
	public Bid getContract() {
		return new Bid(kind, level, denomination);
	}
	
	public String getThinker() {
		return thinker;
	}
	
	/**
	 * ������ŗ^����ꂽ�n���h�������Ƃɂm�r�d�v�̃n���h��ݒ肵�܂��B
	 * �w�肳�ꂽ��������ɂ��������Ăm�r�d�v�̃n���h�ɔz��A��������
	 * �Ŗ���������Ȃ��ꍇ�̓����_���Ɏc�薇������z��܂��B
	 *
	 * @param		north		North�̃n���h��� (S:AKQJT H:9532 ..... �̂悤�ȕ�����)
	 * @param		south		South�̃n���h��� (S:AKQJT H:9532 ..... �̂悤�ȕ�����)
	 */
	public Packet[] getHand() {
		Packet[] hand = new Packet[4];
		for (int i = 0; i < 4; i++) hand[i] = new PacketImpl();
		
		Packet pile = PacketFactory.provideDeck(PacketFactory.WITHOUT_JOKER);
		
		for (int i = 0; i < 4; i++) {
			if (!handStr[i].equalsIgnoreCase("Rest")) {
				draw(pile, hand[i], handStr[i]);
			}
		}
		
		// �c��̓����_���ɔz��
		if (pile.size() > 0) {
			pile.shuffle();
			for (int i = 0; i < 4; i++) {
				for (int j = hand[i].size(); j < 13; j++) {
					hand[i].add(pile.draw());
					// �r���� pile ���s���邱�Ƃ�����
					// ���̏ꍇ�́AisValid() �� false ��Ԃ����ƂɂȂ�
				}
			}
		}
		for (int i = 0; i < 4; i++) {
			hand[i].arrange();
		}
		
		return hand;
	}
	
	/**
	 * ������ŗ^����ꂽ�n���h�������߂��� hand �ɐݒ肵�܂��B
	 */
	private void draw(Packet pile, Packet hand, String str) {
		int suit = Card.SPADE;
		
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			
			if (c == 'S') {
				suit = Card.SPADE;
			} else if (c == 'H') {
				suit = Card.HEART;
			} else if (c == 'D') {
				suit = Card.DIAMOND;
			} else if (c == 'C') {
				suit = Card.CLUB;
			} else if (c == 'K') {
				hand.add(pile.draw(suit, Card.KING));
			} else if (c == 'Q') {
				hand.add(pile.draw(suit, Card.QUEEN));
			} else if (c == 'J') {
				hand.add(pile.draw(suit, Card.JACK));
			} else if (c == 'T') {
				hand.add(pile.draw(suit, 10));
			} else if (c == 'A') {
				hand.add(pile.draw(suit, Card.ACE));
			} else if ( (c >= '2')&&(c <= '9') ) {
				hand.add(pile.draw(suit, (int)(c - '0') ));
			}
		}
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getContractString() {
		String result = String.valueOf(level);
		
		switch (denomination) {
		
		case Bid.NO_TRUMP:
			result += "NT";
			break;
		case Bid.SPADE:
			result += "S";
			break;
		case Bid.HEART:
			result += "H";
			break;
		case Bid.DIAMOND:
			result += "D";
			break;
		case Bid.CLUB:
			result += "C";
		}
		
		return result;
	}
	
	public String getOpeningLead() {
		return openingLead;
	}
	
	public boolean isValid() {
		if ((kind != Bid.BID)&&(kind != Bid.DOUBLE)&&(kind != Bid.REDOUBLE)) return false;
		if ((level < 1)||(kind > 7)) return false;
		if ((denomination < Bid.CLUB)||(denomination > Bid.NO_TRUMP)) return false;
		
		Packet[] pack;
		try {
			pack = getHand();
		} catch (Exception e) {
			return false;
		}
		for (int i = 0; i < 4; i++) {
			if (pack[i].size() != 13) return false;
		}
		
		return true;
		
	}
}
