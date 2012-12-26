package suncertify.db;

/**
 * Instances of this exception class are thrown, if the database or data file is
 * unaccessible or does not conform to the specified database schema.
 * 
 * @author Rasmus Kuschel
 */
public class CorruptDatabaseException extends TechnicalErrorException {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 3666302599426487496L;

	/**
	 * Constructs a new exception instance.
	 */
	public CorruptDatabaseException() {
		super();
	}

	/**
	 * Constructs a new exception instance witht the specified description.
	 * 
	 * @param description
	 */
	public CorruptDatabaseException(String description) {
		super(description);
	}
}
