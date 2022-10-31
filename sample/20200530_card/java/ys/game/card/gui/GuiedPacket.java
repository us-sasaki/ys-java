package ys.game.card.gui;

import java.awt.*;

import ys.game.card.*;

/**
 * GUI �ł� Packet ��\���܂��B
 *
 * @version		a-release		15, July 2001
 * @author		Yusuke Sasaki
 */
public class GuiedPacket extends Entities implements Packet {
	protected PacketImpl	impl;
	
/*-------------
 * constructor
 */
	/**
	 */
	public GuiedPacket() {
		super();
		impl = new PacketImpl();
		setLayout(new CardHandLayout());
	}
	
	/**
	 */
	public GuiedPacket(Packet packet) {
		super();
		if (packet instanceof GuiedPacket) {
			impl = ((GuiedPacket)packet).impl;
System.out.println("Packet is already an instanceof GuiedPacket.");
		} else {
			if (packet instanceof PacketImpl) impl = (PacketImpl)packet;
			else impl = new PacketImpl(packet);
			
			setGui();
			setLayout(new CardHandLayout());
		}
	}
	
/*-----------
 * overrides
 */
	
/*---------------------
 * implements (Packet)
 */
	public int size() { return impl.size(); }
	public int countUnspecified() { return impl.countUnspecified(); }
	public int countSuit(int suit) { return impl.countSuit(suit); }
	public int[] countSuits() { return impl.countSuits(); }
	public int indexOf(Card c) { return impl.indexOf(c); }
	public int indexOf(int suit, int value) { return impl.indexOf(suit, value); }
	public int indexOf(int suit, int value, int startIndex) { return impl.indexOf(suit, value, startIndex); }
	public boolean contains(Card card) { return impl.contains(card); }
	public boolean contains(int suit, int value) { return impl.contains(suit, value); }
	public boolean containsUnspecified() { return impl.containsUnspecified(); }
	public boolean containsSuit(int suit) { return impl.containsSuit(suit); }
	public boolean containsValue(int value) { return impl.containsValue(value); }
	public boolean intersects(Packet packet) { return impl.intersects(packet); }
	public boolean contains(Packet packet) { return impl.contains(packet); }
	
	public void add(Card c) {
		if (c == null) return;
		GuiedCard card;
		if (c instanceof GuiedCard) card = (GuiedCard)c;
		else card = new GuiedCard(c);
		impl.add(card);
		addEntity(card); // PacketImpl �� addEntity �̒ǉ������͐������Ă�H
	}
	
	public void add(Packet packet) {
		for (int i = 0; i < packet.size(); i++) {
			add(packet.peek(i)); // �x������
		}
	}
	
	public void insertAt(Card c, int index) {
		if (c == null) return;
		GuiedCard card;
		if (c instanceof GuiedCard) card = (GuiedCard)c;
		else card = new GuiedCard(c);
		impl.insertAt(card, index);
		insertEntityAt(card, index);
	}
	
	public Card draw() {
		Card drawn = impl.draw();
		return removeGui(drawn);
	}
	
	public Card draw(int n) {
		Card drawn = impl.draw(n);
		return removeGui(drawn);
	}
	
	public Card draw(Card c) {
		Card drawn = impl.draw(c);
		return removeGui(drawn);
	}
	
	public Card draw(int suit, int value) {
		Card drawn = impl.draw(suit, value);
		return removeGui(drawn);
	}
	
	public Card drawUnspecified() {
		Card drawn = impl.drawUnspecified();
		return removeGui(drawn);
	}
	
	/**
	 * �w�肳�ꂽ Card �̃C���X�^���X�� GuiedCard �̃C���X�^���X�ł��邱�Ƃ��m�F���A
	 * Entities ����؂藣���T�u���[�`���ł��B
	 *
	 * @param		drawn		�؂藣���Ώۂ� Card
	 * @return		�؂藣���ꂽ Card
	 */
	private Card removeGui(Card drawn) {
		if (drawn == null) return null;
		if (!(drawn instanceof GuiedCard))
			throw new InternalError("GuiedPacket �� GuiedCard �ȊO�̃C���X�^���X���i�[����Ă��܂�");
		removeEntity((GuiedCard)drawn);
		return drawn;
	}
	
	public Card peek() { return impl.peek(); }
	public Card peek(int n) { return impl.peek(n); }
	public Card peek(int suit, int value) { return impl.peek(suit, value); }
	public Card peekUnspecified() { return impl.peekUnspecified(); }
	public Packet subpacket(int suit) { return new GuiedPacket(impl.subpacket(suit)); }
	public void setCardOrder(CardOrder order) { impl.setCardOrder(order); }
	public CardOrder getCardOrder() { return impl.getCardOrder(); }
	public Packet intersection(Packet target) {
		GuiedPacket result = new GuiedPacket();
		for (int i = 0; i < size(); i++) {
			Card c = peek(i);
			if (target.contains(c)) result.add(c);
		}
		return result;
	}
	public Packet sub(Packet target) {
		GuiedPacket result = new GuiedPacket();
		for (int i = 0; i < size(); i++) {
			Card c = peek(i);
			if (!target.contains(c)) result.add(c);
		}
		return result;
	}
	
	public void arrange() {
		impl.arrange();
		followGui();
	}
	
	public void shuffle() {
		impl.shuffle();
		followGui();
	}
	
	/**
	 * ���̃I�u�W�F�N�g�������Ă��� PacketImpl �̃C���X�^���X���ێ�����
	 * Card �̓��e�� Entities �Ƃ��Ă��ێ����܂��B�����ɁACard�̃C���X�^���X��
	 * ���ׂ� GuiedCard �ɕύX���܂��B
	 */
	private void setGui() {
		int size = impl.size();
		entity = new Entity[impl.size()];
		entities = impl.size();
		
		for (int i = 0; i < size; i++) {
			Card c = impl.draw(i);
			GuiedCard card;
			if (c instanceof GuiedCard) card = (GuiedCard)c;
			else card = new GuiedCard(c);
			impl.insertAt(card, i);
			entity[i] = card; // insertEntityAt(card, i); �͎g���Ȃ��B
								// impl �� Entity �̐��������Ă��Ȃ���Ԃ̂��߁A
								// ��������҂��� DummyHandLayout �Ȃǂ�
								// �듮�삷��B
		}
		layout();
	}
	
	/**
	 * arrange(), shuffle() �́A�J�[�h�̕��ѕ���ς��܂����A�����̎�����
	 * �C���X�^���X�ϐ����_�C���N�g�ɕύX���Ă���AGuied �̏��Ԃ�ύX���Ă��Ȃ����߁A
	 * ���̏����̐�������ۂ��߂ɌĂ΂�܂��B
	 */
	private void followGui() {
		int size = impl.size();
		for (int i = 0; i < size; i++) {
			GuiedCard c = (GuiedCard)(impl.peek(i));
			entity[i] = c; // insertEntityAt(card, i); �͎g���Ȃ��B
								// impl �� Entity �̐��������Ă��Ȃ���Ԃ̂��߁A
								// ��������҂��� DummyHandLayout �Ȃǂ�
								// �듮�삷��B
		}
		layout();
	}
	
	public Packet complement() { return new GuiedPacket(impl.complement()); }
	
	public void turn() { impl.turn(); }
	public void turn(boolean head) { impl.turn(head); }
	
	public String toString() { return impl.toString(); }
}
