package ys.game.card;

/**
 * Packet ‚Ì arrange() ƒƒ\ƒbƒh‚É‚¨‚¢‚ÄA‡˜‹K‘¥‚ð‹K’è‚·‚éƒNƒ‰ƒXB
 *
 * @version		a-release	15, April 2000
 * @author		Yusuke Sasaki
 */
public interface CardOrder {

	/**
	 * Card a ‚Æ Card b ‚Ì‡˜‚ð”äŠr‚·‚éB
	 * a > b ‚Ì‚Æ‚« 1, a = b ‚Ì‚Æ‚« 0, a < b ‚Ì‚Æ‚« -1 ‚Æ‚È‚éB
	 * = ‚É‚Â‚¢‚Ä‚ÍAequals ƒƒ\ƒbƒh‚ÆŒÝŠ·«‚ðŽ‚½‚¹‚é‚×‚«B
	 *
	 * @param		a		”äŠr‘ÎÛ‚P
	 * @param		b		”äŠr‘ÎÛ‚Q
	 * @return
	 */
	int compare(Card a, Card b);
}
