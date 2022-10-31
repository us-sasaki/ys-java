package ys.game.card.bridge;

import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.Card;
import ys.game.card.CardImpl;

/**
 * �v���C���ꂽ�J�[�h�A�f�B�X�J�[�h�Ȃǂ̏�񂩂�A
 * �f�B�X�g���r���[�V�������v�Z����N���X�B
 * �S�l�̃X�[�g�̖����Ɋւ�����A�ԈႢ�Ȃ��E�B�i�[���擾���� static�֐���񋟂��܂��B
 * �����I�ɂ́A�n���h�p�^�[���ɂ��āA���̓��e�i�Ɗm���j��񋟂������B
 * �{�N���X�� countDistribution(Board, int) ���\�b�h�́A�ŏ��A�ő�ɂ��Ę_���I��
 * �l��蕝�������Ȃ�\��������܂��B����́A�ŏ��A�ő告�݂̈ˑ��֌W������A�����I��
 * ���߂�����i�����������߂�ۂ̃j���[�g���@���C�N�j���̗p���Ă��邱�ƂɋN�����܂��B
 * �����I�ɔ����������s���A�������ŋ��߂�Ȃǂŉ��P����]�n������܂����ASimplePlayer2
 * �ł̎g�p����Ȃ����x���ł��B
 *
 * @author		Yusuke Sasaki
 * @version		a-release		20, January 2002
 */
public class ThinkingUtils {
	static final int MIN = 0;
	static final int MAX = 1;
	
	/**
	 * �S�l�̃f�B�X�g���r���[�V������(�킩��͈͂�)�J�E���g���܂��B
	 * �w�肷�� Board �ł́A�_�~�[�Ǝw�肵���Ȃ̃n���h���������Ă���K�v������܂��B
	 *
	 * @param		board		�S�l�̃n���h��ێ����� Board
	 * @param		seat		�����̐�
	 * @return		int �̂R�����z��(int[4][4][2])�ŁA[����][�X�[�g][�ő�(1) or �ŏ�(0)]
	 */
	public static int[][][] countDistribution(Board board, int seat) {
		if (seat == board.getDummy())
			throw new IllegalArgumentException("�_�~�[�ɂ�����J�E���g�̓T�|�[�g���Ă܂���");
		if ( (seat < 0)||(seat > 3) )
			throw new IllegalArgumentException("�w�肳�ꂽ seat �̒l(="+seat+")���ُ�ł�");
		
		int[][][] c = new int[4][][];
		
		// �����ƃ_�~�[�̃n���h�̃f�B�X�g���r���[�V�����͂��łɂ킩���Ă���
		int dummySeat = board.getDummy();
		
		Packet dummyHand	= board.getHand(board.getDummy());
		Packet myHand		= board.getHand(seat);
		
		c[dummySeat]	= countKnownDistribution(dummyHand);
		c[seat]			= countKnownDistribution(myHand);
		
		//
		// ���̂Q�̃f�B�X�g���r���[�V�������v�Z����
		//
		int[] other = new int[2];
		int num = 0;
		for (int dir = 0; dir < 4; dir++) {
			if ( (dir == dummySeat)||(dir == seat) ) continue;
			other[num] = dir;
			num++;
		}
		
		// �܂��A�c���Ă���J�[�h��������v�Z����B
		// (�ŏ�����)=0
		// (�ő喇��)= min( ���̃X�[�g�̎c����, ���̐l�̃n���h�̖���)
		Packet played = getPlayedCards(board);
		
		int[][] playedDist = countKnownDistribution(played); // �v���C���ꂽ�J�[�h�̃f�B�X�g���r���[�V����
		
		c[ other[0] ] = new int[4][2];
		c[ other[1] ] = new int[4][2];
		
		for (int suit = 0; suit < 4; suit++) {
			int restCards =			13
								- playedDist[suit][MIN]
								- c[dummySeat][suit][MIN]
								- c[seat][suit][MIN]; // �c�薇��
			c[ other[0] ][suit][MIN]
			= c[ other[1] ][suit][MIN]
			= 0;
			
			c[ other[0] ][suit][MAX] = Math.min(restCards, board.getHand( other[0] ).size());
			c[ other[1] ][suit][MAX] = Math.min(restCards, board.getHand( other[1] ).size());
		}
		
		// ���ɁA�V���E�A�E�g�̏���p����
		// �V���E�A�E�g i.e. ���̐l�̂��̃X�[�g��MAX=0
		for (int i = 0; i < board.getTricks(); i++) {
			Trick trick = board.getAllTricks()[i];
			int	leadSuit = trick.getLead().getSuit();
			for (int j = 1; j < trick.size(); j++) {
				int player = (j + trick.getLeader())%4;
				if ( ( player == dummySeat )||( player == seat ) ) continue;
				if (trick.peek(j).getSuit() != leadSuit) { // �V���E�A�E�g
					c[ player ][leadSuit-1][MAX] = 0;
					// ������l�͂��ꂩ��������B���̃X�[�g�̖����͊m�肷��B
					int another = 0;
					if (other[0] == player) another = 1;
					c[ other[another] ][leadSuit-1][MAX] =
					c[ other[another] ][leadSuit-1][MIN] =
									13
								- playedDist[leadSuit-1][MAX]
								- c[ dummySeat  ][leadSuit-1][MAX]
								- c[   seat     ][leadSuit-1][MAX];
				}
			}
		}
		// �Ō�ɁA�V���E�A�E�g�������Ƃ���ŏ����������肳��镪�̏C��
		for (int i = 0 ; i < 2; i++) {
			int cards = board.getHand( other[i] ).size()
						- c[other[i]][0][MAX]
						- c[other[i]][1][MAX]
						- c[other[i]][2][MAX]
						- c[other[i]][3][MAX];
			for (int suit = 0; suit < 4; suit++) {
				// �����X�[�g�̃J�[�h��������o�����
				int restMinCards =	13
								- playedDist[suit][MAX]
								- c[ dummySeat  ][suit][MAX]
								- c[   seat     ][suit][MAX]
								- c[ other[1-i] ][suit][MAX];
				
				// �n���h�̑�������o�����
				int cc = cards + c[other[i]][suit][MAX];
				restMinCards = Math.max(restMinCards, cc);
				
				// �V���E�A�E�g�������Ƃɂ���āA��������� other �ɗ^����e��������̂ł́H
				// ��ōl���ς݂�����
				// �ł́AMIN�����܂������Ƃɂ����MAX�ɂ܂��e�����ł�̂ł́H
				// -> �ł�B�ő喇�����ς��
				int newMin = Math.max(c[other[i]][suit][MIN], restMinCards);
//				if (newMin > 0) {
//					for (int otherSuit = 0; otherSuit < 4; otherSuit++) {
//						if (otherSuit == suit) continue;
						
				c[ other[i]][suit][MIN] = newMin;
			}
			
		}
		//
		// �˂ɐ��藧���ɂ��MAX�̕␳(by �a��)
		//
		for (int i = 0; i < 2; i++) {
			for (int suit = 0; suit < 4; suit++) {
				c[ other[1-i]][suit][MAX] = 	13
								- playedDist[suit][MAX]
								- c[ dummySeat  ][suit][MAX]
								- c[   seat     ][suit][MAX]
								- c[  other[i]  ][suit][MIN];
			}
		}
		return c;
	}
	
