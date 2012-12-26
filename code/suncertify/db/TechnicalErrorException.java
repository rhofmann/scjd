package suncertify.db;

/**
 * Instances of this exception class are thrown if a technical error occurs,
 * e.g. if a database or data file is not accessible or if the network is
 * unavailable etc.
 * <p>
 * The DBAccess interface does not provide a way to signal the occurence of such
 * a technical error. Implementing classes may in this case choose to throw a
 * {@code TechnicalErrorException}. Since they are a {@code RuntimeException}s
 * they need not be declared in the interface.
 * 
 * @author Rasmus Kuschel
 */
public class TechnicalErrorException extends RuntimeException {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -1184469646233660694L;

	/**
	 * Creates a new exception instance.
	 */
	public TechnicalErrorException() {
		super();
	}

	/**
	 * Creates a new exception instance with the specified description.
	 * 
	 * @param description
	 *            exception's description
	 */
	public TechnicalErrorException(String description) {
		super(description);
	}

	/**
	 * Constructs a new runtime exception with the specified description and
	 * cause.
	 * 
	 * @param description
	 *            exception's description
	 * @param cause
	 *            exception's cause
	 */
	public TechnicalErrorException(String description, Throwable cause) {
		super(description, cause);
	}
}
