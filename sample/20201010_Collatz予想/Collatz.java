import java.util.*;

/**
 * Collatz 予想が正しいか、有限の数までで確認する。
 * 無限ループ、または例外が出なければ指定した数までは正しい。
 */
public class Collatz {
	Map<Long, Long> solved;
	long solvedMax = 1; // この数以下はすべて 1 になる
	
	private void solve(long number, long life) {
		if (number == 1) return;
		if (number > Long.MAX_VALUE/3)
			throw new RuntimeException("value exceeded: "+ number);
		if (solvedMax >= number) return;
		if (solved.containsKey(number)) return;
		solved.put(number, life);
		if (number == solvedMax+1) {
			while (solved.containsKey(++solvedMax)); // solved.remove(solvedMax);
			solvedMax--;
		}
		if (number%2 == 0) solve(number/2, life + 1);
		else {
			solve((number*3+1)/2, life + 2);
		}
	}
	
	public Collatz(int arg) {
		solved = new HashMap<>();
		for (int i = 2; i < arg; i++) {
			solve(i, 1);
		}
	}
	public static void main(String[] args) throws Exception {
		System.out.println("It takes about 12.7 secs, wait please.");
		long t0 = System.currentTimeMillis();
		Collatz c = new Collatz(40000000); // 80000000
		long max = 0;
		for (long v : c.solved.keySet()) {
			max = Math.max(max, v);
		}
		System.out.println(max);
		System.out.println("elapsed: " + (System.currentTimeMillis() - t0));
		System.out.println("solvedMax="+c.solvedMax);
		System.out.println("solved.size()="+c.solved.size());
		for (long i = 0; i < 10; i++) {
			Long l = c.solved.get(i);
			if (l == null) continue;
			System.out.println(i + ":" + l);
		}
	}
}