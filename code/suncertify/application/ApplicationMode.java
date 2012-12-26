package suncertify.application;

/**
 * Enumeration of the application modes.
 * 
 * @author Rasmus Kuschel
 */
public enum ApplicationMode {

	/**
	 * Server mode. The server program must be run.
	 */
	SERVER("server"),
	/**
	 * Client mode. Network client and GUI must be run.
	 */
	CLIENT(null),
	/**
	 * Standalone mode. Database and GUI must run in the same VM and must
	 * perform no networking.
	 */
	STANDALONE("alone"), ;

	/**
	 * Command line argument indicating this mode must be used.
	 */
	private String commandLineArg;

	/**
	 * Constructor for ApplicationMode. Allows to specify a value for the
	 * command line argument corresponding to this mode.
	 * 
	 * @param commandLineArg
	 *            Command line argument corresponding to this mode.
	 */
	ApplicationMode(String commandLineArg) {
		this.commandLineArg = commandLineArg;
	}

	/**
	 * Returns the command line argument that corresponds to this mode.
	 * 
	 * @return corresponding command line argument
	 */
	public String getCommandLineArg() {
		return commandLineArg;
	}

	/**
	 * Checks if this enum value is SERVER
	 * 
	 * @return true if this enum value is SERVER
	 */
	public boolean isServer() {
		return ApplicationMode.SERVER == this;
	}

	/**
	 * Checks if this enum value is CLIENT
	 * 
	 * @return true if this enum value is CLIENT
	 */
	public boolean isClient() {
		return ApplicationMode.CLIENT == this;
	}

	/**
	 * Checks if this enum value is STANDALONE
	 * 
	 * @return true if this enum value is STANDALONE
	 */
	public boolean isStandalone() {
		return ApplicationMode.STANDALONE == this;
	}
}
