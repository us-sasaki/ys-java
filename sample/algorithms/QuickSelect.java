/**
 * quickselect �̓N�C�b�N�\�[�g�̌��������p�����A���S���Y���ŁA���ς̏ꍇ�� O(n)�A
 * �ň��̏ꍇ O(n^2)�� k �Ԗڂ̗v�f�������邱�Ƃ��ł���B
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
	 * �w�肳�ꂽ�z��l�̂����Ak �Ԗڂɏ������������߂܂��B
	 * �v�Z�ʂ͕��ϓI�� O(n) �A�ň� O(n^2) �ł��B
	 *
	 * @param	arr		�z��l
	 * @aram	k		���Ԗڂ�(0�`)
	 * @return	�z��l�̂����Ak�Ԗڂɏ�������
	 */
	public static int kthSmallest(int[] arr, int k) {
		return kthSmallest(arr, 0, arr.length, k);
	}
	
	/**
	 * �w�肳�ꂽ�z��l�̂����Ak �Ԗڂɏ������������߂܂��B
	 * �v�Z�ʂ͕��ϓI�� O(n), �ň� O(n^2) �ł��B
	 *
	 * @param	arr		�z��l
	 * @param	l		�z��̊J�n�C���f�b�N�X(�܂�)
	 * @param	r		�z��̏I���C���f�b�N�X(�܂܂Ȃ�)
	 * @aram	k		���Ԗڂ�(0�`)
	 * @return	�z��l�̂����Ak�Ԗڂɏ�������
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
	 * �w�肳�ꂽ�z��l�̂����Ak �Ԗڂɑ傫���������߂܂��B
	 * �v�Z�ʂ͕��ϓI�� O(n), �ň� O(n^2) �ł��B
	 *
	 * @param	arr		�z��l
	 * @aram	k		���Ԗڂ�(0�`)
	 * @return	�z��l�̂����Ak�Ԗڂɑ傫����
	 */
	public static int kthLargest(int[] arr, int k) {
		return kthSmallest(arr, 0, arr.length, arr.length - k - 1);
	}
	
	/**
	 * �w�肳�ꂽ�z��l�̂����Ak �Ԗڂɑ傫���������߂܂��B
	 * �v�Z�ʂ͕��ϓI�� O(n), �ň� O(n^2) �ł��B
	 *
	 * @param	arr		�z��l
	 * @param	l		�z��̊J�n�C���f�b�N�X(�܂�)
	 * @param	r		�z��̏I���C���f�b�N�X(�܂܂Ȃ�)
	 * @aram	k		���Ԗڂ�(0�`)
	 * @return	�z��l�̂����Ak�Ԗڂɑ傫����
	 */
	public static int kthLargest(int[] arr, int l, int r, int k) {
		return kthSmallest(arr, l, r, r-l-k-1);
	}
	
	public static void main(String[] args) {
		int[] v = new int[] { 4, 6, 9, 10, 1, 2, 5, 8, 7, 3 };
		
		for (int k = 0; k < 10; k++) {
			System.out.printf("%02d�Ԗڂɏ�������:%02d\n", k, kthSmallest(v, k));
			System.out.printf("%02d�Ԗڂɑ傫����:%02d\n", k, kthLargest(v, k));
		}
		
		System.out.println(kthSmallest(v, 1, 8, 4));
		System.out.println(kthLargest(v, 1, 8, 3));
		
	}
	
}
