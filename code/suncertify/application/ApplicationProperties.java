package suncertify.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Properties of the application, i.e. those used by client or server
 * components.
 * <p>
 * Provides support to load and save the properties in a special properties
 * file.
 * 
 * Provides named access methods to the properties.
 * 
 * @author Rasmus Kuschel
 */
public final class ApplicationProperties extends Properties {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -3187859155456059205L;

	/**
	 * Name of the properties file.
	 */
	public static final String PROPERTY_FILE = "suncertify.properties";

	/**
	 * Key for the database location property.
	 */
	public static final String DATABASE_LOCATION = "databaseLocation";

	/**
	 * Key for the server address property.
	 */
	public static final String SERVER_ADDRESS = "serverAddress";

	/**
	 * Key for the server port property.
	 */
	public static final String SERVER_PORT = "serverPort";

	/**
	 * Tries to load the properties from the properties file, if it exists. The
	 * file is searched in the current working directory.
	 * 
	 * @throws IOException
	 *             indicates an error during file handling.
	 */
	public void load() throws IOException {
		File file = new File(ApplicationProperties.PROPERTY_FILE);
		if (file.exists()) {
			FileInputStream fileInputStream = null;
			try {
				fileInputStream = new FileInputStream(file);
				load(fileInputStream);
			} finally {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			}
		}
	}

	/**
	 * Saves the properties to the properties file.
	 * <p>
	 * If the file does not exist, it is created in the working directory.
	 * 
	 * @throws IOException
	 *             indicates an error during file handling.
	 */
	public void save() throws IOException {
		File file = new File(ApplicationProperties.PROPERTY_FILE);
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			store(fileOutputStream, null);
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		}
	}

	/**
	 * Returns the value of the database location property.
	 * 
	 * @return database location property value
	 */
	public String getDatabaseLocation() {
		return getProperty(ApplicationProperties.DATABASE_LOCATION);
	}

	/**
	 * Sets the value of the database location property.
	 * 
	 * @param databaseLocation
	 *            new databaseLocation value.
	 */
	public void setDatabaseLocation(String databaseLocation) {
		setProperty(ApplicationProperties.DATABASE_LOCATION, databaseLocation);
	}

	/**
	 * Returns the value of the server address property.
	 * 
	 * @return server address property value
	 */
	public String getServerAddress() {
		return getProperty(ApplicationProperties.SERVER_ADDRESS);
	}

	/**
	 * Sets the value of the server address property.
	 * 
	 * @param serverAddress
	 *            new server address property value.
	 */
	public void setServerAddress(String serverAddress) {
		setProperty(ApplicationProperties.SERVER_ADDRESS, serverAddress);
	}

	/**
	 * Returns the value of the server port property.
	 * 
	 * @return server port property value.
	 */
	public String getServerPort() {
		return getProperty(ApplicationProperties.SERVER_PORT);
	}

	/**
	 * Sets the value of the server port property.
	 * 
	 * @param serverPort
	 *            new server port property value.
	 */
	public void setServerPort(String serverPort) {
		setProperty(ApplicationProperties.SERVER_PORT, serverPort);
	}
}