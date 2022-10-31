package ys.game.card.bridge;

import ys.game.card.*;
import ys.util.Param;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Card, Packet, Bid, Board �I�u�W�F�N�g�̃V���A���C�[�[�V����
 * (�ʐM�ɗp���镶����)�Ƌt�V���A���C�[�[�V������񋟂��܂��B
 * �������ACard�APacket �̋t�V���A���C�[�[�V�����ł́Adeck ��
 * �w�肷��K�v������܂��B
 *
 * @author		Yusuke Sasaki
 * @version		making		5, January 2001
 */
public class Converter {
	
	private static final String[] DIRECTION = {"N", "E", "S", "W"};
	
	/**
	 * Card �̃V���A���C�Y�B
	 * �V���A���C�Y�ɂ���āA�J�[�h�I�u�W�F�N�g�� CA S2 HT �Ƃ������Q������
	 * ������ɕϊ�����܂��BUnspecified Card �� [] �ɕϊ�����܂��B
	 *
	 * ���̃o�[�W�����ł͗������̃J�[�h�̏��͎̂Ă��܂��B
	 *
	 * @param		target		�V���A���C�Y�Ώۂ̃J�[�h
	 */
	public static String serialize(Card target) {
		try {
			StringBuffer result = new StringBuffer();
			
			switch (target.getSuit()) {
			
			case Card.JOKER:	return "jo";
			case Card.SPADE:	result.append('S'); break;
			case Card.HEART:	result.append('H'); break;
			case Card.DIAMOND:	result.append('D'); break;
			case Card.CLUB:		result.append('C'); break;
			default:
				throw new InternalError("Card �I�u�W�F�N�g�̃X�[�g���ُ�Ȓl�ɂȂ��Ă��܂�:"+target.getSuit());
			}
			
			int value = target.getValue();
			
			switch (value) {
			
			case Card.ACE:	result.append('A'); break;
			case Card.KING:	result.append('K'); break;
			case Card.QUEEN:	result.append('Q'); break;
			case Card.JACK:	result.append('J'); break;
			case 10:		result.append('T'); break;
			default:
				if ( (value >= 2)&&(value <= 9) )
					result.append(Integer.toString(value));
				else
					throw new InternalError("Card �I�u�W�F�N�g�̃o�����[���ُ�Ȓl�ɂȂ��Ă��܂�:"+value);
			}
			
			return result.toString();
		} catch (UnspecifiedException e) {
			return "[]";
		}
	}
	
	/**
	 * ������ɑΉ����� Card �I�u�W�F�N�g�ւ̎Q�Ƃ��w�肵�� deck ����擾���܂��B
	 * �ԋp���ꂽ Card �I�u�W�F�N�g�� deck ����폜����܂���B
	 * deck �ɂȂ��J�[�h���w�肵���ꍇ�AUnspecified Card ���܂ޏꍇ�A���̃J�[�h��
	 * specify() ��ԋp���܂��B���̂ق��̏ꍇ�Anull ���ԋp����܂��B
	 * �t�H�[�}�b�g�ُ�̏ꍇ�AIllegalArgumentException ���X���[����܂��B
	 *
	 * @param		str		Card�I�u�W�F�N�g��\�����镶����
	 * @param		deck	�ԋp����Card�I�u�W�F�N�g�𒊏o����f�b�L
	 * @return		���o���ꂽ Card �I�u�W�F�N�g
	 */
	public static Card deserializeCard(String str, Packet deck) {
		if (str.length() != 2)
			throw new IllegalArgumentException(str + "�� deserialize �Ɏ��s���܂����BCard �͂Q�����łȂ���΂Ȃ�܂���B");
		
		//
		// Joker ���H
		//
		if ("jo".equals(str)) {
			try {
				return deck.peek(Card.JOKER, Card.JOKER);
			} catch (UnspecifiedException e) {
				Card unspecified = deck.peekUnspecified();
				try {
					unspecified.specify(Card.JOKER, Card.JOKER);
				} catch (AlreadySpecifiedException f) {
					return null;
				}
				return unspecified;
			}
		}
		
		//
		// UnspecifiedCard ���H
		//
		if ("[]".equals(str))
			return deck.peekUnspecified();
		
		// suit �̉��
		int suit = " CDHS".indexOf(str.substring(0, 1));
		if ( (suit < 1)||(suit > 4) )
			throw new IllegalArgumentException(str + "�� deserialize �Ɏ��s���܂����B�X�[�g�ُ�ł��B");
		
		// value �̉��
		int value = " A23456789TJQK".indexOf(str.substring(1, 2));
		if ( (value < 1)||(value > 13) )
			throw new IllegalArgumentException(str + "�� deserialize �Ɏ��s���܂����B�o�����[�ُ�ł��B");
		
		try {
			return deck.peek(suit, value);
		} catch (UnspecifiedException e) {
			Card unspecified = deck.peekUnspecified();
			try {
				unspecified.specify(suit, value);
			} catch (AlreadySpecifiedException f) {
				return null;
			}
			return unspecified;
		}
	}
	
	/**
	 * Packet �I�u�W�F�N�g���V���A���C�Y���܂��B
	 * Packet �I�u�W�F�N�g����̏ꍇ�A�k���������ԋp����܂��B
	 * �V���A���C�Y�́AS375-H235-SK-D53-[][] �̂悤�ɍs���܂��B
	 */
	public static String serialize(Packet target) {
		if (target.size() == 0) return "";
		
		StringBuffer result = new StringBuffer();
		int lastSuit = -1;
		
		for (int i = 0; i < target.size(); i++) {
			Card c = target.peek(i);
			try {
				//
				// �X�[�g�����̒ǉ��A�O��̃X�[�g�ƈقȂ��Ă���΃X�[�g�L����ǉ�
				//
				int suit = c.getSuit(); // may throw UnspecifiedException
				if (suit != lastSuit) {
//					if (i > 0) result.append('-');
					lastSuit = suit;
					switch (suit) {
					
					case Card.SPADE:	result.append('S'); break;
					case Card.HEART:	result.append('H'); break;
					case Card.DIAMOND:	result.append('D'); break;
					case Card.CLUB:		result.append('C'); break;
					case Card.JOKER:	result.append("jo"); continue;
					default:
						throw new InternalError("Card �I�u�W�F�N�g�� Suit ���ُ�Ȓl("
										+ suit + ")�ɂȂ��Ă��܂�");
					}
				}
				//
				// �o�����[����
				//
				int value = c.getValue();
				switch (value) {
				
				case Card.ACE:		result.append('A'); break;
				case Card.KING:		result.append('K'); break;
				case Card.QUEEN:	result.append('Q'); break;
				case Card.JACK:		result.append('J'); break;
				case 10:			result.append('T'); break;
				default:
					if ( (value >= 2)&&(value <= 9) )
						result.append(Integer.toString(value));
					else
						throw new InternalError("Card �I�u�W�F�N�g�̃o�����[���ُ�Ȓl�ɂȂ��Ă��܂�:"
									+ value);
				}
			} catch (UnspecifiedException e) {
//				if (lastSuit != -1) result.append('-');
				lastSuit = -1;
				result.append("[]");
			}
		}
		return result.toString();
	}
	
	/**
	 * �����񂩂� Packet �I�u�W�F�N�g�𕜌����܂��B
	 * ������́AS,H,D,C �̂U�����̓X�[�g�����ŁA�ȍ~�Ɍ����o�����[�����̃X�[�g��
	 * �w�肵�܂��B2,3,4,5,6,7,8,9,T,J,Q,K,A �̓o�����[�����Ō��݂̃X�[�g�̃J�[�h��
	 * ����킵�܂��B[ �� UnspecifiedCard ������킵�Aj �� Joker ������킵�܂��B
	 * �����̕����ȊO�͖�������܂��B
	 * ���ʂ� Packet �� PacketImpl �̃C���X�^���X�Ƃ��ĐV�K��������A���̍\�� Card ��
	 * �C���X�^���X�� deck ����擾����܂��B�擾�ł��Ȃ� card ���������ꍇ�͖�������A
	 * ���������s����܂��B
	 * �\�� Card �� deck ����폜����܂��B
	 *
	 */
	public static Packet deserializePacket(String str, Packet deck) {
		Packet p = new PacketImpl();
		
		int lastSuit = -1;
		
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			
			switch (c) {
			
			// UnspecifiedCard ��ǉ�����
			case '[': Card uns = deck.drawUnspecified(); p.add(uns); lastSuit = -1; continue;
			
			// Joker ��ǉ�����
			case 'j':
				try {
					Card joker = deck.draw(Card.JOKER, Card.JOKER);
					p.add(joker);
				} catch (UnspecifiedException e) {
					Card joker = deck.drawUnspecified();
					joker.specify(Card.JOKER, Card.JOKER);
					p.add(joker);
				}
				lastSuit = Card.JOKER;
				continue;
			
			// �ʏ�̃J�[�h��ǉ�����
			case 'S': lastSuit = Card.SPADE; continue;
			case 'H': lastSuit = Card.HEART; continue;
			case 'D': lastSuit = Card.DIAMOND; continue;
			case 'C': lastSuit = Card.CLUB; continue;
			}
			int value = -1;
			
			switch (c) {
			
			case 'A': value = Card.ACE; break;
			case 'K': value = Card.KING; break;
			case 'Q': value = Card.QUEEN; break;
			case 'J': value = Card.JACK; break;
			case 'T': value = 10; break;
			
			default:
				value = c - '0';
				if ( (value < 2)||(value > 9) ) continue;
			}
			if ( (lastSuit < 1)||(lastSuit > 4) )
					throw new IllegalArgumentException("������̃t�H�[�}�b�g�ُ�ł��B�X�[�g�̒�܂�Ȃ��J�[�h������܂���");
			
			Card toBeAdded = null;
			try {
				toBeAdded = deck.draw(lastSuit, value);
			} catch (UnspecifiedException e) {
				toBeAdded = deck.drawUnspecified();
				try {
					toBeAdded.specify(lastSuit, value);
				} catch (AlreadySpecifiedException f) {
					continue;
				}
			}
			p.add(toBeAdded);
		}
		return p;
	}
	
	/**
	 * Board �� status �l�𕶎���ɕϊ����� private �֐��B
	 */
	public static String statusStr(int status) {
		switch (status) {
		case Board.DEALING: return "DEAL";
		case Board.BIDDING: return "BID";
		case Board.OPENING: return "OPEN";
		case Board.PLAYING: return "PLAY";
		case Board.SCORING: return "END";
		
		default: throw new InternalError("Board�̃X�e�[�^�X���ُ�l("+
								status+")�ɂȂ��Ă��܂�");
		}
	}
	
	public static int statusVal(String str) {
		if ("DEAL".equals(str))	return Board.DEALING;
		if ("BID".equals(str))	return Board.BIDDING;
		if ("OPEN".equals(str))	return Board.OPENING;
		if ("PLAY".equals(str))	return Board.PLAYING;
		if ("END".equals(str))	return Board.SCORING;
		
		throw new IllegalArgumentException("status������t�H�[�}�b�g�ُ�ł��B");
	}
	
	/**
	 * Board �� direction �l�𕶎���ɕϊ����� private �֐��B
	 */
	public static String seatStr(int direction) {
		switch (direction) {
		case Board.NORTH: return "N";
		case Board.EAST : return "E";
		case Board.SOUTH: return "S";
		case Board.WEST : return "W";
		
		default: throw new InternalError("Direction ���ُ�l�ł��BConverter.seatStr()");
		}
	}
	
	public static int seatVal(String str) {
		if ("N".equals(str)) return Board.NORTH;
		if ("E".equals(str)) return Board.EAST;
		if ("S".equals(str)) return Board.SOUTH;
		if ("W".equals(str)) return Board.WEST;
		
		throw new IllegalArgumentException("�Ȃ�\��������s���ł��B");
	}
	
	/**
	 * Board �� vul �l�𕶎���ɕϊ����� private �֐��B
	 */
	public static String vulStr(int vul) {
		switch (vul) {
		case Board.VUL_NONE: return "No";
		case Board.VUL_NS: return "NS";
		case Board.VUL_EW: return "EW";
		case Board.VUL_BOTH: return "Bo";
		
		default: throw new InternalError("Vul �l���ُ�ł��BConverter.vulStr()");
		}
	}
	
	public static int vulVal(String str) {
		if ("No".equals(str)) return Board.VUL_NONE;
		if ("NS".equals(str)) return Board.VUL_NS;
		if ("EW".equals(str)) return Board.VUL_EW;
		if ("Bo".equals(str)) return Board.VUL_BOTH;
		
		throw new IllegalArgumentException("Vul������t�H�[�}�b�g�ُ�ł�");
	}
	
	/**
	 * Bid �𕶎���ɕϊ����� private �֐��B
	 */
	public static String bidStr(Bid bid) {
		int kind	= bid.getKind();
		if (kind == Bid.PASS) return "P";
		
		String result  = Integer.toString(bid.getLevel());
		switch (bid.getSuit()) {
		case Bid.SPADE:		result += "S"; break;
		case Bid.HEART:		result += "H"; break;
		case Bid.DIAMOND:	result += "D"; break;
		case Bid.CLUB:		result += "C"; break;
		case Bid.NO_TRUMP:	result += "N"; break;
		default: throw new InternalError();
		}
		
		switch (kind) {
		case Bid.BID: return result;
		case Bid.DOUBLE: return result + "X";
		case Bid.REDOUBLE: return result + "XX";
		}
		throw new InternalError();
	}
	
	public static Bid bidVal(String str) {
		if ("P".equals(str)) return new Bid(Bid.PASS);
		
		int level, suit;
		
		level = str.charAt(0) - '0';
		if ( (level < 1)||(level > 7) )
				throw new IllegalArgumentException("Bid �̃t�H�[�}�b�g�ُ�ł��B");
		
		switch (str.charAt(1)) {
		case 'S': suit = Bid.SPADE;		break;
		case 'H': suit = Bid.HEART;		break;
		case 'D': suit = Bid.DIAMOND;	break;
		case 'C': suit = Bid.CLUB;		break;
		case 'N': suit = Bid.NO_TRUMP;	break;
		
		default: throw new IllegalArgumentException("Bid�X�[�g�t�H�[�}�b�g�ُ�ł��B");
		}
		
		if (str.length() == 2) return new Bid(Bid.BID, level, suit);
		
		if ( (str.length() == 3)&&(str.endsWith("X")) )
				return new Bid(Bid.DOUBLE, level, suit);
		
		if ( (str.length() == 4)&&(str.endsWith("XX")) )
				return new Bid(Bid.REDOUBLE, level, suit);
		
		throw new IllegalArgumentException("Bid�t�H�[�}�b�g�ُ�ł��B");
	}
	
	/**
	 * �^����ꂽ�{�[�h�ɂ�����͂��߂̃n���h���v�Z���܂��B
	 */
	public static Packet[] calculateOriginalHand(Board board) {
		Packet[] result = new Packet[4];
		for (int i = 0; i < 4; i++) result[i] = new PacketImpl();
		
		// �������Ă���n���h���R�s�[
		Packet[] original = board.getHand();
		for (int i = 0; i < original.length; i++) {
			for (int j = 0; j < original[i].size(); j++) {
				result[i].add(original[i].peek(j));
			}
		}
		
		// �v���C���ꂽ�n���h���R�s�[
		PlayHistory hist = board.getPlayHistory();
		for (int i = 0; i < hist.getTricks(); i++) {
			Trick tr = hist.getTrick(i);
			int seat = tr.getLeader();
			for (int j = 0; j < tr.size(); j++) {
				result[seat].add(tr.peek(j));
				seat++;
				seat = (seat % 4);
			}
		}
		
		// ���בւ�
		for (int i = 0; i < 4; i++) {
			result[i].arrange();
		}
		return result;
	}
	
	/**
	 * Board �̃V���A���C�Y���s���܂��B
	 * Board �̃V���A���C�Y��URL�G���R�[�f�B���O�ɗގ������A���̂悤�Ȍ`���ōs���܂��B
	 *<PRE>
	 * ���S�̂̌`����
	 * key1=value1&key2=value2&key3=value3&...
	 *
	 * ���L�[�ƃo�����[��
	 * key              | value
	 * -----------------+---------------------------------------------------------
	 * status           | DEAL or BID or OPEN or PLAY or END
	 * dealer           | N or E or S or W
	 * vul              | No or NS or EW or Both
	 * N                | (Packet String)
	 * E                | (Packet String)
	 * S                | (Packet String)
	 * W                | (Packet String)
	 * bid              | P-P-P-1N-P-2C-2CX-2S-P-4S-P-P-P
	 * contract         | 4S
	 * declarer         | N or E or S or W
	 * l(n)             | N or E or S or W
	 * p(n)             | (Card String) �~ 4 ex. SAS4S2SQ
	 *</PRE>
	 */
	public static String serialize(Board target) {
		StringBuffer result = new StringBuffer();
		
		// status
		result.append("status=");
		result.append(statusStr(target.getStatus()));
		
		// dealer
		result.append("&dealer=");
		result.append(seatStr(target.getDealer()));
		
		// vul
		result.append("&vul=");
		result.append(vulStr(target.getVulnerability()));
		
		// DEALING��Ԃł͂����܂�
		if (target.getStatus() == Board.DEALING) return result.toString();
		
		// Hand
		Packet[] original = calculateOriginalHand(target);
		for (int i = 0; i < 4; i++) {
			result.append("&"+DIRECTION[i]+"=");
			result.append(serialize(original[i]));
		}
		
		// bid
		BiddingHistory bh = target.getBiddingHistory();
		Bid[] b = bh.getAllBids();
		if (b.length > 0) {
			result.append("&bid=");
			for (int i = 0; i < b.length; i++) {
				result.append(bidStr(b[i]));
				if (i < b.length - 1) result.append('-');
			}
		}
		// contract
		Bid contract = target.getContract();
		if (contract != null) {
			result.append("&contract=");
			result.append(bidStr(contract));
			result.append("&declarer=");
			result.append(seatStr(target.getDeclarer()));
		}
		
		
		// OPENING��Ԃł͂����܂�
		if (target.getStatus() == Board.OPENING) return result.toString();
		
		// play ����
		Trick[] trick = target.getAllTricks();
		if (trick != null) {
			for (int i = 1; i < trick.length + 1; i++) {
				Trick t = trick[i-1];
				result.append("&p" + i + "=");
				for (int j = 0; j < t.size(); j++) {
					result.append(serialize(t.peek(j)));
//					if (j < t.size() - 1) result.append('-');
				}
			}
		}
		
		return result.toString();
	}
	
	/**
	 * �w�肳�ꂽ�����񂩂�w�肳�ꂽ Board �̓��e�𕜌����܂��B
	 * �w�肳�ꂽ Board �́Areset() ����܂��B
	 */
	public static void deserializeBoard(String str, Board target) {
		Hashtable ht = Param.parse(str);
		
		// status �l�擾
		int status	= statusVal((String)(ht.get("status")));
		
		if ( (status < Board.DEALING)||(status > Board.SCORING) )
			throw new IllegalArgumentException("status�l�͈͊O�ł�");
		
		// dealer �l�擾
		int dealer	= seatVal((String)(ht.get("dealer")));
		
		if ( (dealer < Board.NORTH)||(dealer > Board.WEST) )
			throw new IllegalArgumentException("dealer�l�͈͊O�ł�");
		
		// vul �l�擾
		int vul		= vulVal((String)(ht.get("vul")));
		
		if ( (vul < 0)||(vul > 3) )
			throw new IllegalArgumentException("vul�l�͈͊O�ł�");
		
		// Board reset
		target.reset(dealer, vul);
		
		if (status == Board.DEALING) return;
		
		// Hand
		Packet deck = PacketFactory.provideUnspecifiedDeck(PacketFactory.WITHOUT_JOKER);
		Packet[] hand = new Packet[4];
		for (int i = 0; i < 4; i++) {
			String s = (String)(ht.get(DIRECTION[i]));
			if (s == null)
					throw new IllegalArgumentException(DIRECTION[i] + " �p�����[�^������܂���");
			hand[i] = deserializePacket(s, deck);
		}
		if (deck.size() > 0)
			throw new IllegalArgumentException("�n���h�̃J�[�h����������܂���");
		target.deal(hand);
		
		// bid
		String bidSeq = (String)(ht.get("bid"));
		if ( (bidSeq == null)&&(status == Board.BIDDING) ) return;
		
		StringTokenizer t = new StringTokenizer(bidSeq, "-");
		while (t.hasMoreTokens()) {
			String token = t.nextToken();
			if (!token.equals("")) target.play(bidVal(token));
		}
		
		if (status == Board.BIDDING) return;
		
		// contract, declarer ���擾�B
		// �r�b�h�����ۂɍs���Ă���Ƃ��� bid �V�[�P���X�Ƃ̐�����������B
		//
		// (������)
		
		// play ����
	lp:
		for (int i = 1; i < 14; i++) {
			String play		= (String)(ht.get("p"+i));
			
			if (play == null) break;
//			StringTokenizer st = new StringTokenizer(play, "-");
			for (int j = 0; j < 4; j++) {
//				if (!st.hasMoreTokens()) break lp;
				
//				String p = st.nextToken();
				String p = play.substring(j*2, j*2+2);
				target.play(deserializeCard(p, target.getHand(target.getTurn())));
			}
		}
		
		// ��Ԃ̃`�F�b�N
		if (status != target.getStatus())
				throw new IllegalArgumentException("�{�[�h�̃X�e�[�^�X�����ł��B");
		
	}
	
}
