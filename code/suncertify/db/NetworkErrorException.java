package suncertify.db;

/**
 * Exception instances are thrown when a client cannot invoke an operation on a
 * server object because of I/O problems.
 * 
 * @author Rasmus Kuschel
 */
public class NetworkErrorException extends TechnicalErrorException {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 5489381002123437031L;

	/**
	 * Creates a new exception instance.
	 */
	public NetworkErrorException() {
		super();
	}

	/**
	 * Creates a new exception instance with the specified description.
	 * 
	 * @param description
	 *            description of the exception
	 */
	public NetworkErrorException(String description) {
		super(description);
	}

	/**
	 * Creates a new exception instance with the specified description and
	 * cause.
	 * 
	 * @param description
	 *            description of the exception
	 * @param cause
	 *            cause of the exception
	 */
	public NetworkErrorException(String description, Throwable cause) {
		super(description, cause);
	}
}
