package suncertify.server;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * View of the server.
 * 
 * Provides a graphical interface that allows user to perform the use cases of
 * the server: stopping it.
 * 
 * @author Rasmus Kuschel
 */
public final class ServerView extends JFrame implements ActionListener {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -1096559810040837162L;

	/**
	 * Controller for this view.
	 */
	private ServerController controller;

	/**
	 * Creates a new server view.
	 * 
	 * @param controller
	 *            Controller for this view.
	 */
	public ServerView(ServerController controller) {
		super("Server");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(new Rectangle(new Dimension(200, 50)));

		setJMenuBar(createMenuBar());

		this.controller = controller;

		boolean started = controller.startServer();
		if (!started) {
			showError("Server not started!");
			// Releasing recources is done in the shutdown hook
			System.exit(-1);
		} else {
			showMessage("Server started");
		}

	}

	/**
	 * Creates the menu bar for this JFrame.
	 * 
	 * @return menu bar
	 */
	private JMenuBar createMenuBar() {

		JMenuItem quitMenuItem = new JMenuItem("Stop server");
		quitMenuItem.setActionCommand(ServerCommand.QUIT.getActionCommand());
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
	 *            Message to be displayed
	 */
	public void showMessage(String message) {
		JOptionPane.showConfirmDialog(this, message, "Information",
				JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Invoked when an action occurs.
	 * 
	 * @param event
	 *            Information about the action event.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		final String actionCommand = event.getActionCommand();
		if (ServerCommand.QUIT.getActionCommand().equals(actionCommand)) {
			controller.stopServer();
		}
	}
}
