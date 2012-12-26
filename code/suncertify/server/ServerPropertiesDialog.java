package suncertify.server;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import suncertify.application.ApplicationProperties;

/**
 * Provides a dialog that displays the server properties and allows the user to
 * make changes.
 * 
 * The properties provided by the user are validated and stored in the
 * ApplicationProperties.
 * 
 * @author Rasmus Kuschel
 */
public final class ServerPropertiesDialog extends JPanel implements
		ActionListener {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -5730367839776734737L;

	/**
	 * ActionCommand value for the "Start server" button
	 */
	private static final String START_ACTION_COMMAND = "START";

	/**
	 * Label for the "Start" button
	 */
	private static final String START_LABEL = "Start";

	/**
	 * ActionCommand value for the "Exit" button
	 */
	private static final String EXIT_ACTION_COMMAND = "EXIT";

	/**
	 * Label for the "Exit" button
	 */
	private static final String EXIT_LABEL = "Exit";

	/**
	 * ActionCommand value for the "Browse" button
	 */
	private static final String BROWSE_ACTION_COMMAND = "BROWSE";

	/**
	 * Label for the "Browse" button
	 */
	private static final String BROWSE_LABEL = "...";

	/**
	 * Label for the Database location text field
	 */
	public static final String DATABASE_LOCATION_LABEL = "Database location:";

	/**
	 * Tooltip text for the Database location text field
	 */
	private static final String DATABASE_LOCATION_TOOLTIP = "Enter the path and name of the data file";

	/**
	 * Text field to input the database location property
	 */
	private final JTextField databaseLocationTextField;

	/**
	 * Starts a file chooser dialog to select a database file
	 */
	private final JButton browseButton;

	/**
	 * Dialog to present the properties
	 */
	private JDialog dialog;

	/**
	 * ApplicationProperties instance to retrieve and store the properties
	 */
	private final ApplicationProperties properties;

	/**
	 * Creates a new ServerPropertiesDialog instance that operates on the
	 * specified ApplicationProperties.
	 * 
	 * @param properties
	 *            ApplicationProperties to store the properties
	 */
	public ServerPropertiesDialog(ApplicationProperties properties) {
		this.properties = properties;

		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(2, 2, 2, 2);

		JLabel databaseLocationLabel = new JLabel(DATABASE_LOCATION_LABEL);
		layout.setConstraints(databaseLocationLabel, constraints);
		add(databaseLocationLabel);

		databaseLocationTextField = new JTextField(properties
				.getDatabaseLocation(), 40);
		databaseLocationTextField.setName(DATABASE_LOCATION_LABEL);
		databaseLocationTextField.setToolTipText(DATABASE_LOCATION_TOOLTIP);
		layout.setConstraints(databaseLocationTextField, constraints);
		add(databaseLocationTextField);

		browseButton = new JButton(BROWSE_LABEL);
		browseButton.setActionCommand(BROWSE_ACTION_COMMAND);
		browseButton.addActionListener(this);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(browseButton, constraints);
		add(browseButton);
	}

	/**
	 * Displays the dialog.
	 */
	public void showDialog() {
		JOptionPane pane = new JOptionPane(this, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		dialog = pane.createDialog("Server properties");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JButton startButton = new JButton(START_LABEL);
		startButton.setActionCommand(START_ACTION_COMMAND);
		startButton.addActionListener(this);

		JButton exitButton = new JButton(EXIT_LABEL);
		exitButton.setActionCommand(EXIT_ACTION_COMMAND);
		exitButton.addActionListener(this);

		pane.setOptions(new Object[] { startButton, exitButton });

		dialog.setVisible(true);
	}

	/**
	 * Validates the content of the database location text field
	 * 
	 * @return true if the content is a valid database location
	 */
	public boolean validateDatabaseLocation() {

		final String databaseLocation = databaseLocationTextField.getText();
		final File file = new File(databaseLocation);
		final boolean valid = file.exists();

		return valid;
	}

	/**
	 * Interrupts the server startup process and stops this application
	 * instance.
	 */
	private void exitAndDoNotStartServer() {
		// Releasing recources is done in the shutdown hook
		System.exit(0);
	}

	/**
	 * Opens a FileChooser dialog that allows the user to select a data file.
	 */
	private void browseForDatabase() {
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));

		// if the user selected a file, update the file name on screen
		if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(null)) {
			databaseLocationTextField.setText(chooser.getSelectedFile()
					.toString());
		}
	}

	/**
	 * Stores the properties and continues the server startup process.
	 */
	private void continueAndStartServer() {

		final String databaseLocation = databaseLocationTextField.getText();
		properties.setDatabaseLocation(databaseLocation);

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
	 *            Information about the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		final String actionCommand = e.getActionCommand();

		if (EXIT_ACTION_COMMAND.equals(actionCommand)) {
			exitAndDoNotStartServer();
		} else if (BROWSE_ACTION_COMMAND.equals(actionCommand)) {
			browseForDatabase();
		} else if (START_ACTION_COMMAND.equals(actionCommand)) {
			boolean valid = validateDatabaseLocation();
			if (valid) {
				continueAndStartServer();
			} else {
				JOptionPane.showConfirmDialog(this,
						"The database location is not a valid file", "Error",
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
