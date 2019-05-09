
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
public interface ClientInterface extends Remote {
	String updateList(ConcurrentHashMap<String, ArrayList<String[]>> list) throws RemoteException;
	byte[] downloadChunk(String filename, int chunk) throws RemoteException;
	String heartbeat() throws RemoteException;
}

