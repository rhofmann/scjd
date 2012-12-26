package suncertify.server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import suncertify.db.CorruptDatabaseException;
import suncertify.db.FileAccess;
import suncertify.remote.RemoteDBAccessFactory;
import suncertify.remote.RemoteDBAccessFactoryImpl;

/**
 * Controller for the server component of the application.
 * 
 * The controller implements a methods for each use case provided by the server:
 * starting a new server and stopping it.
 * 
 * Starting a server comprises setting up the RMI registry and publishing a
 * factory that clients can use to create connections.
 * 
 * @author Rasmus Kuschel
 */
public final class ServerController {

	/**
	 * Path to the data file
	 */
	private final String databaseLocation;

	/**
	 * Creates a new ServerController instance.
	 * 
	 * @param databaseLocation
	 *            path to the data file
	 */
	public ServerController(String databaseLocation) {
		this.databaseLocation = databaseLocation;
	}

	/**
	 * Starts the server.
	 * 
	 * Starts a new RMI registry and publishes a {@code RemoteDBAccessFactory}
	 * instance. Clients that want to connect to this server, may obtain a proxy
	 * of this factory and can use it to create a connection.
	 * 
	 * @return true if the server was successfully started
	 */
	public boolean startServer() {

		// Open and validate the data file
		try {
			FileAccess.openFile(databaseLocation);
		} catch (final IOException e) {
			// data file cannot be opened
			return false;
		} catch (final CorruptDatabaseException e) {
			// data file cannot be validated
			return false;
		}

		// Start an RMI registry and register a RemoteDBAccessFactory instance.
		// Clients can use this factory to create connections to this server.
		try {
			final RemoteDBAccessFactory remoteDBAccessFactory = new RemoteDBAccessFactoryImpl(
					databaseLocation);
			final Registry registry = LocateRegistry
					.createRegistry(Registry.REGISTRY_PORT);

			registry.rebind(RemoteDBAccessFactory.RMI_KEY,
					remoteDBAccessFactory);
		} catch (RemoteException e) {
			return false;
		}

		return true;
	}

	/**
	 * Stops the server and stops this application instance.
	 */
	public void stopServer() {
		// Releasing recources is done in the shutdown hook
		System.exit(0);
	}
}
