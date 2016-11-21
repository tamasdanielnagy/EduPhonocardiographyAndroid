package noise.phonocardiographygraph.calculation;


import android.graphics.Color;

import java.util.Iterator;

import noise.phonocardiographygraph.exception.SignalIsEmptyException;
import noise.phonocardiographygraph.exception.TooFewDataToCalculateException;
import noise.phonocardiographygraph.signal.SignalD;
import noise.phonocardiographygraph.signal.SignalGraphType;
import noise.phonocardiographygraph.signal.SignalPoint;
import noise.phonocardiographygraph.signal.SignalXY;


/**
 * Stores the measured and calculated curves and indicators. Does all calculations used for plethysmography.
 * 
 * @author Nagy Tamas
 *
 */
public class Phonocardiography {

	// Colors
	public static int RRintervalsColor = Color.RED;
	public static int normalRRintervalsColor = Color.RED;
	public static int beatsPerMinuteColor = Color.RED;
	public static int heartSoundColor = Color.BLACK;
	private static int originalBeatColor = Color.RED;

	private boolean loaded;
	public static final double NO_MAX_LENGTH = -1.0;
	private double maxLength = NO_MAX_LENGTH;

	/* The data of the curves */
	// The PCG measurement
	private SignalD heartSound;

	// The heart beats
	private SignalXY peaks;
	// The time between hearth beats in ms
	private SignalXY RRintervals;
	private SignalXY normalRRintervals;
	// Beats/minute
	private SignalXY beatsPerMinute;

	// Used in heartBeatDetection()
	private double threshold = 0.0;
	private static final double MIN_OF_LENGTH_IN_PEAK_DETECTION = 3.0;
	

	// Non-spectral analysis, in ms
	private double pulse = 0.0;
	private int meanRR = 0;
	private int sdRR = 0;
	private double pNN50 = 0.0;
	private int rMSSD = 0;

	// Heart beat detection
	private double minOfRiseBeforeBeatMultiplier = 0.45;
	private double maxTimeToPeak = 0.1;
	private double jumpedTimeAdaptingPeakDetection = 0.4;
	
	/**
	 * Constructor.
	 */
	public Phonocardiography() {
		constructCurves();
	}
	

	
	public Phonocardiography(double minOfRiseBeforeBeatMultiplier, double maxTimeToPeak, double jumpedTimeAdaptingPeakDetection) {
		this.minOfRiseBeforeBeatMultiplier = minOfRiseBeforeBeatMultiplier;
		this.maxTimeToPeak = maxTimeToPeak;
		this.jumpedTimeAdaptingPeakDetection = jumpedTimeAdaptingPeakDetection;
		constructCurves();
	}


	/**
	 * Creates the curves.
	 */
	public void constructCurves() {
		heartSound = new SignalD(heartSoundColor);
		
		heartSound.setyAxisTitle("Amplitude [A. U.]");
		heartSound.setxAxisTitle("Time [s]");
		heartSound.setTitle("Heart sound");
		heartSound.setColor(heartSoundColor);
		peaks = new SignalXY(originalBeatColor);

		
		peaks.setGraphType(SignalGraphType.SIGNS);
		RRintervals = new SignalXY(RRintervalsColor);
		RRintervals.setTitle("R-R intervals");
		RRintervals.setxAxisTitle("Heart beats");
		RRintervals.setyAxisTitle("Time [ms]");
		normalRRintervals = new SignalXY(normalRRintervalsColor);
		beatsPerMinute = new SignalXY(beatsPerMinuteColor);
		beatsPerMinute.setTitle("Beats/minute");
		beatsPerMinute.setxAxisTitle("Time [s]");
		beatsPerMinute.setyAxisTitle("Beats/minute");
	}

	/**
	 * Does the plethysmographyc calculations.
	 */
	public void runCalculations() {
		//manageLength();
		
		calculateHeartBeatDetectionParams();
		heartBeatDetection(heartSound, peaks, threshold, maxTimeToPeak, jumpedTimeAdaptingPeakDetection);
		calculateRRintervals();
		calculateNormalRRintervals();
		//calculateSampledRRintervals();
		//calculateStatistics();
		calculateBeatsPerMinute();
		if (loaded) {
			nonSpectralAnalysis();
			//System.out.println(type + " " + pulse);
		}
	}


