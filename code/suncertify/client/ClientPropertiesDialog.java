package suncertify.client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import suncertify.application.ApplicationProperties;

/**
 * Provides a dialog that displays the client properties and allows the user to
 * make changes.
 * <p>
 * The properties provide by the user are validated and stored in the
 * ApplicationProperties.
 * 
 * @author Rasmus Kuschel
 */
public final class ClientPropertiesDialog extends JPanel implements
		ActionListener {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -4304434588284449223L;

	/**
	 * ActionCommand value for the "Connect to server" button
	 */
	private static final String CONNECT_ACTION_COMMAND = "CONNECT";

	/**
	 * Label for the "Connect to server" button
	 */
	private static final String CONNECT_LABEL = "Connect to server";

	/**
	 * ActionCommand value for the "Exit" button
	 */
	private static final String EXIT_ACTION_COMMAND = "EXIT";

	/**
	 * Label for the "Exit" button
	 */
	private static final String EXIT_LABEL = "Exit";

	/**
	 * Label for the server address text field
	 */
	private static final String SERVER_ADDRESS_LABEL = "Server address:";

	/**
	 * Tooltip text for the server address text field
	 */
	private static final String SERVER_ADDRESS_TOOLTIP = "Enter the host part of the RMI server's address";

	/**
	 * Text field to input the server address
	 */
	private final JTextField serverAddressTextField;

	/**
	 * Label for the server port text field
	 */
	private static final String SERVER_PORT_LABEL = "Server port:";

	/**
	 * Tooltip text for the server port text field
	 */
	private static final String SERVER_PORT_TOOLTIP = "Enter the port of the RMI server";

	/**
	 * Text field to input the server port
	 */
	private final JTextField serverPortTextField;

	/**
	 * Dialog to present the properties.
	 */
	private JDialog dialog;

	/**
	 * ApplicationProperties instance to retrieve and store the properties
	 */
	private final ApplicationProperties properties;

	/**
	 * Creates a new ClientPropertiesDialog instance that operates on the
	 * specified ApplicationProperties.
	 * 
	 * @param properties
	 *            ApplicationProperties to store the properties
	 */
	public ClientPropertiesDialog(ApplicationProperties properties) {
		this.properties = properties;

		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(2, 2, 2, 2);

		JLabel serverAddressLabel = new JLabel(SERVER_ADDRESS_LABEL);
		layout.setConstraints(serverAddressLabel, constraints);
		add(serverAddressLabel);

		serverAddressTextField = new JTextField(properties.getServerAddress(),
				40);
		serverAddressTextField.setName(SERVER_ADDRESS_LABEL);
		serverAddressTextField.setToolTipText(SERVER_ADDRESS_TOOLTIP);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(serverAddressTextField, constraints);
		add(serverAddressTextField);

		JLabel serverPortLabel = new JLabel(SERVER_PORT_LABEL);
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(serverPortLabel, constraints);
		add(serverPortLabel);

		serverPortTextField = new JTextField(properties.getServerPort(), 40);
		serverPortTextField.setName(SERVER_PORT_LABEL);
		serverPortTextField.setToolTipText(SERVER_PORT_TOOLTIP);
		layout.setConstraints(serverPortTextField, constraints);
		add(serverPortTextField);
	}

	/**
	 * Displays the dialog.
	 */
	public void showDialog() {
		JOptionPane pane = new JOptionPane(this, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		dialog = pane.createDialog("Client properties");
		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

		JButton connectButton = new JButton(CONNECT_LABEL);
		connectButton.setActionCommand(CONNECT_ACTION_COMMAND);
		connectButton.addActionListener(this);

		JButton exitButton = new JButton(EXIT_LABEL);
		exitButton.setActionCommand(EXIT_ACTION_COMMAND);
		exitButton.addActionListener(this);

		pane.setOptions(new Object[] { connectButton, exitButton });

		dialog.setVisible(true);
	}

	/**
	 * Validates the content of the server address text field
	 * 
	 * @return true if the content is a valid server address
	 */
	public boolean validateServerAddress() {

		final String serverAddress = "http://"
				+ serverAddressTextField.getText();
		try {
			new URL(serverAddress);
		} catch (final MalformedURLException e) {
			return false;
		}

		return true;
	}

	/**
	 * Validates the content of the server port text field
	 * 
	 * @return true if the content is a valid server port
	 */
	public boolean validateServerPort() {
		final String serverPort = serverPortTextField.getText();
		try {
			Integer.parseInt(serverPort);
		} catch (final NumberFormatException e) {
			return false;
		}

		return true;

	}

	/**
	 * Interrupts the client startup process and stops this application
	 * instance.
	 */
	private void exitAndDoNotStartClient() {
		// Releasing recources is done in the shutdown hook
		System.exit(0);
	}

	/**
	 * Stores the properties and continues the client startup process
	 */
	private void connectToServer() {
		final String serverAddress = serverAddressTextField.getText();
		final String serverPort = serverPortTextField.getText();

		properties.setServerAddress(serverAddress);
		properties.setServerPort(serverPort);

		try {
			properties.save();
		} catch (final IOException e) {
			// properties cannot be persisted. This is not a fatal exception.
			// We simply log it and continue.
			System.err.println("Cannot store application properties: "
					+ e.getMessage());
		}

		dialog.setVisible(false);
	}

	/**
	 * Invoked when an action occurs.
	 * <p>
	 * Dispatches to the respective handling methods.
	 * 
	 * @param e
	 *            information about the action event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		final String actionCommand = e.getActionCommand();
		if (EXIT_ACTION_COMMAND.equals(actionCommand)) {
			exitAndDoNotStartClient();
		} else if (CONNECT_ACTION_COMMAND.equals(actionCommand)) {
			if (!validateServerAddress()) {
				JOptionPane.showConfirmDialog(this,
						"The server address is not valid", "Error",
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			} else if (!validateServerPort()) {
				JOptionPane.showConfirmDialog(this,
						"The server port is not valid", "Error",
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			} else {
				connectToServer();
			}
		}
	}
}
