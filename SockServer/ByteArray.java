/**
 * byte[] ‚ÉŠÖ‚·‚é•¶Žš—ñ‰»‚Ì‹@”\‚ð’ñ‹Ÿ‚µ‚Ü‚·
 *
 *
 */
public class ByteArray {
	/**
	 *
	 */
	public static String toString(byte[] target) {
		return toString(target, 0, target.length);
	}
	
	/**
	 *
	 */
	public static String toString(byte[] target, int offset, int length) {
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		
		for (int i = offset; i < offset + length; i++) {
			int n = 0xFF & (int)(target[i]);
			if (n < 16) sb1.append('0');
			sb1.append(Integer.toString(n, 16));
			
			if ( (n >= 32) && (n < 128) ) {
				sb2.append( (char)n );
			} else sb2.append( '.' );
		}
		
		return sb2.toString() + " //" + sb1.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(ByteArray.toString("abcde0012“K“–".getBytes()));
	}
	
}
