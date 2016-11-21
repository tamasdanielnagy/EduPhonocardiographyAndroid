package noise.phonocardiographygraph.exception;

/**
 * Thrown when a signal contains too few data to calulate sg.
 * 
 * @author Nagy Tamas
 *
 */
public class TooFewDataToCalculateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2527194868432497563L;

	public TooFewDataToCalculateException() {
		// TODO Auto-generated constructor stub
	}

	public TooFewDataToCalculateException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public TooFewDataToCalculateException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public TooFewDataToCalculateException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}



}
