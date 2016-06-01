import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atilika.kuromoji.*;

/**
 * �x�C�Y����
 * kuromoji �œ��{��̒P�ꕪ�����s���B
 *
 * @date	2016/4/13
 */
public class NaiveBayes implements Serializable{

	private static final long serialVersionUID = -4252904524786950577L;
	
	private Tokenizer tokenizer = Tokenizer.builder().build();
    
    private Set<String> vocabularies;
    private Map<String, Integer> categoryCount;
    private Map<String, Map<String, Integer>> wordCount;

/*-------------
 * constructor
 */
    public NaiveBayes(){
        vocabularies = new HashSet<>();
        categoryCount = new HashMap<>();
        wordCount = new HashMap<>();
    }

/*---------------
 * class methods
 */
	/**
	 * �t�@�C������ǂݍ���
	 */
    public static NaiveBayes load(String filePath) throws IOException, ClassNotFoundException{
        try(ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(filePath)))){
            return (NaiveBayes) ois.readObject();
        }
    }

	/**
	 * �t�@�C�������o��
	 */
    public void save(String filePath) throws IOException{
        try(ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(filePath)))){
            oos.writeObject(this);
        }
    }

/*------------------
 * instance methods
 */
	/**
	 * �ᕶ�Ɠ����̑g��^���A�w�K������B
	 * �����́A"spam"�ȂǁB�ᕶ�́A�����ɊY�����镶�̗�B
	 * �Ȃ��A���͖͂��������݂̂𒊏o���A���肷��B
	 *
	 * @param	doc			��ƂȂ镶��
	 * @param	category	�ǂ̃J�e�S���ɑ����邩(����)
	 */
    public void train(String doc, String category){
        for(String word : toWords(doc)){
            wordCountUp(word, category);
        }
        categoryCountUp(category);
    }

    private void wordCountUp(String word, String category) {
        Map<String, Integer> categoryWord = wordCount.get(category);
        if(categoryWord == null) categoryWord = new HashMap<>();
        if(categoryWord.containsKey(word)) {
            categoryWord.put(word, categoryWord.get(word) + 1);
        }else{
            categoryWord.put(word, 1);
        }
        wordCount.put(category, categoryWord);
        vocabularies.add(word);
    }

    private void categoryCountUp(String category) {
        if(categoryCount.containsKey(category)){
            categoryCount.put(category, categoryCount.get(category) + 1);
        }else{
            categoryCount.put(category, 1);
        }
    }

    private double priorProb(String category){
        return (double)categoryCount.get(category) / sum(categoryCount.values());
    }

    private int numOfAppearance(String word, String category){
        return (wordCount.get(category).containsKey(word)) ? wordCount.get(category).get(word) : 0;
    }

    private double wordProb(String word, String category){
        return ((double)numOfAppearance(word, category) + 1) 
            / (sum(wordCount.get(category).values()) + vocabularies.size());
    }

    private long sum(Collection<Integer> values){
        long ret = 0;
        for(Integer inte : values) ret += inte;
        return ret;
    }
    private double score(String category, List<String> words){
        return score(category, words.toArray(new String[]{}));
    }

    private double score(String category, String... words){
        double score = Math.log(priorProb(category));
        for(String word : words){
            score += Math.log(wordProb(word, category));
        }
        return score;
    }

	/**
	 * �^����ꂽ���͂��ǂ̃J�e�S���ɑ����邩���肷��B
	 * �����Ƃ��m���̍����J�e�S�����ԋp�����B
	 *
	 * @param	doc		�J�e�S���𐄒肵��������
	 */
    public String classify(String doc){
        String bestGuessedCategory = null;
        List<String> words = toWords(doc);
        double maxProbBefore = Double.NEGATIVE_INFINITY;

        for(String category : categoryCount.keySet()){
            double score = score(category, words);
            if(score > maxProbBefore) {
                maxProbBefore = score;
                bestGuessedCategory = category;
            }
        }

        return bestGuessedCategory;
    }

    private List<String> toWords(String doc) {
        List<String> ret = new ArrayList<String>();
		
		List<Token> tokens = tokenizer.tokenize(doc);
		
		for (Token token : tokens) {
            // �Ƃ肠�������������Ƃ��Ă���Ă݂�
            String f = token.getAllFeatures();
			if ( f.startsWith("����")) {
				ret.add(token.getSurfaceForm());
            }
        }
		
        return ret;
    }

