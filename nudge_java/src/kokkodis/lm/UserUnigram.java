package kokkodis.lm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DebugGraphics;

import kokkodis.utils.Counter;


import nlp.langmodel.LanguageModel;

public class UserUnigram implements LanguageModel {

	static final String STOP = "</S>";
	static final String START = "<S>";
	static final String UNK = "<UNK>";
	static final String UNKBI = "<UNKBI>";

	Counter<String> wordCounter = new Counter<String>();
	//HashMap<String, Counter<String>> bigramCounter = new HashMap<String, Counter<String>>();

	//HashMap<String, TreeSet<DummyWord>> wordGivenPreviousMap = new HashMap<String, TreeSet<DummyWord>>();
	//HashMap<String, Double> bigramFreqs = new HashMap<String, Double>();
	TreeMap<Double, Double> freqOfFreq_c = new TreeMap<Double, Double>();
	TreeMap<Double, Double> smoothed_c = new TreeMap<Double, Double>();
	public int unknownCounter = 0;
	//public int unknownBiCounter = 0;
	//public double[] interpolationCoeffs = new double[2];
	public static double numberOfSentences = 0;

	private double Ntotal = 0;
	TreeMap<Double, Double> smoothedFreqOfBigrams = new TreeMap<Double, Double>();
	HashMap<String, Double> starBigramProbs = new HashMap<String, Double>();

	private static double allPossibleBigrams = 0;
	private static double p_gt = 0;
	TreeMap<Double, Double> freqOfBigramCounts;
	TreeMap<String, Double> betas = new TreeMap<String, Double>();
	TreeMap<String, Double> alphas = new TreeMap<String, Double>();
	private double k;

	public double getK() {
		return k;
	}

	public void setK(double k) {
		this.k = k;
	}

	public static double getNumberOfSentences() {
		return numberOfSentences;
	}

	public static void setNumberOfSentences(double numberOfSentences) {
		UserUnigram.numberOfSentences = numberOfSentences;
	}

	public int getUnknownCounter() {
		return unknownCounter;
	}

	public Counter<String> getWordCounter() {
		return wordCounter;
	}

	public void setWordCounter(Counter<String> wordCounter) {
		this.wordCounter = wordCounter;
	}

	public void setUnknownCounter(int unknownCounter) {
		this.unknownCounter = unknownCounter;
	}

	public TreeMap<Double, Double> getSmoothedFreqOfBigrams() {
		return smoothedFreqOfBigrams;
	}

	public void setSmoothedFreqOfBigrams(
			TreeMap<Double, Double> smoothedFreqOfBigrams) {
		this.smoothedFreqOfBigrams = smoothedFreqOfBigrams;
	}

	public static double getP_gt() {
		return p_gt;
	}

	public static void setP_gt(double p_gt) {
		UserUnigram.p_gt = p_gt;
	}

	public HashMap<String, Counter<String>> getBigramCounter() {
		return bigramCounter;
	}

	double total = 0.0;

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public UserUnigram(Collection<List<String>> sentenceCollection) {
		numberOfSentences = sentenceCollection.size();
		for (List<String> sentence : sentenceCollection) {
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, START);
			stoppedSentence.add(STOP);

			ListIterator<String> cur = stoppedSentence.listIterator();

			// wordCounter.incrementCount
			cur.next();

			// wordCounter.incrementCount(START, 1.0);
			while (cur.hasNext()) {
				Ntotal++;
				String prevGram = cur.previous();
				cur.next();
				String curGram = cur.next();

				// if(!wordCounter.containsKey(curGram))
				// wordCounter.incrementCount(UNK, 1);

				wordCounter.incrementCount(curGram, 1.0);

				Counter<String> tmp = bigramCounter.get(curGram);

				if (tmp == null) {
					tmp = new Counter<String>();

					tmp.incrementCount(prevGram, 1.0);

					bigramCounter.put(curGram, tmp);

					// Counter<String> tmp2 = bigramCounter.get(UNK);

					/*
					 * if (tmp2 == null) {
					 * 
					 * tmp2 = new Counter<String>(); bigramCounter.put(UNK,
					 * tmp2);
					 * 
					 * } /* if(!tmp2.containsKey(prevGram)){
					 * wordCounter.incrementCount(UNKBI, 1.0);
					 * tmp2.incrementCount(UNKBI, 1.0); }
					 */

					// tmp2.incrementCount(prevGram, 1.0);

					wordCounter.incrementCount(UNK, 1.0);

				} else {

					tmp.incrementCount(prevGram, 1.0);
					bigramCounter.put(curGram, tmp);
				}

			}
			// wordCounter.incrementCount(STOP, 1.0);
		}

