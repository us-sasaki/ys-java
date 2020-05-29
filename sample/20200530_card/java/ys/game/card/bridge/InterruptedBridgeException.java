package ys.game.card.bridge;

/**
 * ’†’f‚Ì—v‹‚ğó‚¯æ‚Á‚½‚È‚Ç‚ÅŠ„‚è‚İ‚ª”­¶‚µ‚½‚±‚Æ‚ğ¦‚· Exception ‚Å‚·B
 *
 * @version		a-release		30, September 2000
 * @author		Yusuke Sasaki
 */
public class InterruptedBridgeException extends BridgeException {
	public InterruptedBridgeException() {
		super();
	}
	public InterruptedBridgeException(String msg) {
		super(msg);
	}
}
