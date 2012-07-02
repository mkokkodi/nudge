package kokkodis.logitModel;

import java.util.Comparator;
import java.util.Map.Entry;

public class TMComparator implements Comparator<Double> {

	// @Override
	// public int compare(Entry<Double, Integer> e1, Entry<Double, Integer> e2)
	// {
	// if (e1.getKey() != e2.getKey())
	// return -e1.getKey().compareTo(e2.getKey());
	// else
	// return 1;
	// }

	@Override
	public int compare(Double o1, Double o2) {
		if (o1 != o2)
			return -o1.compareTo(o2);
		return -1;
	}

}
