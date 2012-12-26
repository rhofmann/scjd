package suncertify.remote;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import suncertify.db.DBAccess;
import suncertify.db.Data;

/**
 * RemoteDBAccessFactory implementation that creates RemoteDBAccess instances
 * that directly wrap a DBAccess instance.
 * <p>
 * The DBAccess instance is created on the specified database location.
 * 
 * @author Rasmus Kuschel
 */
public final class RemoteDBAccessFactoryImpl extends UnicastRemoteObject
		implements Serializable, Remote, RemoteDBAccessFactory {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -4736665265281321470L;

	/**
	 * Database location for the wrapped DBAccess instances.
	 */
	private transient final String databaseLocation;

	/**
	 * Creates a new factory instance with the specified database location.
	 * 
	 * @param databaseLocation
	 *            database location for the wrapped DBAccess instances.
	 * @throws RemoteException
	 *             if a networking error occurs
	 */
	public RemoteDBAccessFactoryImpl(String databaseLocation)
			throws RemoteException {
		this.databaseLocation = databaseLocation;
	}

	/**
	 * Creates a RemoteDBAccess instance directly wrapping a DBAccess instance
	 * with the specified database location
	 * 
	 * @return RemoteDBAccess instance
	 * @throws RemoteException
	 *             if a networking error occurs
	 */
	public RemoteDBAccess createRemoteDBAccess() throws RemoteException {

		final DBAccess dbAccess = new Data(databaseLocation);
		final RemoteDBAccess remoteDBAccess = new RemoteData(dbAccess);

		return remoteDBAccess;
	}
}
