package suncertify.db;

/**
 * Exception class thrown by DBAccess methods if an access is attempted on a
 * record that is locked with a cookie other than the given cookie value.
 * 
 * @author Rasmus Kuschel
 */
public class SecurityException extends Exception {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 8491334144885559308L;

	/**
	 * Creates a new exception instance.
	 */
	public SecurityException() {
		super();
	}

	/**
	 * Creates a new exception instance with the specified description
	 * 
	 * @param description
	 *            Exception's description
	 */
	public SecurityException(String description) {
		super(description);
	}
}