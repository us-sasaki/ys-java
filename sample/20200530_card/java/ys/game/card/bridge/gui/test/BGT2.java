import java.awt.*;
import java.awt.image.*;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;
import ys.game.card.bridge.gui.*;


public class BGT2 extends Frame {
	
/*-------------
 * Constructor
 */
	public BGT2() {
		super("Board GUI Test");
		
		setLayout(new FlowLayout());
		pack(); // create peer
		
	}
	
	private static void setProblem(PlayMain main) {
		main.addProblem(new RandomProblem());
		main.addProblem(new RegularProblem("7H", Bid.BID, 7, Bid.HEART, new String[] {"S:J832 H:5 D:A C:AKJ7652", "Rest", "S: H:AKQJT92 D:J976 C:Q3", "Rest"}, "コントラクト：７Ｈ by Ｓ\nS((ディクレアラー)とN(ダミー)の手を\n両方操作して 13トリックとってね。\n切り札スーツはＨ（ハート）です。", null));
		main.addProblem(new RegularProblem("6C", Bid.BID, 6, Bid.CLUB, new String[] {"H:J5 C:QT42 D:AJ82 S:642", "Rest", "H:KQT97 C:AKJ65 D:Q6 S:A", "Rest"}, "コントラクト：６Ｃ by Ｓ\nS((ディクレアラー)とN(ダミー)の手を\n両方操作して 12トリックとってね。\n切り札スーツはＣ（クラブ）です。", null));
		main.addProblem(new RegularProblem("7S", Bid.BID, 7, Bid.SPADE, new String[] {"S:AKT96 H:32 D:A5432 C:5", "Rest", "S:QJ87 H:AK4 D:6 C:A7643", "Rest"}, "コントラクト：７Ｓ by Ｓ\nS((ディクレアラー)とN(ダミー)の手を\n両方操作して 13トリックとってね。\n切り札スーツはＳ（スペード）です。", null));
		main.addProblem(new RegularProblem("3NT", Bid.BID, 3, Bid.NO_TRUMP, new String[] {"S:AQJ74 H:6 D:A62 C:T732", "Rest", "S:K6 H:KJT873 D:KJ5 C:A8", "Rest"}, "コントラクト：３ＮＴ by Ｓ\nS((ディクレアラー)とN(ダミー)の手を\n両方操作して 9トリックとってね。\n切り札スーツはありません。", null));
		main.addProblem(new RegularProblem("5D", Bid.BID, 5, Bid.DIAMOND, new String[] {"S:65 H:K754 D:QT652 C:43", "Rest", "S: H:Q32 D:AKJ43 C:AJ752", "Rest"}, "コントラクト：５Ｄ by Ｓ\nS((ディクレアラー)とN(ダミー)の手を\n両方操作して 11トリックとってね。\n切り札スーツはＤ（ダイヤモンド）です。", null));
		main.addProblem(new RegularProblem("7NT", Bid.BID, 7, Bid.NO_TRUMP, new String[] {"S:AKQ H:T987 D:53 C:A753", "Rest", "S: H:AK D:AKQJT92 C:8642", "Rest"}, "コントラクト：７ＮＴby Ｓ\nS((ディクレアラー)とN(ダミー)の手を\n両方操作して 13トリックとってね。\n切り札スーツはありません。", null));
	}

/*------------------
 * デバッグ用メイン
 */
	public static void main(String[] args) {
		BGT2 t = new BGT2();
		t.setSize(640, 500);
		GuiedCard.setCardImageHolder(new LocalCardImageHolder());
		Explanation.media = new LocalMediaLoader();
		
		PlayMain main = new PlayMain(t);
		setProblem(main);
		main.initialize();
		t.pack();
		while (true) {
			main.start();
			main.dispose();
		}
	}
}
