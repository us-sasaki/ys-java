package ys.game.card.bridge.gui;
/*
 * 2001/ 7/23  setName(), getName() 追加
 */
import java.awt.*;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;

/**
 * Board の GUI 表現であり、GuiedCard, GuiedPacket の描画領域を提供します。
 * 抽象的な Board に追加される概念として、基準となる席(視点、方向)があります。
 *
 * @version		a-release		23, July 2001
 * @author		Yusuke Sasaki
 */
public class GuiedBoard extends Entities implements Board {
	// O | - +   | - + O   - + O |   + O | -
	private static final int[] VUL = { 0, 1, 2, 3,  1, 2, 3, 0,
											2, 3, 0, 1, 3, 0, 1, 2 };
	
	public static final int WIDTH	= 640;
	public static final int HEIGHT	= 480;
	
	protected BoardImpl	impl;
	
/*----------------
 * GUI Components
 */
	protected GuiedPacket[] handGui; // BoardLayoutが参照する。
	protected GuiedTrick	trickGui;
	protected WinnerGui		winnerGui;
	protected TableGui		tableGui;
	
/*-------------
 * Constructor
 */
	public GuiedBoard(Board board) {
		if (board instanceof BoardImpl) impl = (BoardImpl)board;
		else impl = new BoardImpl(board);
		
		direction = Entity.UPRIGHT;
		setSize(WIDTH, HEIGHT);
	}
	
/*------------------
 * instance methods
 */
	/**
	 * TableGui, WinnerGui を新規に作成し、PlayHistory を Guied に変更します。
	 * また、GuiedBoard では、handGui を独自に保持している(GuiedPlayHistory
	 * と同じインスタンス)ため、これを取得します。
	 * また、これらを Entity として addEntity() します。
	 */
	private void setBoardGui() {
		entity = null;
		entities = 0;
		
		//
		tableGui = new TableGui(this);
		addEntity(tableGui);
		
		//
		winnerGui = new WinnerGui();
		addEntity(winnerGui);
		
		//
		handGui = new GuiedPacket[4];
		
		
		PlayHistory play = impl.getPlayHistory();
		if (!(play instanceof GuiedPlayHistory))
			impl.setPlayHistory(new GuiedPlayHistory(play));
		
		for (int i = 0; i < 4; i++) {
			handGui[i] = (GuiedPacket)(impl.getHand(i));
			addEntity(handGui[i]);
		}
		
		//
		trickGui = (GuiedTrick)(impl.getTrick());
		if (trickGui != null) addEntity(trickGui);
		
		setLayout(new BoardLayout());
	}
	
/*---------------------
 * Overrides(Entities)
 */

/*-------------------
 * Implements(Board)
 *
 * ほとんど impl に対して委譲します。
 */
	public void setName(String name) { impl.setName(name); }
	public String getName() { return impl.getName(); }
	
	/**
	 * ハンドを配ります。
	 * GuiedBoard では、deal() を行ったときに下に表示されるハンドが表向きと
	 * なります。observer (Open Cards しか見ない人) の場合も特定の人(恐らくSOUTH)
	 * のハンドについて表向きにされるのではないかという懸念がありますが、
	 * 実際にはこのカードは Unspecified のため、画面表示上は裏向きに見えるはずです。
	 */
	public void deal() {
		impl.deal();
		
		//
		// Hand, Trick を Guied に変更する。
		//
		setBoardGui();
		
		//
		// 下に表示されるハンドを表向きにする
		//
		Packet turned = getHand( (direction + 2)%4 );
		turned.turn(true);
	}
	
	/**
	 * 指定されたハンドを配ります。
	 * GuiedBoard では、deal() を行ったときに下に表示されるハンドが表向きと
	 * なります。observer (Open Cards しか見ない人) の場合も特定の人(恐らくSOUTH)
	 * のハンドについて表向きにされるのではないかという懸念がありますが、
	 * 実際にはこのカードは Unspecified のため、画面表示上は裏向きに見えるはずです。
	 */
	public void deal(Packet[] hand) {
		impl.deal(hand);
		
		//
		// Hand, Trick を Guied に変更する。
		//
		setBoardGui();
		
		//
		// 下に表示されるハンドを表向きにする
		//
		Packet turned = getHand( (direction + 2)%4 );
		for (int i = 0; i < turned.size(); i++) {
			turned.peek(i).turn(true);
		}
		
	}
	
