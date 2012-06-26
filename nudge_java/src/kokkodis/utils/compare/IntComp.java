package kokkodis.utils.compare;

import java.util.Comparator;

public class IntComp implements Comparator<Integer> {

	@Override
	public int compare(Integer o1, Integer o2) {
		// TODO Auto-generated method stub
		return -o1.compareTo(o2);
	}

}
