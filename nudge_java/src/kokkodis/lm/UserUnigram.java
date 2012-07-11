package kokkodis.lm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import kokkodis.utils.Counter;

import nlp.langmodel.LanguageModel;

public class UserUnigram implements LanguageModel {

	public static Counter<String> wordCounter;
	public static Counter<String> currentCounter;
	private static HashSet<String> stopwords;
	private static String UNK = "<UNK>";

	public ArrayList<String> preProcess(String s, ArrayList<String> globalList) {
		if (s != null) {
			TokenizerFactory tf = new IndoEuropeanTokenizerFactory();
			LowerCaseTokenizerFactory lct = new LowerCaseTokenizerFactory(tf);

			StopTokenizerFactory stopTokenizer = new StopTokenizerFactory(lct,
					stopwords);

			RegExFilteredTokenizerFactory regexTokenizer = new RegExFilteredTokenizerFactory(
					stopTokenizer, Pattern.compile("[a-z]{2,}"));
			PorterStemmerTokenizerFactory stemmer = new PorterStemmerTokenizerFactory(
					regexTokenizer);

			Tokenizer tokenizer = stemmer.tokenizer(s.toCharArray(), 0,
					s.length());

			List<String> tokenList = new ArrayList<String>();
			List<String> whiteList = new ArrayList<String>();
			tokenizer.tokenize(tokenList, whiteList);

			String[] tokens = tokenList.<String> toArray(new String[tokenList
					.size()]);
			ArrayList<String> words = new ArrayList<String>();

			for (String str : tokens) {
				words.add(str);
				if (globalList != null)
					globalList.add(str);
			}
			return words;
		}
		return null;
	}

	public void generateUnigramLM(ArrayList<String> words) {

		for (String curGram : words) {
			wordCounter.incrementCount(curGram, 1.0);
		}

		currentCounter = new Counter<String>();
		/** Add one smoothing */
		for (Entry<String, Double> e : wordCounter.getEntrySet()) {
			currentCounter.setCount(e.getKey(), e.getValue() + 1);
		//	System.out.println(e.getValue() + 1);
		}
		currentCounter.incrementCount(UNK, 1);
		currentCounter.normalize();
		//System.out.println("counter size:"+wordCounter.size());

	}

	public UserUnigram() {
		wordCounter = new Counter<String>();

		stopwords = readStopwords();
	}

	@Override
	public double getSentenceProbability(List<String> sentence) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String> generateSentence() {
		// TODO Auto-generated method stub
		return null;
	}

	private HashSet<String> readStopwords() {
		HashSet<String> h = new HashSet<String>();
		try {
			BufferedReader input = new BufferedReader(
					new FileReader(
							"/Users/mkokkodi/Documents/workspace/nudge_java/lexicon/stopwords"));
			String line;

			while ((line = input.readLine()) != null) {
				if (line.length() > 0)
					h.add(line.trim());
			}
		} catch (IOException e) {
		}
		return h;
	}

	public double computeLogProb(ArrayList<String> words) {
		// TODO Auto-generated method stub
		double likelihood = 1;
		for (String word : words) {
			if (currentCounter.containsKey(word))
				likelihood += Math.log(currentCounter.getCount(word));
			else
				likelihood += Math.log(currentCounter.getCount(UNK));
		}
		return -likelihood;
	}
}
