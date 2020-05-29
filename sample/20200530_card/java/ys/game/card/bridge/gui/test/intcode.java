public class intcode {
	public static void main(String[] args) {
		System.out.println(Integer.toString(caluculateIntCode(Integer.parseInt(args[0]), args[1]), 16));
	}
	private static int caluculateIntCode(int ts, String str) {
		int result = 0;
		result = ts + 1297321;
		for (int i = 0; i < str.length(); i++) {
			int c = (int)(str.charAt(i));
			result = result * 11157 * c + c;
		}
		result = result * (ts + 12497321);
		
		return result;
	}
	
}
