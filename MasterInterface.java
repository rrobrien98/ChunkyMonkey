import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * defines all methods that can be called on the master by nodes
 * these include methods to modify the chunk list in the event of writes, 
 * and a method for a node to add itsself to the system
 */
public interface MasterInterface extends Remote {
    String modifyList(String filename, String ip_addr, int chunk) throws RemoteException;
    String addNode(String ip_addr) throws RemoteException;
}

