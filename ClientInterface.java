
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Defines methods that can be called on nodes by other nodes and master
 * these include methods to download chunks, to recieve new chunklists, and to respond to master heartbeat pings"
 */
public interface ClientInterface extends Remote {
	String updateList(ConcurrentHashMap<String, ArrayList<String[]>> list) throws RemoteException;
	byte[] downloadChunk(String filename, int chunk) throws RemoteException;
	String heartbeat() throws RemoteException;
}

