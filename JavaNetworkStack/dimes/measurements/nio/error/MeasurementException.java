package dimes.measurements.nio.error;

/**
 * @author Ohad Serfaty
 * <br>
 * <br>
 * A Measurement Exception. 
 */
public class MeasurementException extends Exception {

	private static final long serialVersionUID = 1L;

	public MeasurementException() {
		super();
	}

	public MeasurementException(String message) {
		super(message);
	}

	public MeasurementException(String message, Throwable cause) {
		super(message, cause);
	}

	public MeasurementException(Throwable cause) {
		super(cause);
	}

}
