package suncertify.client;

import java.util.List;

import suncertify.application.BusinessService;
import suncertify.db.RecordAlreadyBookedException;
import suncertify.db.RecordNotFoundException;
import suncertify.db.domain.DataRecord;

/**
 * Controller for the client component of the application.
 * <p>
 * The controller provides methods for each use case of the client: listing all
 * records, searching for records by name and/or location and booking a record.
 * <p>
 * The controller works as an adapter between the business interface and the
 * client view. It calls the methods on a {@code BusinessInterface} object that
 * actually provides the business method implementations and wraps the result in
 * a {@code ClientModel} instance.
 * 
 * @author Rasmus Kuschel
 */
public final class ClientController {

	/**
	 * Business service used to execute the use cases.
	 */
	private final BusinessService businessService;

	/**
	 * Creates a new ClientController instance that uses the specified
	 * BusinessService instance to provide the use cases.
	 * 
	 * @param businessService
	 *            business service of the application
	 */
	public ClientController(BusinessService businessService) {
		this.businessService = businessService;
	}

	/**
	 * Returns a model containing the data of all valid data records.
	 * 
	 * @return model containing all valid data records.
	 */
	public ClientModel retrieveAllRecords() {

		// Retrieve a list of all data records from the business service
		final List<DataRecord> allRecords = businessService
				.retrieveAllRecords();

		// Wrap the list in a ClientModel instance
		final ClientModel clientModel = new ClientModel(allRecords);

		return clientModel;
	}

	/**
	 * Returns a model containing the data of all data records that exactly
	 * match the specified criteria.
	 * <p>
	 * A criteria value of null matches any field value.
	 * 
	 * @param name
	 *            Criteria for the name field
	 * @param location
	 *            Criteria for the location field
	 * 
	 * @return model containing the data records that exactly match the criteria
	 */
	public ClientModel searchRecords(final String name, final String location) {

		// Retrieve a list of all data records that match the criteria from the
		// business service.
		final List<DataRecord> records = businessService.searchRecords(name,
				location);

		// Wrap the list in a ClientModel instance
		final ClientModel clientModel = new ClientModel(records);

		return clientModel;
	}

	/**
	 * Tries to book the record with the specified record number.
	 * <p>
	 * Throws a RecordAlreadyBookedException if the record was already booked.
	 * 
	 * @param record
	 *            data record that shall be booked.
	 * @param model
	 *            model currently used in the view
	 * @return model updated model
	 * @throws RecordAlreadyBookedException
	 *             if the record was already booked.
	 * @throws RecordNotFoundException
	 *             if no record with the given record number exists or if it is
	 *             marked as deleted
	 */
	public ClientModel bookRecord(final DataRecord record,
			final ClientModel model) throws RecordAlreadyBookedException,
			RecordNotFoundException {

		try {
			// Try to book the record. The business service method returns a
			// list of records that reflect the modifications.
			final List<DataRecord> records = businessService.bookRecord(record,
					model.getRecords());

			// Wrap the list in a ClientModel instance
			final ClientModel clientModel = new ClientModel(records);

			return clientModel;
		} finally {
			// Update the model to reflect the changed record
			// This needs to be done, when booking the record has failed and we
			// do not return a new model.
			model.updateRecord(record);
		}
	}
}
