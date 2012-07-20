package kokkodis.textprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import kokkodis.utils.Counter;
import kokkodis.utils.CounterMap;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.TokenNGramTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class TextPreProcessing {

	public static CounterMap<Integer, String> termFrequencies; // lexicon
	public static Counter<String> docFrequencies;//
	private static HashSet<String> stopwords;

	/**
	 * @param args
	 */

	public TextPreProcessing() {
		// termFrequencies = new CounterMap<Integer, String>();
		docFrequencies = new Counter<String>();
		stopwords = readStopwords();

		// TODO Auto-generated constructor stub
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

	public Counter<String> process(String s, Integer application) {
		Counter<String> c = new Counter<String>();
		if (s != null) {
			String[] tokens = analyzeBigrams(s);

			HashSet<String> tmpSet = new HashSet<String>();
			for (String l : tokens) {
				// Main.L.incrementCount(l, 1);

				// termFrequencies.incrementCount(application, l, 1);
				if (!tmpSet.contains(l)) {
					docFrequencies.incrementCount(l, 1);
					tmpSet.add(l);
				}
			}

			// System.out.println(tokens.length);

		}
		return c;
	}

	private String[] analyzeBigrams(String s) {
		TokenizerFactory tf = new IndoEuropeanTokenizerFactory();
		LowerCaseTokenizerFactory lct = new LowerCaseTokenizerFactory(tf);

		StopTokenizerFactory stopTokenizer = new StopTokenizerFactory(lct,
				stopwords);

		RegExFilteredTokenizerFactory regexTokenizer = new RegExFilteredTokenizerFactory(
				stopTokenizer, Pattern.compile("[a-z]{2,}"));
		PorterStemmerTokenizerFactory stemmer = new PorterStemmerTokenizerFactory(
				regexTokenizer);

		TokenNGramTokenizerFactory trigramTokenizerFactory = new TokenNGramTokenizerFactory(
				stemmer, 1, 2);

		Tokenizer tokenizer = trigramTokenizerFactory.tokenizer(
				s.toCharArray(), 0, s.length());
		List<String> tokenList = new ArrayList<String>();
		List<String> whiteList = new ArrayList<String>();
		tokenizer.tokenize(tokenList, whiteList);

		return tokenList.<String> toArray(new String[tokenList.size()]);

	}

	public void process(Counter<String> c, Counter<String> d, String s) {

		if (s != null) {

			String[] tokens = analyzeBigrams(s);

			HashSet<String> tmpSet = new HashSet<String>();
			for (String l : tokens) {
				// Main.L.incrementCount(l, 1);
				c.incrementCount(l, 1);
				// termFrequencies.incrementCount(application, l, 1);
				if (!tmpSet.contains(l)) {
					// docFrequencies.incrementCount(l, 1);
					d.incrementCount(l, 1);
					tmpSet.add(l);
				}
			}

			// System.out.println(tokens.length);

		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String s = "At the time of the current LingPipe release, "
				+ "we've completed the following chapters, totalling "
				+ "a little over 450 pages printed in a relatively compact"
				+ " programming text format. Note that this includes some extensive "
				+ "introductions to the relevant features of Java, specifically characters, "
				+ "encodings, strings, regular expressions, and I/O. "
				+ "These introductions to text processing in Java go well "
				+ "beyond anything I've seen in other introductions to Java. "
				+ "There is also much more thorough discussion of the underlying "
				+ "mathematical basis of our models, though that has been moved to "
				+ "an appendix for each chapter to make the rest of the chapter less "
				+ "demanding for those without strong algorithm and statistics backgrounds.";

		TextPreProcessing tpp = new TextPreProcessing();
		tpp.process(s, 1);

	}

	public HashSet<String> getVector(String text) {
		String[] tokens = analyzeBigrams(text);
		HashSet<String> h = new HashSet<String>(Arrays.asList(tokens));
		return h;
	}

}
