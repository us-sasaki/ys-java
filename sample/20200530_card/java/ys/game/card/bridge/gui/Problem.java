package ys.game.card.bridge.gui;

import ys.game.card.bridge.*;
import ys.game.card.*;

/**
 * ブリッジシミュレータのハンドなどのデータを格納するクラス。
 *
 * @version		release		22, July 2001
 * @author		Yusuke Sasaki
 */
public interface Problem {
	/**
	 * この Problem が選択され、使用されることが決定した際に呼ばれます。
	 * 動的な Problem では、この中でハンド内容などの初期化を行います。
	 */
	void start();
	
	/**
	 * GUI におけるこの問題のハンドルです。
	 * start() 前に呼ばれる可能性があります。
	 */
	String getTitle();
	
	/**
	 * コントラクトを返します。
	 */
	Bid getContract();
	
	/**
	 * それぞれのハンドを返します。
	 * このインスタンスが実際にプレイで使用されるため、この内容は変更されます。
	 * したがって、つねにインスタンスのコピーを作成するか、start()
	 * の中で毎回新規作成したインスタンスを返す必要があります。
	 */
	Packet[] getHand();
	
	/**
	 * すみれの説明内容を返します。
	 */
	String getDescription();
	
	/**
	 * コントラクトをあらわす 4S などの文字列を返します。
	 */
	String getContractString();
	
	/**
	 * オープニングリードの指定を Problem から行うことができます。
	 */
	String getOpeningLead();
	
	/**
	 * 思考ルーチンを示す文字列を返します。
	 */
	String getThinker();
	
	/**
	 * この問題が使用可能であるかどうかをテストします。
	 * valid でない場合、GUI に対して登録されません。
	 * start() 前に呼ばれる可能性があります。
	 */
	boolean isValid();
}