	/**
	 * Params for adapting peak detection
	 */
	public void calculateHeartBeatDetectionParams() {
		synchronized (heartSound) {
			try {
				if (heartSound.isEmpty())
					throw (new SignalIsEmptyException());
				if (heartSound.getLength() >= MIN_OF_LENGTH_IN_PEAK_DETECTION) {
					double max = heartSound.max();
					threshold = minOfRiseBeforeBeatMultiplier * max;
				}
			} catch (SignalIsEmptyException e) {
				// do nothing
			}
		}
	}

	
	/**
	 * 
	 * Detects the heart beats in a phonocardiographic signal.
	 */
	public void heartBeatDetection(SignalD heartSound, SignalXY beats, double threshold, double maxTimeToPeak, double jumpedTime) {
		synchronized (heartSound) {
			synchronized (beats) {
				try {
					// if the curve is too short, do nothing
					if (!heartSound.isEmpty()
							&& ((heartSound.getLength()) <= MIN_OF_LENGTH_IN_PEAK_DETECTION))
						throw (new TooFewDataToCalculateException());

					// index calculation
					int startIndex;
					double peaksLastX;
					if (beats.isEmpty()) {
						startIndex = 0;
					} else {
						peaksLastX = beats.getLast().getX();
						startIndex = (int) Math.round((peaksLastX - heartSound
								.getStartTime()) / heartSound.getDt()) + 1;
					}
					int jumpInIndex = (int) Math
							.round(jumpedTime
									/ (heartSound.getDt())) - 1;
					int timeToPeakInIndex = (int) Math.round(maxTimeToPeak / (heartSound.getDt()));
					if (jumpInIndex < 0)
						jumpInIndex = 0;
					if (!beats.isEmpty()) {
						startIndex += jumpInIndex;
					}
					int maxIndex = 0;
					int tempIndex = 0;
					int index = startIndex;
					if (heartSound.size() - startIndex <= timeToPeakInIndex)
						throw (new TooFewDataToCalculateException());

					// this cycle is responsible for the peak detection
					while (heartSound.size() > index) {
						// level crossing
						if (heartSound.get(index) >= threshold) {
							maxIndex = index;

							if (timeToPeakInIndex >= (heartSound.size() - index))
								throw (new TooFewDataToCalculateException());

							// maximum searching for heart beat
							for (tempIndex = index; tempIndex - index <= timeToPeakInIndex; tempIndex++) {
								if (heartSound.get(maxIndex) < heartSound
										.get(tempIndex)) {
									maxIndex = tempIndex;
								}
							}

							// store found heart beat
							beats.add(heartSound.getX(maxIndex),
									heartSound.get(maxIndex));
							index += (maxIndex - index) + jumpInIndex;
						}
						index++;
					}
				} catch (TooFewDataToCalculateException e) {
					// do nothing
				}
			}
		}


		// manage signal length
		while (!peaks.isEmpty() && !heartSound.isEmpty()
				&& peaks.getFirst().getX() < heartSound.getX(0)) {
			peaks.removeFirst();
		}
	}

	/**
	 * RR intervals
	 */
	public void calculateRRintervals() {
		if (peaks == null) {
			heartBeatDetection(heartSound, peaks, threshold, maxTimeToPeak, jumpedTimeAdaptingPeakDetection);
		}
		synchronized (peaks) {
			synchronized (RRintervals) {
				try {
					if (peaks.isEmpty())
						throw (new SignalIsEmptyException());
					RRintervals.removeAll();
					Iterator<SignalPoint> hearthBeatsIterator = peaks.iterator();
					SignalPoint point1, point2 = new SignalPoint(0.0f, 0.0f);
					if (hearthBeatsIterator.hasNext())
						point2 = hearthBeatsIterator.next();
					while (hearthBeatsIterator.hasNext()) {
						point1 = point2;
						point2 = hearthBeatsIterator.next();
						RRintervals
								.add(new SignalPoint(point2.getX(), (float) 1000.0 * (point2.getX() - point1.getX())));
					}
				} catch (SignalIsEmptyException e) {
					// do nothing
				}
			}
		}
	}

	/**
	 * Remove ectopic beats from RR intervals.
	 */
	public void calculateNormalRRintervals() {
		if (RRintervals == null)
			calculateRRintervals();
		synchronized (RRintervals) {
			synchronized (normalRRintervals) {
				try {
					if (RRintervals.isEmpty())
						throw (new SignalIsEmptyException());
					SignalPoint point1, point2, point3;
					normalRRintervals.removeAll();
					Iterator<SignalPoint> it = RRintervals.iterator();
					if (it.hasNext())
						point2 = it.next();
					else
						throw (new TooFewDataToCalculateException());
					if (it.hasNext())
						point3 = it.next();
					else
						throw (new TooFewDataToCalculateException());
					//it = RRintervals.iterator();
					normalRRintervals.add(point2);
					// Could be problem if the first RRinterval is ectopic
					while (it.hasNext()) {
						point1 = point2;
						point2 = point3;
						point3 = it.next();
						if (point2.getY() > 1.5 * point1.getY() || point2.getY() < 0.5 * point1.getY()) {
							normalRRintervals.add(new SignalPoint(point2.getX(), SignalPoint.linearInterpolateInX(
									point2.getX(), point1, point3)));
						} else
							normalRRintervals.add(point2);
					}
					if (point3.getY() > 1.5 * point2.getY() || point3.getY() < 0.5 * point2.getY()) {
						normalRRintervals.add(new SignalPoint(point2.getX(), point2.getY()));
					} else
						normalRRintervals.add(point3);
				} catch (SignalIsEmptyException e) {
					// do nothing
				} catch (TooFewDataToCalculateException e) {
					// do nothing
				}
			}
		}
	}


