package suncertify.db.domain;

/**
 * Metadata of a data file.
 * <p>
 * This includes all fields in the file header and schema description section.
 * <p>
 * Provides methods to calculate aggregated properties (e.g. record length,
 * offset of a record within the file).
 * 
 * @author Rasmus Kuschel
 */
public final class FileMetaData {

	/**
	 * Expected magic cookie value in the data file (as provided with the SCJD
	 * assignment).
	 */
	public static final int EXPECTED_MAGIC_COOKIE_VALUE = 0x0202;

	/**
	 * Expected offset value in the data file (as provided with the SCJD
	 * assignment).
	 */
	public static final int EXPECTED_OFFSET = 70;

	/**
	 * Expected field count in the data file.
	 */
	public static final short EXPECTED_FIELD_COUNT = 6;

	/**
	 * Name of the "name" data field
	 */
	public static final String FIELD_NAME_NAME = "name";

	/**
	 * Name of the "location" data field
	 */
	public static final String FIELD_NAME_LOCATION = "location";

	/**
	 * Name of the "specialities" data field
	 */
	public static final String FIELD_NAME_SPECIALTIES = "specialties";

	/**
	 * Name of the "size" data field
	 */
	public static final String FIELD_NAME_SIZE = "size";

	/**
	 * Name of the "rate" data field
	 */
	public static final String FIELD_NAME_RATE = "rate";

	/**
	 * Name of the "owner" data field
	 */
	public static final String FIELD_NAME_OWNER = "owner";

	/**
	 * Expected field names
	 */
	public static final String[] EXPECTED_FIELD_NAMES = new String[] {
			FIELD_NAME_NAME, FIELD_NAME_LOCATION, FIELD_NAME_SPECIALTIES,
			FIELD_NAME_SIZE, FIELD_NAME_RATE, FIELD_NAME_OWNER };

	/**
	 * Length of the "name" data field
	 */
	public static final int FIELD_LENGTH_NAME = 32;

	/**
	 * Length of the "name" data field
	 */
	public static final int FIELD_LENGTH_LOCATION = 64;

	/**
	 * Length of the "specialities" data field
	 */
	public static final int FIELD_LENGTH_SPECIALTIES = 64;

	/**
	 * Length of the "size" data field
	 */
	public static final int FIELD_LENGTH_SIZE = 6;

	/**
	 * Length of the "name" data field
	 */
	public static final int FIELD_LENGTH_RATE = 8;

	/**
	 * Length of the "owner" data field
	 */
	public static final int FIELD_LENGTH_OWNER = 8;

	/**
	 * Expected field lengths.
	 */
	public static final int[] EXPECTED_FIELD_LENGTHS = new int[] {
			FIELD_LENGTH_NAME, FIELD_LENGTH_LOCATION, FIELD_LENGTH_SPECIALTIES,
			FIELD_LENGTH_SIZE, FIELD_LENGTH_RATE, FIELD_LENGTH_OWNER };

	/**
	 * path to the data file
	 */
	private final String dataFilePath;

	/**
	 * magic cookie value
	 */
	private final int magicCookieValue;

	/**
	 * offset to start of record zero
	 */
	private final int offset;

	/**
	 * number of fields in each record
	 */
	private final short fieldCount;

	/**
	 * schema description section
	 */
	private final SchemaDescription[] schemaDescriptions;

	/**
	 * Length (number of bytes) of a record
	 */
	private final int recordLength;

	/**
	 * Constructs a new FileMetaData instance.
	 * 
	 * @param pathname
	 *            name of the file
	 * @param magicCookieValue
	 *            magic cookie value
	 * @param offset
	 *            offset to start of record zero
	 * @param fieldCount
	 *            number of fields in each record
	 * @param schemaDescriptions
	 *            schema description section
	 */
	public FileMetaData(String pathname, int magicCookieValue, int offset,
			short fieldCount, SchemaDescription[] schemaDescriptions) {

		this.dataFilePath = pathname;

		this.magicCookieValue = magicCookieValue;
		this.offset = offset;
		this.fieldCount = fieldCount;
		this.schemaDescriptions = schemaDescriptions;

		// calculate the number of bytes in a record
		int calculatedRecordLength = 2; // flag
		for (SchemaDescription schemaDescription : schemaDescriptions) {
			calculatedRecordLength += schemaDescription.getFieldLength();
		}
		this.recordLength = calculatedRecordLength;
	}

	/**
	 * Returns the path to the data file
	 * 
	 * @return path to the data file
	 */
	public String getDataFilePath() {
		return dataFilePath;
	}

	/**
	 * Returns the magic cookie value
	 * 
	 * @return magic cookie value
	 */
	public int getMagicCookieValue() {
		return magicCookieValue;
	}

	/**
	 * Returns the offset
	 * 
	 * @return offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Returns the field count
	 * 
	 * @return field count
	 */
	public short getFieldCount() {
		return fieldCount;
	}

	/**
	 * Returns the schema descriptions
	 * 
	 * @return schema descriptions
	 */
	public SchemaDescription[] getSchemaDescriptions() {
		return schemaDescriptions;
	}

	/**
	 * Returns the schema description with the given index. If the index is not
	 * legal in the schema description section, an IndexOutOfBoundsException is
	 * thrown.
	 * 
	 * @param i
	 *            index into schema description section
	 * @return schema description at the given index
	 */
	public SchemaDescription getFieldSchemaDescription(int i) {
		if (i >= schemaDescriptions.length) {
			throw new IndexOutOfBoundsException("Invalid index " + i
					+ " in schema descritpion section");
		}
		return schemaDescriptions[i];
	}

	/**
	 * Returns the length of the field with the given index. If the index is not
	 * legal in the schema description section, an IndexOutOfBoundsException is
	 * thrown.
	 * 
	 * @param i
	 *            index of the field
	 * @return field length at the given index
	 */
	public short getFieldLength(int i) {
		if (i < schemaDescriptions.length) {
			return schemaDescriptions[i].getFieldLength();
		}
		return 0;
	}

	/**
	 * Returns the name of the field with the given index. If the index is not
	 * legal in the schema description section, an IndexOutOfBoundsException is
	 * thrown.
	 * 
	 * @param i
	 *            index of the field
	 * @return field name at the given index
	 */
	public String getFieldName(int i) {
		if (i < schemaDescriptions.length) {
			return schemaDescriptions[i].getFieldName();
		}
		return null;
	}

	/**
	 * Returns the complete length (number of bytes) in a record.
	 * 
	 * @return record length
	 */
	public int getRecordLength() {
		return recordLength;
	}

	/**
	 * Returns the offset of a record with the specified index within the file.
	 * 
	 * @param recNo
	 *            index of the record
	 * @return position of the record within the file.
	 */
	public int getRecordOffset(long recNo) {
		return getOffset() + getRecordLength() * (int) recNo;
	}

	/**
	 * Returns a String representation of this FileMetaData instance.
	 * 
	 * @return String representation of the FileMetaData instance
	 */
	@Override
	public String toString() {
		String result = "magic cookie value: " + magicCookieValue + "\n";
		result += "offset: " + offset + "\n";
		result += "field count: " + fieldCount + "\n";

		for (SchemaDescription schemaDescription : schemaDescriptions) {
			result += schemaDescription.toString() + "\n";
		}

		return result;
	}
}