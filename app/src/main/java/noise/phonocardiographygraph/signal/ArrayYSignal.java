/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package noise.phonocardiographygraph.signal;

/**
 * Represents a signal, stored in an array.
 * 
 * @author Nagy Tam�s
 */
public abstract class ArrayYSignal<N extends Number> extends YSignal<N> {

	
	/**
	 * The samples.
	 */
	protected N[] values;

	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#get(int)
	 */
	@Override
	public N get(int index) {
		return values[index];
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#set(int, java.lang.Number)
	 */
	@Override
	public void set(int index, N y) {
		values[index] = y;
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#getLast()
	 */
	@Override
	public N getLast() {
		return values[values.length - 1];
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#getFirst()
	 */
	@Override
	public N getFirst() {
		return values[0];
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#getLength()
	 */
	public synchronized double getLength() {
		return (values.length - 1) * dt;
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.Signal#mean()
	 */
	@Override
	public synchronized float mean() {
		float mean = 0.0f;
		for (int i = 0; i < values.length; i++) {
			mean += values[i].floatValue();
		}
		return mean / values.length;
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.Signal#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		if (values != null)
			return true;
		return false;
	}

}
