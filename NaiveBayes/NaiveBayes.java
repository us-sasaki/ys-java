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
 * ベイズ推定
 * kuromoji で日本語の単語分解を行う。
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
	 * ファイルから読み込み
	 */
    public static NaiveBayes load(String filePath) throws IOException, ClassNotFoundException{
        try(ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(filePath)))){
            return (NaiveBayes) ois.readObject();
        }
    }

	/**
	 * ファイル書き出し
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
	 * 例文と答えの組を与え、学習させる。
	 * 答えは、"spam"など。例文は、答えに該当する文の例。
	 * なお、文章は名詞成分のみを抽出し、判定する。
	 *
	 * @param	doc			例となる文章
	 * @param	category	どのカテゴリに属するか(答え)
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
	 * 与えられた文章がどのカテゴリに属するか推定する。
	 * もっとも確率の高いカテゴリが返却される。
	 *
	 * @param	doc		カテゴリを推定したい文章
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
            // とりあえず名詞だけとってやってみる
            String f = token.getAllFeatures();
			if ( f.startsWith("名詞")) {
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
        nb.train("Python（パイソン）は，オランダ人のグイド・ヴァンロッサムが作ったオープンソースのプログラミング言語。オブジェクト指向スクリプト言語の一種であり，Perlとともに欧米で広く普及している。イギリスのテレビ局 BBC が製作したコメディ番組『空飛ぶモンティパイソン』にちなんで名付けられた。Python は英語で爬虫類のニシキヘビの意味で，Python言語のマスコットやアイコンとして使われることがある。Pythonは汎用の高水準言語である。プログラマの生産性とコードの信頼性を重視して設計されており，核となるシンタックスおよびセマンティクスは必要最小限に抑えられている反面，利便性の高い大規模な標準ライブラリを備えている。Unicode による文字列操作をサポートしており，日本語処理も標準で可能である。多くのプラットフォームをサポートしており（動作するプラットフォーム），また，豊富なドキュメント，豊富なライブラリがあることから，産業界でも利用が増えつつある。","Python");
        nb.train("ヘビ（蛇）は、爬虫綱有鱗目ヘビ亜目（Serpentes）に分類される爬虫類の総称。体が細長く、四肢がないのが特徴。ただし、同様の形の動物は他群にも存在する。", "Snake");
        nb.train("Ruby（ルビー）は，まつもとゆきひろ（通称Matz）により開発されたオブジェクト指向スクリプト言語であり，従来 Perlなどのスクリプト言語が用いられてきた領域でのオブジェクト指向プログラミングを実現する。Rubyは当初1993年2月24日に生まれ， 1995年12月にfj上で発表された。名称のRubyは，プログラミング言語Perlが6月の誕生石であるPearl（真珠）と同じ発音をすることから，まつもとの同僚の誕生石（7月）のルビーを取って名付けられた。","Ruby");
        nb.train("ルビー（英: Ruby、紅玉）は、コランダム（鋼玉）の変種である。赤色が特徴的な宝石である。天然ルビーは産地がアジアに偏っていて欧米では採れないうえに、産地においても宝石にできる美しい石が採れる場所は極めて限定されており、3カラットを超える大きな石は産出量も少ない。", "Gem");

        System.out.println(nb.classify("グイド・ヴァンロッサムが作ったオープンソース"));
        System.out.println(nb.classify("プログラミング言語のRubyは純粋なオブジェクト指向言語です."));
        System.out.println(nb.classify("コランダム"));
    }
}