	/**
	 * �^����ꂽ�n���h�̃f�B�X�g���r���[�V�������J�E���g���܂��B
	 * UnspecifiedCard �̓J�E���g����܂��񂪁A�{���\�b�h�� Specified Card
	 * ����Ȃ� Packet �ɑ΂��Ďg�p���邱�Ƃ�z�肵�Ă��܂��B
	 *
	 * @param		hand		�J�E���g�������n���h
	 * @return		���Y���̓X�[�g������킵�A���Y���́A�ŏ��l���ő�l�̑I�������܂��B
	 */
	private static int[][] countKnownDistribution(Packet hand) {
		int[][] result = new int[4][2];
		
		for (int i = 0; i < 4; i++) {
			result[i][MIN] = result[i][MAX] = hand.countSuit(i+1); // �ŏ��l = �ő�l (�����m��)
		}
		return result;
	}
	
	/**
	 * ���łɃv���C���ꂽ�J�[�h���擾���܂��B
	 * ���݁ATrick ����P��������Ă���A���S���Y���ł��B
	 */
	private static Packet getPlayedCards(Board board) {
		PacketImpl result = new PacketImpl();
		
		Trick[] tr = board.getAllTricks();
		if (tr == null) return result;
		
		int trickCount = board.getTricks();
		if (trickCount < 13) trickCount++;
		for (int i = 0; i < trickCount; i++) {
			for (int j = 0; j < tr[i].size(); j++) {
				result.add(tr[i].peek(j));
			}
		}
		return result;
	}
		
	
}
