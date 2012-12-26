package suncertify.remote;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import suncertify.db.DBAccess;
import suncertify.db.DuplicateKeyException;
import suncertify.db.RecordNotFoundException;
import suncertify.db.SecurityException;

/**
 * Implementation of the {@code RemoteDBAccess} interface.
 * <p>
 * The implementation wraps a {@code DBAccess} instance that operates directly
 * on a data file on the server and exposes all methods via the corresponding
 * methods in the {@code RemoteDBAccess} interface.
 * <p>
 * It extends {@code UnicastRemoteObject} and can be published in an RMI
 * registry.
 * 
 * @author Rasmus Kuschel
 */
public final class RemoteData extends UnicastRemoteObject implements
		Serializable, Remote, RemoteDBAccess {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -6253777173007854244L;

	/**
	 * Wrapped DBAccess instance.
	 * <p>
	 * RemoteData is designed to be used via RMI and must therefore be
	 * serializable. Since this file does not make sense outside of the server
	 * VM, it is marked as transient.
	 */
	private transient final DBAccess dbAccess;

	/**
	 * Creates a new RemoteData instance that wraps the specified DBAccess.
	 * 
	 * @param dbAccess
	 *            wrapped DBAccess
	 * @throws RemoteException
	 *             if a networking error occurs
	 */
	protected RemoteData(DBAccess dbAccess) throws RemoteException {
		super();
		this.dbAccess = dbAccess;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long createRecord(String[] data) throws DuplicateKeyException,
			RemoteException {
		return dbAccess.createRecord(data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteRecord(long recNo, long lockCookie)
			throws RecordNotFoundException, SecurityException, RemoteException {
		dbAccess.deleteRecord(recNo, lockCookie);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long[] findByCriteria(String[] criteria) throws RemoteException {
		return dbAccess.findByCriteria(criteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long lockRecord(long recNo) throws RecordNotFoundException,
			RemoteException {
		return dbAccess.lockRecord(recNo);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] readRecord(long recNo) throws RecordNotFoundException,
			RemoteException {
		return dbAccess.readRecord(recNo);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unlock(long recNo, long cookie) throws SecurityException,
			RemoteException {
		dbAccess.unlock(recNo, cookie);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateRecord(long recNo, String[] data, long lockCookie)
			throws RecordNotFoundException, SecurityException, RemoteException {
		dbAccess.updateRecord(recNo, data, lockCookie);
	}
}
