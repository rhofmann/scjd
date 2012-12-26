package suncertify.client;

/**
 * Enumeration of the commands that can be triggered in the {@code ClientView}.
 * <p>
 * Each element has an associated actionCommand value that can be used by
 * {@code ActionListener} instances to identify an event source.
 * 
 * @author Rasmus Kuschel
 */
public enum ClientCommand {

	/**
	 * Command to show all data records.
	 */
	SHOW_ALL("showall"),
	/**
	 * Command to search for data records.
	 */
	SEARCH("search"),
	/**
	 * Command to book a record.
	 */
	BOOK("book"),
	/**
	 * Command to quit the client.
	 */
	QUIT("quit");

	/**
	 * ActionCommand value associated with the ClientCommand instance.
	 */
	private final String actionCommand;

	/**
	 * Creates a new ClientCommand with the specified ActionCommand value.
	 * 
	 * @param actionCommand
	 *            ActionCommand value of this ClientCommand.
	 */
	private ClientCommand(String actionCommand) {
		this.actionCommand = actionCommand;
	}

	/**
	 * Returns the ActionCommand value associated with this ClientCommand.
	 * 
	 * @return associated ActionCommand value
	 */
	public String getActionCommand() {
		return actionCommand;
	}
}
