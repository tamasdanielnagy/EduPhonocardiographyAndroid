package noise.phonocardiographygraph.signal;

import java.util.LinkedList;

/**
 * Implements the moving average digital filter.
 * 
 * @author Nagy Tamas
 *
 */
public class MovingAverage {

	/**
	 * The number of elements.
	 */
	private int N;
	
	private LinkedList<Double> values;
	
	private double sum;
	
	/**
	 * 
	 * @param N
	 */
	public MovingAverage(int N) {
		this.N = N;
		values = new LinkedList<>();
		sum = 0.0;
	}
	
	/**
	 * Filters the next sample.
	 * 
	 * @param y the new sample.
	 * @return the filtered sample.
	 */
	public double makeNext(double y) {
		if (values.size() < N) {
			values.add(y);
			sum += y;
			return sum / values.size();
		} else {
			sum -= values.getFirst();
			values.removeFirst();
			values.add(y);
			sum += y;
			return sum / N;
		}
	}
	
	/**
	 * Clears the filter.
	 */
	public void clear() {
		values.clear();
		sum = 0.0;
	}
	
	
}
