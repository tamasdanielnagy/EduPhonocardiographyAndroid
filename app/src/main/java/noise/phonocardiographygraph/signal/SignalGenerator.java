package noise.phonocardiographygraph.signal;




/**
 * Generates waveforms.
 * 
 * @author Nagy Tamas
 *
 */
public abstract class SignalGenerator {

	
	public static final float TWO_PI = (float) (2.0f * Math.PI);
	
	
	/**
	 * Sine signal.
	 * @param frequency
	 * @param amplitude
	 * @param sampleRate
	 * @param length
	 * @return
	 */
	public static SignalI generateSinusSignalI(float frequency, int amplitude, float sampleRate, float length) {
		float T = 1.0f / frequency;
		float dt = 1.0f / sampleRate;
		SignalI signal = new SignalI(dt);
		int signalLengthInSamples = (int) Math.floor(length / dt);
		float dtPerTtimesTwoPi = (dt / T) * TWO_PI;
		for (int i = 0; i < signalLengthInSamples; i++) {
			signal.add((int) Math.round(amplitude * Math.sin(i * dtPerTtimesTwoPi)));
			
		}
		return signal;
	}
	
	/**
	 * Sine signal.
	 * @param frequency
	 * @param amplitude
	 * @param sampleRate
	 * @param length
	 * @return
	 */
	public static SignalS generateSinusSignalS(float frequency, short amplitude, float sampleRate, float length) {
		float T = 1.0f / frequency;
		float dt = 1.0f / sampleRate;
		SignalS signal = new SignalS(dt);
		int signalLengthInSamples = (int) Math.floor(length / dt);
		float dtPerTtimesTwoPi = (dt / T) * TWO_PI;
		for (int i = 0; i < signalLengthInSamples; i++) {
			signal.add((short) Math.round(amplitude * Math.sin(i * dtPerTtimesTwoPi)));
			
		}
		return signal;
	}
	
	/**
	 * Sine signal.
	 * @param frequency
	 * @param amplitude
	 * @param sampleRate
	 * @param length
	 * @return
	 */
	public static SignalF generateSinusSignalD(float frequency, float amplitude, float sampleRate, float length) {
		float T = 1.0f / frequency;
		float dt = 1.0f / sampleRate;
		SignalF signal = new SignalF(dt);
		int signalLengthInSamples = (int) Math.floor(length / dt);
		float dtPerTtimesTwoPi = (dt / T) * TWO_PI;
		for (int i = 0; i < signalLengthInSamples; i++) {
			signal.add((float) (amplitude * Math.sin(i * dtPerTtimesTwoPi)));
		}
		return signal;
	}
	
	
	
}
