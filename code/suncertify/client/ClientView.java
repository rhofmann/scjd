package suncertify.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import suncertify.db.RecordAlreadyBookedException;
import suncertify.db.RecordNotFoundException;
import suncertify.db.TechnicalErrorException;
import suncertify.db.domain.DataRecord;

/**
 * View of the client.
 * <p>
 * Provides a graphical interface that allow users to perform the use cases of
 * the client: showing all records, searching for records and booking a record.
 * The result of each operation is displayed in a {@code JTable} instance.
 * 
 * @author Rasmus Kuschel
 */
public final class ClientView extends JFrame implements ActionListener {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 3616708815040884656L;

	/**
	 * Label of the "show all" button
	 */
	private static final String LABEL_SHOWALL_BUTTON = "show all";

	/**
	 * Tooltip text for the "show all" button
	 */
	private static final String TOOLTIP_SHOWALL_BUTTON = "Click to show all contractors";

	/**
	 * Label of the "search" button
	 */
	private static final String LABEL_SEARCH_BUTTON = "search";

	/**
	 * Tooltip text of the "search button"
	 */
	private static final String TOOLTIP_SEARCH_BUTTON = "Click to search for contractors with the given name and/or location";

	/**
	 * Label for the location text field
	 */
	private static final String LABEL_LOCATION_TEXTFIELD = "Location";

	/**
	 * Tooltip text of the location text field
	 */
	private static final String TOOLTIP_LOCATION_TEXTFIELD = "Enter the location of contractors you would like to search for";

	/**
	 * Label for the name text field
	 */
	private static final String LABEL_NAME_TEXTFIELD = "Name";

	/**
	 * Tooltip text of the name text field
	 */
	private static final String TOOLTIP_NAME_TEXTFIELD = "Enter the name of a contractor you would like to search for";

	/**
	 * Tooltip text of the result table.
	 */
	private static final String TOOLTIP_TABLE = "This table displays the result of the last search operation. Doubleclick on an entry to book it.";

	/**
	 * Message used to ask to confirmation about booking a record
	 */
	private static final String BOOK_CONFIRMATION = "Do you want to book the selected record?";

	/**
	 * Title of the frame.
	 */
	private static final String FRAME_TITLE = "Bodgitt and Scarper, LLC";

	/**
	 * Textfield for the name search parameter
	 */
	private JTextField nameTextField;

	/**
	 * Textfield for the location search parameter
	 */
	private JTextField locationTextField;

	/**
	 * Button to start the search operation
	 */
	private JButton searchButton;

	/**
	 * Button to reset the search parameters and show all data records
	 */
	private final JButton showAllButton;

	/**
	 * Table to present search results
	 */
	private final JTable resultTable;

	/**
	 * Controller for this view.
	 */
	private ClientController controller;

