package suncertify.db.domain;

/**
 * Enumeration of all legal states of data records, i.e. valid or deleted
 * 
 * @author Rasmus Kuschel
 */
public enum DataRecordState {

	/**
	 * State indicating that the data record is valid
	 */
	VALID((short) 0x0000),
	/**
	 * State indicating that the data record is deleted and the slot is
	 * available for reuse
	 */
	DELETED((short) 0x8000);

	/**
	 * Encoding of the state.
	 */
	private short encoding;

	/**
	 * Constructor for DataRecordState. Allows to specify a value for the
	 * state's encoding
	 * 
	 * @param encoding
	 *            Encoding of the state
	 */
	DataRecordState(short encoding) {
		this.encoding = encoding;
	}

	/**
	 * Returns the encoding of the data record state.
	 * 
	 * @return encoding of the state
	 */
	public short getEncoding() {
		return encoding;
	}

	/**
	 * Returns an instance of this enum that corresponds to the given encoding.
	 * If the encoding is not a legal value, null is returned.
	 * 
	 * @param encoding
	 *            state encoding
	 * @return corresponding enum instance
	 */
	public static DataRecordState forValue(short encoding) {
		for (DataRecordState state : values()) {
			if (state.getEncoding() == encoding) {
				return state;
			}
		}

		return null;
	}

	/**
	 * Returns a String representation of this DataRecordState.
	 * 
	 * @return String representation
	 */
	@Override
	public String toString() {
		return name().substring(0, 3);
	}
}
