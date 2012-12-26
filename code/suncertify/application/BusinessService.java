package suncertify.application;

import java.util.ArrayList;
import java.util.List;

import suncertify.db.DBAccess;
import suncertify.db.RecordAlreadyBookedException;
import suncertify.db.RecordNotFoundException;
import suncertify.db.SecurityException;
import suncertify.db.TechnicalErrorException;
import suncertify.db.domain.DataRecord;

/**
 * Provides a business method for each use case of the application: listing all
 * valid records, searching for records by name and/or location and booking a
 * record.
 * <p>
 * Internally the class uses an instance of some class implementing the {@code
 * DBAccess} interface to access the data. Since the implementation only uses
 * the methods provided by the DBAccess interface, we do not need to distinguish
 * whether we operate directly on a data file or whether the data operations are
 * sent over a network.
 * <p>
 * The business methods manage the flow of activities and provide the necessary
 * synchronisation for calls to the DBAccess instance.
 * 
 * @author Rasmus Kuschel
 */
public final class BusinessService {

	/**
	 * DBAccess instance used to access the data
	 */
	private final DBAccess dbAccess;

	/**
	 * Creates a new BusinessService instance.
	 * 
	 * @param dbAccess
	 */
	public BusinessService(DBAccess dbAccess) {
		this.dbAccess = dbAccess;
	}

	/**
	 * Returns a list of all valid data records.
	 * 
	 * @return all data records
	 */
	public List<DataRecord> retrieveAllRecords() {

		// Fetching all records is the same as "searching" for records without
		// any filtering criteria.
		return searchRecords(null, null);
	}

	/**
	 * Returns a list of the data records that exactly match the specified
	 * criteria.
	 * <p>
	 * A criteria value of null matches any field value.
	 * 
	 * @param name
	 *            Criteria for the name field
	 * @param location
	 *            Criteria for the location field
	 * @return all data records exactly matching the criteria
	 */
	public List<DataRecord> searchRecords(String name, String location) {

		// Build the criteria array for the findByCriteria method.
		// the fields that are not used to filter the result are assigned null
		// and are therefore not used for filtering.
		final String[] criteria = new String[] { name, location, null, null,
				null, null };

		final long[] indices = dbAccess.findByCriteria(criteria);

		final List<DataRecord> records = new ArrayList<DataRecord>();

		// For each index returned by the findByCriteria method, read the record
		// and check whether it is an exact match for the filtering criteria.
		if (indices != null) {
			for (final long index : indices) {
				try {
					final String[] recordData = dbAccess.readRecord(index);
					final DataRecord record = new DataRecord(index, recordData);

					// findByCriteria returns records where the respective
					// fields start with the specified criteria.
					// We want only those records, where the fields exactly
					// match the criteria.
					if (isExactMatch(record, name, location)) {
						records.add(record);
					}
				} catch (final RecordNotFoundException ignored) {
					// Ignore this exception.

					// This situation may happen, when another client deletes
					// the record between the call to findByCriteria and this
					// call to readRecord.
				}
			}
		}

		return records;
	}

	/**
	 * Tries to book the record with the specified record number.
	 * <p>
	 * Throws a RecordAlreadyBookedException if the record was already booked.
	 * <p>
	 * The parameter containing a collection of records is modified to reflect
	 * the update. So even if the method throws an exception (and does not
	 * return an updated record collection), the parameter's modified value can
	 * be used by callers to receive an updated view of the records.
	 * 
	 * @param record
	 *            data record to be booked.
	 * @param records
	 *            records that are currently visible to the caller. The record
	 *            to be booked must not necessarily be one of these (although it
	 *            probably is)
	 * @return collection of records containing the update
	 * @throws RecordAlreadyBookedException
	 *             if the record was already booked.
	 * @throws RecordNotFoundException
	 *             if no record with the given record number exists or if it is
	 *             marked as deleted
	 */
	public List<DataRecord> bookRecord(final DataRecord record,
			final List<DataRecord> records) throws RecordNotFoundException,
			RecordAlreadyBookedException {

		final long recNo = record.getRecNo();
		long cookie = -1;
		boolean locked = false;

		try {
			// Lock the record. This call may be blocking, but after it
			// returns, we own the lock and store its cookie value.
			cookie = dbAccess.lockRecord(recNo);

			// The locked flag is used in the finally block to determine
			// whether the lockRecord operation succeeded or whether it has
			// thrown an exception.
			locked = true;

			// Although the client already has this data record in its view, we
			// have to reread it, as another client might have made changes.
			// While we hold the lock, no other client can concurrently update
			// this record.
			final String[] data = dbAccess.readRecord(recNo);
			final DataRecord updatedRecord = new DataRecord(recNo, data);

			// If the record was already booked (it has a non-null owner),
			// update the data record collection to reflect this and throw an
			// exception.
			if (updatedRecord.isBooked()) {
				updateRecordCollection(records, updatedRecord);
				throw new RecordAlreadyBookedException();
			}

			// Set the owner field. Since the application does not interact with
			// this value other then checking if it is set at all, we simply set
			// a constant "owner ID", signifying that the record is "booked" by
			// some client.
			updatedRecord.setOwner(DataRecord.OWNER_ID);

			// Write the changed record back to the data source and update the
			// records collection to reflect the changed data.
			dbAccess.updateRecord(recNo, updatedRecord.getData(), cookie);
			updateRecordCollection(records, updatedRecord);
		} catch (final SecurityException e) {
			// This should not really happen, unless there is a programming
			// error.
			// Propagate this as a technical error to the client.
			throw new TechnicalErrorException("Invalid lock cookie", e);
		} finally {
			// Unlock the record.
			// This must be done in the finally block, to ensure that it is
			// unlocked, even if a (runtime) exception occured in the method.
			try {
				// The lockRecord operation may have failed with a
				// RecordNotFoundException: We need to unlock only when it has
				// succeeded and the locked flag is set to true
				if (locked) {
					dbAccess.unlock(recNo, cookie);
				}
			} catch (final SecurityException e) {
				throw new TechnicalErrorException("Cannot unlock record "
						+ recNo, e);
			}
		}

		return records;
	}

	/**
	 * Checks whether the specified name and location exactly match the name and
	 * location field in the specified data record.
	 * <p>
	 * If name or location is null, they are not used for the comparison.
	 * 
	 * @param record
	 *            DataRecord used for comparison.
	 * @param name
	 *            name value for comparison. Not used if null
	 * @param location
	 *            location value for comparison. Not used if null
	 * @return true if all non-null values match the values in the respective
	 *         record fields.
	 */
	private boolean isExactMatch(final DataRecord record, final String name,
			final String location) {

		boolean exactMatch = true;
		if (name != null) {
			exactMatch &= name.equals(record.getName());
		}
		if (location != null) {
			exactMatch &= location.equals(record.getLocation());
		}
		return exactMatch;
	}

	/**
	 * Updates the records collection, i.e. if the list of the data records
	 * contains an entry with the same record number as the updateRecord
	 * parameter, its value are overwritten.
	 * 
	 * @param records
	 *            list of data records
	 * @param updateRecord
	 *            data record to be updated in the collection
	 */
	private void updateRecordCollection(List<DataRecord> records,
			DataRecord updateRecord) {
		for (final DataRecord record : records) {
			if (updateRecord.getRecNo() == record.getRecNo()) {
				record.setName(updateRecord.getName());
				record.setLocation(updateRecord.getLocation());
				record.setSpecialties(updateRecord.getSpecialties());
				record.setSize(updateRecord.getSize());
				record.setRate(updateRecord.getRate());
				record.setOwner(updateRecord.getOwner());
			}
		}
	}
}
