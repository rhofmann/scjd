package suncertify.db;

/**
 * Interface that needs to be implemented by classes providing access to the
 * database.
 * <p>
 * Implementing classes are not constrained in the way they provide access to
 * the data. They can e.g. operate directly on a data file or data base or
 * through a network connection.
 * 
 * @author Provided with the SCJD assignment
 */
public interface DBAccess {

	/**
	 * Reads a record from the file. Returns an array where each element is a
	 * record value.
	 * 
	 * @param recNo
	 *            number of the record to be read
	 * @return field values of the record for the given number
	 * @throws RecordNotFoundException
	 *             if the record with the specified number does not exist or is
	 *             marked as deleted
	 * @throws TechnicalErrorException
	 *             Indicates that a technical error occured while reading the
	 *             record
	 */
	public String[] readRecord(long recNo) throws RecordNotFoundException;

	/**
	 * Modifies the fields of a record. The new value for field n appears in
	 * data[n]. Throws SecurityException if the record is locked with a cookie
	 * other than lockCookie.
	 * 
	 * @param recNo
	 *            number of the record to be updated
	 * @param data
	 *            data to be updated
	 * @param lockCookie
	 *            lock cookie value
	 * 
	 * @throws RecordNotFoundException
	 *             if a record with the specified number does not exist or is
	 *             marked as deleted.
	 * @throws SecurityException
	 *             if the record is locked with a cookie other than the
	 *             specified lockCookie
	 * @throws IllegalArgumentException
	 *             if specified data is null
	 */
	public void updateRecord(long recNo, String[] data, long lockCookie)
			throws RecordNotFoundException, SecurityException;

	/**
	 * Deletes a record, making the record number and associated disk storage
	 * available for reuse.
	 * <p>
	 * Throws SecurityException if the record is locked with a cookie other than
	 * lockCookie.
	 * 
	 * @param recNo
	 *            number of the record to be deleted
	 * @param lockCookie
	 *            lock cookie value
	 * 
	 * @throws RecordNotFoundException
	 *             if a record with the specified number does not exist or is
	 *             marked as deleted.
	 * @throws SecurityException
	 *             if the record is locked with a cookie other than the
	 *             specified lockCookie
	 */
	public void deleteRecord(long recNo, long lockCookie)
			throws RecordNotFoundException, SecurityException;

	/**
	 * Returns an array of record numbers that match the specified criteria.
	 * Field n in the database file is described by criteria[n]. A null value in
	 * criteria[n] matches any field value. A non-null value in criteria[n]
	 * matches any field value that begins with criteria[n]. (For example,
	 * "Fred" matches "Fred" or "Freddy".)
	 * 
	 * @param criteria
	 *            criteria for the
	 * @return array of record numbers that match the specified criteria
	 */
	public long[] findByCriteria(String[] criteria);

	/**
	 * Creates a new record in the database (possibly reusing a deleted entry).
	 * Inserts the given data, and returns the record number of the new record.
	 * 
	 * @param data
	 *            data of the record
	 * @return record number of the new record
	 * @throws DuplicateKeyException
	 *             is not thrown, as the key is not part of the data
	 */
	public long createRecord(String[] data) throws DuplicateKeyException;

	/**
	 * Locks a record so that it can only be updated or deleted by this client.
	 * <p>
	 * Returned value is a cookie that must be used when the record is unlocked,
	 * updated, or deleted. If the specified record is already locked by a
	 * different client, the current thread gives up the CPU and consumes no CPU
	 * cycles until the record is unlocked.
	 * 
	 * @param recNo
	 *            number of the record
	 * @return lock cookie value
	 * @throws RecordNotFoundException
	 *             if a record with the specified number does not exist or is
	 *             marked as deleted.
	 */
	public long lockRecord(long recNo) throws RecordNotFoundException;

	/**
	 * Releases the lock on a record. Cookie must be the cookie returned when
	 * the record was locked; otherwise throws SecurityException.
	 * 
	 * @param recNo
	 *            number of the record
	 * @param cookie
	 *            lock cookie value
	 * @throws SecurityException
	 *             if the record is locked with a cookie other than the
	 *             specified lockCookie
	 */
	public void unlock(long recNo, long cookie) throws SecurityException;
}
