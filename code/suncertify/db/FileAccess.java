package suncertify.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import suncertify.db.domain.DataRecord;
import suncertify.db.domain.DataRecordState;
import suncertify.db.domain.FileMetaData;
import suncertify.db.domain.SchemaDescription;

/**
 * Component used to manage the low-level accesses to the data file.
 * <p>
 * A {@code FileAccess} instance must be initialized before it is used by
 * invoking its {@code openFile} method. The initialization opens a {@code
 * RandomAccessFile} instance for read/write access and validates the schema
 * information in the data file.
 * <p>
 * Trying to invoke any access methods before the initialization completes
 * results in an {@code IllegalStateException} to be thrown.
 * 
 * @author Rasmus Kuschel
 */
public final class FileAccess {

	/**
	 * Name of the charset used for character conversion
	 */
	private static final String CHARSET = "US-ASCII";

	/**
	 * RandomAccessFile instance used to access the data file.
	 * <p>
	 * It is appropriate to use a RandomAccessFile since the numeric values
	 * stored in the header information use the format of the DataInputStream
	 * and DataOutputStream classes as does the RandomAccessFile class.
	 */
	private static RandomAccessFile dbFile;

	/**
	 * Metadata of the file used by this file manager instance
	 */
	private static FileMetaData activeFileMetaData;

	/**
	 * Opens the file to be used by this file access instance.
	 * <p>
	 * An IOException may be thrown if an error occurs while accessing the file.
	 * The metadata of the db file (magic cookie value, schema descriptions
	 * etc.) is read, validated (e.g. expected magic cookie value) and stored
	 * for use in following requests.
	 * 
	 * @param databaseLocation
	 *            path to the data file
	 * 
	 * @throws IOException
	 *             if any I/O operation fails
	 */
	public static synchronized void openFile(String databaseLocation)
			throws IOException {

		// If the dbFile was already opened for another datafile throw a
		// technical error exception
		if (dbFile != null
				&& !activeFileMetaData.getDataFilePath().equals(
						databaseLocation)) {
			throw new TechnicalErrorException(
					"Cannot change database location in FileAccess");
		}

		// Open a dbFile, if none was opened yet
		if (dbFile == null) {
			File file = new File(databaseLocation);
			// Open RandomAccessFile for read/write operations
			dbFile = new RandomAccessFile(file, "rw");

			validateFile(databaseLocation);
		}
	}

	/**
	 * Closes the file used by this file access instance.
	 * <p>
	 * An IOException may be thrown if an error occurs while accessing the file.
	 * 
	 * @throws IOException
	 *             if any I/O operation fails
	 */
	public static void closeFile() throws IOException {
		if (dbFile != null) {
			dbFile.close();
		}
	}

	/**
	 * Reads the schema information from the data file and validates it against
	 * the fixed schema supplied for this application.
	 * <p>
	 * If the generic schema description from the file does not conform to the
	 * application's schema, a {@code CorruptDatabaseException} is thrown. If
	 * the schema description from the file is successfully validated, the
	 * schema information is stored in the {@code activeFileMetaData} field.
	 * <p>
	 * No explicit synchronisation of the file object is necessary, since this
	 * method is called only from within a synchronized method during
	 * initialization.
	 * 
	 * @param databaseLocation
	 *            location of the data file
	 * @throws IOException
	 *             if any I/O operation fails
	 * @throws CorruptDatabaseException
	 *             if the schema information from the data file does not conform
	 *             to the fixed schema of the assignment.
	 */
	private static void validateFile(final String databaseLocation)
			throws IOException, CorruptDatabaseException {

		// validate magic cookie value in the data file
		final int magicCookieValue = dbFile.readInt();
		if (FileMetaData.EXPECTED_MAGIC_COOKIE_VALUE != magicCookieValue) {
			throw new CorruptDatabaseException(
					"corrupt data file: invalid magic cookie value");
		}

		final int offset = dbFile.readInt();
		if (FileMetaData.EXPECTED_OFFSET != offset) {
			throw new CorruptDatabaseException(
					"corrupt data file: invalid offset value");
		}

		final short fieldCount = dbFile.readShort();
		if (FileMetaData.EXPECTED_FIELD_COUNT != fieldCount) {
			throw new CorruptDatabaseException(
					"corrupt data file: invalid field count value");
		}

		// read all schema descriptions from the data file header and compare
		// the value to the expected values
		final SchemaDescription[] schemaDescriptions = new SchemaDescription[fieldCount];
		for (int i = 0; i < fieldCount; i++) {
			final short fieldNameLength = dbFile.readShort();
			final byte[] fieldNameBytes = new byte[fieldNameLength];
			for (int j = 0; j < fieldNameLength; j++) {
				fieldNameBytes[j] = dbFile.readByte();
			}
			final String fieldName = new String(fieldNameBytes, CHARSET);
			if (!FileMetaData.EXPECTED_FIELD_NAMES[i].equals(fieldName)) {
				throw new CorruptDatabaseException(
						"corrupt data file: unexpected field name value "
								+ fieldName + " at position " + i);
			}
			final short fieldLength = dbFile.readShort();
			if (FileMetaData.EXPECTED_FIELD_LENGTHS[i] != fieldLength) {
				throw new CorruptDatabaseException(
						"corrupt data file: unexpected field length "
								+ fieldLength + " at position " + i);
			}
			schemaDescriptions[i] = new SchemaDescription(fieldName,
					fieldLength);
		}

		activeFileMetaData = new FileMetaData(databaseLocation,
				magicCookieValue, offset, fieldCount, schemaDescriptions);
	}

