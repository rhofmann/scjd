package suncertify.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import suncertify.db.domain.DataRecord;
import suncertify.db.domain.DataRecordState;
import suncertify.db.domain.FileMetaData;
import suncertify.db.lock.LockManager;

/**
 * Implementation of the DBAccess interface that operates directly on a data
 * file.
 * 
 * @author Rasmus Kuschel
 */
public final class Data implements DBAccess {

	/**
	 * Component used for low-level file access
	 */
	private FileAccess fileAccess;

	/**
	 * Component used to manage record locking
	 */
	private LockManager lockManager;

	/**
	 * Creates a new instance and initializes all components.
	 * <p>
	 * Initializing the data file access component may result in a {@code
	 * RuntimeException} instance to be thrown, e.g. if the data file is
	 * inaccessible or if its schema cannot be validated.
	 * 
	 * @param databaseLocation
	 *            path to the data file
	 */
	public Data(String databaseLocation) {
		initialize(databaseLocation);
	}

	/**
	 * Initializes this instance.
	 * <p>
	 * The components for low-level file access and lock management are
	 * initizialized. This may result in a {@code RuntimeException} instance to
	 * be thrown if the initialization of any of these components fails.
	 * 
	 * @param databaseLocation
	 *            path to the data file
	 */
	private void initialize(String databaseLocation) {
		fileAccess = new FileAccess();
		try {
			FileAccess.openFile(databaseLocation);
		} catch (IOException e) {
			throw new TechnicalErrorException("cannot initialize database", e);
		}

		lockManager = new LockManager();
	}

