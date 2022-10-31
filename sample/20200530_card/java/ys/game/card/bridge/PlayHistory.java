package ys.game.card.bridge;

import ys.game.card.Packet;
import ys.game.card.Card;
import ys.game.card.UnspecifiedException;

/**
 * PlayHistory クラスは、コントラクトブリッジにおけるプレイ部分
 * の状態、ルールをパックします。
 * 本クラスは Board オブジェクトに保持され、プレイ部分の実処理を
 * 行います。
 *
 * @version		a-release		4, May 2000
 * @author		Yusuke Sasaki
 */
public interface PlayHistory {
	
	/**
	 * この PlayHistory で使用する初期ハンド全体を設定します。
	 * すでにプレイされていた場合、IllegalStatusException がスローされます。
	 *
	 * @param		hand		設定するハンドデータ(North が 添字 0 に格納されている)
	 */
	void setHand(Packet[] hand);
	
	/**
	 * この PlayHistory におけるコントラクトを設定します。
	 * すでにプレイされていた場合、IllegalStatusException がスローされます。
	 *
	 * @param		leader		オープニングリーダーの座席番号(Board.NORTH など)
	 * @param		trump		トランプ(Bid.NO_TRUMP など)
	 */
	void setContract(int leader, int trump);
	
	/**
	 * 指定されたプレイが可能であるか判定します。
	 * プレイヤーとして、この PlayHistory の getTurn() のプレイヤーが
	 * プレイしていると仮定しています。
	 * 具体的には、現在順番
	 * 
	 * @param		p		プレイ可能か判定したいカード
	 * @return		プレイできるかどうか
	 */
	boolean allows(Card p);
	
	/**
	 * 指定されたカードをプレイして状態を更新します。
	 * プレイできないカードをプレイしようとすると IllegalPlayException
	 * がスローされます。
	 *
	 * @param		p		プレイするカード
	 */
	void play(Card p);
	
	/**
	 * 現在プレイを行うべき座席の番号を返します。dummy の席番号も返ります。
	 *
	 * @return		プレイを期待される座席の番号(Board.NORTH など)
	 */
	int getTurn();
	
	/**
	 * 指定された座席のハンド情報を取得します。
	 *
	 * @param		seat		座席番号(Board.NORTH など)
	 * @return		ハンド情報
	 */
	Packet getHand(int seat);
	
	/**
	 * NESW すべての座席のハンド情報を配列として取得します。
	 * 添字の 0,1,2,3 がそれぞれ N, E, S, W のハンドを示します。
	 *
	 * @return		全体のハンド情報
	 */
	Packet[] getHand();
	
	/**
	 * トランプスーツを返します。
	 *
	 * @return		トランプスーツ(Bid.SPADE など, 設定されていない場合 -1)
	 */
	int getTrump();
	
	/**
	 * 現在まででプレイされているトリック数を取得します。
	 * 現在プレイ中のトリックについてはカウントされません。
	 */
	int getTricks();
	
	/**
	 * 現在プレイ中のトリックを返します。
	 */
	Trick getTrick();
	
	/**
	 * 指定されたラウンドのトリックを返却します。
	 * ラウンドは 0 から 12 までの整数値です。
	 */
	Trick getTrick(int index);
	
	/**
	 * 現在までプレイされたトリックすべてを取得します。
	 * プレイが終了しているときを除いて、最終要素は現在プレイ中のトリックです。
	 * setHand(), setContract() のどちらかが行われていなければ null が返ります。
	 *
	 * @return		すべてのトリック
	 */
	Trick[] getAllTricks();
	
	/**
	 * プレイが１３トリックすべて終了したかテストします。
	 *
	 * @return		終了したか(終了..true)
	 */
	boolean isFinished();
	
	void reset();
	
	Card undo();
}
