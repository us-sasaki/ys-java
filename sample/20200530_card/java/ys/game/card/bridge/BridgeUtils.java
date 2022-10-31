package ys.game.card.bridge;

import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.Card;
import ys.game.card.CardImpl;

/**
 * ブリッジ固有の概念に関する各種便利 static 関数を提供します。
 *
 * @version		a-release		7, October 2001
 * @author		Yusuke Sasaki
 */
public class BridgeUtils {
	/**
	 * 指定されたハンドにおいて、指定スーツのアナーの点をカウントします。
	 * アナー点は、A:4 K:3 Q:2 J:1 で計算します。
	 *
	 * @param		hand		アナー点計算の対象となるハンド
	 * @param		suit		カウントしたいスートの指定
	 * @return		アナー点(0から10の間)
	 */
	public static int countHonerPoint(Packet hand, int suit) {
		int result = 0;
		Packet oneSuit = hand.subpacket(suit);
		
		if (oneSuit.containsValue(Card.ACE  )) result += 4;
		if (oneSuit.containsValue(Card.KING )) result += 3;
		if (oneSuit.containsValue(Card.QUEEN)) result += 2;
		if (oneSuit.containsValue(Card.JACK )) result += 1;
		
		return result;
	}
	
	/**
	 * 指定されたハンドでのアナーの枚数をカウントします。
	 * ただし、アナーが１枚でもあるスートについては、10 もアナーとみなします。
	 */
	public static int countHoners(Packet hand, int suit) {
		int result = 0;
		Packet oneSuit = hand.subpacket(suit);
		
		if (oneSuit.containsValue(Card.ACE  )) result++;
		if (oneSuit.containsValue(Card.KING )) result++;
		if (oneSuit.containsValue(Card.QUEEN)) result++;
		if (oneSuit.containsValue(Card.JACK )) result++;
		if ((oneSuit.containsValue(10))&&(result > 0)) result++;
		
		return result;
	}
	
	/**
	 * アナー点(全体点、各スーツの点)を計算します。
	 * アナー点は、A:4 K:3 Q:2 J:1 で計算します。
	 * 配列の添字０に全体点、添字 Card.CLUB(=1), ……, Card.SPADE(=4)に
	 * 各スーツの点数が格納されます。
	 *
	 * @param		hand		アナー点を計算するハンド
	 * @return		アナー点に関する情報
	 */
	public static int[] countHonerPoint(Packet hand) {
		int[] result = new int[5];
		result[0] = 0;
		for (int suit = 1; suit < 5; suit++) {
			result[suit] = countHonerPoint(hand, suit);
			result[0] += result[suit];
		}
		
		return result;
	}
	
	/**
	 * 指定されたハンドの指定されたスートについて、そのバリューを文字列にします。
	 * AKQ52 のように降順に変換されます。
	 */
	public static String valuePattern(Packet hand, int suit) {
		Packet p = hand.subpacket(suit);
		p.arrange();
		
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < p.size(); i++) {
			int v = p.peek(i).getValue();
			switch (v) {
			case Card.ACE:		s.append('A'); break;
			case Card.KING:		s.append('K'); break;
			case Card.QUEEN:	s.append('Q'); break;
			case Card.JACK:		s.append('J'); break;
			case 10:			s.append('T'); break;
			default:			s.append(v); break;
			}
		}
		
		return s.toString();
	}
	
	/**
	 * 指定されたハンドが指定されたハンドパターンに適合するかの判定を行います。
	 * 本メソッドで指定するハンドパターンは valuePattern() の形式に準拠しますが、
	 * ワイルドカードを使用することができます。ワイルドカードとして、以下の文字
	 * が使用できます。ただし、ハンドパターンは降順に記述する必要があります。<BR>
	 *<BR>
	 * x...2-9 のいずれか１枚 <BR>
	 * X...2-T のいずれか１枚 <BR>
	 * ?...いずれか１枚 <BR>
	 * *...いずれか０枚以上(現在末尾でしか正しく機能しません) <BR>
	 *
	 */
	public static boolean patternMatch(Packet hand, String pattern, int suit) {
		String handpat = valuePattern(hand, suit);
		for (int i = 0; i < handpat.length(); i++) {
			if (i == pattern.length()) return false;
			char conditionLetter = pattern.charAt(i);
			char target = handpat.charAt(i);
			
			switch (conditionLetter) {
			
			case 'x':
				if ((target >= '2')&&(target <= '9')) break;
				return false;
			case 'X':
				if ( ((target >= '2')&&(target <= '9'))||(target == 'T') ) break;
				return false;
			case '?':
				break;
			case '*':
				return true; // 手抜き
			default:
				if ( conditionLetter == target ) break;
				return false;
			}
		}
		if (pattern.length() == handpat.length()) return true;
		if ( (pattern.length() == handpat.length() + 1)&&(pattern.endsWith("*")) ) return true;
		return false;
	}
	
	/**
	 * 与えられたボードにおけるオリジナルハンドを計算します。
	 * 本メソッドでは、現在持っているハンドにこれまでのトリックのカードを
	 * 追加して結果を求めているため、指定 Board に unspecified Card が含まれて
	 * いた場合、結果には unspecified Card が含まれることとなります。
	 * 
	 * @param		board		オリジナルハンドを求めたい Board
	 *
	 * @return		オリジナルハンドの配列(添字には Board.NORTH などを指定)
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
		Trick[] trick = board.getAllTricks();
		for (int i = 0; i < trick.length; i++) {
			Trick tr = trick[i];
			if (tr == null) break;
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
	 * 与えられたボードにおいてディクレアラー側のとったトリック数をカウントします。
	 *
	 * @param		board		ウィナーを数える対象となるボード
	 */
	public static int countDeclarerSideWinners(Board board) {
		Trick[]	tr			= board.getAllTricks();
		if (tr == null) return 0;
		
		int		win			= 0;
		int		declarer	= board.getDeclarer();
		
		for (int i = 0; i < tr.length; i++) {
			if (tr[i] == null) break;
			if (!tr[i].isFinished()) break;
			int winner = tr[i].getWinner();
			if ( ((winner ^ declarer) & 1) == 0 ) win++;
		}
		
		return win;
	}
	
	/**
	 * 与えられたボードにおいてディフェンダー側のとったトリック数をカウントします。
	 *
	 * @param		board		ウィナーを数える対象となるボード
	 */
	public static int countDefenderSideWinners(Board board) {
		return board.getTricks() - countDeclarerSideWinners(board);
	}
	
	/**
	 * スート定数の文字列表現を返却します
	 *
	 * @since	2015/8/15
	 *
	 * @return		スートの文字列表現(* S H D C Jo)
	 */
	public static String suitString(int suit) {
		switch (suit) {
			case Card.UNSPECIFIED:
				return "*";
			case Card.SPADE:
				return "S";
			case Card.HEART:
				return "H";
			case Card.DIAMOND:
				return "D";
			case Card.CLUB:
				return "C";
			default:
				return "Jo";
		}
	}
	
	/**
	 * バリュー定数の文字列表現を返却します
	 *
	 * @since	2015/8/15
	 *
	 * @return		バリューの文字列表現(AKQJT98765432)
	 *
	 */
	public static String valueString(int value) {
		switch (value) {
			case Card.UNSPECIFIED:
			case Card.JOKER:
				return "";
			case Card.ACE:
				return "A";
			case 10:
				return "T";
			case Card.JACK:
				return "J";
			case Card.QUEEN:
				return "Q";
			case Card.KING:
				return "K";
			default:
				return Integer.toString(value);
		}
	}
	
}
