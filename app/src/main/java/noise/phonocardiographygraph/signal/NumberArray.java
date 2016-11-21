package noise.phonocardiographygraph.signal;

/**
 * Functions for number arrays.
 * 
 * @author Nagy Tam�s
 *
 */
public abstract class NumberArray {

	
	public static int intArrayMax(int[] iarr) {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < iarr.length; i++)
			if (iarr[i] > max)
				max = iarr[i];
		return max;
	}
}