	/**
	 * Returns the database location that is accessed. If the database has not
	 * been initialized yet, null is returned.
	 * 
	 * @return database location
	 */
	public String getDatabaseLocation() {
		if (FileAccess.getActiveFileMetaData() != null) {
			return FileAccess.getActiveFileMetaData().getDataFilePath();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] readRecord(long recNo) throws RecordNotFoundException {

		// Validation and access of the record must be handled atomically.
		// Otherwise another client might change/delete the record, after it was
		// successfully validated, resulting in a dirty read.
		synchronized (Data.class) {
			// check if record exists and is not deleted
			validateRecord(recNo);
			try {
				final DataRecord record = fileAccess.readRecord(recNo);
				final String[] result = record.getData();

				return result;
			} catch (final IOException e) {
				throw new TechnicalErrorException("Cannot read record: "
						+ recNo, e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateRecord(long recNo, String[] data, long lockCookie)
			throws RecordNotFoundException, SecurityException {

		// Validation and record access must be handled atomically.
		// Otherwise another client might change/delete the record, after it was
		// successfully validated, resulting in a lost write.
		synchronized (Data.class) {
			// check if record exists and is not deleted
			validateRecord(recNo);
			// check lock cookie value
			lockManager.validateCookie(recNo, lockCookie);

			final DataRecord record = new DataRecord(recNo, data);
			try {
				fileAccess.writeRecord(recNo, record);
			} catch (final IOException e) {
				throw new TechnicalErrorException("Cannot update record "
						+ recNo, e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteRecord(long recNo, long cookie)
			throws RecordNotFoundException, SecurityException {

		// Validation and record access must be handled atomically.
		// Otherwise another client might change/delete the record, after it was
		// successfully validated, resulting in a lost write.
		synchronized (Data.class) {
			// check if record exists and is not deleted
			validateRecord(recNo);
			// check lock cookie value
			lockManager.validateCookie(recNo, cookie);

			try {
				final DataRecord record = fileAccess.readRecord(recNo);
				record.setState(DataRecordState.DELETED);

				fileAccess.writeRecord(recNo, record);
			} catch (final IOException e) {
				throw new TechnicalErrorException("Cannot deleted record "
						+ recNo, e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long[] findByCriteria(String[] criteria) {

		// The complex findByCriteria method must be handled atomically.
		// Otherwise other clients might change/delete records, after they have
		// been read and before they were stored in the return value.
		synchronized (Data.class) {
			final List<Long> found = new ArrayList<Long>();
			if (criteria != null
					&& criteria.length == FileMetaData.EXPECTED_FIELD_COUNT) {
				try {
					final List<DataRecord> allRecords = fileAccess
							.readAllRecords();
					for (final DataRecord record : allRecords) {
						if (record.getState() != DataRecordState.DELETED) {

							// Compare all criteria that are non-null
							boolean criteriaMatch = true;
							final String[] data = record.getData();

							for (int i = 0; i < criteria.length; i++) {
								final String criterion = criteria[i];
								// Use the criterion only, if it is not null
								if (criterion != null) {
									// Check if the respective field starts with
									// the criterion's value
									if (!data[i].startsWith(criterion)) {
										criteriaMatch = false;
									}
								}
							}

							// Add only records to the result set, when criteria
							// match
							if (criteriaMatch) {
								found.add(record.getRecNo());
							}
						}
					}
				} catch (final IOException e) {
					throw new TechnicalErrorException(
							"Cannot access data file", e);
				}
			}

			final long[] result = new long[found.size()];
			for (int i = 0; i < result.length; i++) {
				result[i] = found.get(i);
			}

			return result;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long createRecord(String[] data) throws DuplicateKeyException {

		// The createRecord method needs to be handled atomically.
		// Otherwise clients that are concurrently searching for an empty slot
		// to write a record, may see the same state of the data file and choose
		// the same slot for writing.
		synchronized (Data.class) {
			// We need to find an empty slot, in which to save the record.
			// This can either be a slot, where the record was deleted and that
			// can
			// be reused or if no such slot can be found, the record will be
			// appended at the data file's end
			final long NOT_FOUND = -1;
			long emptySlotNo = NOT_FOUND;

			try {
				// Check all existant slots for one, where the record was
				// deleted
				List<DataRecord> records = fileAccess.readAllRecords();
				for (int i = 0; i < records.size(); i++) {
					DataRecord record = records.get(i);
					if (record.getState() == DataRecordState.DELETED
							&& emptySlotNo == NOT_FOUND) {
						emptySlotNo = i;
					}
				}

				// If no empty slot was found, append the record to the end of
				// the file
				if (emptySlotNo == NOT_FOUND) {
					emptySlotNo = records.size();
				}

				// Write the record to the data file
				DataRecord newRecord = new DataRecord(data);
				fileAccess.writeRecord(emptySlotNo, newRecord);
			} catch (IOException e) {
				// ignore I/O exception
				e.printStackTrace();
			}

			return emptySlotNo;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long lockRecord(long recNo) throws RecordNotFoundException {

		// The lock operation cannot be called from within the synchronized
		// block because it is a blocking call.
		// We lock the record first and then check if it is valid (existent and
		// not deleted) at all. If it is not, we need to unlock it again.
		final long lockCookie = lockManager.lock(recNo);

		synchronized (Data.class) {
			try {
				validateRecord(recNo);
			} catch (final RecordNotFoundException e) {
				// Validation failed. We need to rethrow this exception, but
				// first unlock the record.
				try {
					lockManager.unlock(recNo, lockCookie);
				} catch (final SecurityException securityException) {
					throw new TechnicalErrorException("Cannot unlock record "
							+ recNo, securityException);
				}
				throw e;
			}
		}

		return lockCookie;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unlock(long recNo, long cookie) throws SecurityException {
		synchronized (Data.class) {
			lockManager.unlock(recNo, cookie);
		}
	}

	/**
	 * Checks whether the record with the specified record number exists in the
	 * data file and is not deleted.
	 * <p>
	 * Otherwise a {@code RecordNotFoundException} is thrown.
	 * <p>
	 * This method needs to be called from within a block synchronized on the
	 * Data class object provide a context within which no concurrent
	 * modifications by other clients invalidate or falsify the method's result.
	 * 
	 * @param recNo
	 *            record number
	 * @throws RecordNotFoundException
	 *             if the record with the specified number is not in the data
	 *             file or is marked as deleted
	 */
	private void validateRecord(final long recNo)
			throws RecordNotFoundException {
		final boolean valid = fileAccess.isValidRecordNumber(recNo);
		if (!valid) {
			throw new RecordNotFoundException("record with number " + recNo
					+ " does not exist in the data file");
		} else {
			final boolean deleted = fileAccess.isDeleted(recNo);
			if (deleted) {
				throw new RecordNotFoundException("record with number " + recNo
						+ " is deleted from data file");
			}
		}
	}
}
