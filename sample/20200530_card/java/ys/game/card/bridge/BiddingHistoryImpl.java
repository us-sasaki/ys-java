package ys.game.card.bridge;

/**
 * Board の機能のうち、オークションに関係する部分の実際の処理を受け持つ
 * クラスです。
 *
 * @version		a-release	4, January 2001
 * @author		Yusuke Sasaki
 */
public class BiddingHistoryImpl implements BiddingHistory {
	
	/** ビッド履歴の配列 */
	private Bid[]	bid;
	
	/** ビッドカウント */
	private int		bidCount;
	
	/** 現在までで決まっているコントラクト */
	private Bid		contract;
	
	/** 現在までで決まっているディクレアラー */
	private int		declarer;
	
	/** ディーラー(ビッドを開始する)の位置 */
	private int		dealer;
	
	/** ビッドシーケンスが終了したかどうかのフラグ */
	private boolean	finished;
	
/*-------------
 * Constructor
 */
	/**
	 * ディーラーを指定して bidding history を作成します。
	 *
	 * @param		dealer		ディーラー
	 */
	public BiddingHistoryImpl(int dealer) {
		this.dealer = dealer;
		bid = new Bid[319];
		bidCount = 0;
		contract = null;
		declarer = -1;
		finished = false;
	}
	