	/**
	 * BPM.
	 */
	public void calculateBeatsPerMinute() {
		synchronized (RRintervals) {
			synchronized (beatsPerMinute) {
				try {
					if (RRintervals == null)
						calculateRRintervals();
					if (RRintervals.isEmpty())
						throw (new SignalIsEmptyException());
					beatsPerMinute.removeAll();
					Iterator<SignalPoint> timeBetweenBeatsIterator = RRintervals.iterator();
					SignalPoint point;
					while (timeBetweenBeatsIterator.hasNext()) {
						point = timeBetweenBeatsIterator.next();
						beatsPerMinute.add(new SignalPoint(point.getX(), (float) (6E4 / point.getY())));
					}
				} catch (SignalIsEmptyException e) {
					// do nothing
				}
			}
		}
	}


	/**
	 * Cardiac function indicators.
	 */
	public synchronized void nonSpectralAnalysis() {
		int n = 0;
		// meanRR, sdRR, pNN50, rMSSD
		double meanRRdouble = 0.0;
		double sdRRdouble = 0.0;
		double rMSSDdouble = 0.0;
		int noc = 0;
		double prevPointY = 0.0;
		double pointY;
		double delta;
		synchronized (normalRRintervals) {
			for (SignalPoint point : normalRRintervals) {
				pointY = point.getY();
				meanRRdouble += pointY;
				//System.out.println(type + " nonspectral  " + pointY);
				sdRRdouble += Math.pow(pointY, 2);
				if (n > 0) {
					delta = prevPointY - pointY;
					rMSSDdouble += Math.pow(delta, 2);
					if (Math.abs(delta) > 50.0)
						noc++;
				}
				prevPointY = pointY;
				n++;
			}
		}
		meanRRdouble /= n;
		sdRRdouble /= n;
		sdRRdouble = Math.sqrt(sdRRdouble - Math.pow(meanRRdouble, 2));
		rMSSDdouble /= n - 1;
		rMSSDdouble = Math.sqrt(rMSSDdouble);
		pulse = (meanRRdouble > 0.0) ? 60000.0 / meanRRdouble : 0.0;
		meanRR = (int) Math.round(meanRRdouble);
		sdRR = (int) Math.round(sdRRdouble);
		rMSSD = (int) Math.round(rMSSDdouble);
		pNN50 = 100.0 * ((double) noc / (n - 1));
	}

	/**
	 * Clears the curves.
	 */
	public void reset() {
		if (heartSound != null)
			heartSound.clear();
		if (peaks != null)
			peaks.removeAll();
		if (RRintervals != null)
			RRintervals.removeAll();
		if (normalRRintervals != null)
			normalRRintervals.removeAll();
		if (beatsPerMinute != null)
			beatsPerMinute.removeAll();
		threshold = 0.0;
		meanRR = 0;
	}



	/**
	 * @return original plethysmographic curve.
	 */
	public SignalD getHeartSound() {
		return heartSound;
	}


	/**
	 * @return the heart beats.
	 */
	public SignalXY getBeats() {
		return peaks;
	}


	/**
	 * @return time between beats.
	 */
	public SignalXY getTimeBetweenBeats() {
		return RRintervals;
	}

	/**
	 * @return BPM.
	 */
	public SignalXY getBeatsPerMinute() {
		return beatsPerMinute;
	}


	/**
	 * @return the RR intervals.
	 */
	public SignalXY getRRintervals() {
		return RRintervals;
	}

	/**
	 * @return the non-ectopic RR intervals.
	 */
	public SignalXY getNormalRRintervals() {
		return normalRRintervals;
	}

	/**
	 * @return the mean of the RR intervals.
	 */
	public int getMeanRR() {
		return meanRR;
	}

	/**
	 * @return sdRR.
	 */
	public int getsdRR() {
		return sdRR;
	}

	/**
	 * @return pNN50.
	 */
	public double getpNN50() {
		return pNN50;
	}

	/**
	 * @return rMSSD.
	 */
	public int getrMSSD() {
		return rMSSD;
	}


	/**
	 * @return is loaded from file.
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * @param loaded loaded from file.
	 */
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	/**
	 * @return the max length of the curves.
	 */
	public double getMaxLength() {
		return maxLength;
	}

	/**
	 * @param maxLength the max length of the curves.
	 */
	public void setMaxLength(double maxLength) {
		this.maxLength = maxLength;
	}

	/**
	 * @return the pulse.
	 */
	public double getPulse() {
		return pulse;
	}

	/**
	 * @param pulse the pulse.
	 */
	public void setPulse(double pulse) {
		this.pulse = pulse;
	}

}
