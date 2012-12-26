package suncertify.application;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import suncertify.db.DBAccess;
import suncertify.db.Data;
import suncertify.db.NetworkErrorException;
import suncertify.remote.RemoteDBAccess;
import suncertify.remote.RemoteDBAccessFactory;
import suncertify.remote.RemoteDataAdapter;

/**
 * Factory that can create instances implementing the {@code DBAccess}
 * interface.
 * <p>
 * The created instances are not constrained in the way they provide access to
 * the data, as long as they implement the DBAccess interface.
 * 
 * @author Rasmus Kuschel
 */
public final class DBAccessFactory {

	/**
	 * Private constructor to prevent instance creation.
	 */
	private DBAccessFactory() {
	}

	/**
	 * Creates a local DBAccess instance, i.e. one that directly accesses the
	 * data file.
	 * 
	 * @param databaseLocation
	 *            Path to the data file
	 * @return DBAccess instance
	 */
	public static DBAccess getLocalDBAccess(String databaseLocation) {

		final DBAccess dbAccess = new Data(databaseLocation);

		return dbAccess;
	}

	/**
	 * Creates a remote DBAccess instance, i.e. one that communicates with a
	 * server at the specified endpoint to provide data access.
	 * 
	 * @param serverAddress
	 *            Address of the server endpoint
	 * @param serverPort
	 *            Port of the server endpoint
	 * @return DBAccess instance
	 * @throws NetworkErrorException
	 *             if an error occurs in the communication with the server
	 */
	public static DBAccess getRemoteDBAccess(String serverAddress,
			String serverPort) throws NetworkErrorException {

		final RemoteDBAccessFactory factory = getRemoteDBAccessFactory(
				serverAddress, serverPort);

		RemoteDBAccess remoteDBAccess = null;
		try {
			remoteDBAccess = factory.createRemoteDBAccess();
		} catch (final RemoteException e) {
			throw new NetworkErrorException(
					"Cannot create RemoteDBAccess instance", e);
		}

		final DBAccess dbAccess = new RemoteDataAdapter(remoteDBAccess);

		return dbAccess;
	}

	/**
	 * Returns an instance of a {@code RemoteDBAccessFactory} implementing
	 * class.
	 * <p>
	 * The instance is created by an RMI lookup.
	 * 
	 * @param serverAddress
	 *            Address of the server endpoint
	 * @param serverPort
	 *            Port of the server endpoint
	 * @return RemoteDBAccessFactory implementation
	 * @throws NetworkErrorException
	 *             if an error occurs in the communication with the server
	 */
	private static RemoteDBAccessFactory getRemoteDBAccessFactory(
			String serverAddress, String serverPort)
			throws NetworkErrorException {

		// URL to look up the registered factory
		final String url = "rmi://" + serverAddress + ":" + serverPort
				+ "/RemoteDBAccessFactory";

		RemoteDBAccessFactory factory = null;
		try {
			factory = (RemoteDBAccessFactory) Naming.lookup(url);
		} catch (final MalformedURLException e) {
			throw new NetworkErrorException("URL to server invalid", e);
		} catch (final RemoteException e) {
			throw new NetworkErrorException(
					"Connection to server not possible", e);
		} catch (final NotBoundException e) {
			throw new NetworkErrorException("RemoteDBAccessFactory not bound",
					e);
		}

		return factory;
	}
}
