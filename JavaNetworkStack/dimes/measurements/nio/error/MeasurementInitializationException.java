package dimes.measurements.nio.error;

/**
 * @author Ohad Serfaty
 *<br>
 *<br>
 * A Measurement Initialization exception.
 *
 */
public class MeasurementInitializationException extends MeasurementException {

	private static final long serialVersionUID = 1L;

	public MeasurementInitializationException(String cause) {
		super(cause);
	}

}