/*--------------
 * main(sample)
 */
    public static void main(String[] args) {
        NaiveBayes nb = new NaiveBayes();
        nb.train("Python�i�p�C�\���j�́C�I�����_�l�̃O�C�h�E���@�����b�T����������I�[�v���\�[�X�̃v���O���~���O����B�I�u�W�F�N�g�w���X�N���v�g����̈��ł���CPerl�ƂƂ��ɉ��ĂōL�����y���Ă���B�C�M���X�̃e���r�� BBC �����삵���R���f�B�ԑg�w���ԃ����e�B�p�C�\���x�ɂ��Ȃ�Ŗ��t����ꂽ�BPython �͉p���঒��ނ̃j�V�L�w�r�̈Ӗ��ŁCPython����̃}�X�R�b�g��A�C�R���Ƃ��Ďg���邱�Ƃ�����BPython�͔ėp�̍���������ł���B�v���O���}�̐��Y���ƃR�[�h�̐M�������d�����Đ݌v����Ă���C�j�ƂȂ�V���^�b�N�X����уZ�}���e�B�N�X�͕K�v�ŏ����ɗ}�����Ă��锽�ʁC���֐��̍�����K�͂ȕW�����C�u����������Ă���BUnicode �ɂ�镶���񑀍���T�|�[�g���Ă���C���{�ꏈ�����W���ŉ\�ł���B�����̃v���b�g�t�H�[�����T�|�[�g���Ă���i���삷��v���b�g�t�H�[���j�C�܂��C�L�x�ȃh�L�������g�C�L�x�ȃ��C�u���������邱�Ƃ���C�Y�ƊE�ł����p����������B","Python");
        nb.train("�w�r�i�ցj�́A঒��j�L�ؖڃw�r���ځiSerpentes�j�ɕ��ނ����঒��ނ̑��́B�̂��ג����A�l�����Ȃ��̂������B�������A���l�̌`�̓����͑��Q�ɂ����݂���B", "Snake");
        nb.train("Ruby�i���r�[�j�́C�܂��Ƃ䂫�Ђ�i�ʏ�Matz�j�ɂ��J�����ꂽ�I�u�W�F�N�g�w���X�N���v�g����ł���C�]�� Perl�Ȃǂ̃X�N���v�g���ꂪ�p�����Ă����̈�ł̃I�u�W�F�N�g�w���v���O���~���O����������BRuby�͓���1993�N2��24���ɐ��܂�C 1995�N12����fj��Ŕ��\���ꂽ�B���̂�Ruby�́C�v���O���~���O����Perl��6���̒a���΂ł���Pearl�i�^��j�Ɠ������������邱�Ƃ���C�܂��Ƃ̓����̒a���΁i7���j�̃��r�[������Ė��t����ꂽ�B","Ruby");
        nb.train("���r�[�i�p: Ruby�A�g�ʁj�́A�R�����_���i�|�ʁj�̕ώ�ł���B�ԐF�������I�ȕ�΂ł���B�V�R���r�[�͎Y�n���A�W�A�ɕ΂��Ă��ĉ��Ăł͍̂�Ȃ������ɁA�Y�n�ɂ����Ă���΂ɂł���������΂��̂��ꏊ�͋ɂ߂Č��肳��Ă���A3�J���b�g�𒴂���傫�Ȑ΂͎Y�o�ʂ����Ȃ��B", "Gem");

        System.out.println(nb.classify("�O�C�h�E���@�����b�T����������I�[�v���\�[�X"));
        System.out.println(nb.classify("�v���O���~���O�����Ruby�͏����ȃI�u�W�F�N�g�w������ł�."));
        System.out.println(nb.classify("�R�����_��"));
    }
}
