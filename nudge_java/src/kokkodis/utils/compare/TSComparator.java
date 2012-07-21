package kokkodis.utils.compare;

import java.util.Comparator;

import kokkodis.holders.ProbHolder;

public class TSComparator implements Comparator<ProbHolder> {

	@Override
	public int compare(ProbHolder o1, ProbHolder o2) {
		if (o1.getProb() == o2.getProb())
			return o1.getActualabel().compareTo(o2.getActualabel());
		else
			return -o1.getProb().compareTo(o2.getProb());
	}

}
