package suncertify.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface of components that can create instances implementing the {@code
 * RemoteDBAccess} interface.
 * 
 * @author Rasmus Kuschel
 */
public interface RemoteDBAccessFactory extends Remote {

	/**
	 * Key used to bind an instance in an RMI registry.
	 */
	public final static String RMI_KEY = "RemoteDBAccessFactory";

	/**
	 * Creates a new instance implementing RemoteDBAccess.
	 * 
	 * @return RemoteDBAccess instance.
	 * @throws RemoteException
	 *             indicates an I/O error.
	 */
	public RemoteDBAccess createRemoteDBAccess() throws RemoteException;
}
