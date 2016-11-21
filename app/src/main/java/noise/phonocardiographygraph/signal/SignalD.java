package noise.phonocardiographygraph.signal;

import java.util.ArrayList;

/**
 * Represents a list signal with double values.
 * 
 * @author Nagy Tamas
 *
 */
public class SignalD extends ArrayListYSignal<Double> {

	/**
	 * Constructor, with color.
	 * @param color
	 */
	public SignalD(int color) {
		startTime = 0.0f;
		values = new ArrayList<>();
		this.color = color;
	}

	/**
	 * Default constructor.
	 */
	public SignalD() {
		startTime = 0.0f;
		values = new ArrayList<>();
	}

	/**
	 * Constructor, with sampling time and color.
	 * @param dt
	 * @param color
	 */
	public SignalD(double dt, int color) {
		startTime = 0.0f;
		this.dt = dt;
		values = new ArrayList<>();
		this.color = color;
	}

	/**
	 * Constructor, with samplin time.
	 * @param dt
	 */
	public SignalD(double dt) {
		startTime = 0.0f;
		this.dt = dt;
		values = new ArrayList<>();
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.ArrayListYSignal#modulate(plethysmography.signal.SignalF)
	 */
	@Override
	public synchronized void modulate(SignalF modulator) {
		for (int i = 0; i < values.size(); i++) {
			set(i, (values.get(i) * modulator.get(i % modulator.size())));
		}
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.Signal#max()
	 */
	@Override
	public synchronized Double max() {
		double max = Float.NEGATIVE_INFINITY;
		for (double y : this) {
			if (y > max) {
				max = y;
			}
		}
		return max;
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.Signal#min()
	 */
	@Override
	public synchronized Double min() {
		double min = Float.POSITIVE_INFINITY;
		for (double y : this) {
			if (y < min) {
				min = y;
			}
		}
		return min;
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#minus(java.lang.Number)
	 */
	@Override
	public synchronized void minus(Double i) {
		for (int j = 0; j < values.size(); j++) {
			set(j, get(j) - i);
		}
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#plus(java.lang.Number)
	 */
	@Override
	public synchronized void plus(Double i) {
		for (int j = 0; j < values.size(); j++) {
			set(j, get(j) + i);
		}
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.ArrayListYSignal#toArray()
	 */
	@Override
	public synchronized Double[] toArray() {
		Double[] arr = new Double[values.size()];
		for (int i = 0; i < values.size(); i++) {
			arr[i] = values.get(i);
		}
		return arr;
	}

	/* (non-Javadoc)
	 * @see plethysmography.signal.YSignal#concatByteArrayToSignal(float, byte[], int)
	 */
	@Override
	public synchronized void concatByteArrayToSignal(float dt, byte[] bytes,
			int numBytesPerInt) {
		this.dt = dt;
		byte[] temp = new byte[numBytesPerInt];
		for (int i = 0; i < bytes.length / numBytesPerInt; i++) {
			for (int j = 0; j < numBytesPerInt; j++)
				temp[j] = bytes[(i * numBytesPerInt) + j];
			add((double) ByteArray.byteArrayToInt(temp));
		}
	}

	/**
	 * Calculates the numeric difference of the signal into the param signal.
	 * 
	 * @param derivative the signal where the derivative will be stored.
	 */
	public synchronized void derivative(SignalD derivative) {
		// if (derivative == null) -> do sg
		synchronized (derivative) {
			derivative.clear();
			derivative.setDt(dt);
			derivative.setStartTime(startTime + (dt / 2.0));
			for (int i = 0; i < values.size() - 1; i++) {
				derivative.add((values.get(i + 1) - values.get(i)) / dt);
			}
		}
	}
	

}
