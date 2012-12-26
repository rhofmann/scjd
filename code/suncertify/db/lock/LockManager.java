package suncertify.db.lock;

import java.util.HashMap;
import java.util.Map;

import suncertify.db.SecurityException;

/**
 * Component for the management of record locks.
 * <p>
 * The operations to lock and unlock a record are delegated by the Data
 * implementation to an instance of this class. Internally, lock information is
 * stored in a static hashmap, mapping record numbers to record locks. This map
 * needs to be static so that information about lock can be shared between
 * several Data instances. Access to the map needs to be appropriately
 * synchronized.
 * 
 * @author Rasmus Kuschel
 */
public final class LockManager {

	/**
	 * Mapping of the record index (long) to a DataRecordLock instance.
	 */
	private static Map<Long, DataRecordLock> lockMap = new HashMap<Long, DataRecordLock>();

	/**
	 * Acquires the lock on the record with the given index and returns the
	 * value of the lock. This method blocks until the lock succeeds.
	 * <p>
	 * Threads trying to acquire a lock are synchronized on the corresponding
	 * DataRecordLock instance. I.e. if a lock is already held by another thread
	 * the calling thread suspends execution and must be notified when the lock
	 * is released.
	 * <p>
	 * This implementation does not test whether a record with the given index
	 * does exist at all or whether it is deleted. It is possible to lock on any
	 * index of a record, and it is the responsibility of the caller to ensure
	 * that it is only called with valid indices.
	 * 
	 * @param recNo
	 *            index of the record to be locked
	 * @return cookie value of the lock after it is acquired
	 */
	public long lock(long recNo) {

		final DataRecordLock recordLock = getLock(recNo);
		final long cookie = recordLock.acquire(); // blocking

		return cookie;
	}

	/**
	 * Releases the lock on the record with the given index.
	 * 
	 * If the caller cannot prove lock ownership by presenting the correct
	 * cookie value, a SecurityException is thrown.
	 * 
	 * @param recNo
	 *            index of the record
	 * @param cookie
	 *            cookie value of the lock
	 * @throws SecurityException
	 *             if the record was locked with a cookie value other than the
	 *             specified cookie value
	 */
	public void unlock(long recNo, long cookie) throws SecurityException {

		final DataRecordLock lock = getLock(recNo);
		lock.release(cookie);
	}

	/**
	 * Checks if the record with the specified number is locked with the
	 * specified cookie value. If the correct cookie value is used, the method
	 * simply returns. If the wrong cookie value is supplied, a {@code
	 * SecurityException} is thrown.
	 * 
	 * @param recNo
	 *            number of the record
	 * @param cookie
	 *            lock cookie value
	 * @throws SecurityException
	 *             if the record is locked with a cookie other than the
	 *             specified lockCookie
	 */
	public void validateCookie(long recNo, long cookie)
			throws SecurityException {

		final DataRecordLock lock = getLock(recNo);
		if (lock == null || lock.getLockCookie() != cookie) {
			throw new SecurityException("invalid cookie value");
		}
	}

	/**
	 * Returns the DataRecordLock instance for the record with the given
	 * instance, lazily creating one if necessary.
	 * 
	 * @param recNo
	 *            index of the record
	 * @return DataRecordLock instance for the record
	 */
	private DataRecordLock getLock(long recNo) {

		// Synchronize on the lockMap to ensure, that for each record number,
		// at most one lock instance exists
		synchronized (lockMap) {
			// Get the DataRecordLock instance, lazily creating one if necessary
			DataRecordLock recordLock = lockMap.get(recNo);
			if (recordLock == null) {
				recordLock = new DataRecordLock();
				lockMap.put(recNo, recordLock);
			}

			return recordLock;
		}
	}
}