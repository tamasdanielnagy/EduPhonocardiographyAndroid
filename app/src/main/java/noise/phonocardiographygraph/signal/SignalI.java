package noise.phonocardiographygraph.signal;

import java.util.ArrayList;


/**
 * Represents a list signal with integer values.
 * 
 * @author Nagy Tamas
 *
 */
public class SignalI extends ArrayListYSignal<Integer> {

    /**
     * Constructor, with color.
     * @param color
     */
    public SignalI(int color) {
	startTime = 0.0f;
	values = new ArrayList<>();
	this.color = color;
    }

    /**
     * Default Constructor.
     * 
     */
    public SignalI() {
	startTime = 0.0f;
	values = new ArrayList<>();
    }

    /**
     * Constructor, with  sampling time and color.
     * @param dt
     * @param color
     */
    public SignalI(float dt, int color) {
	startTime = 0.0f;
	this.dt = dt;
	values = new ArrayList<>();
	this.color = color;
    }

    /**
     * Constructor, with samplint time.
     * @param dt
     */
    public SignalI(float dt) {
	startTime = 0.0f;
	this.dt = dt;
	values = new ArrayList<>();
    }

    /**
     * Constructor, from byte array.
     * @param dt
     * @param bytes
     * @param numBytesPerInt
     */
    public SignalI(float dt, byte[] bytes, int numBytesPerInt) {
	startTime = 0.0f;
	this.dt = dt;
	values = new ArrayList<>();
	byte[] temp = new byte[numBytesPerInt];
	for (int i = 0; i < bytes.length / numBytesPerInt; i++) {
	    for (int j = 0; j < numBytesPerInt; j++) {
		temp[j] = bytes[(i * numBytesPerInt) + j];
	    }
	    values.add(ByteArray.byteArrayToInt(temp));
	}
    }

    /* (non-Javadoc)
     * @see plethysmography.signal.ArrayListYSignal#modulate(plethysmography.signal.SignalF)
     */
    @Override
    public synchronized void modulate(SignalF modulator) {
	for (int i = 0; i < values.size(); i++) {
	    set(i, (int) Math.round(values.get(i) * modulator.get(i % modulator.size())));
	}
    }

    /* (non-Javadoc)
     * @see plethysmography.signal.Signal#max()
     */
    @Override
    public synchronized Integer max() {
	int max = Integer.MIN_VALUE;
	for (int y : this) {
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
    public synchronized Integer min() {
	int min = Integer.MAX_VALUE;
	for (int y : this) {
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
    public synchronized void minus(Integer i) {
	for (int j = 0; j < values.size(); j++) {
	    set(j, get(j) - i);
	}
    }

    /* (non-Javadoc)
     * @see plethysmography.signal.YSignal#plus(java.lang.Number)
     */
    @Override
    public synchronized void plus(Integer i) {
	for (int j = 0; j < values.size(); j++) {
	    set(j, get(j) + i);
	}
    }

    /* (non-Javadoc)
     * @see plethysmography.signal.ArrayListYSignal#toArray()
     */
    @Override
    public synchronized Integer[] toArray() {
	Integer[] arr = new Integer[values.size()];
	for (int i = 0; i < values.size(); i++) {
	    arr[i] = values.get(i);
	}
	return arr;
    }
    
  

    /* (non-Javadoc)
     * @see plethysmography.signal.YSignal#concatByteArrayToSignal(float, byte[], int)
     */
    @Override
    public synchronized void concatByteArrayToSignal(float dt, byte[] bytes, int numBytesPerInt) {
	this.dt = dt;
	byte[] temp = new byte[numBytesPerInt];
	for (int i = 0; i < bytes.length / numBytesPerInt; i++) {
	    for (int j = 0; j < numBytesPerInt; j++) {
		temp[j] = bytes[(i * numBytesPerInt) + j];
	    }
	    add(ByteArray.byteArrayToInt(temp));
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
		derivative.add((float) (values.get(i + 1) - values.get(i)) / (float) dt);
	    }
	}
    }
}
