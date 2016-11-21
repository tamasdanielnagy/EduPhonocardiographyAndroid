package noise.phonocardiographygraph.signal;

public class HighPassFilter {

	private double fs;
	private double fc;

	private double b;
	private double a0;
	private double a1;

	private double xprew;
	private double yprew;

	public HighPassFilter(double fs, double fc) {
		this.fs = fs;
		this.fc = fc;
		xprew = 0.0;
		yprew = 0.0;
		b = Math.exp((-2.0) * Math.PI * (fc/fs));
		a0 = (1.0 + b) / 2.0;
		a1 = -a0;
	}
	
	public double filterNext(double x) {
		double y = (a0 * x) + (a1 * xprew) + (b * yprew);
		xprew = x;
		yprew = y;
		return y;
	}

	public double getFs() {
		return fs;
	}

	public void setFs(double fs) {
		this.fs = fs;
	}

	public double getFc() {
		return fc;
	}

	public void setFc(double fc) {
		this.fc = fc;
	}

	public double getXprew() {
		return xprew;
	}

	public void setXprew(double xprew) {
		this.xprew = xprew;
	}

	public double getYprew() {
		return yprew;
	}

	public void setYprew(double yprew) {
		this.yprew = yprew;
	}

	public double getB() {
		return b;
	}

	public double getA0() {
		return a0;
	}

	public double getA1() {
		return a1;
	}
	
	
	
}
