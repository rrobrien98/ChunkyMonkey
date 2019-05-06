
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.ArrayList;

public interface ClientInterface extends Remote {
	String updateList(Hashtable<String, ArrayList<String[]>> list) throws RemoteException;
	byte[] downloadChunk(String filename, int chunk) throws RemoteException;
	String heartbeat() throws RemoteException;
}

