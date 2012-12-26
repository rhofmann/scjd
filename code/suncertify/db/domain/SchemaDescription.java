package suncertify.db.domain;

/**
 * Stores the meta information about a single column in the schema description
 * of the data file, i.e. the field's name and its length.
 * 
 * @author Rasmus Kuschel
 */
public final class SchemaDescription {

	/**
	 * Name of the field
	 */
	private final String fieldName;

	/**
	 * Length of the field
	 */
	private final short fieldLength;

	/**
	 * Constructs a new instance with the specified field name and length
	 * 
	 * @param fieldName
	 *            field name
	 * @param fieldLength
	 *            field length
	 */
	public SchemaDescription(String fieldName, short fieldLength) {
		this.fieldName = fieldName;
		this.fieldLength = fieldLength;
	}

	/**
	 * Returns the field name
	 * 
	 * @return field name
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Returns the field length
	 * 
	 * @return field length
	 */
	public short getFieldLength() {
		return fieldLength;
	}

	/**
	 * Returns a String representation of this SchemaDescription instance.
	 * 
	 * @return String representation of this SchemaDescription instance
	 */
	@Override
	public String toString() {
		return "name: " + fieldName + ", length: " + fieldLength;
	}
}
