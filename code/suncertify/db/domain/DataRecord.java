package suncertify.db.domain;

/**
 * Represents a data record as it appears in the data file.
 * <p>
 * Besides the attributes of a data record (name, location, specialities, size,
 * rate, owner), instances of {@code DataRecord} contain the number of the
 * record in the data file and its state (i.e. valid or deleted).
 * <p>
 * This class is basically a wrapper for a String array representing a data
 * record. It allows to easily convert from one representation of a data record
 * to another and provides more meaningful access methods (e.g.
 * recordObject.getLocation() instead of recordArray[2], or
 * recordObject.isBooked() instead of recordArray[5] != null).
 * 
 * @author Rasmus Kuschel
 */
public final class DataRecord {

	/**
	 * Constant value for the record number attribute signifying that this
	 * record has not yet been assigned a number.
	 */
	public static final long NO_NUMBER_ASSIGNED = -1;

	/**
	 * Constant value used as value for owner field.
	 * <p>
	 * As specified, the system does not interact with these numbers, rather it
	 * simply records them. In the context of the application we only care, if
	 * the owner field has a value or not. We don't inspect the value or use it
	 * in any way, other than detect if it is set at all.
	 */
	public static final String OWNER_ID = "12345678";

	/**
	 * Index of the name value in the array
	 */
	public static final int INDEX_NAME = 0;

	/**
	 * Index of the location value in the array
	 */
	public static final int INDEX_LOCATION = 1;

	/**
	 * Index of the specialties value in the array
	 */
	public static final int INDEX_SPECIALTIES = 2;

	/**
	 * Index of the size value in the array
	 */
	public static final int INDEX_SIZE = 3;

	/**
	 * Index of the rate value in the array
	 */
	public static final int INDEX_RATE = 4;

	/**
	 * Index of the owner value in the array
	 */
	public static final int INDEX_OWNER = 5;

	/**
	 * Number of this record in the data file.
	 */
	private final long recNo;

	/**
	 * State of the record in the data file.
	 */
	private DataRecordState state;

	/**
	 * Data array
	 */
	private final String[] data;

	/**
	 * Creates a new DataRecord with the specified attribute values.
	 * <p>
	 * Trailing whitespace is deleted from all specified parameters.
	 * 
	 * @param state
	 *            State of the DataRecord in the db file
	 * @param name
	 *            name of the subcontractor
	 * @param location
	 *            locality in which this contractor works
	 * @param specialties
	 *            Comma separated list of types of work this contractor can
	 *            perform
	 * @param size
	 *            number of workers available
	 * @param rate
	 *            Charge per hour for the subcontractor
	 * @param owner
	 *            ID value (an 8 digit number) of the customer
	 */
	public DataRecord(long recNo, DataRecordState state, String name,
			String location, String specialties, String size, String rate,
			String owner) {
		this.recNo = recNo;
		this.state = state;

		this.data = new String[6];
		this.data[INDEX_NAME] = rtrim(name);
		this.data[INDEX_LOCATION] = rtrim(location);
		this.data[INDEX_SPECIALTIES] = rtrim(specialties);
		this.data[INDEX_SIZE] = rtrim(size);
		this.data[INDEX_RATE] = rtrim(rate);
		this.data[INDEX_OWNER] = rtrim(owner);
	}

	/**
	 * Creates a new data record with the specified number, state and attribute
	 * values.
	 * <p>
	 * This constructor allows to directly use a String array parameter to build
	 * the DataRecord wrapper from.
	 * <p>
	 * Trailing whitespace is deleted from all specified parameters.
	 * 
	 * @param recNo
	 *            number of the record
	 * @param state
	 *            state of the record
	 * @param recordData
	 *            attribute values
	 */
	public DataRecord(long recNo, DataRecordState state, String[] recordData) {

		this.recNo = recNo;
		this.state = state;

		if (recordData == null
				|| recordData.length != FileMetaData.EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Cannot create data record from invalid array.");
		}
		this.data = new String[6];
		this.data[INDEX_NAME] = rtrim(recordData[INDEX_NAME]);
		this.data[INDEX_LOCATION] = rtrim(recordData[INDEX_LOCATION]);
		this.data[INDEX_SPECIALTIES] = rtrim(recordData[INDEX_SPECIALTIES]);
		this.data[INDEX_SIZE] = rtrim(recordData[INDEX_SIZE]);
		this.data[INDEX_RATE] = rtrim(recordData[INDEX_RATE]);
		this.data[INDEX_OWNER] = rtrim(recordData[INDEX_OWNER]);
	}

	/**
	 * Creates a new data record object with the specified number and attribute
	 * values.
	 * <p>
	 * The state is assumed to be valid.
	 * 
	 * @param recNo
	 *            number of the record
	 * @param recordData
	 *            attribute values
	 */
	public DataRecord(long recNo, String[] recordData) {
		this(recNo, DataRecordState.VALID, recordData);
	}

