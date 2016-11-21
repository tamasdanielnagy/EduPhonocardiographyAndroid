package noise.phonocardiographygraph.signal;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import noise.phonocardiographygraph.exception.SignalIsEmptyException;

/**
 * Represents an unevenly sampled signal.
 * 
 * @author Nagy Tamas
 * 
 */
public class SignalXY extends Signal<Double> implements Iterable<SignalPoint>, ListSignal {

	/**
	 * The points of the signal.
	 */
	private LinkedList<SignalPoint> points;

	/**
	 * Constructor with color.
	 * 
	 * @param color
	 */
	public SignalXY(int color) {
		points = new LinkedList<>();
		this.color = color;
	}

	/**
	 * Default constructor.
	 */
	public SignalXY() {
		points = new LinkedList<>();
	}

	/**
	 * Adds a point to the end of the signal.
	 * 
	 * @param point
	 * @return
	 */
	public boolean add(SignalPoint point) {
		return points.add(point);
	}

	/**
	 * Adds a point to the end of the signal.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean add(double x, double y) {
		return points.add(new SignalPoint(x, y));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<SignalPoint> iterator() {
		return points.iterator();
	}

	/**
	 * @return the first point of the signal.
	 */
	public SignalPoint getFirst() {
		return points.getFirst();
	}

	/**
	 * @return the last point of the signal.
	 */
	public SignalPoint getLast() {
		return points.getLast();
	}

	/**
	 * 
	 * Calculates the "derivative" of the original curve.
	 * 
	 * @return
	 */
	public synchronized SignalXY derivative() {
		SignalXY derivative = new SignalXY();
		double x1, x2 = 0, y1, y2 = 0;
		SignalPoint point;
		Iterator<SignalPoint> it = this.iterator();
		if (it.hasNext()) {
			point = it.next();
			x2 = point.getX();
			y2 = point.getY();
		}
		while (it.hasNext()) {
			x1 = x2;
			y1 = y2;
			point = it.next();
			x2 = point.getX();
			y2 = point.getY();
			derivative.add(new SignalPoint((x1 + x2) / 2.0f, (y2 - y1) / (x2 - x1)));
		}
		return derivative;
	}

	/**
	 * Calculates the "derivative" of the original curve container and stores it
	 * in the derivative container.
	 * 
	 * @param derivative
	 */
	public synchronized void derivative(SignalXY derivative) {
		synchronized (this) {
			synchronized (derivative) {
				if (derivative != null) {
					derivative.removeAll();
					double x1, x2 = 0, y1, y2 = 0;
					SignalPoint point;
					Iterator<SignalPoint> it = this.iterator();
					if (it.hasNext()) {
						point = it.next();
						x2 = point.getX();
						y2 = point.getY();
					}
					while (it.hasNext()) {
						x1 = x2;
						y1 = y2;
						point = it.next();
						x2 = point.getX();
						y2 = point.getY();
						derivative.add(new SignalPoint((x1 + x2) / 2.0f, (y2 - y1) / (x2 - x1)));
					}
				}
			}
		}
	}

	/**
	 * Numeric integral of the signal.
	 * 
	 * @param beg
	 * @param end
	 * @return
	 */
	public synchronized float areaUnderCurve(float beg, float end) {
		try {
			if (isEmpty()) {
				throw (new SignalIsEmptyException());
			}
			if (this.getLast().getX() <= beg || this.getFirst().getX() >= end) {
				return 0.0f;
			}
			float area = 0.0f;
			SignalPoint point1 = new SignalPoint(0.0f, 0.0f), point2 = new SignalPoint(0.0f, 0.0f);
			Iterator<SignalPoint> it = this.iterator();
			point2 = it.next();
			while (it.hasNext()) {
				point1 = point2;
				point2 = it.next();
				if (point2.getX() > beg) {
					break;
				}
			}

			if (point1.getX() < beg) {
				point1 = new SignalPoint(beg, SignalPoint.linearInterpolateInX(beg, point1, point2));
			}

			area += (point2.getX() - point1.getX()) * ((point1.getY() + point2.getY()) / 2);
			while (it.hasNext()) {
				point1 = point2;
				point2 = it.next();
				if (point2.getX() > end) {
					break;
				}
				area += (point2.getX() - point1.getX()) * ((point1.getY() + point2.getY()) / 2);
			}

			if (point1.getX() < beg) {
				point2 = new SignalPoint(beg, SignalPoint.linearInterpolateInX(beg, point1, point2));
			}
			area += (point2.getX() - point1.getX()) * ((point1.getY() + point2.getY()) / 2);
			return area;
		} catch (SignalIsEmptyException e) {
			return 0.0f;
		}

	}

