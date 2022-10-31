package ys.game.card.bridge;
/*
 * 2001/ 7/23  setName(), getName() を追加
 */
import ys.game.card.Packet;

/**
 * ブリッジにおける１ボードをパックするオブジェクトです。
 * BoardManagerに対しては受動的なオブジェクトで、状態変化を起こす
 * メソッドを提供します。
 * Playerに対しては状態参照を提供します。
 *
 * @version		a-release		23, July 2001
 * @author		Yusuke Sasaki
 */
public interface Board {
	
	/**
	 * 座席定数として使用され、North(=0) であることを示します。
	 */
	int NORTH = 0;
	
	/**
	 * 座席定数として使用され、East(=1) であることを示します。
	 */
	int EAST  = 1;
	
	/**
	 * 座席定数として使用され、South(=2) であることを示します。
	 */
	int SOUTH = 2;
	
	/**
	 * 座席定数として使用され、West(=3) であることを示します。
	 */
	int WEST  = 3;
	
	String[] STATUS_STRING	= {"Dealing", "Bid", "Opening Lead", "Playing", "Scoring"};
	String[] VUL_STRING		= {"none", "N-S", "E-W", "Both"};
	String[] SEAT_STRING	= {"North", "East ", "South", "West "};
	
	/**
	 * getVulnerability() メソッドの返却値として使用される、
	 * NS-EW のいずれもバルでないこと(None Vul.)を示す定数です。
	 *
	 * @see		#getVulnerability()
	 */
	int VUL_NONE	= 0;
	
	/**
	 * getVulnerability() メソッドの返却値として使用される、
	 * NS がバルであることを示す定数です。
	 *
	 * @see		#getVulnerability()
	 */ 
	int VUL_NS		= 1;
	
	/**
	 * getVulnerability() メソッドの返却値として使用される、
	 * EW がバルであることを示す定数です。
	 *
	 * @see		#getVulnerability()
	 */ 
	int VUL_EW		= 2;
	
	/**
	 * getVulnerability() メソッドの返却値として使用される、
	 * NS-EW の両方がバルであること(Both Vul.)を示す定数です。
	 *
	 * @see		#getVulnerability()
	 */ 
	int VUL_BOTH	= 3;
	
	/**
	 * １ボード中、ボードが新規に作成され、まだプレイヤーにカードが
	 * ディールされていない状態を示す定数です。
	 * deal() メソッドをコールすることで BIDDING 状態に移行します。
	 *
	 * @see		#getStatus()
	 * @see		#deal()
	 * @see		#deal(ys.game.card.Packet[])
	 * @see		#BIDDING
	 */
	int DEALING = 0;
	
	/**
	 * １ボード中、ビッドが行われている状態を示す定数です。
	 * ビッドは play(Object) メソッドに対して Bid オブジェクトを
	 * 与えることによって進行します。
	 * ビッディングシーケンスの中で、
	 * Pass Out の場合は SCORING 状態に移行し、
	 * ビッド後 Pass が３回続いた場合は OPENING 状態に移行します。
	 *
	 * @see		ys.game.card.bridge.Bid
	 * @see		#play(Object)
	 * @see		#getStatus()
	 * @see		#OPENING
	 * @see		#SCORING
	 */
	int BIDDING = 1;
	
	/**
	 * １ボード中、オープニングリードを待っている状態を示す定数です。
	 * オープニングリードは play(Object) に対して Card オブジェクトを
	 * 与えることによって進行します。
	 * オープニングリードが行われるとダミーハンドがオープンされ、
	 * PLAYING 状態に移行します。
	 *
	 * @see		ys.game.card.Card
	 * @see		#play(Object)
	 * @see		#getStatus()
	 * @see		#PLAYING
	 */
	int OPENING = 2;
	
	/**
	 * １ボード中、プレイ中であることを示す定数です。
	 * プレイが完了すると SCORING 状態に移行します。
	 * 本状態になったボードは reset(int,int) メソッドで DEALING
	 * 状態に移行します。
	 *
	 * @see		#getStatus()
	 * @see		#reset(int,int)
	 * @see		#DEALING
	 */
	int PLAYING = 3;
	
