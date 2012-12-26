package suncertify.db;

/**
 * Exception thrown by the {@code createRecord} method of the {@code DBAccess}
 * interface.
 * <p>
 * In this implementation, this exception is not actually thrown. As specified
 * the createMethod uses an empty slot to store the newly created record. The
 * key (i.e. the record number) is not a parameter for the method. Therefore a
 * situation in which the slot chosen by the createOperation conflicts with a
 * key already present in the data file, cannot arise
 * 
 * @author Rasmus Kuschel
 */
public class DuplicateKeyException extends Exception {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -6689165809485807888L;

	/**
	 * Creates a new exception instance.
	 */
	public DuplicateKeyException() {
		super();
	}

	/**
	 * Creates a new exception instance with the specified description.
	 * 
	 * @param description
	 *            exception's description
	 */
	public DuplicateKeyException(String description) {
		super(description);
	}
}
