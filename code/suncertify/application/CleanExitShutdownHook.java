package suncertify.application;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import suncertify.db.FileAccess;
import suncertify.remote.RemoteDBAccessFactory;

/**
 * Thread that can be registered as a shutdown hook for the application.
 * <p>
 * All components (gui, network client, server) that were started by this
 * application instance, are signalled to shut down and release any used
 * resources.
 * 
 * @author Rasmus Kuschel
 */
public final class CleanExitShutdownHook extends Thread {

	/**
	 * Signals the components of this application to shut down and release any
	 * used resources.
	 */
	@Override
	public void run() {

		final ApplicationMode mode = Application.getApplicationMode();

		// Depending on the mode of the application, shut down and clean up the
		// respective components

		if (mode != null) {
			if (mode.isServer()) {
				// Unbind the RemoteDBAccessFactory
				try {
					final Registry registry = LocateRegistry
							.getRegistry(Registry.REGISTRY_PORT);
					registry.unbind(RemoteDBAccessFactory.RMI_KEY);
				} catch (final RemoteException ignored) {
					// We cannot obtain a reference to the registry.
					// Since the server is about to be shut down anyway, we
					// ignore this.
					System.err.println(ignored.getMessage());
				} catch (final NotBoundException ignored) {
					// The factory we were about to unbind, is already unbound.
					// Again, we simply ignore this.
					System.err.println(ignored.getMessage());
				}
			}

			if (mode.isServer() || mode.isStandalone()) {
				// Flush and close the data file
				try {
					FileAccess.closeFile();
				} catch (final IOException ignored) {
					// The data file cannot be closed
					System.err.println(ignored.getMessage());
				}
			}

			if (mode.isClient()) {
				// nothing to do
			}
		}
	}
}
