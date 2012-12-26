package suncertify.remote;

import java.rmi.RemoteException;

import suncertify.db.DBAccess;
import suncertify.db.DuplicateKeyException;
import suncertify.db.NetworkErrorException;
import suncertify.db.RecordNotFoundException;
import suncertify.db.SecurityException;

/**
 * Adapter that wraps a RemoteDBAccess instance and exposes all its methods via
 * their corresponding DBAccess methods.
 * <p>
 * {@code RemoteException} instances are caught and wrapped in a
 * NetworkErrorException and then rethrown.
 * 
 * @author Rasmus Kuschel
 */
public final class RemoteDataAdapter implements DBAccess {

	/**
	 * Wrapped RemoteDBAccess instance.
	 */
	private final RemoteDBAccess remoteDBAccess;

	/**
	 * Creates a new RemoteAdapter instance wrapping the specified
	 * RemoteDBAccess instance.
	 * 
	 * @param remoteDBAccess
	 *            wrapped RemoteDBAccess instance
	 */
	public RemoteDataAdapter(RemoteDBAccess remoteDBAccess) {
		this.remoteDBAccess = remoteDBAccess;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws NetworkErrorException
	 *             if a networking error occurs
	 */
	@Override
	public long createRecord(String[] data) throws DuplicateKeyException {
		try {
			return remoteDBAccess.createRecord(data);
		} catch (final RemoteException remoteException) {
			throw new NetworkErrorException(
					"Cannot invoke createRecord remotely", remoteException);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws NetworkErrorException
	 *             if a networking error occurs
	 */
	@Override
	public void deleteRecord(long recNo, long lockCookie)
			throws RecordNotFoundException, SecurityException {
		try {
			remoteDBAccess.deleteRecord(recNo, lockCookie);
		} catch (final RemoteException remoteException) {
			throw new NetworkErrorException(
					"Cannot invoke deleteRecord remotely", remoteException);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws NetworkErrorException
	 *             if a networking error occurs
	 */
	@Override
	public long[] findByCriteria(String[] criteria) {
		try {
			return remoteDBAccess.findByCriteria(criteria);
		} catch (final RemoteException remoteException) {
			throw new NetworkErrorException(
					"Cannot invoke findByCriteria remotely", remoteException);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws NetworkErrorException
	 *             if a networking error occurs
	 */
	@Override
	public long lockRecord(long recNo) throws RecordNotFoundException {
		try {
			return remoteDBAccess.lockRecord(recNo);
		} catch (final RemoteException remoteException) {
			throw new NetworkErrorException(
					"Cannot invoke lockRecord remotely", remoteException);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws NetworkErrorException
	 *             if a networking error occurs
	 */
	@Override
	public String[] readRecord(long recNo) throws RecordNotFoundException {
		try {
			return remoteDBAccess.readRecord(recNo);
		} catch (final RemoteException remoteException) {
			throw new NetworkErrorException(
					"Cannot invoke readRecord remotely", remoteException);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws NetworkErrorException
	 *             if a networking error occurs
	 */
	@Override
	public void unlock(long recNo, long cookie) throws SecurityException {
		try {
			remoteDBAccess.unlock(recNo, cookie);
		} catch (final RemoteException remoteException) {
			throw new NetworkErrorException("Cannot invoke unlock remotely",
					remoteException);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws NetworkErrorException
	 *             if a networking error occurs
	 */
	@Override
	public void updateRecord(long recNo, String[] data, long lockCookie)
			throws RecordNotFoundException, SecurityException {
		try {
			remoteDBAccess.updateRecord(recNo, data, lockCookie);
		} catch (final RemoteException remoteException) {
			throw new NetworkErrorException(
					"Cannot invoke updateRecord remotely", remoteException);
		}
	}
}