	/**
	 * １ボードが終了したことを示す定数です。
	 * プレイが完了すると SCORING 状態に移行します。
	 *
	 * @see		#getStatus()
	 * @see		#SCORING
	 */
	int SCORING = 4;
	
// プレイ順番
	int LEAD	= 0;
	int SECOND	= 1;
	int THIRD	= 2;
	int FORTH	= 3;
	
/*----------------------------------------------------------
 * 状態変化メソッド(公開されないパッケージレベルのメソッド)
 */
	/**
	 * ビッド、またはプレイを行います。状態が適宜変化します。
	 *
	 * @param		c		ビッド(Bid のインスタンス)または
	 *						プレイ(Card のインスタンス)
	 */
	void play(Object c);
	
	/**
	 * ランダムなハンドを設定し、オークションできる状態(BIDDING)に移行します。
	 */
	void deal();
	
	/**
	 * 指定されたハンドを設定し、オークションできる状態(BIDDING)に移行します。
	 * 本メソッドをコールすると、ハンドは裏返しの状態となります。toText()を
	 * 行う場合、表示されなくなるので、注意が必要です。
	 */
	void deal(Packet[] hand);
	
	// void undo();
	
/*------------------------------
 * 状態参照メソッド(公開される)
 */
	/**
	 * この Board の名前を設定します。
	 */
	void setName(String name);
	
	/**
	 * この Board の名前を取得します。設定されていない場合、空文字が返されます。
	 */
	String getName();
	
	/**
	 * この Board で使用している BiddingHistory を取得します。
	 *
	 * @return		BiddingHistory
	 */
	BiddingHistory getBiddingHistory();
	
	/**
	 * この Board で使用している PlayHistory を取得します。
	 *
	 * @return		PlayHistory
	 */
	PlayHistory getPlayHistory();
	
	/**
	 * この Board で使用する PlayHistory を指定します。
	 *
	 * @param		設定したい playHistory
	 */
	void setPlayHistory(PlayHistory playHistory);
	
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
	 * 指定されたビッド、もしくはプレイ可能であるかテストします。
	 * 本オブジェクトでは、次のようなチェックを行います。<BR>
	 * (1) Boardの進行状態と指定されたオブジェクトの整合性<BR>
	 * (2) BIDDING の場合、ビッディングシーケンスとして許されること<BR>
	 * (3) PLAYING の場合、Revoke を行っていないこと<BR>
	 * (4) PLAYING の場合、スートフォローを行っていること<BR>
	 *
	 * @param		play		判定対象のビッド、またはプレイ
	 * @return		true：可能    false:不可能
	 */
	boolean allows(Object play);
	
	/**
	 * Boardの進行状態を取得します。
	 *
	 * @return		Board の進行状態(DEALING,BIDDING,OPENING,PLAYING,SCORING)
	 * @see			#DEALING
	 * @see			#BIDDING
	 * @see			#OPENING
	 * @see			#PLAYING
	 * @see			#SCORING
	 */
	int getStatus();
	
	/**
	 * ステータスが OPENING, PLAYING の場合にプレイ順を
	 * 示す定数を返却します。
	 * ステータスが他の値の場合、 -1 が返却されます。
	 *
	 * @return		プレイ順を示す定数(LEAD, SECOND, THIRD, FORTH)
	 * @see			#LEAD
	 * @see			#SECOND
	 * @see			#THIRD
	 * @see			#FORTH
	 */
	int getPlayOrder();
	
	/**
	 * Vulnerability を取得します。
	 *
	 * @return		このボードの vulnerability。
	 *				(0:VUL_NONE, 1:VUL_NS, 2:VUL_EW, 3:VUL_BOTH)
	 */
	int getVulnerability();
	
	/**
	 * ビッド、またはプレイが現在だれの番であるかを座席定数で取得します。
	 * DEALING, SCORING では -1 が返却されます。
	 *
	 * @see			#NORTH
	 * @see			#EAST
	 * @see			#SOUTH
	 * @see			#WEST
	 * @see			#DEALING
	 * @see			#SCORING
	 */
	int getTurn();
	
	/**
	 * 現在だれがプレイする番かを座席定数で取得します。
	 * getTurn() との違いは、プレイのとき、ダミーの番でディクレアラーの
	 * 座席定数が返される点です。
	 *
	 * @see			#NORTH
	 * @see			#EAST
	 * @see			#SOUTH
	 * @see			#WEST
	 * @see			#DEALING
	 * @see			#SCORING
	 */
	int getPlayer();
	
