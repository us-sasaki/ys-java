package ys.game.card.bridge;

import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.Card;
import ys.game.card.CardImpl;

/**
 * �u���b�W�ŗL�̊T�O�Ɋւ���e��֗� static �֐���񋟂��܂��B
 *
 * @version		a-release		7, October 2001
 * @author		Yusuke Sasaki
 */
public class BridgeUtils {
	/**
	 * �w�肳�ꂽ�n���h�ɂ����āA�w��X�[�c�̃A�i�[�̓_���J�E���g���܂��B
	 * �A�i�[�_�́AA:4 K:3 Q:2 J:1 �Ōv�Z���܂��B
	 *
	 * @param		hand		�A�i�[�_�v�Z�̑ΏۂƂȂ�n���h
	 * @param		suit		�J�E���g�������X�[�g�̎w��
	 * @return		�A�i�[�_(0����10�̊�)
	 */
	public static int countHonerPoint(Packet hand, int suit) {
		int result = 0;
		Packet oneSuit = hand.subpacket(suit);
		
		if (oneSuit.containsValue(Card.ACE  )) result += 4;
		if (oneSuit.containsValue(Card.KING )) result += 3;
		if (oneSuit.containsValue(Card.QUEEN)) result += 2;
		if (oneSuit.containsValue(Card.JACK )) result += 1;
		
		return result;
	}
	
	/**
	 * �w�肳�ꂽ�n���h�ł̃A�i�[�̖������J�E���g���܂��B
	 * �������A�A�i�[���P���ł�����X�[�g�ɂ��ẮA10 ���A�i�[�Ƃ݂Ȃ��܂��B
	 */
	public static int countHoners(Packet hand, int suit) {
		int result = 0;
		Packet oneSuit = hand.subpacket(suit);
		
		if (oneSuit.containsValue(Card.ACE  )) result++;
		if (oneSuit.containsValue(Card.KING )) result++;
		if (oneSuit.containsValue(Card.QUEEN)) result++;
		if (oneSuit.containsValue(Card.JACK )) result++;
		if ((oneSuit.containsValue(10))&&(result > 0)) result++;
		
		return result;
	}
	
	/**
	 * �A�i�[�_(�S�̓_�A�e�X�[�c�̓_)���v�Z���܂��B
	 * �A�i�[�_�́AA:4 K:3 Q:2 J:1 �Ōv�Z���܂��B
	 * �z��̓Y���O�ɑS�̓_�A�Y�� Card.CLUB(=1), �c�c, Card.SPADE(=4)��
	 * �e�X�[�c�̓_�����i�[����܂��B
	 *
	 * @param		hand		�A�i�[�_���v�Z����n���h
	 * @return		�A�i�[�_�Ɋւ�����
	 */
	public static int[] countHonerPoint(Packet hand) {
		int[] result = new int[5];
		result[0] = 0;
		for (int suit = 1; suit < 5; suit++) {
			result[suit] = countHonerPoint(hand, suit);
			result[0] += result[suit];
		}
		
		return result;
	}
	
	/**
	 * �w�肳�ꂽ�n���h�̎w�肳�ꂽ�X�[�g�ɂ��āA���̃o�����[�𕶎���ɂ��܂��B
	 * AKQ52 �̂悤�ɍ~���ɕϊ�����܂��B
	 */
	public static String valuePattern(Packet hand, int suit) {
		Packet p = hand.subpacket(suit);
		p.arrange();
		
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < p.size(); i++) {
			int v = p.peek(i).getValue();
			switch (v) {
			case Card.ACE:		s.append('A'); break;
			case Card.KING:		s.append('K'); break;
			case Card.QUEEN:	s.append('Q'); break;
			case Card.JACK:		s.append('J'); break;
			case 10:			s.append('T'); break;
			default:			s.append(v); break;
			}
		}
		
