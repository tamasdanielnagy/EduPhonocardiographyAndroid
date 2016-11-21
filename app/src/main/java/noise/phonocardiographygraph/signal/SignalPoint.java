package noise.phonocardiographygraph.signal;

/**
 * Represents one point with x and y coordinates
 *
 *
 * @author Nagy Tamas
 *
 */

public class SignalPoint {
	
	/**
	 * The x coordinate of the point.
	 */
	protected double x;
	
	/**
	 * the y cordinate of the point.
	 */
	protected double y;
	
	/**
	 * Contructor.
	 * 
	 * @param x
	 * @param y
	 */
	public SignalPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Linear interpolation between two SignalPoint
	 * 
	 * @param x
	 * @param point1
	 * @param point2
	 * @return
	 */
	public static double linearInterpolateInX(double x, SignalPoint point1, SignalPoint point2) {
		synchronized (point1) {
			synchronized (point2) {
				return point1.y + ((x - point1.x) * (point2.y - point1.y)) / (point2.x - point1.x);

			}
		}
	}
	
	/**
	 * Linear interpolation between two SignalPoint
	 * @param y
	 * @param point1
	 * @param point2
	 * @return
	 */
	public static double linearInterpolateInY(double y, SignalPoint point1, SignalPoint point2) {
		synchronized (point1) {
			synchronized (point2) {
				return point1.x + ((y - point1.y) * (point2.x - point1.x)) / (point2.y - point1.y);

			}
		}
	}
	
	/**
	 * @return the x coordiate.
	 */
	public double getX() {
		return this.x;
	}
	
	/**
	 * @return the y coordinate.
	 */
	public double getY() {
		return this.y;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public SignalPoint clone() {
		return new SignalPoint(x, y);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()  {
		return "(" + x + ", " + y + ")"; 
	}

	/**
	 * @param x the new x coordinate.
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @param y the new y coordinate.
	 */
	public void setY(double y) {
		this.y = y;
	}
	
}
