/**
 * �u���b�W�T�[�o�Ƀ��O�C�����Ă��郆�[�U�X�^�u
 *
 * 
 */
public class UserStub implements Runnable {
	/**
	 * ���̃��[�U�̃n���h���l�[���B�{�V�X�e���ł́A���[�UID�ł�����B
	 */
	protected String		name;
	
	/**
	 * Join ���Ă���e�[�u���I�u�W�F�N�g�BBroadCast�Ȃǂ��s���B
	 */
	protected Table			table;
	
	/**
	 * �����[�g���[�U����̃R�}���h���󂯎����̓X�g���[��
	 */
	protected InputStream	in;
	protected BufferedReader	br;
	
	/**
	 * �����[�g���[�U�ɒʒm����o�̓X�g���[��
	 */
	protected OutputStream	out;
	
	/**
	 * �X�g���[������e��R�}���h���󂯎��A�f�B�X�p�b�`����X���b�h
	 */
	protected Thread		streamObserver;
	
/*-------------
 * Constructor
 */
	public UserStub(InputStream in, OutputStream out, String name) {
		this.in		= in;
		this.out	= out;
		this.name	= name;
		
		br = new BufferedReader(new InputStreamReader(in));
		
		streamObserver = new Thread(this);
		streamObserver.start();
	}
	
/*-----------------------
 * implements (Runnable)
 */
	/**
	 * ���̓X�g���[����������A�e��R�}���h����M�����ۂɓK�؂ȃ��\�b�h���Ăяo���B
	 */
	public void run() {
		while (true) {
			String line = br.readLine();
		}
		
	}
	
/*------------------
 * instance methods
 */
	/**
	 * �e��R�}���h���b�Z�[�W�𑗐M����B
	 * �������̂��ߑ��M�̕ʃX���b�h�����s���ꍇ�ABroadCast���s���I�u�W�F�N�g�Ŏ��{���邱�ƁB
	 */
	public void send(String message) {
	}
	
	