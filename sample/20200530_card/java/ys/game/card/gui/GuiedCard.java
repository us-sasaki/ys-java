package ys.game.card.gui;

import java.awt.*;

import ys.game.card.*;

/**
 * Applet として動作させるための Card の GUI 表現を与えます。
 * 抽象的な Card 概念(CardImplでの実装)に追加される概念として、カードの位置などの
 * 占有範囲、向きがあります。
 * Human Player のもつ Board はこのインスタンスを保持し、Human Playerに対して GUI
 * 操作機能を提供します。
 *
 * @version		a-release		17, May 2000
 * @author		Yusuke Sasaki
 */
public class GuiedCard extends Entity implements Card {
	
/*-----------
 * Constants
 */
	/**
	 * 直立時の最小横幅です。
	 * 左上のカード種別を示すマークが見えるサイズが設定されています。
	 */
	public static final int XSTEP = 14;
	
	/**
	 * 直立時の最小縦幅です。
	 * 左上のカード種別を示すマークが見えるサイズが設定されています。
	 */
	public static final int YSTEP = 16;
	
	/** 直立時の横幅です。 */
	public static final int XSIZE = 59;
	
	/** 直立時の縦幅です。 */
	public static final int YSIZE = 87;
	
/*-----------
 * variables
 */
	/**
	 * イメージを保持するオブジェクトです。
	 */
	protected static CardImageHolder	imageHolder;
	
	/** 実際のカード処理はこのオブジェクトに委譲されます。 */
	protected CardImpl		impl;
	
	/** カードの向きです。0-3の値で、0が正の向きで以後90°ずつ左回転します。 */
//	protected int			direction;
	
/*-------------
 * constructor
 */
	/**
	 * Unspecified Card を作成します。
	 * Unspecified GuiedCard は初期状態として、直立、裏向きに設定されます。
	 */
	public GuiedCard() {
		super();
		impl = new CardImpl();
		setSize(XSIZE, YSIZE);
	}
	
	/**
	 * 指定されたスート、バリューの Card を作成します。
	 * 初期状態は直立、表向きです。
	 *
	 * @param		suit		Card.JOKER, Card.SPADE, .., Card.CLUB
	 * @param		value		Card.JOKER, 1, .., 13(== Card.KING)
	 *
	 * @see			ys.game.card.Card#SPADE
	 * @see			ys.game.card.Card#HEART
	 * @see			ys.game.card.Card#DIAMOND
	 * @see			ys.game.card.Card#CLUB
	 * @see			ys.game.card.Card#JOKER
	 */
	public GuiedCard(int suit, int value) {
		super();
		impl = new CardImpl(suit, value);
		setSize(XSIZE, YSIZE);
	}
	
	public GuiedCard(Card c) {
		super();
		if (c instanceof CardImpl) impl = (CardImpl)c;
		else impl = new CardImpl(c);
	}
	
/*--------------
 * class method
 */
	/**
	 * このクラスで使用する CardImageHolder を指定します。
	 * この指定を行わない場合、カードは表示されません。
	 *
	 * @param		holder		CardImageHolder
	 */
	public static void setCardImageHolder(CardImageHolder holder) {
		imageHolder = holder;
	}
	
	/**
	 * このクラスで使用する CardImageHolder を取得します。
	 */
	public static CardImageHolder getCardImageHolder() {
		return imageHolder;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * カードの向きを設定します。
	 *
	 * @param		direction		カード
	 */
	public void setDirection(int direction) {
		this.direction = direction;
		if ( (direction & 1) == 0 ) setSize(XSIZE, YSIZE);
		else setSize(YSIZE, XSIZE);
	}
	
/*-------------------
 * overrides(Entity)
 */
	public void draw(Graphics g) {
		if (imageHolder == null) return;
		if (!visible) return;
		
		Image image;
		
		if ( (this.isUnspecified())||(!this.isHead()) ) {
			image = imageHolder.getBackImage(direction);
		}
		else {
			image = imageHolder.getImage(getSuit(), getValue(), direction);
		}
		java.awt.image.ImageObserver obs = null;
		if (field != null) obs = field.getCanvas();
		g.drawImage(image, x, y, obs );
	}
	
/*------------------
 * implements(Card)
 */
	public int getValue() { return impl.getValue(); }
	
	public int getSuit() { return impl.getSuit(); }
	
	public void specify(int suit, int value) { impl.specify(suit, value); turn(true); }
	
	public void specify(Card card) { impl.specify(card); turn(true); }
	
	public void invalidate() { impl.invalidate(); }
	
	public boolean isSpecifiableAs(int suit, int value) {
		return impl.isSpecifiableAs(suit, value);
	}
	
	public boolean isSpecifiableAs(Card card) {
		return impl.isSpecifiableAs(card);
	}
	
	public void turn() { impl.turn(); }
	
	public void turn(boolean head) { impl.turn(head); }
	
	public boolean isHead() { return impl.isHead(); }
	
	public boolean isUnspecified() { return impl.isUnspecified(); }
	
	public Packet getHolder() { return impl.getHolder(); }
	
	public void setHolder(Packet holder) { impl.setHolder(holder); }
	
	public String toString() { return impl.toString(); }
	
	public boolean equals(Object obj) { return impl.equals(obj); }

}