	public void play(Object c) {
		int oldStatus = impl.getStatus();
		impl.play(c);
		
		if (impl.getStatus() == Board.PLAYING) {
			TrickAnimation animation = new TrickAnimation(this);
			animation.start();
			
			if (oldStatus == Board.OPENING) {
				trickGui = (GuiedTrick)getTrick();
				addEntity(trickGui);
				//------------
				// 向きの設定
				//------------
				int d = (4 + trickGui.getLeader() - trickGui.getDirection() ) % 4;
				Entity ent = trickGui.getEntity(0);
				ent.setDirection((10 - d )%4);
				trickGui.layout();
				setDummyLayout();
			}
		} else if (impl.getStatus() == Board.SCORING) {
			TrickAnimation animation = new TrickAnimation(this);
			animation.start();
		}
	}
	
	public void undo() {
		throw new InternalError("未実装！");
	}
	
	private void setDummyLayout() {
		GuiedPacket dummyHand = (GuiedPacket)getHand(getDummy());
		dummyHand.setLayout(new DummyHandLayout());
		dummyHand.layout();
		layout();
	}
	
	// undo() 用
	private void removeDummyLayout() {
		GuiedPacket dummyHand = (GuiedPacket)getHand(getDummy());
		dummyHand.setLayout(new CardHandLayout());
		dummyHand.layout();
		layout();
	}
	
	public BiddingHistory getBiddingHistory() { return impl.getBiddingHistory(); }
	public PlayHistory getPlayHistory() { return impl.getPlayHistory(); }
	public void setPlayHistory(PlayHistory playHistory) {
		if (!(playHistory instanceof GuiedPlayHistory)) {
			playHistory = new GuiedPlayHistory(playHistory);
		}
		impl.setPlayHistory(playHistory);
	}
		
	public void setContract(Bid contract, int declarer) { impl.setContract(contract, declarer); }
	public boolean allows(Object play) { return impl.allows(play); }
	public int getVulnerability() { return impl.getVulnerability(); }
	public boolean isVul(int seat) { return isVul(seat); }
	public int getStatus() { return impl.getStatus(); }
	public int getPlayOrder() { return impl.getPlayOrder(); }
	public int getTurn() { return impl.getTurn(); }
	public int getPlayer() { return impl.getPlayer(); }
	public Bid getContract() { return impl.getContract(); }
	public int getTrump() { return impl.getTrump(); }
	public int getDeclarer() { return impl.getDeclarer(); }
	public int getDummy() { return impl.getDummy(); }
	public int getDealer() { return impl.getDealer(); }
	public Packet getHand(int seat) { return impl.getHand(seat); }
	public Packet[] getHand() { return impl.getHand(); }
	public int getTricks() { return impl.getTricks(); }
	public Trick getTrick() { return impl.getTrick(); }
	public Trick[] getAllTricks() { return impl.getAllTricks(); }
	
	/**
	 * 本メソッドは思考ルーチンで使用されることを期待しているため、
	 * GuiedBoard においても返却される Packet のインスタンスが Guied
	 * であることは保証されません。
	 *
	 * @return		既知のカードからなる Packet
	 */
	public Packet getOpenCards() { return impl.getOpenCards(); }
	
	public void reset(int num) {
		reset((num - 1)%4, VUL[(num - 1)%16]);
	}
	
	/**
	 * GuiedBoard では、reset() のときに各種GUIコンポーネントを切り離します。
	 * 切り離される GUI コンポーネントとして TableGui も含まれるため、
	 * この後に field.repaint() を実行するとテーブルの画面が消去されます。
	 * TrickAnimation(not a thread) などとの間で排他制御を実行すべきですが、
	 * このオブジェクトでは行って
	 * いないので、上位アプリケーションで制御する必要があります。
	 * これら各種 GUI コンポーネントは deal で新しく作られます。
	 * (private void setBoardGui() で作成)
	 */
	public void reset(int dealer, int vul) {
		removeEntity(tableGui);		tableGui	= null;
		removeEntity(winnerGui);	winnerGui	= null;
		if (handGui != null) {
			for (int i = 0; i < 4; i++) {
				removeEntity(handGui[i]);
			}
		}
		removeEntity(trickGui);		trickGui	= null;
		
		impl.reset(dealer, vul);
	}
	
	public String toString() {
		return impl.toString();
	}
	
	public String toText() {
		return impl.toText();
	}
}
