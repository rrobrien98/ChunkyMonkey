
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;


public interface ClientInterface extends Remote {
	String updateList(Hashtable<String, String[]> list) throws RemoteException;
	byte[] downloadChunk(String filename, int chunk) throws RemoteException;
}

