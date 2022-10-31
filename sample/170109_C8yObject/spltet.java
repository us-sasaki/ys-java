public class spltet {
	public static void main(String[] args) {
		System.out.println( Integer.toString((int)'|', 16) );
		String[] s = "|self|URL|1|Link to this resource.||".replace("||","| |").split("\\x7c");
		
		for (int i = 0; i < s.length; i++) {
			System.out.println(String.valueOf(i) + ":" + s[i]);
		}
	}
}
