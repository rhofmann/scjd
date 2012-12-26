package suncertify.db;

/**
 * Exception class thrown if a record cannot be booked because it has already
 * been booked.
 * 
 * @author Rasmus Kuschel
 */
public class RecordAlreadyBookedException extends Exception {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 6883989718077183109L;

	/**
	 * Creates a new exception instance.
	 */
	public RecordAlreadyBookedException() {
		super();
	}

	/**
	 * Creates a new exception instance with the specified description.
	 * 
	 * @param description
	 *            Exception's description
	 */
	public RecordAlreadyBookedException(String description) {
		super(description);
	}
}
