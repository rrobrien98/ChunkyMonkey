import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;

public class Node extends Thread implements ClientInterface{
        private Hashtable<String, String[]> chunkList = new Hashtable<String, String[]>();

        public Node() {}
        public void setChunkList(Hashtable<String, String[]> newList){
                this.chunkList = newList;
        }
        public void run(){

                try
                {

                        //ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(this, 8002);

                        // Bind the remote object's stub in the registry

                       // Registry registry = LocateRegistry.createRegistry(8074);
                        //registry.bind("Node", stub);

                        //System.err.println("Server ready");



                }
                catch (Exception e){                                                                                         // Throwing an exception
                        System.out.println ("Exception is caught" + e.toString());
                }
        }
        public void updateList(Hashtable<String, String[]> list) {
                setChunkList(list);
                System.out.println("new list recieved");
        }
	private static Node obj;
        public static void main(String args[]) {
           	obj = new Node();
                obj.start();
		System.out.println("sent request");
                try {
                        Registry registry = LocateRegistry.getRegistry("turing.bowdoin.edu", 8087);
                        MasterInterface stub = (MasterInterface) registry.lookup("Master");
                        String response = stub.addNode("new node added");
                        System.out.println("response: " + response);
                } catch (Exception e) {
                        System.err.println("Client exception: " + e.toString());
                        e.printStackTrace();
                }


        }

}

