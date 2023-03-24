/**
 * quickselect はクイックソートの原理を応用したアルゴリズムで、平均の場合に O(n)、
 * 最悪の場合 O(n^2)で k 番目の要素を見つけることができる。
 */
class QuickSelect {
	private static int partition(int[] arr, int l, int r) {
		int pivot = arr[r-1], pivotloc = l;
		for (int i = l; i < r; i++) {
			if (arr[i] < pivot) {
				int tmp = arr[i];
				arr[i] = arr[pivotloc];
				arr[pivotloc] = tmp;
				pivotloc++;
			}
		}
		
		int tmp = arr[r-1];
		arr[r-1] = arr[pivotloc];
		arr[pivotloc] = tmp;
		
		return pivotloc;
	}
	
	/**
	 * 指定された配列値のうち、k 番目に小さい数を求めます。
	 * 計算量は平均的に O(n) 、最悪 O(n^2) です。
	 *
	 * @param	arr		配列値
	 * @aram	k		何番目か(0～)
	 * @return	配列値のうち、k番目に小さい数
	 */
	public static int kthSmallest(int[] arr, int k) {
		return kthSmallest(arr, 0, arr.length, k);
	}
	
	/**
	 * 指定された配列値のうち、k 番目に小さい数を求めます。
	 * 計算量は平均的に O(n), 最悪 O(n^2) です。
	 *
	 * @param	arr		配列値
	 * @param	l		配列の開始インデックス(含む)
	 * @param	r		配列の終了インデックス(含まない)
	 * @aram	k		何番目か(0～)
	 * @return	配列値のうち、k番目に小さい数
	 */
	public static int kthSmallest(int[] arr, int l, int r, int k) {
		int partition = partition(arr, l, r);
		
		if (partition == k)
			return arr[partition];
		else
			if (partition < k) return kthSmallest(arr, partition + 1, r, k);
		else
			return kthSmallest(arr, l, partition, k);
	}
	
	/**
	 * 指定された配列値のうち、k 番目に大きい数を求めます。
	 * 計算量は平均的に O(n), 最悪 O(n^2) です。
	 *
	 * @param	arr		配列値
	 * @aram	k		何番目か(0～)
	 * @return	配列値のうち、k番目に大きい数
	 */
	public static int kthLargest(int[] arr, int k) {
		return kthSmallest(arr, 0, arr.length, arr.length - k - 1);
	}
	
	/**
	 * 指定された配列値のうち、k 番目に大きい数を求めます。
	 * 計算量は平均的に O(n), 最悪 O(n^2) です。
	 *
	 * @param	arr		配列値
	 * @param	l		配列の開始インデックス(含む)
	 * @param	r		配列の終了インデックス(含まない)
	 * @aram	k		何番目か(0～)
	 * @return	配列値のうち、k番目に大きい数
	 */
	public static int kthLargest(int[] arr, int l, int r, int k) {
		return kthSmallest(arr, l, r, r-l-k-1);
	}
	
	public static void main(String[] args) {
		int[] v = new int[] { 4, 6, 9, 10, 1, 2, 5, 8, 7, 3 };
		
		for (int k = 0; k < 10; k++) {
			System.out.printf("%02d番目に小さい数:%02d\n", k, kthSmallest(v, k));
			System.out.printf("%02d番目に大きい数:%02d\n", k, kthLargest(v, k));
		}
		
		System.out.println(kthSmallest(v, 1, 8, 4));
		System.out.println(kthLargest(v, 1, 8, 3));
		
	}
	
}
