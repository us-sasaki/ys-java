package ys.game.card.gui;

import java.awt.*;

import ys.game.card.*;

/**
 * Applet �Ƃ��ē��삳���邽�߂� Card �� GUI �\����^���܂��B
 * ���ۓI�� Card �T�O(CardImpl�ł̎���)�ɒǉ������T�O�Ƃ��āA�J�[�h�̈ʒu�Ȃǂ�
 * ��L�͈́A����������܂��B
 * Human Player �̂��� Board �͂��̃C���X�^���X��ێ����AHuman Player�ɑ΂��� GUI
 * ����@�\��񋟂��܂��B
 *
 * @version		a-release		17, May 2000
 * @author		Yusuke Sasaki
 */
public class GuiedCard extends Entity implements Card {
	
/*-----------
 * Constants
 */
	/**
	 * �������̍ŏ������ł��B
	 * ����̃J�[�h��ʂ������}�[�N��������T�C�Y���ݒ肳��Ă��܂��B
	 */
	public static final int XSTEP = 14;
	
	/**
	 * �������̍ŏ��c���ł��B
	 * ����̃J�[�h��ʂ������}�[�N��������T�C�Y���ݒ肳��Ă��܂��B
	 */
	public static final int YSTEP = 16;
	
	/** �������̉����ł��B */
	public static final int XSIZE = 59;
	
	/** �������̏c���ł��B */
	public static final int YSIZE = 87;
	
/*-----------
 * variables
 */
	/**
	 * �C���[�W��ێ�����I�u�W�F�N�g�ł��B
	 */
	protected static CardImageHolder	imageHolder;
	
	/** ���ۂ̃J�[�h�����͂��̃I�u�W�F�N�g�ɈϏ�����܂��B */
	protected CardImpl		impl;
	
	/** �J�[�h�̌����ł��B0-3�̒l�ŁA0�����̌����ňȌ�90��������]���܂��B */
//	protected int			direction;
	
/*-------------
 * constructor
 */
	/**
	 * Unspecified Card ���쐬���܂��B
	 * Unspecified GuiedCard �͏�����ԂƂ��āA�����A�������ɐݒ肳��܂��B
	 */
	public GuiedCard() {
		super();
		impl = new CardImpl();
		setSize(XSIZE, YSIZE);
	}
	
	/**
	 * �w�肳�ꂽ�X�[�g�A�o�����[�� Card ���쐬���܂��B
	 * ������Ԃ͒����A�\�����ł��B
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
	 * ���̃N���X�Ŏg�p���� CardImageHolder ���w�肵�܂��B
	 * ���̎w����s��Ȃ��ꍇ�A�J�[�h�͕\������܂���B
	 *
	 * @param		holder		CardImageHolder
	 */
	public static void setCardImageHolder(CardImageHolder holder) {
		imageHolder = holder;
	}
	
	/**
	 * ���̃N���X�Ŏg�p���� CardImageHolder ���擾���܂��B
	 */
	public static CardImageHolder getCardImageHolder() {
		return imageHolder;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * �J�[�h�̌�����ݒ肵�܂��B
	 *
	 * @param		direction		�J�[�h
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
