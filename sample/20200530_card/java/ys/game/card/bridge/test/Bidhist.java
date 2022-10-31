package ys.game.card.bridge;

import ys.game.card.bridge.*;

public class Bidhist {
	public static void main(String[] args) {
		BiddingHistory hist = new BiddingHistory(Board.EAST);
		
		print(hist);
		
		hist.bid(new Bid(Bid.PASS, 0, 0));
		print(hist);
		
		hist.bid(new Bid(Bid.BID, 1, Bid.CLUB));
		print(hist);
		
		hist.bid(new Bid(Bid.DOUBLE, 1, Bid.CLUB));
		print(hist);
		
		hist.bid(new Bid(Bid.PASS, 0, 0));
		print(hist);
		
		hist.bid(new Bid(Bid.BID, 1, Bid.DIAMOND));
		print(hist);
		
		hist.bid(new Bid(Bid.BID, 2, Bid.CLUB));
		print(hist);
		
		hist.bid(new Bid(Bid.BID, 3, Bid.CLUB));
		print(hist);
		
		hist.bid(new Bid(Bid.BID, 4, Bid.CLUB));
		print(hist);
		
		hist.bid(new Bid(Bid.DOUBLE, 4, Bid.CLUB));
		print(hist);
		
		hist.bid(new Bid(Bid.REDOUBLE, 4, Bid.CLUB));
		print(hist);
		
	}
	
	private static void print(BiddingHistory h) {
		System.out.println("Bidding History");
		System.out.println(h);
		System.out.println("Contract = " + h.getContract());
		System.out.println("Declarer = " + h.getDeclarer());
	}
}
