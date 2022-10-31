import ys.game.card.*;
import ys.game.card.bridge.*;

public class BoardManager {
	protected Board			masterBoard;
	protected Board[]		neswBoard;
	protected Board			openBoard;
	
	/** プレイイベントを受け取るオブジェクト。Playerは含まない。 */
	protected Observer[]	observer;
	
	/** Observer の数 */
	protected int			observers;
	
	/** プレイイベントを発生するオブジェクト */
	protected Player[]		player;
	
	public BoardManager() {
		neswBoard = new Board[4];
		
		observer = new Observer[4];
		player = new Player[4];
	}
	
	public void setPlayer(Player p, int seat) {
		if (player[seat] != null) return;
		player[seat] = p;
	}
	
	public void addObserver(Observer o) { }
	
	public void main() throws InterruptedException {
		for (int i = 0; i < 4; i++) {
			if (player[i] == null) throw new IllegalStatusException("人がそろってないよ");
		}
		
		// Board の初期化
		masterBoard = new BoardImpl(1);
		for (int i = 0; i < 4; i++) {
			neswBoard[i] = new BoardImpl(1);
		}
		openBoard = new BoardImpl(1);
		
		//
		// Deal
		//
		
		// masterは、普通に配る
		masterBoard.deal();
		
		// neswは、自分のカード以外unspecifiedを配っておく
		for (int i = 0; i < 4; i++) {
			Packet		deck = PacketFactory.provideUnspecifiedDeck(PacketFactory.WITHOUT_JOKER);
			Packet[]	hand = PacketFactory.deal(deck, 4);
			
			Packet h = masterBoard.getHand(i);
			for (int j = 0; j < 13; j++) {
				hand[i].peek(j).specify(h.peek(j));
			}
			neswBoard[i].deal(hand);
		}
		
		// openはすべてunspecified
		Packet		deck = PacketFactory.provideUnspecifiedDeck(PacketFactory.WITHOUT_JOKER);
		Packet[]	hand = PacketFactory.deal(deck, 4);
		openBoard.deal(hand);
		
		// 差分情報を通知する
		for (int i = 0; i < observers; i++) {
			observer[i].notifyDeal(masterBoard.getHand());
		}
		
		//
		// Bid
		//
		while (masterBoard.getStatus() == Board.BIDDING) {
			Object bid = player[masterBoard.getPlayer()].play();
			masterBoard.play(bid);
			for (int i = 0; i < 4; i++) {
				neswBoard[i].play(bid);
			}
			openBoard.play(bid);
			
			// 差分情報通知
			for (int i = 0; i < observers; i++) {
				observer[i].notifyPlay(bid);
			}
		}
		
		//
		// Opening Lead
		//
		Object ol = player[masterBoard.getPlayer()].play();
		masterBoard.play(ol);
		for (int i = 0; i < 4; i++) {
			neswBoard[i].play(ol);
		}
		openBoard.play(ol);
		
		// 差分情報通知
		for (int i = 0; i < observers; i++) {
			observer[i].notifyPlay(ol);
		}
		
		// ダミーオープン
		int dummy = masterBoard.getDummy();
		Packet dummyHand = masterBoard.getHand(dummy);
		for (int i = 0; i < 4; i++) {
			if (i == dummy) continue; // ダミーは specify 不要
			Packet h = neswBoard[i].getHand(dummy);
			for (int j = 0; j < 13; j++) {
				h.peek(j).specify(dummyHand.peek(j));
			}
		}
		Packet oh = openBoard.getHand(dummy);
		for (int j = 0; j < 13; j++) {
			oh.peek(j).specify(dummyHand.peek(j));
		}
		
		// 差分情報通知
		for (int i = 0; i < observers; i++) {
			observer[i].notifyDummy(dummyHand);
		}
		
		//
		// Play
		//
		while (masterBoard.getStatus() == Board.PLAYING) {
			Object card = player[masterBoard.getPlayer()].play();
			masterBoard.play(card);
			for (int i = 0; i < 4; i++) {
				neswBoard[i].play(card);
			}
			openBoard.play(card);
			
			// 差分情報通知
			for (int i = 0; i < observers; i++) {
				observer[i].notifyPlay(card);
			}
		}
		
	}
	
}
