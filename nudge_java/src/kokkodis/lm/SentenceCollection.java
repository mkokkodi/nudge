package kokkodis.lm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


public class SentenceCollection extends AbstractCollection<List<String>> {

	static class SentenceIterator implements Iterator<List<String>> {

		BufferedReader reader;

		public boolean hasNext() {
			try {
				return reader.ready();
			} catch (IOException e) {
				return false;
			}
		}

		public List<String> next() {
			try {
				String line = reader.readLine();
				String[] words = line.split("\\s+");
				List<String> sentence = new ArrayList<String>();
				for (int i = 0; i < words.length; i++) {
					String word = words[i];
					sentence.add(word.toLowerCase());
				}
				return sentence;
			} catch (IOException e) {
				throw new NoSuchElementException();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public SentenceIterator(BufferedReader reader) {
			this.reader = reader;
		}
	}

	String fileName;

	public Iterator<List<String>> iterator() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					fileName));
			return new SentenceIterator(reader);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Problem with SentenceIterator for "
					+ fileName);
		}
	}

	public int size() {
		int size = 0;
		Iterator i = iterator();
		while (i.hasNext()) {
			size++;
			i.next();
		}
		return size;
	}

	public SentenceCollection(String fileName) {
		this.fileName = fileName;
	}

	public static class Reader {
		static Collection<List<String>> readSentenceCollection(
				String fileName) {
			return new SentenceCollection(fileName);
		}
	}
}
