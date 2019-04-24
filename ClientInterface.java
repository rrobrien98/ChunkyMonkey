
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;


public interface ClientInterface extends Remote {
    void updateList(Hashtable<String, String[]> list) throws RemoteException;
}

