package kokkodis.utils.compare;

import java.util.Comparator;

import kokkodis.factory.XYPair;
import kokkodis.utils.Counter;

public class XYPairComparator implements Comparator<XYPair> {

	@Override
	public int compare(XYPair a, XYPair b) {
		if (a.getX() > b.getX()) {
			return 1;
		} else if (a.getX() == b.getX()) {
			if (a.getY() > b.getY()) {
				return 1;
			} else
				return -1;

		} else
			return -1;
	}

}
