package noise.phonocardiographygraph.signal;

import java.util.ArrayList;

/**
 * Represents a list signal vith float values.
 * 
 * @author Nagy Tamas
 * 
 */
public class SignalF extends ArrayListYSignal<Float> {

	/**
	 * Constructor, with color.
	 * @param color
	 */
	public SignalF(int color) {
		startTime = 0.0f;
		values = new ArrayList<>();
		this.color = color;
	}

	/**
     * Default constructor.
     */
	public SignalF() {
		startTime = 0.0f;
		values = new ArrayList<>();
	}

	/**
	 * Constructor, with sampling time and color.
	 * @param dt
	 * @param color
	 */
	public SignalF(float dt, int color) {
		startTime = 0.0f;
		this.dt = dt;
		values = new ArrayList<>();
		this.color = color;
	}

	/**
	 * Constructor, with sampling time.
	 * @param dt
	 */
	public SignalF(float dt) {
		startTime = 0.0f;
		this.dt = dt;
		values = new ArrayList<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * plethysmography.signal.ArrayListYSignal#modulate(plethysmography.signal
	 * .SignalF)
	 */
	@Override
	public synchronized void modulate(SignalF modulator) {
		for (int i = 0; i < values.size(); i++) {
			set(i, (values.get(i) * modulator.get(i % modulator.size())));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.Signal#max()
	 */
	@Override
	public synchronized Float max() {
		float max = Float.NEGATIVE_INFINITY;
		for (float y : this) {
			if (y > max) {
				max = y;
			}
		}
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.Signal#min()
	 */
	@Override
	public synchronized Float min() {
		float min = Float.POSITIVE_INFINITY;
		for (float y : this) {
			if (y < min) {
				min = y;
			}
		}
		return min;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.YSignal#minus(java.lang.Number)
	 */
	@Override
	public synchronized void minus(Float i) {
		for (int j = 0; j < values.size(); j++) {
			set(j, get(j) - i);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.YSignal#plus(java.lang.Number)
	 */
	@Override
	public synchronized void plus(Float i) {
		for (int j = 0; j < values.size(); j++) {
			set(j, get(j) + i);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.ArrayListYSignal#toArray()
	 */
	@Override
	public synchronized Float[] toArray() {
		Float[] arr = new Float[values.size()];
		for (int i = 0; i < values.size(); i++) {
			arr[i] = values.get(i);
		}
		return arr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.YSignal#concatByteArrayToSignal(float,
	 * byte[], int)
	 */
	@Override
	public synchronized void concatByteArrayToSignal(float dt, byte[] bytes, int numBytesPerInt) {
		this.dt = dt;
		byte[] temp = new byte[numBytesPerInt];
		for (int i = 0; i < bytes.length / numBytesPerInt; i++) {
			for (int j = 0; j < numBytesPerInt; j++)
				temp[j] = bytes[(i * numBytesPerInt) + j];
			add((float) ByteArray.byteArrayToInt(temp));
		}
	}

	/**
	 * Calculates the numeric difference of the signal into the param signal.
	 * 
	 * @param derivative the signal where the derivative will be stored.
	 */
	public synchronized void derivative(SignalF derivative) {
		synchronized (derivative) {
			derivative.clear();
			derivative.setDt(dt);
			for (int i = 0; i < values.size() - 1; i++) {
				derivative.add((values.get(i + 1) - values.get(i)) / (float) dt);
			}
		}
	}

}
