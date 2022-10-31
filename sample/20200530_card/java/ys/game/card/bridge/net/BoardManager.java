import ys.game.card.*;
import ys.game.card.bridge.*;

public class BoardManager {
	protected Board			masterBoard;
	protected Board[]		neswBoard;
	protected Board			openBoard;
	
	/** �v���C�C�x���g���󂯎��I�u�W�F�N�g�BPlayer�͊܂܂Ȃ��B */
	protected Observer[]	observer;
	
	/** Observer �̐� */
	protected int			observers;
	
	/** �v���C�C�x���g�𔭐�����I�u�W�F�N�g */
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
			if (player[i] == null) throw new IllegalStatusException("�l��������ĂȂ���");
		}
		
		// Board �̏�����
		masterBoard = new BoardImpl(1);
		for (int i = 0; i < 4; i++) {
			neswBoard[i] = new BoardImpl(1);
		}
		openBoard = new BoardImpl(1);
		
		//
		// Deal
		//
		
		// master�́A���ʂɔz��
		masterBoard.deal();
		
		// nesw�́A�����̃J�[�h�ȊOunspecified��z���Ă���
		for (int i = 0; i < 4; i++) {
			Packet		deck = PacketFactory.provideUnspecifiedDeck(PacketFactory.WITHOUT_JOKER);
			Packet[]	hand = PacketFactory.deal(deck, 4);
			
			Packet h = masterBoard.getHand(i);
			for (int j = 0; j < 13; j++) {
				hand[i].peek(j).specify(h.peek(j));
			}
			neswBoard[i].deal(hand);
		}
		
		// open�͂��ׂ�unspecified
		Packet		deck = PacketFactory.provideUnspecifiedDeck(PacketFactory.WITHOUT_JOKER);
		Packet[]	hand = PacketFactory.deal(deck, 4);
		openBoard.deal(hand);
		
		// ��������ʒm����
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
			
			// �������ʒm
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
		
		// �������ʒm
		for (int i = 0; i < observers; i++) {
			observer[i].notifyPlay(ol);
		}
		
		// �_�~�[�I�[�v��
		int dummy = masterBoard.getDummy();
		Packet dummyHand = masterBoard.getHand(dummy);
		for (int i = 0; i < 4; i++) {
			if (i == dummy) continue; // �_�~�[�� specify �s�v
			Packet h = neswBoard[i].getHand(dummy);
			for (int j = 0; j < 13; j++) {
				h.peek(j).specify(dummyHand.peek(j));
			}
		}
		Packet oh = openBoard.getHand(dummy);
		for (int j = 0; j < 13; j++) {
			oh.peek(j).specify(dummyHand.peek(j));
		}
		
		// �������ʒm
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
			
			// �������ʒm
			for (int i = 0; i < observers; i++) {
				observer[i].notifyPlay(card);
			}
		}
		
	}
	
}