		return s.toString();
	}
	
	/**
	 * �w�肳�ꂽ�n���h���w�肳�ꂽ�n���h�p�^�[���ɓK�����邩�̔�����s���܂��B
	 * �{���\�b�h�Ŏw�肷��n���h�p�^�[���� valuePattern() �̌`���ɏ������܂����A
	 * ���C���h�J�[�h���g�p���邱�Ƃ��ł��܂��B���C���h�J�[�h�Ƃ��āA�ȉ��̕���
	 * ���g�p�ł��܂��B�������A�n���h�p�^�[���͍~���ɋL�q����K�v������܂��B<BR>
	 *<BR>
	 * x...2-9 �̂����ꂩ�P�� <BR>
	 * X...2-T �̂����ꂩ�P�� <BR>
	 * ?...�����ꂩ�P�� <BR>
	 * *...�����ꂩ�O���ȏ�(���ݖ����ł����������@�\���܂���) <BR>
	 *
	 */
	public static boolean patternMatch(Packet hand, String pattern, int suit) {
		String handpat = valuePattern(hand, suit);
		for (int i = 0; i < handpat.length(); i++) {
			if (i == pattern.length()) return false;
			char conditionLetter = pattern.charAt(i);
			char target = handpat.charAt(i);
			
			switch (conditionLetter) {
			
			case 'x':
				if ((target >= '2')&&(target <= '9')) break;
				return false;
			case 'X':
				if ( ((target >= '2')&&(target <= '9'))||(target == 'T') ) break;
				return false;
			case '?':
				break;
			case '*':
				return true; // �蔲��
			default:
				if ( conditionLetter == target ) break;
				return false;
			}
		}
		if (pattern.length() == handpat.length()) return true;
		if ( (pattern.length() == handpat.length() + 1)&&(pattern.endsWith("*")) ) return true;
		return false;
	}
	
	/**
	 * �^����ꂽ�{�[�h�ɂ�����I���W�i���n���h���v�Z���܂��B
	 * �{���\�b�h�ł́A���ݎ����Ă���n���h�ɂ���܂ł̃g���b�N�̃J�[�h��
	 * �ǉ����Č��ʂ����߂Ă��邽�߁A�w�� Board �� unspecified Card ���܂܂��
	 * �����ꍇ�A���ʂɂ� unspecified Card ���܂܂�邱�ƂƂȂ�܂��B
	 * 
	 * @param		board		�I���W�i���n���h�����߂��� Board
	 *
	 * @return		�I���W�i���n���h�̔z��(�Y���ɂ� Board.NORTH �Ȃǂ��w��)
	 */
	public static Packet[] calculateOriginalHand(Board board) {
		Packet[] result = new Packet[4];
		for (int i = 0; i < 4; i++) result[i] = new PacketImpl();
		
		// �������Ă���n���h���R�s�[
		Packet[] original = board.getHand();
		for (int i = 0; i < original.length; i++) {
			for (int j = 0; j < original[i].size(); j++) {
				result[i].add(original[i].peek(j));
			}
		}
		
		// �v���C���ꂽ�n���h���R�s�[
		Trick[] trick = board.getAllTricks();
		for (int i = 0; i < trick.length; i++) {
			Trick tr = trick[i];
			if (tr == null) break;
			int seat = tr.getLeader();
			for (int j = 0; j < tr.size(); j++) {
				result[seat].add(tr.peek(j));
				seat++;
				seat = (seat % 4);
			}
		}
		
		// ���בւ�
		for (int i = 0; i < 4; i++) {
			result[i].arrange();
		}
		return result;
	}
	
	/**
	 * �^����ꂽ�{�[�h�ɂ����ăf�B�N���A���[���̂Ƃ����g���b�N�����J�E���g���܂��B
	 *
	 * @param		board		�E�B�i�[�𐔂���ΏۂƂȂ�{�[�h
	 */
	public static int countDeclarerSideWinners(Board board) {
		Trick[]	tr			= board.getAllTricks();
		if (tr == null) return 0;
		
		int		win			= 0;
		int		declarer	= board.getDeclarer();
		
		for (int i = 0; i < tr.length; i++) {
			if (tr[i] == null) break;
			if (!tr[i].isFinished()) break;
			int winner = tr[i].getWinner();
			if ( ((winner ^ declarer) & 1) == 0 ) win++;
		}
		
		return win;
	}
	
	/**
	 * �^����ꂽ�{�[�h�ɂ����ăf�B�t�F���_�[���̂Ƃ����g���b�N�����J�E���g���܂��B
	 *
	 * @param		board		�E�B�i�[�𐔂���ΏۂƂȂ�{�[�h
	 */
	public static int countDefenderSideWinners(Board board) {
		return board.getTricks() - countDeclarerSideWinners(board);
	}
	
	/**
	 * �X�[�g�萔�̕�����\����ԋp���܂�
	 *
	 * @since	2015/8/15
	 *
	 * @return		�X�[�g�̕�����\��(* S H D C Jo)
	 */
	public static String suitString(int suit) {
		switch (suit) {
			case Card.UNSPECIFIED:
				return "*";
			case Card.SPADE:
				return "S";
			case Card.HEART:
				return "H";
			case Card.DIAMOND:
				return "D";
			case Card.CLUB:
				return "C";
			default:
				return "Jo";
		}
	}
	
	/**
	 * �o�����[�萔�̕�����\����ԋp���܂�
	 *
	 * @since	2015/8/15
	 *
	 * @return		�o�����[�̕�����\��(AKQJT98765432)
	 *
	 */
	public static String valueString(int value) {
		switch (value) {
			case Card.UNSPECIFIED:
			case Card.JOKER:
				return "";
			case Card.ACE:
				return "A";
			case 10:
				return "T";
			case Card.JACK:
				return "J";
			case Card.QUEEN:
				return "Q";
			case Card.KING:
				return "K";
			default:
				return Integer.toString(value);
		}
	}
	
}
