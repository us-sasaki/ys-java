import java.util.*;

/**
 * Collatz �\�z�����������A�L���̐��܂łŊm�F����B
 * �������[�v�A�܂��͗�O���o�Ȃ���Ύw�肵�����܂ł͐������B
 */
public class Collatz {
	Set<Long> solved;
	long solvedMax = 1; // ���̐��ȉ��͂��ׂ� 1 �ɂȂ�
	
	private void solve(long number) {
		if (number == 1) return;
		if (number > Long.MAX_VALUE/3)
			throw new RuntimeException("value exceeded: "+ number);
		if (solvedMax >= number) return;
		if (solved.contains(number)) return;
		solved.add(number);
		if (number == solvedMax+1) {
			while (solved.contains(++solvedMax)) solved.remove(solvedMax);
			solvedMax--;
		}
		if (number%2 == 0) solve(number/2);
		else {
			solve((number*3+1)/2);
		}
	}
	
	public Collatz(int arg) {
		solved = new HashSet<>();
		for (int i = 2; i < arg; i++) {
			solve(i);
		}
	}
	public static void main(String[] args) throws Exception {
		System.out.println("It takes about 23.5 secs, wait please.");
		long t0 = System.currentTimeMillis();
		Collatz c = new Collatz(80000000);
		long max = 0;
		for (long v : c.solved) {
			max = Math.max(max, v);
		}
		System.out.println(max);
		System.out.println("elapsed: " + (System.currentTimeMillis() - t0));
		System.out.println("solvedMax="+c.solvedMax);
		System.out.println("solved.size()="+c.solved.size());
	}
}