package noise.phonocardiographygraph.signal;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * Represents an evenly sampled signal, with values in a list.
 * 
 * @author Nagy Tam�s
 */
public abstract class ArrayListYSignal<N extends Number> extends YSignal<N> implements ListSignal, Iterable<N> {

	/**
	 * The samples.
	 */
	protected ArrayList<N> values;

	public synchronized void manageSize(float maxLength) {
		int maxSize = (int) Math.round(maxLength / dt) + 1;
		if (values.size() > maxSize) {
			int u = values.size() - maxSize;
			for (int i = 0; i < u; i++) {
				values.remove(0);
				startTime += dt;
			}
		}
	}

	/**
	 * @param modulator the modulator signal.
	 */
	public abstract void modulate(SignalF modulator);

	@Override
	public synchronized Iterator<N> iterator() {
		return values.iterator();
	}

	/**
	 * Adds a new value to the end of the signal.
	 * 
	 * @param y the new value.
	 * @return is added.
	 */
	public synchronized boolean add(N y) {
		return values.add(y);
	}

	/**
	 * Adds a new value at a given index.
	 * 
	 * @param index
	 * @param y the new value.
	 */
	public synchronized void add(int index, N y) {
		values.add(index, y);
	}

	
	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#get(int)
	 */
	@Override
	public synchronized N get(int index) {
		return values.get(index);
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#set(int, java.lang.Number)
	 */
	@Override
	public synchronized void set(int index, N y) {
		values.set(index, y);
	}

	/**
	 * Removes all elements of the signal.
	 */
	public synchronized void clear() {
		values.clear();
		startTime = 0.0;
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.Signal#isEmpty()
	 */
	@Override
	public synchronized boolean isEmpty() {
		return values.isEmpty();
	}

	/**
	 * Removes the sample at a given index.
	 * 
	 * @param index
	 */
	public synchronized void remove(int index) {
		values.remove(index);
		if (index == 0) {
			startTime += dt;
		}
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.ListSignal#removeAll()
	 */
	@Override
	public synchronized void removeAll() {
		startTime = getX(values.size() - 1) + dt;
		values.removeAll(values);
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.Signal#size()
	 */
	@Override
	public synchronized int size() {
		return values.size();
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#getLast()
	 */
	@Override
	public synchronized N getLast() {
		return values.get(values.size() - 1);
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#getFirst()
	 */
	@Override
	public synchronized N getFirst() {
		return values.get(0);
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.Signal#mean()
	 */
	@Override
	public synchronized float mean() {
		int i = 0;
		float mean = 0.0f;
		for (Number y : this) {
			mean += y.floatValue();
			i++;
		}
		return mean / ((float) i);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public synchronized String toString() {
		String s = "";
		for (N y : this) {
			s += (y + " ");
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#getLength()
	 */
	public synchronized double getLength() {
		return (values.size() - 1) * dt;
	}

	/**
	 * @return the signal in an array.
	 */
	public abstract N[] toArray();
}