	/**
	 * Creates a new data record with the specified attribute values.
	 * <p>
	 * The record number is set to a special value, signifying that the number
	 * is not assigned yet. The record state is assumed to be valid
	 * 
	 * @param recordData
	 *            attribute values
	 */
	public DataRecord(String[] recordData) {
		this(DataRecord.NO_NUMBER_ASSIGNED, DataRecordState.VALID, recordData);
	}

	/**
	 * Removes trailing whitespace from the specified String
	 * 
	 * @param source
	 *            original String
	 * @return String without trailing white space
	 */
	private static String rtrim(String source) {
		if (source == null) {
			return "";
		}

		return source.replaceAll("\\s+$", "");
	}

	/**
	 * Returns the state of this data record.
	 * 
	 * @return data record's state
	 */
	public DataRecordState getState() {
		return state;
	}

	/**
	 * Returns the number of this data record.
	 * 
	 * @return data record's number
	 */
	public long getRecNo() {
		return recNo;
	}

	/**
	 * Checks if this data record is deleted in the data file.
	 * 
	 * @return true if the data record is deleted in the data file.
	 */
	public boolean isDeleted() {
		return DataRecordState.DELETED == state;
	}

	/**
	 * Check if this data record is booked, i.e. its owner attribute is not null
	 * and not empty.
	 * 
	 * @return true if this data record is booked.
	 */
	public boolean isBooked() {
		final String owner = data[INDEX_OWNER];
		return owner != null && !("".equals(owner));
	}

	/**
	 * Sets the state of this data record.
	 * 
	 * @param state
	 *            data record state
	 */
	public void setState(DataRecordState state) {
		this.state = state;
	}

	/**
	 * Returns the attribute values of this data record in String array format-
	 * 
	 * @return attribute values as String array
	 */
	public String[] getData() {
		return data;
	}

	/**
	 * Returns the name of the subcontractor this data record relates to.
	 * 
	 * @return name of the subcontractor
	 */
	public String getName() {
		return data[INDEX_NAME];
	}

	/**
	 * Sets the name of the subcontractor this data record relates to.
	 * 
	 * @param name
	 *            name of the subcontractor
	 */
	public void setName(String name) {
		data[INDEX_NAME] = name;
	}

	/**
	 * Returns the location of the subcontractor this data record relates to.
	 * 
	 * @return location of the subcontractor
	 */
	public String getLocation() {
		return data[INDEX_LOCATION];
	}

	/**
	 * Sets the location of the subcontractor this data record relates to.
	 * 
	 * @param location
	 *            locatin of the subcontractor
	 */
	public void setLocation(String location) {
		data[INDEX_LOCATION] = location;
	}

	/**
	 * Returns the specialities of the subcontractor this data record relates
	 * to.
	 * 
	 * @return specialities of the subcontractor
	 */
	public String getSpecialties() {
		return data[INDEX_SPECIALTIES];
	}

	/**
	 * Sets the specialties of the subcontractor this data record relates to.
	 * 
	 * @param specialties
	 *            specialties of the subcontractor
	 */
	public void setSpecialties(String specialties) {
		data[INDEX_SPECIALTIES] = specialties;
	}

	/**
	 * Returns the size of the subcontractor this data record relates to.
	 * 
	 * @return size of the subcontractor
	 */
	public String getSize() {
		return data[INDEX_SIZE];
	}

	/**
	 * Sets the size of the subcontractor this data record relates to.
	 * 
	 * @param size
	 *            size of the subcontractor
	 */
	public void setSize(String size) {
		data[INDEX_SIZE] = size;
	}

	/**
	 * Returns the rate of the subcontractor this data record relates to.
	 * 
	 * @return rate of the subcontractor
	 */
	public String getRate() {
		return data[INDEX_RATE];
	}

	/**
	 * Sets the rate of the subcontractor this data record relates to.
	 * 
	 * @param rate
	 *            rate of the subcontractor
	 */
	public void setRate(String rate) {
		data[INDEX_RATE] = rate;
	}

	/**
	 * Returns the owner of the subcontractor this data record relates to.
	 * 
	 * @return owner of the subcontractor
	 */
	public String getOwner() {
		return data[INDEX_OWNER];
	}

	/**
	 * Sets the owner of the subcontractor this data record relates to.
	 * 
	 * @param owner
	 *            owner of the subcontractor
	 */
	public void setOwner(String owner) {
		data[INDEX_OWNER] = owner;
	}

	/**
	 * Returns a String representation of this data record.
	 * 
	 * @return String representation of this data record.
	 */
	@Override
	public String toString() {
		String result = "[recNo: " + recNo + ", ";
		result += "state: " + state + ", ";
		result += "name: " + data[INDEX_NAME] + ", ";
		result += "location: " + data[INDEX_LOCATION] + ", ";
		result += "specialties: " + data[INDEX_SPECIALTIES] + ", ";
		result += "size: " + data[INDEX_SIZE] + ", ";
		result += "rate: " + data[INDEX_RATE] + ", ";
		result += "owner: " + data[INDEX_OWNER] + "]";

		return result;
	}
}
