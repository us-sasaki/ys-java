/**
 * ���[�U���瑗��ꂽ�����A�K�؂ȑ���Ƀu���[�h�L���X�g���܂��B
 * ���̃I�u�W�F�N�g�̓T�[�o���Ŏ��s����܂��B
 * 
 * @author		Yusuke Sasaki
 * @version		making		10, December 2000
 */
public class Multicaster {
	protected Server server;
	
	public Multicaster(Server server) {
		this.server = server;
	}
	
	public void chat(String input) {
		server.broadcast(input);
	}
}
