package suncertify.server;

/**
 * Enumeration of the commands that can be triggered in the {@code ServerView}.
 * 
 * Each element has an associated ActionCommand value that can be used by
 * {@code ActionListener} instances to identify an event source.
 * 
 * @author Rasmus Kuschel
 */
public enum ServerCommand {

	/**
	 * Command to stop the server.
	 */
	QUIT("quit");

	/**
	 * ActionCommand value associated with the ServerCommand instance.
	 */
	private final String actionCommand;

	/**
	 * Creates a new ServerCommand with the specified ActionCommand value.
	 * 
	 * @param actionCommand
	 *            ActionCommand value of this ServerCommand.
	 */
	private ServerCommand(String actionCommand) {
		this.actionCommand = actionCommand;
	}

	/**
	 * Returns the ActionCommand value associated with this ServerCommand.
	 * 
	 * @return associated ActionCommand value
	 */
	public String getActionCommand() {
		return actionCommand;
	}
}
