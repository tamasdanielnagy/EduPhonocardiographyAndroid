package noise.phonocardiographygraph.signal;

/**
 * Represents a signal with Short values.
 * 
 * @author Nagy Tamas
 * 
 */
public class ArraySignalS extends ArrayYSignal<Short> {

	/**
	 * The pre-calculated sine values in amplitude demodulation.
	 */
	private static float[] sinus;

	/**
	 * The pre-calculated cosine values in amplitude demodulation.
	 */
	private static float[] cosinus;

	/**
	 * Create a signal from a Short array.
	 * 
	 * @param values
	 * @param dt
	 */
	public ArraySignalS(Short[] values, float dt) {
		this.values = values;
		this.dt = dt;
	}

	/**
	 * Create a signal from a byte array.
	 * 
	 * @param bytes
	 * @param numBytesPerInt
	 * @param dt
	 */
	public ArraySignalS(byte[] bytes, int numBytesPerInt, float dt) {
		this.values = ByteArray.byteArrayToShortArray(bytes, numBytesPerInt);
		this.dt = dt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.ArrayYSignal#set(int, java.lang.Number)
	 */
	@Override
	public void set(int i, Short y) {
		values[i] = y;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.ArrayYSignal#get(int)
	 */
	@Override
	public Short get(int i) {
		return values[i];
	}

	/**
	 * The implementation of the frequency demodulation method.
	 * 
	 * @param threshold the threshold of the level crossing algorithm.
	 * @return the frequency of the signal.
	 */
	public double getFrequency(short threshold) {
		int i = 0;
		int indexOfFirst = -1;
		while ((i < (values.length - 1)) && (indexOfFirst == -1)) {
			if ((values[i] < threshold) && (values[i + 1] >= threshold)) {
				indexOfFirst = i;
			}
			i++;
		}
		if (indexOfFirst == -1) {
			return 0.0f;
		}

		int numOfPeriods = 0;
		int indexOfLast = -1;
		for (i = indexOfFirst + 1; i < (values.length - 1); i++) {
			if ((values[i] < threshold) && (values[i + 1] >= threshold)) {
				numOfPeriods++;
				indexOfLast = i;
			}
		}
		if (numOfPeriods == 0) {
			return 0.0f;
		}
		SignalPoint beg1 = new SignalPoint(indexOfFirst * dt, values[indexOfFirst]);
		SignalPoint beg2 = new SignalPoint((indexOfFirst + 1) * dt, values[indexOfFirst + 1]);
		SignalPoint end1 = new SignalPoint(indexOfLast * dt, values[indexOfLast]);
		SignalPoint end2 = new SignalPoint((indexOfLast + 1) * dt, values[indexOfLast + 1]);
		double t = SignalPoint.linearInterpolateInY(threshold, end1, end2)
				- SignalPoint.linearInterpolateInY(threshold, beg1, beg2);
		return numOfPeriods / t;
	}

	

	/**
	 * Pre-calculates the sine and cosine values used in amplitude demodulation.
	 * 
	 * @param N the length of the demodulating window.
	 */
	private void calculateSinAndCos(int N) {
		sinus = new float[N];
		cosinus = new float[N];
		double angle;
		for (int i = 0; i < N; i++) {
			angle = (2.0 * Math.PI * i) / N;
			sinus[i] = (float) Math.sin(angle);
			cosinus[i] = (float) Math.cos(angle);
		}
	}

	/**
	 * The implementation of the amplitude demodulation, at the default 1000 Hz modulator freq.
	 * 
	 * @return the the amplitude of the signal at the modulator frequency
	 */
	public float getAmplitude() {
		int N = 44;
		if (sinus == null || cosinus == null) {
			calculateSinAndCos(N);
		}
		float sumSin = 0.0f;
		float sumCos = 0.0f;
		float angle;
		int L = values.length;
		for (int i = 0; i < L; i++) {
			if (values[i] >= Short.MAX_VALUE || values[i] < Short.MIN_VALUE) {
				System.out.println(values[i]);
			}
			angle = (float) (2.0f * Math.PI * i) / N;
			sumSin += values[i] * Math.sin(angle);
			sumCos += values[i] * Math.cos(angle);
		}

		return (float) Math.sqrt((sumSin * sumSin) + (sumCos * sumCos));
	}

	/**
	 * The implementation of the amplitude demodulation.
	 * 
	 * @param modulatorFreq the frequency of the modulating signal.
	 * @return the amplitude of the signal at the modulator frequency.
	 */
	public float getAmplitude(float modulatorFreq) {
		int N = (int) Math.round(1.0 / (dt * modulatorFreq));
		if (sinus == null || cosinus == null) {
			calculateSinAndCos(N);
		}
		double sumSin = 0.0;
		double sumCos = 0.0;
		int L = values.length;
		for (int i = 0; i < L; i++) {
			if (values[i] >= Short.MAX_VALUE || values[i] <= Short.MIN_VALUE) {
				System.out.println(values[i]);
			}
			sumSin += values[i] * sinus[i % N];
			sumCos += values[i] * cosinus[i % N];
		}
		return (float) Math.sqrt((sumSin * sumSin) + (sumCos * sumCos));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.Signal#max()
	 */
	@Override
	public Short max() {
		short max = Short.MIN_VALUE;
		for (int i = 0; i < values.length; i++) {
			if (values[i] > max) {
				max = values[i];
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
	public Short min() {
		short min = Short.MAX_VALUE;
		for (int i = 0; i < values.length; i++) {
			if (values[i] < min) {
				min = values[i];
			}
		}
		return min;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.Signal#size()
	 */
	@Override
	public int size() {
		return values.length;
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
		values = new Short[bytes.length / numBytesPerInt];
		for (int i = 0; i < bytes.length / numBytesPerInt; i++) {
			for (int j = 0; j < numBytesPerInt; j++) {
				temp[j] = bytes[(i * numBytesPerInt) + j];
			}
			values[i] = (ByteArray.byteArrayToShort(temp));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.YSignal#minus(java.lang.Number)
	 */
	@Override
	public void minus(Short i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.YSignal#plus(java.lang.Number)
	 */
	@Override
	public void plus(Short i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