	/**
	 * Re-samples the signal, using linear interpolation.
	 * @param samplingTime
	 * @param numberOfSamples
	 * @return
	 */
	public SignalD sample(double samplingTime, int numberOfSamples) {
		SignalD sampled = new SignalD();
		int i = 1;
		sampled.setDt(samplingTime);
		double time = this.getFirst().getX();
		sampled.setStartTime(time);
		Iterator<SignalPoint> oIt = this.iterator();
		SignalPoint point1;
		SignalPoint point2;
		point2 = this.getFirst();
		while (oIt.hasNext() && i <= numberOfSamples) {
			point1 = point2;
			point2 = oIt.next();
			while (time >= point1.getX() && time < point2.getX() && i <= numberOfSamples) {
				sampled.add(SignalPoint.linearInterpolateInX(time, point1, point2));
				time += samplingTime;
				i++;
			}
		}

		return sampled;
	}

	/**
	 * Re-samples the signal, using linear interpolation, store it in the sampled container.
	 * @param sampled
	 * @param samplingTime
	 * @param numberOfSamples
	 */
	public void sample(SignalD sampled, double samplingTime, int numberOfSamples) {
		synchronized (this) {
			synchronized (sampled) {
				try {
					if (isEmpty()) {
						throw (new SignalIsEmptyException());
					}
					sampled.removeAll();
					int i = 1;
					double time = this.getFirst().getX();
					sampled.setStartTime(time);
					sampled.setDt(samplingTime);
					Iterator<SignalPoint> oIt = this.iterator();
					SignalPoint point1;
					SignalPoint point2;
					point2 = this.getFirst();
					while (oIt.hasNext() && i <= numberOfSamples) {
						point1 = point2;
						point2 = oIt.next();
						while (time >= point1.getX() && time < point2.getX() && i <= numberOfSamples) {
							sampled.add(SignalPoint.linearInterpolateInX(time, point1, point2));
							time += samplingTime;
							i++;
						}
					}
				} catch (SignalIsEmptyException e) {
					// do nothing
				}
			}
		}

	}

	/**
	 * @return the length of the signal is seconds.
	 */
	public synchronized double length() {
		if (points.isEmpty())
			return 0.0f;
		else
			return points.getLast().getX() - points.getFirst().getX();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.Signal#mean()
	 */
	public synchronized float mean() {
		int i = 0;
		float mean = 0.0f;
		for (SignalPoint cp : this) {
			mean += cp.getY();
			i++;
		}
		return mean / ((float) i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.Signal#max()
	 */
	public synchronized Double max() {
		double max = Float.NEGATIVE_INFINITY;
		for (SignalPoint cp : this) {
			if (cp.getY() > max) {
				max = cp.getY();
			}
		}
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.Signal#min()
	 */
	public synchronized Double min() {
		double min = Float.POSITIVE_INFINITY;
		for (SignalPoint cp : this) {
			if (cp.getY() < min) {
				min = cp.getY();
			}
		}
		return min;
	}

	/**
	 * Decreases the signal by d.
	 * @param d
	 */
	public synchronized void minus(float d) {
		for (SignalPoint cp : this) {
			cp.setY(cp.getY() - d);
		}
	}

	/**
	 * increases the signal by d.
	 * @param d
	 */
	public synchronized void plus(float d) {
		for (SignalPoint cp : this) {
			cp.setY(cp.getY() + d);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public SignalXY clone() {
		SignalXY clone = new SignalXY();
		clone.color = color;
		clone.title = title;
		clone.xAxisTitle = xAxisTitle;
		clone.yAxisTitle = yAxisTitle;
		for (SignalPoint point : this) {
			clone.add(point);
		}
		return clone;

	}

	/**
	 * Adds a new point to the beginning of the signal.
	 * @param p
	 */
	public void addFirst(SignalPoint p) {
		points.addFirst(p);
	}

	/**
     * Removes the first point of the signal.
     */
	public synchronized void removeFirst() {
		points.removeFirst();
	}

	/**
     * Removes the last point of the signal.
     */
	public synchronized void removeLast() {
		points.removeLast();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.ListSignal#removeAll()
	 */
	public synchronized void removeAll() {
		points.removeAll(points);
	}

	/**
     * Reverse the signal.
     */
	public void reverse() {
		Collections.reverse(points);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.Signal#isEmpty()
	 */
	public boolean isEmpty() {
		return points.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plethysmography.signal.Signal#size()
	 */
	public int size() {
		return points.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public synchronized String toString() {
		String s = "";
		for (SignalPoint cp : this) {
			s += (cp + " ");
		}
		return s;
	}
}
