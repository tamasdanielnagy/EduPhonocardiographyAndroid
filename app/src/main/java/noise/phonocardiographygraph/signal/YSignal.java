package noise.phonocardiographygraph.signal;

/**
 * Represents an evenly sampled signal.
 * 
 * @author Nagy Tamas
 */
public abstract class YSignal<N extends Number> extends Signal<N> {

	/**
	 * Sampling time.
	 */
    protected double dt;
    
    /**
     * Strating time.
     */
    protected double startTime;

    /**
     * @return the sampling time.
     */
    public synchronized double getDt() {
	return dt;
    }

    /**
     * @param dt the new samplint time.
     */
    public synchronized void setDt(double dt) {
	this.dt = dt;
    }

    /**
     * @param index the index of the sample.
     * @return the time of the sample of the given index.
     */
    public synchronized double getX(int index) {
	return startTime + index * dt;
    }

    /**
     * @param dt the sampling time of the signal to concat.
     * @param bytes the byte array contains the signal to concat.
     * @param numBytesPerInt number of bytes used to store one number in bytes.
     */
    public abstract void concatByteArrayToSignal(float dt, byte[] bytes,
	    int numBytesPerInt);

    /**
     * Get the given sample of the signal.
     * 
     * @param index the index of the sample.
     * @return the value of the sample at the diven index.
     */
    public abstract N get(int index);

   
    /**
     * @return the start time.
     */
    public synchronized double getStartTime() {
	return startTime;
    }

    /**
     * @param startTime the new start time.
     */
    public synchronized void setStartTime(double startTime) {
	this.startTime = startTime;
    }

    /**
     * Set the value of a sample az a given index.
     * 
     * @param index the index of the sample.
     * @param y the new value.
     */
    public abstract void set(int index, N y);

    /**
     * @return the last sample of the signal.
     */
    public abstract N getLast();

    /**
     * @return the last sample of ths signal.
     */
    public abstract N getFirst();
    
    /**
     * @return the length of the signal is seconds.
     */
    public abstract double getLength();

    /**
     * Decrease all samples by i.
     *
     * @param i
     */
    public abstract void minus(N i);

    /**
     * Increase all samples by i.
     * 
     * @param i
     */
    public abstract void plus(N i);
}