	/**
	 * Creates a new client view.
	 * 
	 * @param controller
	 *            Controller for this view
	 */
	public ClientView(ClientController controller) {
		super(FRAME_TITLE);
		this.controller = controller;
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		BorderLayout layout = new BorderLayout();
		setLayout(layout);

		resultTable = createTable();
		resultTable.setToolTipText(TOOLTIP_TABLE);
		JScrollPane scrollPane = new JScrollPane(resultTable);
		scrollPane.setViewportBorder(null);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(5, 5, 5, 5), BorderFactory
				.createEtchedBorder()));
		add(scrollPane, BorderLayout.NORTH);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridLayout panelLayout = new GridLayout(2, 3);
		panelLayout.setVgap(5);
		panelLayout.setHgap(5);
		panel.setLayout(panelLayout);

		JLabel nameLabel = new JLabel(LABEL_NAME_TEXTFIELD);
		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(nameLabel);

		nameTextField = new JTextField();
		nameLabel.setLabelFor(nameTextField);
		nameTextField.setToolTipText(TOOLTIP_NAME_TEXTFIELD);
		panel.add(nameTextField);

		searchButton = new JButton(LABEL_SEARCH_BUTTON);
		searchButton.setActionCommand(ClientCommand.SEARCH.getActionCommand());
		searchButton.addActionListener(this);
		searchButton.setToolTipText(TOOLTIP_SEARCH_BUTTON);
		panel.add(searchButton);

		JLabel locationLabel = new JLabel(LABEL_LOCATION_TEXTFIELD);
		locationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(locationLabel);

		locationTextField = new JTextField();
		locationLabel.setLabelFor(locationTextField);
		locationTextField.setToolTipText(TOOLTIP_LOCATION_TEXTFIELD);
		panel.add(locationTextField);

		showAllButton = new JButton(LABEL_SHOWALL_BUTTON);
		showAllButton.setActionCommand(ClientCommand.SHOW_ALL
				.getActionCommand());
		showAllButton.addActionListener(this);
		showAllButton.setToolTipText(TOOLTIP_SHOWALL_BUTTON);
		panel.add(showAllButton);

		panel.setFocusTraversalPolicy(createPanelFocusTraversalPolicy());
		panel.setFocusTraversalPolicyProvider(true);
		add(panel, BorderLayout.SOUTH);

		JMenuBar menubar = createMenuBar();
		setJMenuBar(menubar);
		pack();

		// Center on screen
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((d.getWidth() - this.getWidth()) / 2);
		int y = (int) ((d.getHeight() - this.getHeight()) / 2);
		this.setLocation(x, y);
		this.setVisible(true);
	}

	/**
	 * Creates a JTable instance that is used to display the results of the
	 * clients actions.
	 * 
	 * @return JTable instance to display results
	 */
	private JTable createTable() {

		Object[] columnNames = ClientModel.COLUMN_NAMES;
		TableModel model = new DefaultTableModel(columnNames, 20);

		final JTable table = new JTable(model);
		table.setPreferredScrollableViewportSize(new Dimension(900, 500));
		table.setFillsViewportHeight(true);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// double click
				if (e.getClickCount() == 2) {
					makeBooking();
				}
			}
		});

		table.setCellSelectionEnabled(false);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setDragEnabled(false);

		return table;
	}

	/**
	 * Creates the menu bar for this JFrame.
	 * 
	 * @return menu bar
	 */
	private JMenuBar createMenuBar() {

		JMenuItem quitMenuItem = new JMenuItem("Quit");
		quitMenuItem.setActionCommand(ClientCommand.QUIT.getActionCommand());
		quitMenuItem.setMnemonic('Q');
		quitMenuItem.addActionListener(this);

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		fileMenu.add(quitMenuItem);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);

		return menuBar;
	}

	/**
	 * Creates a focus traversal policy for the client view's panel.
	 * <p>
	 * For ease of use, the components of the panel need to receive focus in a
	 * different order as the default ordering.
	 * 
	 * @return focus traversal policy for the panel
	 */
	private FocusTraversalPolicy createPanelFocusTraversalPolicy() {

		final FocusTraversalPolicy policy = new FocusTraversalPolicy() {
			// focus ordering of the panel components
			private final Component[] order = { nameTextField,
					locationTextField, searchButton, showAllButton };

			@Override
			public Component getLastComponent(Container arg0) {
				return order[order.length - 1];
			}

			@Override
			public Component getFirstComponent(Container arg0) {
				return order[0];
			}

			@Override
			public Component getDefaultComponent(Container arg0) {
				return order[0];
			}

			@Override
			public Component getComponentBefore(Container container,
					Component component) {
				for (int i = 0; i < order.length; i++) {
					if (order[i].equals(component)) {
						if (i == 0) {
							return order[order.length - 1];
						} else {
							return order[i - 1];
						}
					}
				}
				return null;
			}

			@Override
			public Component getComponentAfter(Container container,
					Component component) {
				for (int i = 0; i < order.length; i++) {
					if (order[i].equals(component)) {
						if (i == order.length - 1) {
							return order[0];
						} else {
							return order[i + 1];
						}
					}
				}
				return null;
			}
		};

		return policy;
	}

	/**
	 * Invoked when an action occurs.
	 * <p>
	 * Dispatches control to the respective command handler methods.
	 * 
	 * @param event
	 *            Information about the action event
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		final String actionCommand = event.getActionCommand();
		if (ClientCommand.SEARCH.getActionCommand().equals(actionCommand)) {
			search();
		} else if (ClientCommand.SHOW_ALL.getActionCommand().equals(
				actionCommand)) {
			showAll();
		} else if (ClientCommand.QUIT.getActionCommand().equals(actionCommand)) {
			quit();
		} else {
			showError("Unknown command");
		}
	}

	/**
	 * Command handler method for the "search records" command.
	 * <p>
	 * Prepares the parameters for the business method call, executes it and
	 * processes its result. The view is updates with the data of the new model.
	 */
	private void search() {

		// Fetch search criteria from the text fields
		String name = nameTextField.getText();
		String location = locationTextField.getText();

		// Empty strings as parameter values means we do not filter records
		// according to the respective attibute.
		// The searchRecords method needs to be passed null in this case.
		if ("".equals(name)) {
			name = null;
		}
		if ("".equals(location)) {
			location = null;
		}

		try {
			// Call to the controller business method
			final ClientModel model = controller.searchRecords(name, location);

			// Update the model and force a repaint of the component
			resultTable.setModel(model);
			invalidate();
		} catch (final TechnicalErrorException e) {
			// If a technical error occurs, display the error in a dialog
			showError(e.getMessage());
		}
	}

	/**
	 * Command handler method for the "show all records" command.
	 */
	private void showAll() {

		// Reset text fields
		nameTextField.setText("");
		locationTextField.setText("");

		try {
			// Call to the business method
			final ClientModel model = controller.retrieveAllRecords();

			// Update the model and force a repaint of the component
			resultTable.setModel(model);
			invalidate();
		} catch (final TechnicalErrorException e) {
			// If a technical error occurs, display the error in a dialog
			showError(e.getMessage());
		}
	}

	/**
	 * Command handler method for the "book a record" command.
	 */
	private void makeBooking() {

		// Determine the record the should be booked from the table.
		int selectedRow = resultTable.getSelectedRow();
		if (selectedRow == -1) {
			showMessage("There is no selected record");
			return;
		}

		TableModel model = resultTable.getModel();
		if (model instanceof ClientModel) {
			ClientModel clientModel = (ClientModel) model;
			DataRecord record = clientModel.getDataRecord(selectedRow);

			// Ask for confirmation in a dialog.
			boolean confirmBooking = showConfirm(BOOK_CONFIRMATION);
			if (!confirmBooking) {
				// Answer to confirmation is no, so do nothing
				return;
			}

			try {
				// Call to the business method
				ClientModel updatedModel = controller.bookRecord(record,
						clientModel);
				resultTable.setModel(updatedModel);

				// If no error occured, the record was booked
				showMessage("The record was successfully booked");
			} catch (final RecordAlreadyBookedException e) {
				// May occur, if another client has already booked the record
				showError("This record is already booked.");
			} catch (final RecordNotFoundException e) {
				// May occur, if another client has deleted the record
				showError("The record cannot be found. It may have been deleted by another client.");
			} catch (final TechnicalErrorException e) {
				// Some general technical error occured, e.g. networking or file
				// problems.
				showError(e.getMessage());
			} finally {
				// force a repaint of the component
				invalidate();
			}
		}
	}

	/**
	 * Stops this application.
	 */
	private void quit() {
		System.exit(0);
	}

	/**
	 * Display a confirm message in a modal dialog.
	 * 
	 * @param message
	 *            Message to be displayed
	 * @return true if the confirm option was chosen
	 */
	public boolean showConfirm(String message) {
		int chosenOption = JOptionPane.showConfirmDialog(this, message,
				"Confirmation", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		return JOptionPane.YES_OPTION == chosenOption;
	}

	/**
	 * Displays an error message in a modal dialog.
	 * 
	 * @param message
	 *            Message to be displayed
	 */
	public void showError(String message) {
		JOptionPane.showConfirmDialog(this, message, "Error",
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays a message in a modal dialog.
	 * 
	 * @param message
	 *            s * Message to be displayed
	 */
	public void showMessage(String message) {
		JOptionPane.showConfirmDialog(this, message, "Message",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
	}
}
