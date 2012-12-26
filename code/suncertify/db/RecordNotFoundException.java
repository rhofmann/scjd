package suncertify.db;

/**
 * Exception class thrown by DBAccess method if a record with the specified
 * number does not exist or if it is marked as deleted
 * 
 * @author Rasmus Kuschel
 */
public class RecordNotFoundException extends Exception {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 9172845648588845215L;

	/**
	 * Creates a new exception instance.
	 */
	public RecordNotFoundException() {
		super();
	}

	/**
	 * Creates a new exception instance with the specified description
	 * 
	 * @param description
	 *            Exception's description
	 */
	public RecordNotFoundException(String description) {
		super(description);
	}
}