package noise.phonocardiographygraph.signal;

import java.util.ArrayList;


/**
 * Represents a list signal with short values.
 *
 * @author Nagy Tamas
 */
public class SignalS extends ArrayListYSignal<Short> {


    
    /**
     * Constructor, with color.
     * @param color
     */
    public SignalS(int color) {
        startTime = 0.0f;
        values = new ArrayList<>();
        this.color = color;
    }

    /**
     * Default constructor.
     */
    public SignalS() {
        startTime = 0.0f;
        values = new ArrayList<>();
    }

    /**
     * Constructor, with sampling time and color.
     * @param dt
     * @param color
     */
    public SignalS(float dt, int color) {
        startTime = 0.0f;
        this.dt = dt;
        values = new ArrayList<>();
        this.color = color;
    }

    /**
     * Constructor, with sampling time.
     * @param dt
     */
    public SignalS(float dt) {
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
    public SignalS(float dt, byte[] bytes, int numBytesPerInt) {
        this.dt = dt;
        startTime = 0.0f;
        values = new ArrayList<>();
        byte[] temp = new byte[numBytesPerInt];
        for (int i = 0; i < bytes.length / numBytesPerInt; i++) {
            for (int j = 0; j < numBytesPerInt; j++) {
                temp[j] = bytes[(i * numBytesPerInt) + j];
            }
            add(ByteArray.byteArrayToShort(temp));
        }
    }
    
    /**
	 * Calculates the numeric difference of the signal into the param signal.
	 * 
	 * @param derivative the signal where the derivative will be stored.
	 */
    public synchronized void derivative(SignalS derivative) {
	synchronized (derivative) {
	    int startIndex;
	    float derLastX;
	    if (derivative.isEmpty()) {
		startIndex = 0;
		derivative.setDt(dt);
		derivative.setStartTime(startTime + (dt / 2.0f));
	    } else {
		while (derivative.getStartTime() < startTime)
		    derivative.remove(0);
		derLastX = (float) derivative.getX(derivative.size() - 1);
		startIndex = (int) Math.round(((derLastX + (dt / 2.0)) - startTime) / dt);
	    }
	    if (!isEmpty()) {
		short y1;
		short y2 = values.get(startIndex);
		for (int i = startIndex + 1; i < values.size(); i++) {
		    y1 = y2;
		    y2 = values.get(i);
		    derivative.add((short) Math.round((y2 - y1) / dt));
		}
	    }
	}
    }

    /* (non-Javadoc)
     * @see plethysmography.signal.ArrayListYSignal#modulate(plethysmography.signal.SignalF)
     */
    @Override
    public synchronized void modulate(SignalF modulator) {
        for (int i = 0; i < values.size(); i++) {
            set(i, (short) Math.round(values.get(i) * modulator.get(i % modulator.size())));
        }
    }



    /* (non-Javadoc)
     * @see plethysmography.signal.ArrayListYSignal#mean()
     */
    @Override
    public synchronized float mean() {
        int i = 0;
        float mean = 0.0f;
        for (float y : this) {
            mean += y;
            i++;
        }
        return mean / ((float) i);
    }

    /* (non-Javadoc)
     * @see plethysmography.signal.Signal#max()
     */
    @Override
    public synchronized Short max() {
        short max = Short.MIN_VALUE;
        for (short y : this) {
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
    public synchronized Short min() {
        short min = Short.MAX_VALUE;
        for (short y : this) {
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
    public synchronized void minus(Short i) {
        for (int j = 0; j < values.size(); j++) {
            set(j, (short) (get(j) - i));
        }
    }

    /* (non-Javadoc)
     * @see plethysmography.signal.YSignal#plus(java.lang.Number)
     */
    @Override
    public synchronized void plus(Short i) {
        for (int j = 0; j < values.size(); j++) {
            set(j, (short) (get(j) + i));
        }
    }

    /* (non-Javadoc)
     * @see plethysmography.signal.ArrayListYSignal#toArray()
     */
    @Override
    public synchronized Short[] toArray() {
		Short[] arr = new Short[values.size()];
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
		for (int i = 0; i < bytes.length/numBytesPerInt; i++) {
			for (int j = 0; j < numBytesPerInt; j++)
				temp[j] = bytes[(i * numBytesPerInt) + j];
			add(ByteArray.byteArrayToShort(temp));
		}
    }

}
