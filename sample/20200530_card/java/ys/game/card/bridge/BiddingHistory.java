package ys.game.card.bridge;

/**
 * Board の機能のうち、オークションに関係する部分の実際の処理を受け持つ
 * クラスです。
 *
 * @version		a-release	4, January 2001
 * @author		Yusuke Sasaki
 */
public interface BiddingHistory {
	
	/**
	 * 指定されたビッドが、ビッディングシーケンス上許可されるかテストします。
	 * 座席関係、ビッドの強さなどが調べられます。
	 *
	 * @param		b		テストするビッド
	 * @return		許可されるビッドか
	 */
	boolean allows(Bid b);
	
	/**
	 * ビッドを行い，ビッディングシーケンスを進めます。
	 * 不可能なビッドを行おうとすると、IllegalPlayException がスローされます。
	 * このメソッドは Board が内部で呼び出し、単独で呼ぶとステータス異常を
	 * 引き起こします。
	 *
	 * @param		newBid		新たに行うビッド
	 */
	void bid(Bid newBid);
	
	/**
	 * 行われたすべてのビッドを配列形式で返却します。
	 * 配列の添字 0 をディーラーのビッドとして以降座席順に格納されています。
	 * 現時点まででビッドされた回数分の要素を含みます。
	 *
	 * @return		すべてのビッド
	 */
	Bid[] getAllBids();
	
	/**
	 * 次にビッドする席の番号を返します。
	 *
	 * @return		席番号
	 */
	int getTurn();
	
	/**
	 * コントラクト、ディクレアラーを指定します。
	 * ビッディングシーケンスを用いずにコントラクトのみが決定している
	 * ときに使用します。
	 * このメソッドはビッディングシーケンスが空の場合にコールできます。
	 * ビッド後の場合、IllegalStatusException がスローされます。
	 *
	 * @param		contract		コントラクト
	 * @param		declarer		ディクレアラー
	 */
	void setContract(Bid contract, int declarer);
	
	/**
	 * (現在までで確定している)コントラクトを取得します。
	 *
	 * @return		コントラクト
	 */
	Bid getContract();
	
	/**
	 * (現在までで確定している)ディクレアラーの席番号を取得します。
	 *
	 * @return		ディクレアラーの席番号
	 */
	int getDeclarer();
	
	/**
	 * ディーラーの席番号を取得します。
	 *
	 * @return		ディーラーの席番号
	 */
	int getDealer();
	
	/**
	 * ビッディングシーケンスが終了したかどうかテストします。
	 *
	 * @return		ビッディングシーケンスを終了したか
	 */
	boolean isFinished();
	
	void reset(int dealer);
	
	/**
	 * 現在までビッドされた数を返します。
	 * まだ誰もビッドしていない状態では 0 が返却されます。
	 */
	int countBid();
	
	/**
	 * １つ前の状態に戻します。
	 * countBid() で 0 を返す状態のときは、IllegalStatusException をスローします。
	 */
	void undo();
}
