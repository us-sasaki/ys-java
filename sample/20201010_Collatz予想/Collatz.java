import java.util.*;

/**
 * Collatz �\�z�����������A�L���̐��܂łŊm�F����B
 * �������[�v�A�܂��͗�O���o�Ȃ���Ύw�肵�����܂ł͐������B
 */
public class Collatz {
	Set<Long> solved;
	
	private void solve(long number) {
		if (number == 1) return;
		if (number > Long.MAX_VALUE/3)
			throw new RuntimeException("value exceeded: "+ number);
		if (solved.contains(number)) return;
		solved.add(number);
		if (number%2 == 0) solve(number/2);
		else {
			solve(number*3+1);
		}
	}
	
	public Collatz(int arg) {
		solved = new HashSet<>();
		for (int i = 2; i < arg; i++) {
			solve(i);
		}
	}
	public static void main(String[] args) throws Exception {
		long t0 = System.currentTimeMillis();
		Collatz c = new Collatz(20000000);
		long max = 0;
		for (long v : c.solved) {
			max = Math.max(max, v);
		}
		System.out.println(max);
		System.out.println("elapsed: " + (System.currentTimeMillis() - t0));
	}
}