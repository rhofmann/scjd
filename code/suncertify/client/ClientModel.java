package suncertify.client;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import suncertify.db.domain.DataRecord;

/**
 * Model of the data records used by the client view frame.
 * <p>
 * {@code ClientModel} implements the {@code TableModel} interface. An instance
 * of this class can be created from a list of data records and can be passed as
 * the model to a {@code JTable} component.
 * 
 * @author Rasmus Kuschel
 */
public final class ClientModel implements TableModel {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -1380316774207010301L;

	/**
	 * Column names of the search result table.
	 */
	public static final String[] COLUMN_NAMES = { "Name", "Location",
			"Specialties", "Size", "Rate", "Owner" };

	/**
	 * Records that are represented by this table model
	 */
	private List<DataRecord> records;

	/**
	 * List of all listeners that need to be modified about model updates.
	 */
	private List<TableModelListener> listeners;

	/**
	 * Constructs a new table model that contains the data of the specified
	 * records
	 * 
	 * @param records
	 *            Records that are represented by this table model
	 */
	public ClientModel(List<DataRecord> records) {
		this.records = records;
		this.listeners = new ArrayList<TableModelListener>();
	}

	/**
	 * Returns the data record at the specified position.
	 * <p>
	 * If no data records are present or there is no data record at the
	 * specified index, null is returned.
	 * 
	 * @param index
	 *            index of the data record
	 * @return data record at the specified index or null if it does not exist
	 */
	public DataRecord getDataRecord(int index) {
		DataRecord record = null;
		if (records != null && records.size() > index) {
			record = records.get(index);
		}
		return record;
	}

	/**
	 * Returns the list of data record wrapped in the model.
	 * 
	 * @return list of data records
	 */
	public List<DataRecord> getRecords() {
		return records;
	}

	/**
	 * Updates the collection of data records.
	 * <p>
	 * If the collection contains a data record with the same record number as
	 * that of the specified data record, the values of the specified data
	 * record are transferred into the collection.
	 * 
	 * Listeners of this model instance are notified of the update.
	 * 
	 * @param updateRecord
	 *            Data record instance
	 */
	public void updateRecord(DataRecord updateRecord) {
		final long recNo = updateRecord.getRecNo();
		if (records != null) {
			for (DataRecord record : records) {
				if (record.getRecNo() == recNo) {
					record.setName(updateRecord.getName());
					record.setLocation(updateRecord.getLocation());
					record.setSpecialties(updateRecord.getSpecialties());
					record.setRate(updateRecord.getRate());
					record.setSize(updateRecord.getSize());
					record.setOwner(updateRecord.getOwner());
				}
			}
		}

		// notify listeners
		for (TableModelListener listener : listeners) {
			listener.tableChanged(new TableModelEvent(this));
		}
	}

	/**
	 * Returns the most specific superclass for all the cell values in the
	 * column. This is used by the JTable to set up a default renderer and
	 * editor for the column.
	 * 
	 * @param columnIndex
	 *            the index of the column
	 * @return the common ancestor class of the object values in the model.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return Object.class;
	}

	/**
	 * Returns the number of columns in the model. A JTable uses this method to
	 * determine how many columns it should create and display by default.
	 * 
	 * @return the number of columns in the model
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	/**
	 * Returns the name of the column at columnIndex. This is used to initialize
	 * the table's column header name. Note: this name does not need to be
	 * unique; two columns in a table can have the same name.
	 * 
	 * @param columnIndex
	 *            the index of the column
	 * @return the name of the column
	 */
	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex < COLUMN_NAMES.length) {
			return COLUMN_NAMES[columnIndex];
		}
		return null;
	}

	/**
	 * Returns the number of rows in the model. A JTable uses this method to
	 * determine how many rows it should display. This method should be quick,
	 * as it is called frequently during rendering.
	 * 
	 * @return the number of rows in the model
	 */
	@Override
	public int getRowCount() {
		int rowCount = 0;
		if (records != null) {
			rowCount = records.size();
		}
		return rowCount;
	}

	/**
	 * Returns the value for the cell at columnIndex and rowIndex.
	 * 
	 * @param rowIndex
	 *            the row whose value is to be queried
	 * @param columnIndex
	 *            the column whose value is to be queried
	 * @return the value Object at the specified cell
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object value = null;
		if (records != null && records.size() > rowIndex) {
			DataRecord record = records.get(rowIndex);
			switch (columnIndex) {
			case DataRecord.INDEX_NAME:
				value = record.getName();
				break;
			case DataRecord.INDEX_LOCATION:
				value = record.getLocation();
				break;
			case DataRecord.INDEX_SPECIALTIES:
				value = record.getSpecialties();
				break;
			case DataRecord.INDEX_SIZE:
				value = record.getSize();
				break;
			case DataRecord.INDEX_RATE:
				value = record.getRate();
				break;
			case DataRecord.INDEX_OWNER:
				value = record.getOwner();
				break;
			// no default
			}
		}
		return value;
	}

	/**
	 * Returns true if the cell at rowIndex and columnIndex is editable.
	 * Otherwise, setValueAt on the cell will not change the value of that cell.
	 * 
	 * @param rowIndex
	 *            the row whose value to be queried
	 * @param columnIndex
	 *            the column whose value to be queried
	 * @return true if the cell is editable
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	/**
	 * Adds a listener to the list that is notified each time a change to the
	 * data model occurs.
	 * 
	 * @param l
	 *            the TableModelListener
	 */
	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	/**
	 * Removes a listener from the list that is notified each time a change to
	 * the data model occurs.
	 * 
	 * @param l
	 *            the TableModelListener
	 */
	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	/**
	 * Sets the value in the cell at columnIndex and rowIndex to aValue.
	 * 
	 * @param aValue
	 *            the new value
	 * @param rowIndex
	 *            the row whose value is to be changed
	 * @param columnIndex
	 *            the row whose value is to be changed
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// not used
	}
}