		unknownCounter = (int) wordCounter.getCount(UNK);
		// unknownBiCounter = (int) bigramCounter.get(UNK).getCount(UNKBI);
		total = wordCounter.totalCount();
		// System.out.println("UNK:"+unknownCounter+" Total:"+total);
		createWordGivenPreviousMap();
		checkWordCount(wordCounter);
		checkProbs();
	

		/**
		 * Katz
		 */
		
		/*
		 * for(Entry<Double,Double> e:smoothedFreqOfBigrams.entrySet()){
		 * System.out.println(e.getKey()+ ","+e.getValue()); }
		 * //TreeSet<DummyWord> ts = wordGivenPreviousMap.get("the");
		 * //for(DummyWord dw: ts){
		 * //System.out.println(dw.getWord()+" "+dw.getCount()); //}
		 * 
		 * /* for (List<String> sentence : sentenceCollection) { List<String>
		 * stoppedSentence = new ArrayList<String>(sentence);
		 * getBigramProbability(stoppedSentence, 1); }
		 */
		// printMap();
	}

/*	private void computeAlphas() {
		HashMap<String, Double> pstar_w_n = new HashMap<String, Double>();
		Set<Entry<String, Double>> set = starBigramProbs.entrySet();
		Iterator<Entry<String, Double>> setIt = set.iterator();
		while (setIt.hasNext()) {
			Entry<String, Double> me = setIt.next();
			String[] tmpAr = me.getKey().split("_");
			double prob = wordCounter.getCount(tmpAr[0]) / total;
			Double tmp = pstar_w_n.get(tmpAr[0]);
			if (tmp == null) {
				pstar_w_n.put(tmpAr[0], prob);
				if (prob > 1)
					System.out.println("Problem!!!");

			}
		}

		set = starBigramProbs.entrySet();
		setIt = set.iterator();
		while (setIt.hasNext()) {
			Entry<String, Double> me = setIt.next();
			String[] tmpAr = me.getKey().split("_");
			double a = betas.get(tmpAr[1]) / (1 - pstar_w_n.get(tmpAr[0]));
			alphas.put(tmpAr[1], a);

		}

	}
*/
	public void computeAlphas() {
		Set<Entry<String, Double>> set = starBigramProbs.entrySet();
		Iterator<Entry<String, Double>> setIt = set.iterator();
		while (setIt.hasNext()) {
			Entry<String, Double> me = setIt.next();
			String[] tmpAr = me.getKey().split("_");
			Double tmp = alphas.get(tmpAr[1]);
			if (tmp == null) {
				alphas.put(tmpAr[1], me.getValue());
			} else
				alphas.put(tmpAr[1], me.getValue() + tmp);
		

		}
		for (Entry<String, Double> e : alphas.entrySet()) {
			e.setValue(1 - e.getValue());
			if (e.getValue() < 0)
				System.out.println(e.getKey() + " " + e.getValue());
		}
	//	alphas.put(UNK, 1-())
	}

	public void computeStarBigramProbs() {
		Set<Entry<String, Counter<String>>> outerSet = bigramCounter.entrySet();
		Iterator<Entry<String, Counter<String>>> outerSetIt = outerSet
				.iterator();
		while (outerSetIt.hasNext()) {
			Entry<String, Counter<String>> outerMe = outerSetIt.next();

			Set<Entry<String, Double>> innerSet = outerMe.getValue()
					.getEntrySet();
			Iterator<Entry<String, Double>> innerSetIt = innerSet.iterator();
			if (!(outerMe.getKey().equals(UNK))) {
				while (innerSetIt.hasNext()) {
					Entry<String, Double> innerMe = innerSetIt.next();
					String key = outerMe.getKey() + "_" + innerMe.getKey();
					double c_n_minus_1 = wordCounter.getCount(innerMe.getKey());
					if (c_n_minus_1 == 0)
						c_n_minus_1 = numberOfSentences; // fpr <S>
					double cstar_n_n_minus_1 = smoothedFreqOfBigrams
							.get(innerMe.getValue());
					double prob = cstar_n_n_minus_1 / c_n_minus_1;
					if (prob > 1)
						System.out.println("prob larger than one.");
					starBigramProbs.put(key, prob);

				}
			}
		}

	}

	public void gtSmooth() {
		freqOfBigramCounts = computeFreqOfBiGrams();
		/*
		 * first estimate probability of unseen bigrams:
		 */
		//System.out.println("NTotal:" + Ntotal);
		allPossibleBigrams = Math.pow(wordCounter.size()-1, 2); //no unk. 
		p_gt = freqOfBigramCounts.get(1.0) / Ntotal;
		//System.out.println("P_GT=" + p_gt);
		//System.out.println("All possible bigrams:" + allPossibleBigrams);
		//System.out.println("C*_0=" + freqOfBigramCounts.get(1.0)
			//	/ (allPossibleBigrams - Ntotal));
		/**
		 * 
		 * change wordCounter size to bigrams counter!!!!!!!!
		 */

		smoothedFreqOfBigrams.put(0.0, freqOfBigramCounts.get(1.0)
				/ (allPossibleBigrams - Ntotal));

		/*
		 * for(Entry<Double,Double> e:freqOfBigramCounts.entrySet()){
		 * System.out.println(e.getKey()+" "+e.getValue()); }
		 */

		for (Entry<Double, Double> e : freqOfBigramCounts.entrySet()) {
			smoothedFreqOfBigrams.put(e.getKey(), e.getKey()); // in order to
																// let c for
																// c>k;
		}
		/**
		 * Nc by Katz, k=5;
		 */

		double k = getK();
		double n_k_1 = freqOfBigramCounts.get(k + 1);
		double n_1 = freqOfBigramCounts.get(1.0);
		for (double c = 1; c <= k; c++) {
			double cstar = 0;
			Double n_c = freqOfBigramCounts.get(c);
			/*
			 * if(n_c==null){ double expo = 5.9432 - (Math.log(c) * 0.5489) ;
			 * n_c = Math.exp(expo); }else if(n_c >= 5) cstar = n_c; else{
			 * Double n_c_1 = freqOfBigramCounts.get(c+1); if(n_c_1 == null){
			 * double expo = 5.9432 - (Math.log(c+1) * 0.5489); n_c_1 =
			 * Math.exp(expo); }
			 */
			Double n_c_1 = freqOfBigramCounts.get(c + 1);
			cstar = ((c + 1) * (n_c_1) / n_c) - (c * (k + 1) * (n_k_1) / n_1)
					/ (1 - (k + 1) * n_k_1 / n_1);
		//	System.out.println(c + "->" + cstar);
			// }

			/**
			 * Nc combined!
			 * 
			 * 
			 * for(double c=1; c<=maxCount; c++){ Double n_c =
			 * freqOfBigramCounts.get(c); if(n_c == null){
			 * 
			 * System.out.println("Empty c:"+c); double expo = Math.log(c) * (
			 * -0.5489) + 5.9432; n_c = Math.exp(expo);
			 * System.out.println("New n_c:"+n_c); } Double n_c_1 =
			 * freqOfBigramCounts.get(c+1);
			 * 
			 * if(n_c_1 == null){ double expo = Math.log(c+1) * ( -0.5489) +
			 * 5.9432; n_c_1 = Math.exp(expo); }
			 * 
			 * double cstar = (c+1) * (n_c_1 / n_c); /* if(c>60 && c<100){
			 * System.out.println("n_c_1:"+n_c_1+" n_c:"+n_c);
			 * System.out.println(c+" -> "+cstar); }
			 */
			smoothedFreqOfBigrams.put(c, cstar);

		}

		// regressZeros();

	}

	private void regressZeros() {
		Entry<Double, Double> max = freqOfBigramCounts.lastEntry();
	//	System.out.println("Max:" + max.getKey() + " " + max.getValue());
		double maxCount = max.getKey();
		for (double i = 6; i < maxCount; i++) {
			if (freqOfBigramCounts.get(i) == null) {
				double expo = Math.log(i) * (-0.5489) + 5.9432;
				double cstar = Math.exp(expo);
				freqOfBigramCounts.put(i, cstar);
			}
		}

	}

	private TreeMap<Double, Double> computeFreqOfBiGrams() {

		TreeMap<Double, Double> hm = new TreeMap<Double, Double>();
		Set<Entry<String, Counter<String>>> outerSet = bigramCounter.entrySet();
		Iterator<Entry<String, Counter<String>>> outerSetIt = outerSet
				.iterator();
		while (outerSetIt.hasNext()) {
			Entry<String, Counter<String>> outerMe = outerSetIt.next();

			Set<Entry<String, Double>> innerSet = outerMe.getValue()
					.getEntrySet();
			Iterator<Entry<String, Double>> innerSetIt = innerSet.iterator();
			while (innerSetIt.hasNext()) {
				Entry<String, Double> innerMe = innerSetIt.next();

				Double biGramCounts = innerMe.getValue();
				Double freqOfbigramCounts = hm.get(biGramCounts);
				if (freqOfbigramCounts == null)
					hm.put(biGramCounts, 1.0);
				else
					hm.put(biGramCounts, freqOfbigramCounts + 1.0);

			}

		}
		return hm;
	}

	public void estimateUnknownFromTestSet(
			Collection<List<String>> validationSentenceCollection) {
		for (List<String> sentence : validationSentenceCollection) {
			for (String s : sentence) {
				if (!wordCounter.containsKey(s))
					unknownCounter++;
			}
		}
	//	System.out.println("Unknown:" + unknownCounter);
		total += unknownCounter;
	}

	public void estimateLamdaExhustive(
			Collection<List<String>> validationSentenceCollection) {
		System.out.println("Estimating lambda...");
		double lambda1 = 0.5;
		double lambda2 = 0.5;
		double lambda1new = 1;
		double lambda2new = 1;
		double logLikelihood = 0;
		;
		for (double i = 0.001; i < 1; i += 0.001) {

			lambda1 = i;
			lambda2 = 1 - i;

			double Likelihood = 0.0;
			for (List<String> sentence : validationSentenceCollection) {
				List<String> stoppedSentence = new ArrayList<String>(sentence);
				stoppedSentence.add(0, START);
				stoppedSentence.add(STOP);
				for (int index = 1; index < stoppedSentence.size(); index++) {
					String word_n = stoppedSentence.get(index);
					String word_n_minus_1 = stoppedSentence.get(index - 1);
					double nominator1 = (lambda1 * getProb(word_n));
					double nominator2 = lambda2
							* getProb(word_n, word_n_minus_1);
					double denominator = (nominator1 + nominator2);

					Likelihood += Math.log(denominator);
				}
			}
			if (logLikelihood == 0)
				logLikelihood = Likelihood;
			else if (logLikelihood < Likelihood) {
				logLikelihood = Likelihood;
				lambda1new = lambda1;
				lambda2new = lambda2;
			}
		}
		System.out.println("Nea Lambdas:" + lambda1new + " " + lambda2new);
	//	System.out.println("Log likelihood for previous lamdas:"
		//		+ logLikelihood);

		interpolationCoeffs[0] = lambda1new;

		interpolationCoeffs[1] = lambda2new;

	}

	public void estimateLamda(
			Collection<List<String>> validationSentenceCollection) {
		System.out.println("Estimating lambda...");
		double lambda1 = 0.5;
		double lambda2 = 0.5;
		double beta1 = 0;
		double beta2 = 0;
		double lambda1new = 1;
		double lambda2new = 1;

		while ((Math.abs(lambda1 - lambda1new) > 0.0001)
				|| (Math.abs(lambda2 - lambda2new) > 0.0001)) {
			if (lambda1new != 1) {
				lambda1 = lambda1new;
				lambda2 = lambda2new;
			}
			double Likelihood = 0.0;
			for (List<String> sentence : validationSentenceCollection) {
				List<String> stoppedSentence = new ArrayList<String>(sentence);
				stoppedSentence.add(0, START);
				stoppedSentence.add(STOP);
				for (int index = 1; index < stoppedSentence.size(); index++) {
					String word_n = stoppedSentence.get(index);
					String word_n_minus_1 = stoppedSentence.get(index - 1);
					double nominator1 = (lambda1 * getProb(word_n));
					double nominator2 = lambda2
							* getProb(word_n, word_n_minus_1);
					double denominator = (nominator1 + nominator2);
					// System.out.println(denominator);
					// if(denominator == 0)
					// System.out.println(word_n+" "+word_n_minus_1);
					beta1 += nominator1 / denominator;
					beta2 += nominator2 / denominator;
					Likelihood += Math.log(denominator);
				}
			}

			lambda1new = beta1 / (beta1 + beta2);
			lambda2new = beta2 / (beta1 + beta2);
			//System.out.println("Nea Lambdas:" + lambda1new + " " + lambda2new);
			//System.out.println("Log likelihood for previous lamdas:"
				//	+ Likelihood);
		}

		interpolationCoeffs[0] = lambda1new;

		interpolationCoeffs[1] = lambda2new;
		System.out.println("Nea Lambdas:" + lambda1new + " " + lambda2new);

	}

	private double getProb(String word_n, String word_n_minus_1) {

		Counter<String> tmp = bigramCounter.get(word_n);

		if (tmp == null) {
			// System.out.println("Returning c*:"+smoothedFreqOfBigrams.get(0.0)/total);
			return smoothedFreqOfBigrams.get(0.0) * p_gt;// /total;
			/**
			 * Unknown words:
			 */
			/*
			 * tmp = bigramCounter.get(UNK); double nomCount =
			 * tmp.getCount(word_n_minus_1); double denomCount = unknownCounter;
			 * 
			 * if(nomCount == 0) nomCount = 1/(double)unknownBiCounter; double
			 * prob = (nomCount / denomCount); return prob;
			 */

		}

		double nomCount = tmp.getCount(word_n_minus_1);
		double denomCount = wordCounter.getCount(word_n_minus_1);

		if (nomCount == 0)
			return smoothedFreqOfBigrams.get(0.0) * p_gt;// //total;

		if (word_n_minus_1.equals(START))
			denomCount = numberOfSentences;

		// System.out.println(nomCount);
		double prob = smoothedFreqOfBigrams.get(nomCount) / denomCount;
		// nomCount/denomCount;
		if (prob > 1) {
			// System.out.println("Word n:" + word_n +
			// " word n-1:"+word_n_minus_1+"Prob:"+prob);
			System.out.println(nomCount);
			System.out.println(smoothedFreqOfBigrams.get(nomCount) + " "
					+ denomCount);
		}
		return prob;// (dtotal + 1.0);
	}

	private double getProb(String word_n) {

		double count = wordCounter.getCount(word_n);
		double prob = 0;
		if (count == 0) {
			// prob = (1/unknownCounter)/total;
			prob = // 1/total;
			unknownCounter / total;
		} else
			prob = count / total;

		return prob;
	}

	private void checkWordCount(Counter<String> tmp) {
		double sum = 0;
		Set<Entry<String, Double>> set1 = tmp.getEntrySet();
		Iterator<Entry<String, Double>> setIt1 = set1.iterator();
		while (setIt1.hasNext()) {
			Entry<String, Double> me1 = setIt1.next();
			sum += me1.getValue() / total;

		}
	//	System.out.println("WordCounter sum:" + sum);
	}

	public void checkProbs() {

		Set<Entry<String, Counter<String>>> set = bigramCounter.entrySet();
		Iterator<Entry<String, Counter<String>>> setIt = set.iterator();
		double sum = 0.0;

		while (setIt.hasNext()) {

			Entry<String, Counter<String>> me = setIt.next();

			Counter<String> tmp = me.getValue();
			Set<Entry<String, Double>> set1 = tmp.getEntrySet();
			Iterator<Entry<String, Double>> setIt1 = set1.iterator();
			while (setIt1.hasNext()) {

				Entry<String, Double> me1 = setIt1.next();
				// System.out.println(me1.getValue()+" "+smoothedFreqOfBigrams.get(me1.getValue()));

				if (me1.getKey().equals("<S>"))
					sum += me1.getValue() / numberOfSentences
							* (numberOfSentences / total);
				else
					sum += me1.getValue() / wordCounter.getCount(me1.getKey())
							* (wordCounter.getCount(me1.getKey()) / total);

				// sum+= smoothedFreqOfBigrams.get(me1.getValue())/Ntotal;
			}

		}
		sum += unknownCounter / total;
		//System.out.println("Sum:" + sum);
		// sum+=wordCounter.getCount(START)/total;
		//
		// System.out.println("Adjusted  sum:"+sum);
	}

	private void createWordGivenPreviousMap() {
		Set<Entry<String, Counter<String>>> set = bigramCounter.entrySet();
		Iterator<Entry<String, Counter<String>>> setIt = set.iterator();
		while (setIt.hasNext()) {
			Entry<String, Counter<String>> me = setIt.next();
			String curGram = me.getKey();
			Counter<String> wc = me.getValue();
			Set<Entry<String, Double>> set1 = wc.getEntrySet();
			Iterator<Entry<String, Double>> setIt1 = set1.iterator();
			while (setIt1.hasNext()) {
				Entry<String, Double> me1 = setIt1.next();
				String prevGram = me1.getKey();
				TreeSet<DummyWord> ts = wordGivenPreviousMap.get(prevGram);
				if (ts == null) {
					ts = new TreeSet<DummyWord>(new DummyWordComp());
					wordGivenPreviousMap.put(prevGram, ts);

				}
				DummyWord dw = new DummyWord();
				dw.setWord(curGram);
				dw.setCount(me1.getValue());
				ts.add(dw);

			}

		}
	}

	private void printMap() {
		Set<Entry<String, Counter<String>>> set = bigramCounter.entrySet();
		Iterator<Entry<String, Counter<String>>> setIt = set.iterator();
		while (setIt.hasNext()) {
			Entry<String, Counter<String>> me = setIt.next();
			Counter<String> tmp = me.getValue();
			Set<Entry<String, Double>> set1 = tmp.getEntrySet();
			Iterator<Entry<String, Double>> setIt1 = set1.iterator();
			while (setIt1.hasNext()) {
				Entry<String, Double> me1 = setIt1.next();
				System.out.println(me.getKey() + " " + me1.getKey() + " "
						+ me1.getValue());

			}

		}

	}

	public double getBigramProbabilityWInterpolation(List<String> sentence,
			int index) {
		String word = sentence.get(index);
		String previousWord = sentence.get(index - 1);

		return interpolationCoeffs[0] * getProb(word) + interpolationCoeffs[1]
				* getProb(word, previousWord);

	}

	public double getBigramProbability(List<String> sentence, int index) {

		String word = sentence.get(index);

		String previousWord = sentence.get(index - 1);
		Counter<String> tmp = bigramCounter.get(word);
		if (tmp == null) {
			/**
			 * Unknown words:
			 */
			tmp = bigramCounter.get(UNK);
			double nomCount = tmp.getCount(previousWord);
			double denomCount = wordCounter.getCount(UNK);
			double prob = (nomCount / denomCount);
			// System.out.println(prob);
			if (nomCount == 0.0) {
				// System.out.println("Error error!!!");
				return 1.0 / (total + 1.0);
			}
			return prob;
			// return 1.0/(total+1.0);

		}
		double nomCount = tmp.getCount(previousWord);
		double denomCount = wordCounter.getCount(word);

		if (nomCount == 0.0) {
			// System.out.println("Error error!!!");
			return 1.0 / (total + 1.0);
		}
		double prob = nomCount / denomCount;
		if (prob == 0)
			System.out.println("Zero.2");
		return prob;// (dtotal + 1.0);
	}

	public double getSentenceProbabilityWInter(List<String> sentence) {
		List<String> stoppedSentence = new ArrayList<String>(sentence);
		stoppedSentence.add(0, START);
		stoppedSentence.add(STOP);
		double probability = 1.0;
		for (int index = 1; index < stoppedSentence.size(); index++) {
			probability *= getBigramProbabilityWInterpolation(stoppedSentence,
					index);

		}
	//	System.out.println("Probability- Bigram:"+Math.log(probability));
		return probability;
	}

	public double getSentenceProbability(List<String> sentence) {
		List<String> stoppedSentence = new ArrayList<String>(sentence);
		stoppedSentence.add(0, START);
		stoppedSentence.add(STOP);
		double probability = 1.0;
		for (int index = 1; index < stoppedSentence.size(); index++) {
			probability *= getBigramProbability(stoppedSentence, index);

		}
		// System.out.println("Probability:"+Math.log(probability));
		return probability;
	}

	String generateWord(String previousWord) {

		TreeSet<DummyWord> ts = wordGivenPreviousMap.get(previousWord);
		if (ts == null) {
			System.out.println("NuLLL");
			return "the";
		}
		DummyWord freqWord = ts.last();
		ts.remove(freqWord);
		return freqWord.getWord();

	}

	@Override
	public List<String> generateSentence() {
		List<String> sentence = new ArrayList<String>();
		String word = generateWord(START);
		while (!word.equals(STOP)) {
			sentence.add(word);
			word = generateWord(word);
		}
		return sentence;
	}

	public UserUnigram() {
		super();
		// TODO Auto-generated constructor stub
	}

	public double getSentenceProbabilityKatz(List<String> sentence) {
		List<String> stoppedSentence = new ArrayList<String>(sentence);
		stoppedSentence.add(0, START);
		stoppedSentence.add(STOP);
		double probability = 1.0;
		for (int index = 1; index < stoppedSentence.size(); index++) {
			probability *= getBigramProbabilityKatz(stoppedSentence, index);

		}
		// System.out.println("Probability:"+Math.log(probability));
			if(probability==0)
				System.out.println("zero");
		return probability;
	}

	private double getBigramProbabilityKatz(List<String> sentence, int index) {
		String word = sentence.get(index);
		String previousWord = sentence.get(index - 1);
		Double prob = starBigramProbs.get(word + "_" + previousWord);
		if (prob != null){
			if(prob==0)
				System.out.println("Prob 0");
			return prob;
		}
		Double alpha = alphas.get(previousWord);
		if (alpha == null) {
			return unknownCounter / total;
		} else if(wordCounter.getCount(previousWord)== 0)
			return unknownCounter / total;
		else{
			double nom = wordCounter.getCount(word)/total;
			double denom = 1-alpha;
			double prob2 = alpha * (nom/denom); 
			if(prob2 == 0)
				return unknownCounter / total; //alpha = 0;
			return prob2;
	}
	}

}
