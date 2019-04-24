import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterInterface extends Remote {
    String modifyList(String filename, String ip_addr, int chunk) throws RemoteException;
    String addNode(String ip_addr) throws RemoteException;
}

