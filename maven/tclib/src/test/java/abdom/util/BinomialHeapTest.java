package abdom.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.PriorityQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


/**
 * 二項ヒープのテスト
 */
class BinomialHeapTest {

	@Nested class BinomialHeapNodetest {
		BinomialHeapNode b1 = new BinomialHeapNode(1);
		BinomialHeapNode b2 = new BinomialHeapNode(3);
		BinomialHeapNode b3 = new BinomialHeapNode(2);
		
		@Nested
		class mergeできること {
			@Test
			void _１と３をマージして１３が得られること() {
				BinomialHeapNode a1 = b1.merge(b2);
				assertEquals("[d:1]1 3 ", a1.toString());
			}

			@Test
			void _１３に２をマージして１２３が得られること() {
				BinomialHeapNode a1 = b1.merge(b2).merge(b3);
				assertEquals("[d:0]2 ([d:1]1 3 )", a1.toString());
			}
		}

		@Nested
		class addできること {
			BinomialHeap b;
			@BeforeEach
			void init() {
				b = new BinomialHeap();
				b.add(1);
				b.add(3);
				b.add(2);
			}

			@Test
			void _１が得られること() {
				assertEquals(1, b.poll());
			}
			@Test
			void _２が得られること() {
				b.poll();
				assertEquals(2, b.poll());
			}
			@Test
			void _３が得られること() {
				b.poll();
				b.poll();
				assertEquals(3, b.poll());
			}
		}
	}

	PriorityQueue<Integer> merge(PriorityQueue<Integer> a, PriorityQueue<Integer> b) {
		while (!b.isEmpty()) {
			a.add(b.poll());
		}
		return a;
	}

	void add(BinomialHeap bh, int ...values) {
    for (int value: values) {
      bh.add(value);
      System.out.println("add " + value + ": result=" + bh);
    }
  }
  // test0
  // @SuppressWarnings("unchecked")
	@Test
	void test0() {
    System.out.println("--------------");
    BinomialHeap bh1 = new BinomialHeap();
    add(bh1, 26,1,43);
    add(bh1,21);
    add(bh1, 8);
    System.out.println("bh1="+bh1);
    BinomialHeap bh2 = new BinomialHeap();
    add(bh2, 8,6,2,4);
    System.out.println("bh2="+bh2);

    bh1.merge(bh2);
    System.out.println("merged="+bh1);
    System.out.println("merged.size="+bh1.size());
  }
	
	/*
  // test1
  @Test
  @SuppressWarnings("unchecked")
	void test1() {
		int M = 10000;
		Random r = new Random(12345L);
		
		int N = 801;
		BinomialHeap[] heaps = new BinomialHeap[N];
		for (int i = 0; i < N; i++) heaps[i] = new BinomialHeap();
		
		PriorityQueue<Integer>[] ques = new PriorityQueue[N];
		for (int i = 0; i < N; i++) ques[i] = new PriorityQueue<>();
		
		for (int i = 0; i < N; i++) {
			int rnd = r.nextInt(N) + 1;
			heaps[i].add(rnd);
      // System.out.println("heaps["+i+"]="+heaps[i]);
			ques[i].add(rnd);
		}
		
		for (int i = 0; i < M; i++) {
			int i1 = r.nextInt(N);
			// System.out.println("i1 = " +i1);
			while (heaps[i1] == null) i1 = (i1+1) %N;
			
			int i2 = r.nextInt(N);
			if (i1 == i2) i2 = (i2 + 1) %N;
			if (heaps[i2] == null) {
				int rnd = r.nextInt(N) + 1;
				// System.out.println("add " + rnd + " to ["+i1+"]");
				heaps[i1].add(rnd);
				ques[i1].add(rnd);
        // System.out.println("added heap =" + heaps[i1]);
			} else {
				// System.out.println("merge [" + i2 + "] to [" + i1 + "]");
				heaps[i1].merge(heaps[i2]); heaps[i2] = null;
				merge(ques[i1], ques[i2]); ques[i2] = null;
        // System.out.println("merged heap =" + heaps[i1]);
        if (heaps[i1].size() != ques[i1].size()) System.out.println("size mismatch");
			}
		}
		// check
		for (int i = 0; i < N; i++) {
			System.out.println("test No." + i);
			if (heaps[i] == null) {
				if (ques[i] != null) System.out.println("mismatch "+ i);
			} else {
				while (!heaps[i].isEmpty()) {
					int a = heaps[i].poll();
					int b = ques[i].poll();
					if (a != b) {
						System.out.println("mismatch2 " + i + " heap="+a+" que="+b);
					}
				}
			}
		}
	}
*/
}