	/**
	 * Reads the record at the given position.
	 * <p>
	 * Throws an {@code IOException} if an I/O error occurs while accessing the
	 * file. An {@code IllegalStateException} may be thrown if the FileManager
	 * has not been initialized correctly, e.g. if no file to open has been
	 * selected.
	 * 
	 * @param recNo
	 *            position of the record to read
	 * @return content of the record at the given position
	 * @throws IOException
	 *             error while accessing the file
	 */
	public DataRecord readRecord(long recNo) throws IOException {

		if (dbFile == null) {
			throw new IllegalStateException("no active file for access");
		}

		// We have to synchronize on dbFile to ensure that no concurrent
		// operations change the file pointer during this read operation
		synchronized (dbFile) {

			final String[] data = new String[activeFileMetaData.getFieldCount()];

			final int pos = activeFileMetaData.getRecordOffset(recNo);
			dbFile.seek(pos);

			final short flag = dbFile.readShort();

			for (int i = 0; i < activeFileMetaData.getFieldCount(); i++) {
				final short fieldLength = activeFileMetaData.getFieldLength(i);
				final byte[] fieldValueBytes = new byte[fieldLength];
				dbFile.readFully(fieldValueBytes);

				final String fieldValue = new String(fieldValueBytes, CHARSET);
				data[i] = fieldValue;
			}

			final DataRecord dataRecord = new DataRecord(recNo, data);

			final DataRecordState state = DataRecordState.forValue(flag);
			dataRecord.setState(state);

			return dataRecord;
		}
	}

	/**
	 * Writes the specified record to the specified position in the data file.
	 * <p>
	 * Throws an IOException if an error occurs while accessing the file. An
	 * IllegalStateException may be thrown if the FileManager has not been
	 * initialized correctly, e.g. if no file to open has been selected.
	 * 
	 * @param recNo
	 *            position of the record to be written
	 * @param record
	 *            content of the record to be written
	 * @throws IOException
	 *             error while accessing the file
	 */
	public void writeRecord(long recNo, DataRecord record) throws IOException {

		if (dbFile == null) {
			throw new IllegalStateException("no active file for access");
		}

		// We have to synchronize on dbFile, to ensure that no concurrent
		// operations change the file pointer during this write operation
		synchronized (dbFile) {
			final int pos = activeFileMetaData.getRecordOffset(recNo);
			dbFile.seek(pos);

			dbFile.writeShort(record.getState().getEncoding());
			final String[] data = record.getData();

			for (int i = 0; i < activeFileMetaData.getFieldCount(); i++) {
				final short fieldLength = activeFileMetaData.getFieldLength(i);
				final byte[] fieldValueBytes = data[i].getBytes(CHARSET);

				// write exactly "fieldLength" bytes.
				// If fieldValueBytes is longer, skip the last bytes.
				// If fieldValueBytes is shorter, fill with blanks (0x0020)
				for (int written = 0; written < fieldLength; written++) {
					if (written < fieldValueBytes.length) {
						dbFile.writeByte(fieldValueBytes[written]);
					} else {
						dbFile.writeByte(0x020);
					}
				}
			}
		}
	}

	/**
	 * Determines the number of records in the file.
	 * 
	 * @return number of records in the file.
	 */
	private int getRecordCount() throws IOException {

		if (dbFile == null) {
			throw new IllegalStateException("no active file for access");
		}

		// We have to synchronize on dbFile to ensure that no concurrent
		// operations change the file pointer during this read operation
		synchronized (dbFile) {
			long fileSize = dbFile.length();

			// calculate the number of bytes occupied by the data records, i.e.
			// size of the entire file minus the header size
			int payloadSize = (int) fileSize - activeFileMetaData.getOffset();

			// divide by the size of one record, to get the number of records
			int recordCount = payloadSize
					/ activeFileMetaData.getRecordLength();

			return recordCount;
		}
	}

	/**
	 * Returns a list of all data records in the data file
	 * 
	 * @return list of all records
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public List<DataRecord> readAllRecords() throws IOException {

		final int recordCount = getRecordCount();
		final List<DataRecord> allRecords = new ArrayList<DataRecord>();

		for (int i = 0; i < recordCount; i++) {
			DataRecord record = readRecord(i);
			allRecords.add(record);
		}

		return allRecords;
	}

	/**
	 * Checks if the specified record number is valid, i.e. if it points to a
	 * record inside the data file.
	 * 
	 * @param recNo
	 *            record number
	 * @return true if the record number points to a record inside the data file
	 */
	public boolean isValidRecordNumber(long recNo) {
		long recCount = -1;
		try {
			recCount = getRecordCount();
		} catch (final IOException ignore) {
			System.out.println(ignore.getMessage());
			// ignore I/O exception
		}

		return recNo < recCount;
	}

	/**
	 * Checks if the record with the specified record number is deleted.
	 * 
	 * @param recNo
	 *            number of the record
	 * @return true if the record is deleted
	 */
	public boolean isDeleted(long recNo) {
		try {
			DataRecord record = readRecord(recNo);
			return record.isDeleted();
		} catch (IOException e) {
			return true;
		}
	}

	/**
	 * Returns the meta deta of the active file.
	 * 
	 * @return active file's meta data
	 */
	public static FileMetaData getActiveFileMetaData() {
		return activeFileMetaData;
	}
}