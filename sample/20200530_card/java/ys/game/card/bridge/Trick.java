package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;

/**
 * Trick は、場に出ているトリック、プレイされたトリックをパックする。
 * Trick では、１トリックを構成するカードの保持の他、トリックのウィナー
 * の判定を行う。
 * hand に関する情報は保持しない。
 *
 * @version		a-release		5, May 2000
 * @author		Yusuke Sasaki
 */
public interface Trick extends Packet {
	
	/**
	 * はじめに台札を出す座席の番号を取得します。
	 *
	 * @return		leader の座席番号
	 */
	int getLeader();
	
	/**
	 * 設定されている Trump を取得します。
	 *
	 * @return		トランプスート
	 */
	int getTrump();
	
	/**
	 * 次にカードをだす座席の番号が返されます。
	 *
	 * @return		次にカードを出す座席の番号
	 */
 	int getTurn();
 	
	/**
	 * このトリックが終っているかテストします。size() == 4 と同等です。
	 * 終っている場合、getWinner(), getWinnerCard() の値が有効となります。
	 *
	 * @return		このトリックが終っているか
	 */
	boolean isFinished();
	
	/**
	 * 台札(このトリックで最初に出されたカード)を取得します。
	 * 台札が出ていない場合、null が返ります。
	 *
	 * @return		台札
	 */
	Card getLead();
	
	/**
	 * このトリックが終了している(isFinished()==true)ときにウィナーとなった
	 * カードを出した座席の番号が返却されます。次のトリックはこの座席から
	 * リードされることになります。
	 * まだトリックが終了していない場合、-1 が返却されます。
	 *
	 * @return		勝った座席番号
	 */
	int getWinner();
	
	/**
	 * このトリックが終了している(isFinished()==true)ときに、ウィナーカード
	 * を取得します。
	 *
	 * @return		勝ったカード
	 */
	Card getWinnerCard();
	
}
