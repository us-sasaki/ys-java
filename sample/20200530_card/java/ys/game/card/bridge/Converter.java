package ys.game.card.bridge;

import ys.game.card.*;
import ys.util.Param;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Card, Packet, Bid, Board オブジェクトのシリアライゼーション
 * (通信に用いる文字列化)と逆シリアライゼーションを提供します。
 * ただし、Card、Packet の逆シリアライゼーションでは、deck を
 * 指定する必要があります。
 *
 * @author		Yusuke Sasaki
 * @version		making		5, January 2001
 */
public class Converter {
	
	private static final String[] DIRECTION = {"N", "E", "S", "W"};
	
	/**
	 * Card のシリアライズ。
	 * シリアライズによって、カードオブジェクトが CA S2 HT といった２文字の
	 * 文字列に変換されます。Unspecified Card は [] に変換されます。
	 *
	 * このバージョンでは裏向きのカードの情報は捨てられます。
	 *
	 * @param		target		シリアライズ対象のカード
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
				throw new InternalError("Card オブジェクトのスートが異常な値になっています:"+target.getSuit());
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
					throw new InternalError("Card オブジェクトのバリューが異常な値になっています:"+value);
			}
			
			return result.toString();
		} catch (UnspecifiedException e) {
			return "[]";
		}
	}
	
	/**
	 * 文字列に対応する Card オブジェクトへの参照を指定した deck から取得します。
	 * 返却された Card オブジェクトは deck から削除されません。
	 * deck にないカードを指定した場合、Unspecified Card を含む場合、そのカードを
	 * specify() 後返却します。そのほかの場合、null が返却されます。
	 * フォーマット異常の場合、IllegalArgumentException がスローされます。
	 *
	 * @param		str		Cardオブジェクトを表現する文字列
	 * @param		deck	返却するCardオブジェクトを抽出するデッキ
	 * @return		抽出された Card オブジェクト
	 */
	public static Card deserializeCard(String str, Packet deck) {
		if (str.length() != 2)
			throw new IllegalArgumentException(str + "の deserialize に失敗しました。Card は２文字でなければなりません。");
		
		//
		// Joker か？
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
		// UnspecifiedCard か？
		//
		if ("[]".equals(str))
			return deck.peekUnspecified();
		
		// suit の解析
		int suit = " CDHS".indexOf(str.substring(0, 1));
		if ( (suit < 1)||(suit > 4) )
			throw new IllegalArgumentException(str + "の deserialize に失敗しました。スート異常です。");
		
		// value の解析
		int value = " A23456789TJQK".indexOf(str.substring(1, 2));
		if ( (value < 1)||(value > 13) )
			throw new IllegalArgumentException(str + "の deserialize に失敗しました。バリュー異常です。");
		
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
	 * Packet オブジェクトをシリアライズします。
	 * Packet オブジェクトが空の場合、ヌル文字が返却されます。
	 * シリアライズは、S375-H235-SK-D53-[][] のように行われます。
	 */
	public static String serialize(Packet target) {
		if (target.size() == 0) return "";
		
		StringBuffer result = new StringBuffer();
		int lastSuit = -1;
		
		for (int i = 0; i < target.size(); i++) {
			Card c = target.peek(i);
			try {
				//
				// スート文字の追加、前回のスートと異なっていればスート記号を追加
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
						throw new InternalError("Card オブジェクトの Suit が異常な値("
										+ suit + ")になっています");
					}
				}
				//
				// バリュー文字
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
						throw new InternalError("Card オブジェクトのバリューが異常な値になっています:"
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
	 * 文字列から Packet オブジェクトを復元します。
	 * 文字列は、S,H,D,C の６文字はスート文字で、以降に現れるバリュー文字のスートを
	 * 指定します。2,3,4,5,6,7,8,9,T,J,Q,K,A はバリュー文字で現在のスートのカードを
	 * あらわします。[ は UnspecifiedCard をあらわし、j は Joker をあらわします。
	 * これらの文字以外は無視されます。
	 * 結果の Packet は PacketImpl のインスタンスとして新規生成され、その構成 Card の
	 * インスタンスは deck から取得されます。取得できない card があった場合は無視され、
	 * 処理が続行されます。
	 * 構成 Card は deck から削除されます。
	 *
	 */
	public static Packet deserializePacket(String str, Packet deck) {
		Packet p = new PacketImpl();
		
		int lastSuit = -1;
		
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			
			switch (c) {
			
			// UnspecifiedCard を追加する
			case '[': Card uns = deck.drawUnspecified(); p.add(uns); lastSuit = -1; continue;
			
			// Joker を追加する
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
			
			// 通常のカードを追加する
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
					throw new IllegalArgumentException("文字列のフォーマット異常です。スートの定まらないカードがありました");
			
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
	 * Board の status 値を文字列に変換する private 関数。
	 */
	public static String statusStr(int status) {
		switch (status) {
		case Board.DEALING: return "DEAL";
		case Board.BIDDING: return "BID";
		case Board.OPENING: return "OPEN";
		case Board.PLAYING: return "PLAY";
		case Board.SCORING: return "END";
		
		default: throw new InternalError("Boardのステータスが異常値("+
								status+")になっています");
		}
	}
	
	public static int statusVal(String str) {
		if ("DEAL".equals(str))	return Board.DEALING;
		if ("BID".equals(str))	return Board.BIDDING;
		if ("OPEN".equals(str))	return Board.OPENING;
		if ("PLAY".equals(str))	return Board.PLAYING;
		if ("END".equals(str))	return Board.SCORING;
		
		throw new IllegalArgumentException("status文字列フォーマット異常です。");
	}
	
	/**
	 * Board の direction 値を文字列に変換する private 関数。
	 */
	public static String seatStr(int direction) {
		switch (direction) {
		case Board.NORTH: return "N";
		case Board.EAST : return "E";
		case Board.SOUTH: return "S";
		case Board.WEST : return "W";
		
		default: throw new InternalError("Direction が異常値です。Converter.seatStr()");
		}
	}
	
	public static int seatVal(String str) {
		if ("N".equals(str)) return Board.NORTH;
		if ("E".equals(str)) return Board.EAST;
		if ("S".equals(str)) return Board.SOUTH;
		if ("W".equals(str)) return Board.WEST;
		
		throw new IllegalArgumentException("席を表す文字列不正です。");
	}
	
	/**
	 * Board の vul 値を文字列に変換する private 関数。
	 */
	public static String vulStr(int vul) {
		switch (vul) {
		case Board.VUL_NONE: return "No";
		case Board.VUL_NS: return "NS";
		case Board.VUL_EW: return "EW";
		case Board.VUL_BOTH: return "Bo";
		
		default: throw new InternalError("Vul 値が異常です。Converter.vulStr()");
		}
	}
	
	public static int vulVal(String str) {
		if ("No".equals(str)) return Board.VUL_NONE;
		if ("NS".equals(str)) return Board.VUL_NS;
		if ("EW".equals(str)) return Board.VUL_EW;
		if ("Bo".equals(str)) return Board.VUL_BOTH;
		
		throw new IllegalArgumentException("Vul文字列フォーマット異常です");
	}
	
	/**
	 * Bid を文字列に変換する private 関数。
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
				throw new IllegalArgumentException("Bid のフォーマット異常です。");
		
		switch (str.charAt(1)) {
		case 'S': suit = Bid.SPADE;		break;
		case 'H': suit = Bid.HEART;		break;
		case 'D': suit = Bid.DIAMOND;	break;
		case 'C': suit = Bid.CLUB;		break;
		case 'N': suit = Bid.NO_TRUMP;	break;
		
		default: throw new IllegalArgumentException("Bidスートフォーマット異常です。");
		}
		
		if (str.length() == 2) return new Bid(Bid.BID, level, suit);
		
		if ( (str.length() == 3)&&(str.endsWith("X")) )
				return new Bid(Bid.DOUBLE, level, suit);
		
		if ( (str.length() == 4)&&(str.endsWith("XX")) )
				return new Bid(Bid.REDOUBLE, level, suit);
		
		throw new IllegalArgumentException("Bidフォーマット異常です。");
	}
	
	/**
	 * 与えられたボードにおけるはじめのハンドを計算します。
	 */
	public static Packet[] calculateOriginalHand(Board board) {
		Packet[] result = new Packet[4];
		for (int i = 0; i < 4; i++) result[i] = new PacketImpl();
		
		// 今もっているハンドをコピー
		Packet[] original = board.getHand();
		for (int i = 0; i < original.length; i++) {
			for (int j = 0; j < original[i].size(); j++) {
				result[i].add(original[i].peek(j));
			}
		}
		
		// プレイされたハンドをコピー
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
		
		// 並べ替え
		for (int i = 0; i < 4; i++) {
			result[i].arrange();
		}
		return result;
	}
	
	/**
	 * Board のシリアライズを行います。
	 * Board のシリアライズはURLエンコーディングに類似した、次のような形式で行われます。
	 *<PRE>
	 * ＜全体の形式＞
	 * key1=value1&key2=value2&key3=value3&...
	 *
	 * ＜キーとバリュー＞
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
	 * p(n)             | (Card String) × 4 ex. SAS4S2SQ
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
		
		// DEALING状態ではここまで
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
		
		
		// OPENING状態ではここまで
		if (target.getStatus() == Board.OPENING) return result.toString();
		
		// play 履歴
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
	 * 指定された文字列から指定された Board の内容を復元します。
	 * 指定された Board は、reset() されます。
	 */
	public static void deserializeBoard(String str, Board target) {
		Hashtable ht = Param.parse(str);
		
		// status 値取得
		int status	= statusVal((String)(ht.get("status")));
		
		if ( (status < Board.DEALING)||(status > Board.SCORING) )
			throw new IllegalArgumentException("status値範囲外です");
		
		// dealer 値取得
		int dealer	= seatVal((String)(ht.get("dealer")));
		
		if ( (dealer < Board.NORTH)||(dealer > Board.WEST) )
			throw new IllegalArgumentException("dealer値範囲外です");
		
		// vul 値取得
		int vul		= vulVal((String)(ht.get("vul")));
		
		if ( (vul < 0)||(vul > 3) )
			throw new IllegalArgumentException("vul値範囲外です");
		
		// Board reset
		target.reset(dealer, vul);
		
		if (status == Board.DEALING) return;
		
		// Hand
		Packet deck = PacketFactory.provideUnspecifiedDeck(PacketFactory.WITHOUT_JOKER);
		Packet[] hand = new Packet[4];
		for (int i = 0; i < 4; i++) {
			String s = (String)(ht.get(DIRECTION[i]));
			if (s == null)
					throw new IllegalArgumentException(DIRECTION[i] + " パラメータがありません");
			hand[i] = deserializePacket(s, deck);
		}
		if (deck.size() > 0)
			throw new IllegalArgumentException("ハンドのカード枚数が足りません");
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
		
		// contract, declarer を取得。
		// ビッドが実際に行われているときは bid シーケンスとの整合性を見る。
		//
		// (未実装)
		
		// play 履歴
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
		
		// 状態のチェック
		if (status != target.getStatus())
				throw new IllegalArgumentException("ボードのステータス矛盾です。");
		
	}
	
}