	/**
	 * (現在までの)最終コントラクトを取得します。
	 * まだビッドが行われていない場合、null が返却されます。
	 */
	Bid getContract();
	
	/**
	 * (現在までの)最終トランプをスート定数で取得します。
	 * ビッドがまだ行われていない場合、-1 が返ります。
	 *
	 * @return	スート定数(SPADE, HEART, DIAMOND, CLUB)
	 * @see		ys.game.card.Card#SPADE
	 * @see		ys.game.card.Card#HEART
	 * @see		ys.game.card.Card#DIAMOND
	 * @see		ys.game.card.Card#CLUB
	 */
	int getTrump();
	
	/**
	 * (現在までで決定している)ディクレアラーを座席定数で取得します。
	 * まだ決定していない場合、-1 が返却されます。
	 *
	 * @return	ディクレアラーの座席定数、または -1
	 */
	int getDeclarer();
	
	/**
	 * (現在までで決定している)ダミーを座席定数で取得します。
	 * まだ決定していない場合、-1 が返却されます。
	 *
	 * @return	ダミーの座席定数、または -1
	 */
	int getDummy();
	
	/**
	 * (ビッドを開始する)ディーラーを座席定数で取得します。
	 *
	 * @return	ディーラーの座席定数、または -1
	 */
	int getDealer();
	
	/**
	 * 指定された座席のハンドを取得します。
	 * このボードがマスターでない場合、未知のハンドには
	 * UnspecifiedCard が含まれる可能性があります。
	 */
	Packet getHand(int seat);
	
	/**
	 * プレイヤー全員のハンドを取得します。配列の添え字は座席定数です。
	 * このボードがマスターでない場合、未知のハンドには
	 * UnspecifiedCard が含まれる可能性があります。
	 */
	Packet[] getHand();
	
	/**
	 * 現在までにプレイされたトリック数を取得します。
	 * これは、finished() となったトリックの数を示しており、たとえば
	 * はじめの１トリックが終了して現在２トリック目にある場合には 1
	 * が返却されます。
	 * １３トリックすべてが完了した状態では 13 が返却されます。
	 *
	 * @return		プレイされたトリック数
	 * @see			#getAllTricks()
	 * @see			#getTrick()
	 */
	int getTricks();
	
	/**
	 * 現在場に出ているトリックを取得します。
	 * まだコントラクトが決定していない場合、null が返却されます。
	 * OPENING, PLAYING 状態では null を返されることはなく、
	 * 常に finished() ではない Trick のインスタンスが返却されます。
	 * SCORING 状態では、最終 Trick （通常、finished() の状態）
	 * が返却されます。
	 */
	Trick getTrick();
	
	/**
	 * プレイされた過去のトリックすべてを取得します。DEALING, BIDDING 
	 * の場合は null が返却されます。
	 */
	Trick[] getAllTricks();
	
	/**
	 * 指定された座席がバルであるか判定します。
	 *
	 * @param		seat		判定する座席(N/E/S/W)
	 * @see		#NORTH
	 * @see		#EAST
	 * @see		#SOUTH
	 * @see		#WEST
	 */
	boolean isVul(int seat);
	
	/**
	 * 既知のカード(プレイされたカード、ダミーハンド)の集合を返します。
	 * 思考ルーチンで使用されることを期待しているメソッドです。
	 *
	 * @return		既知のカードからなる Packet
	 */
	Packet getOpenCards();
	
	/**
	 * この Board の状態を初期化後の状態にリセットします。
	 * 
	 * @param		boardNum	ボード番号
	 */
	void reset(int boardNum);
	
	/**
	 * この Board の状態を初期化後の状態にリセットします。
	 * 
	 * @param		dealer		ディーラーの番号
	 * @param		vul			バルネラビリティ
	 */
	void reset(int dealer, int vul);
	
	/**
	 * この Board の状態を１つ前の状態に戻します。
	 * DEALING 状態でこのメソッドを呼ぶと IllegalStatusException がスローされます
	 *
	 * @see		#DEALING
	 * @see		ys.game.card.bridge.IllegalArgumentException
	 * @exception	IllegalArgumentException	DEALING 状態であった
	 */
	void undo();
	
	String toText();
}
