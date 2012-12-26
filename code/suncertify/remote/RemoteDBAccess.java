package suncertify.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import suncertify.db.DuplicateKeyException;
import suncertify.db.RecordNotFoundException;
import suncertify.db.SecurityException;

/**
 * Interface that needs to be implemented by classes providing access to the
 * database by communicating over a network.
 * <p>
 * The interface extends {@code java.rmi.Remote} and can therefore be used in an
 * RMI environment.
 * <p>
 * Each method corresponds to a method from the {@code DBAccess} interface with
 * the same signature, except that it declares a {@code RemoteException} may be
 * thrown.
 * 
 * @author Rasmus Kuschel
 */
public interface RemoteDBAccess extends Remote {

	/**
	 * Reads a record. Returns an array where each element is a record value.
	 * 
	 * @param recNo
	 *            number of the record to be read
	 * @return field values of the record for the given number
	 * @throws RecordNotFoundException
	 *             if the record with the specified number does not exist or is
	 *             marked as deleted
	 * @throws RemoteException
	 *             if an error occurs concering the networking
	 */
	public String[] readRecord(long recNo) throws RecordNotFoundException,
			RemoteException;

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
	 * @throws RemoteException
	 *             if an error occurs concering the networking
	 */
	public void updateRecord(long recNo, String[] data, long lockCookie)
			throws RecordNotFoundException, SecurityException, RemoteException;

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
	 * @throws RemoteException
	 *             if an error occurs concering the networking
	 */
	public void deleteRecord(long recNo, long lockCookie)
			throws RecordNotFoundException, SecurityException, RemoteException;

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
	 * @throws RemoteException
	 *             if an error occurs concering the networking
	 */
	public long[] findByCriteria(String[] criteria) throws RemoteException;

	/**
	 * Creates a new record in the database (possibly reusing a deleted entry).
	 * Inserts the given data, and returns the record number of the new record.
	 * 
	 * @param data
	 *            data of the record
	 * @return record number of the new record
	 * @throws DuplicateKeyException
	 *             is not thrown, as the key is not part of the data
	 * @throws RemoteException
	 *             if an error occurs concering the networking
	 */
	public long createRecord(String[] data) throws DuplicateKeyException,
			RemoteException;

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
	 * @throws RemoteException
	 *             if an error occurs concering the networking
	 */
	public long lockRecord(long recNo) throws RecordNotFoundException,
			RemoteException;

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
	 * @throws RemoteException
	 *             if an error occurs concering the networking
	 */
	public void unlock(long recNo, long cookie) throws SecurityException,
			RemoteException;
}
