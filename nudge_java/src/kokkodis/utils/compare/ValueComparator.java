package kokkodis.utils.compare;

import java.util.Comparator;
import java.util.Map;

import kokkodis.utils.Counter;

public class ValueComparator implements Comparator {

	Counter<Object> c;

	public ValueComparator(Counter c) {
		this.c = c;
	}

	@Override
	public int compare(Object a, Object b) {
		if (c.getCount(a) <= c.getCount(b)) {
			return 1;
		} else {
			return -1;
		}
	}

}
