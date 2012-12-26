package suncertify.application;

import java.io.IOException;

import suncertify.client.ClientController;
import suncertify.client.ClientPropertiesDialog;
import suncertify.client.ClientView;
import suncertify.db.DBAccess;
import suncertify.db.NetworkErrorException;
import suncertify.db.TechnicalErrorException;
import suncertify.server.ServerController;
import suncertify.server.ServerPropertiesDialog;
import suncertify.server.ServerView;

/**
 * Main class of the application.
 * <p>
 * Holds information about the mode (Server, Client, Standalone) and properties
 * used by this application instance. The main method determines application
 * mode and properties and starts the appropriate components.
 * 
 * @author Rasmus Kuschel
 */
public final class Application {

	/**
	 * Mode of this application instance, i.e. Server, Client or Standalone.
	 */
	private static ApplicationMode applicationMode;

	/**
	 * Properties of the application.
	 */
	private static ApplicationProperties properties;

	/**
	 * Private constructor to prevent instance creation.
	 */
	private Application() {
	}

	/**
	 * Main entry point of the application.
	 * <p>
	 * Application mode and properties are initialized and the appropriate
	 * components are started.
	 * 
	 * @param args
	 *            command line parameters
	 */
	public static void main(String[] args) {

		// register shutdown hook
		Runtime.getRuntime().addShutdownHook(new CleanExitShutdownHook());

		// determine application mode from command line arguments
		determineApplicationMode(args);

		// load application properties from properties file
		loadProperties();

		// dispatch to appropriate start method
		if (ApplicationMode.SERVER.equals(applicationMode)) {
			startServer();
		} else if (ApplicationMode.CLIENT.equals(applicationMode)) {
			startClient();
		} else if (ApplicationMode.STANDALONE.equals(applicationMode)) {
			startStandAlone();
		} else {
			System.err.println("Unknown application mode");
		}
	}

	/**
	 * Starts the server components.
	 * <p>
	 * Before the server is started, the relevant properties used by a server
	 * instance are displayed in a dialog and can be changed.
	 * <p>
	 * When started, the server exposes a defined endpoint, to which clients can
	 * connect.
	 */
	private static void startServer() {

		final ServerPropertiesDialog dialog = new ServerPropertiesDialog(
				properties);
		dialog.showDialog();

		final String databaseLocation = properties.getDatabaseLocation();

		final ServerController controller = new ServerController(
				databaseLocation);

		final ServerView view = new ServerView(controller);
		view.setVisible(true);
	}

	/**
	 * Starts the client component.
	 * <p>
	 * Before the client is started, the relevant properties used by a client
	 * instance are displayed in a dialog and can be changed.
	 * <p>
	 * When started, the client tries to connect to the configured server
	 * instance via the network client.
	 */
	private static void startClient() {

		final ClientPropertiesDialog dialog = new ClientPropertiesDialog(
				properties);
		dialog.showDialog();

		final String serverAddress = properties.getServerAddress();
		final String serverPort = properties.getServerPort();

		// Fetch a DBAccess instance that encapsulates communication with the
		// server.
		DBAccess dbAccess = null;
		try {
			dbAccess = DBAccessFactory.getRemoteDBAccess(serverAddress,
					serverPort);
		} catch (final NetworkErrorException e) {
			System.err.println("Cannot start communication with the server: "
					+ e.getMessage());
			// Releasing recources is done in the shutdown hook
			System.exit(-1);
		} catch (final TechnicalErrorException e) {
			System.err.println("Cannot start client: " + e.getMessage());
			System.exit(-1);
		}

		if (dbAccess == null) {
			System.err.println("Cannot start remote access to the server");
			// Releasing recources is done in the shutdown hook
			System.exit(-1);
		}

		// BusinessService is initialized with remote DBAccess
		final BusinessService businessService = new BusinessService(dbAccess);
		final ClientController controller = new ClientController(
				businessService);
		final ClientView view = new ClientView(controller);
		view.setVisible(true);
	}

	/**
	 * Starts server and client components within this application instance.
	 * <p>
	 * Before the components are started, the relevant properties used by a
	 * standalone configuration are displayed and can be changed.
	 * <p>
	 * When started, client and server are run in the same VM and communicate
	 * directly, i.e. without the use of any of the network specific code.
	 */
	private static void startStandAlone() {

		final ServerPropertiesDialog dialog = new ServerPropertiesDialog(
				properties);
		dialog.showDialog();

		final String databaseLocation = properties.getDatabaseLocation();

		DBAccess dbAccess = null;
		try {
			dbAccess = DBAccessFactory.getLocalDBAccess(databaseLocation);
		} catch (final TechnicalErrorException e) {
			System.err.println("Cannot start stand alone application: "
					+ e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}

		// BusinessService is initialized with local DBAccess
		final BusinessService businessService = new BusinessService(dbAccess);
		final ClientController controller = new ClientController(
				businessService);
		final ClientView view = new ClientView(controller);
		view.setVisible(true);
	}

	/**
	 * Determines the application mode from the command line arguments provided
	 * to the application.
	 * <p>
	 * If the number of command line arguments is 2 or higher or if the
	 * specified command line argument does not correspond to an
	 * ApplicationMode, the application prints a short description of its usage
	 * and quits.
	 * 
	 * @param args
	 *            command line arguments
	 */
	private static void determineApplicationMode(String[] args) {

		if (args != null) {
			if (args.length == 0) {
				applicationMode = ApplicationMode.CLIENT; // default if no args
				// are given
			} else if (args.length == 1) {
				final String commandLineArg = args[0];
				for (ApplicationMode appMode : ApplicationMode.values()) {
					if (commandLineArg.equalsIgnoreCase(appMode
							.getCommandLineArg())) {
						applicationMode = appMode;
					}
				}
				if (applicationMode == null) {
					// commandLineArgument does not correspond to an
					// ApplicationMode
					printUsageAndExit();
				}
			} else {
				// there is more than 1 command line argument
				printUsageAndExit();
			}
		} else {
			// The specified commandLineArguments array is null.
			// Method parameter must have been chosen wrong. We better exit.
			printUsageAndExit();
		}
	}

	/**
	 * Creates an instance of the application properties and tries to load the
	 * values from the suncertify.properties file.
	 * <p>
	 * If the file does not exist, the properties are initialized with empty
	 * values.
	 */
	private static void loadProperties() {

		properties = new ApplicationProperties();
		try {
			properties.load();
		} catch (final IOException ignored) {
			// it is not a fatal error, if the property file cannot be read.
			// We only log the exception and ignore it.
			System.err.println(ignored.getMessage());
		}
	}

	/**
	 * Prints a short description about how to call the application and then
	 * stops execution.
	 */
	public static void printUsageAndExit() {
		System.out.println("usage: application [" + ApplicationMode.SERVER
				+ "|" + ApplicationMode.STANDALONE + "]");
		System.exit(0);
	}

	/**
	 * Returns the mode in which the application is running.
	 * 
	 * @return application mode
	 */
	public static ApplicationMode getApplicationMode() {
		return applicationMode;
	}
}