	/**
	 * 指定された BiddingHistory と同一の状態の BiddingHistoryImpl
	 * のインスタンスを新規に生成します。
	 * Bid のインスタンスはコピー元のインスタンスが使用されます。
	 *
	 * @param		src		コピー元の BiddingHistory
	 */
	public BiddingHistoryImpl(BiddingHistory src) {
		this(src.getDealer());
		
		Bid[] bids = src.getAllBids();
		for (int i = 0; i < bids.length; i++) {
			bid(bids[i]);
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 指定されたビッドが、ビッディングシーケンス上許可されるかテストします。
	 * 座席関係、ビッドの強さなどが調べられます。
	 *
	 * @param		b		テストするビッド
	 * @return		許可されるビッドか
	 */
	public boolean allows(Bid b) {
		if (finished) return false;
		
		if (contract == null) {
			switch (b.getKind()) {
			
			case Bid.PASS:
			case Bid.BID:
				return true;
			
			case Bid.DOUBLE:
			case Bid.REDOUBLE:
				return false;
				
			default:
				throw new InternalError(
							"Bid instance status error: " + b);
			}
		}
		
		// double, redouble
		switch (b.getKind()) {
		
		case Bid.DOUBLE:
			if (contract.getKind() != Bid.BID) return false;
			if ( (contract.getSuit() != b.getSuit())||
				(contract.getLevel() != b.getLevel()) ) return false;
			if (((declarer ^ bidCount ^ dealer) & 1) != 1) return false;
			break;
		
		case Bid.REDOUBLE:
			if (contract.getKind() != Bid.DOUBLE) return false;
			if ( (contract.getSuit() != b.getSuit())||
				(contract.getLevel() != b.getLevel()) ) return false;
			if (((declarer ^ bidCount ^ dealer) & 1) != 0) return false;
			break;
			
		}
		
		// レベルによる判定
		if ((b.getKind() != Bid.PASS)&&
			(!b.isBiddableOver(contract))) return false;
		
		return true;
	}
	
	/**
	 * ビッドを行い，ビッディングシーケンスを進めます。
	 * 不可能なビッドを行おうとすると、IllegalPlayException がスローされます。
	 *
	 * @param		newBid		新たに行うビッド
	 */
	public void bid(Bid newBid) {
		// ビッドできるかのチェックを行う.
		if (!this.allows(newBid))
			throw new IllegalPlayException("Illegal bid:" + newBid.toString());
		
		// ビッドできるので、ビッド履歴に加える.
		bid[bidCount++] = newBid;
		
		// declarer, contract 更新, パス続いたか判定
		switch (newBid.getKind()) {
		case Bid.PASS:
			// パスが続いたか
			int passCount = 0;
			for (int i = bidCount - 1; i >= 0; i--) {
				if (bid[i].getKind() == Bid.PASS) passCount++;
				else break;
			}
			if (passCount < 3) break;
			if ((passCount == 3)&&(contract == null)) break;
			
			// Pass out ?
			finished = true;
			if (passCount == 4) {
				contract = new Bid(Bid.PASS, 0, 0);
				declarer = dealer;
				return;
			}
			break;
		case Bid.DOUBLE:
		case Bid.REDOUBLE:
			contract = newBid;
			break;
		
		case Bid.BID:
			contract = newBid;
			
			// declarer を見つける.
			int n;
			for (n = (1 - (bidCount & 1)); n < bidCount; n += 2) {
				Bid b = bid[n];
				if ((b.getKind() == Bid.BID)&&
					(b.getSuit() == newBid.getSuit())) break;
			}
			declarer = (n + dealer)%4;
			break;
		}
		
	}
	
	public void undo() {
		if (bidCount == 0)
			throw new IllegalStatusException("ビッドされていないので、undo() できません");
		contract		= null;
		bid[--bidCount]	= null;
		declarer		= -1;
		finished		= false;
		
		// contract を見つける
		int lastBidCount = 0;
		for (int i = bidCount-1; i >= 0; i--) {
			if (bid[i].getKind() == Bid.BID) {
				contract = bid[i];
				lastBidCount = i+1;
				break;
			}
		}
		
		// declarer を見つける
		int n;
		for (n = (1 - (lastBidCount & 1)); n < lastBidCount; n += 2) {
			Bid b = bid[n];
			if ((b.getKind() == Bid.BID)&&
				(b.getSuit() == contract.getSuit())) break;
		}
		declarer = (n + dealer)%4;
	}
	
	/**
	 * 行われたすべてのビッドを配列形式で返却します。
	 * 配列の添字 0 をディーラーのビッドとして以降座席順に格納されています。
	 * 現時点まででビッドされた回数分の要素を含みます。
	 *
	 * @return		すべてのビッド
	 */
	public Bid[] getAllBids() {
		Bid[] result = new Bid[bidCount];
		System.arraycopy(bid, 0, result, 0, bidCount);
		
		return result;
	}
	
	/**
	 * 次にビッドする席の番号を返します。
	 *
	 * @return		席番号
	 */
	public int getTurn() {
		return (dealer + bidCount) % 4;
	}
	
	public void setContract(Bid contract, int declarer) {
		if ( (bidCount != 0)||(finished) )
			throw new IllegalStatusException("すでにビッドされているためコントラクトを指定できません。");
		if ( (declarer < Board.NORTH)||(declarer > Board.WEST) )
			throw new IllegalArgumentException("declarer の値が不正です。");
		
		this.contract = contract;
		this.declarer = declarer;
		finished = true;
	}
	
	/**
	 * (現在までで確定している)コントラクトを取得します。
	 *
	 * @return		コントラクト
	 */
	public Bid getContract() {
		return contract;
	}
	
	/**
	 * (現在までで確定している)ディクレアラーの席番号を取得します。
	 *
	 * @return		ディクレアラーの席番号
	 */
	public int getDeclarer() {
		return declarer;
	}
	
	/**
	 * ディーラーの席番号を取得します。
	 *
	 * @return		ディーラーの席番号
	 */
	public int getDealer() {
		return dealer;
	}
	
	/**
	 * ビッディングシーケンスが終了したかどうかテストします。
	 *
	 * @return		ビッディングシーケンスを終了したか
	 */
	public boolean isFinished() {
		return finished;
	}
	
	public void reset(int dealer) {
		this.dealer = dealer;
		reset();
	}
	
	private void reset() {
		for (int i = 0; i < 319; i++) {
			bid[i] = null;
		}
		bidCount = 0;
		contract = null;
		declarer = -1;
		finished = false;
	}
	
	public int countBid() {
		return bidCount;
	}
	
/*-----------
 * overrides
 */
	/**
	 * 文字列表現を得る.
	 */
	public String toString() {
		if ( (bidCount == 0)&&(finished) )
			return "Bidding Sequence Unknown";
		String result = "   N       E       S       W\n";
		for (int i = 0; i < dealer; i++) {
			result += "        ";
		}
		int seat = dealer;
		
		for (int i = 0; i < bidCount; i++) {
			result += bid[i];
			seat++;
			if (seat == 4) {
				result += "\n";
				seat = 0;
			}
		}
		return result + "\n";
	}
}
