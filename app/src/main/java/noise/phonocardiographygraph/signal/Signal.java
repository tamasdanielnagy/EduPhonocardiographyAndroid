package noise.phonocardiographygraph.signal;


import android.graphics.Color;

/**
 * Represents a signal.
 * 
 * @author Nagy Tamas
 */
public abstract class Signal<N extends Number> {

	/**
	 * Color of the signal, used in the Graph class.
	 */
	protected int color = Color.BLACK;
	
	/**
	 * Line, points or signs, used in the Graph class.
	 */
	protected SignalGraphType graphType = SignalGraphType.LINE;
	
	/**
	 * Title of the signal, used in the Graph class.
	 */
	protected String title = null;
	
	/**
	 * X axis title of the signal, used in the Graph class.
	 */
	protected String xAxisTitle = null;
	
	/**
	 * Y axis title of the signal, used in the Graph class.
	 */
	protected String yAxisTitle = null;

	/**
	 * @return the color.
	 */
	public int getColor() {
		return color;
	}

	/**
	 * @param color the new color.
	 */
	public void setColor(int color) {
		this.color = color;
	}

	/**
	 * @return the graph type.
	 */
	public SignalGraphType getGraphType() {
		return graphType;
	}

	/**
	 * @param graphType the new graph type.
	 */
	public void setGraphType(SignalGraphType graphType) {
		this.graphType = graphType;
	}

	/**
	 * @return the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param graphTitle the new title.
	 */
	public void setTitle(String graphTitle) {
		this.title = graphTitle;
	}

	/**
	 * @return x axis title.
	 */
	public String getxAxisTitle() {
		return xAxisTitle;
	}

	/**
	 * @param xAxisTitle the new x axis title.
	 */
	public void setxAxisTitle(String xAxisTitle) {
		this.xAxisTitle = xAxisTitle;
	}

	/**
	 * @return the y axis title.
	 */
	public String getyAxisTitle() {
		return yAxisTitle;
	}

	
	/**
	 * @param yAxisTitle the new y axis title.
	 */
	public void setyAxisTitle(String yAxisTitle) {
		this.yAxisTitle = yAxisTitle;
	}

	/**
	 * @return the mean of the signal.
	 */
	public abstract float mean();

	/**
	 * @return true, if the signal has no elements, else false.
	 */
	public abstract boolean isEmpty();

	/**
	 * @return the maximum of the signal.
	 */
	public abstract N max();

	/**
	 * @return the minimum of the signal.
	 */
	public abstract N min();

	/**
	 * @return the number of samples in the signal.
	 */
	public abstract int size();
}
