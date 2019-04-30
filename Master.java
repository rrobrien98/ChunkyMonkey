import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;

public class Master extends Thread implements MasterInterface{
	private Hashtable<String, String[]> chunkList = new Hashtable<String, String[]>();
        private ArrayList<String> nodeList = new ArrayList<String>();
	public void addChunk(String key, String[] value){
		chunkList.put(key, value);
	}
		
	private Master() {}
        public void run(){

                try
                {
                        //System.out.println("before stub");
                        MasterInterface stub = (MasterInterface) UnicastRemoteObject.exportObject(this, 8106);

                        // Bind the remote object's stub in the registry
                        //System.out.println("before create reg");
                        Registry registry = LocateRegistry.createRegistry(8087);
                        registry.bind("Master", stub);

                        System.err.println("Master Server ready");



                }
                catch (Exception e){                                                                                         // Throwing an exception
                        System.out.println (e.toString());
                }
	}
	public Hashtable<String, String[]> getChunkList(){
		return chunkList;
	}
	public void updateNodelist(String ip_addr){
		nodeList.add(ip_addr);
	}
	public void sendChunkList(String ip_addr){
		System.out.println("Send chunk list called");
		try {
                      
			
			Registry registry = LocateRegistry.getRegistry(ip_addr, 8074);
                      	System.out.println("registry found");
			ClientInterface stub = (ClientInterface) registry.lookup("Node");
                      	System.out.println("stub created");
			String response = stub.updateList(this.getChunkList());
                      	System.out.println("response: " + response);
                } catch (Exception e) {
                      	System.err.println("Client exception: " + e.toString());
                      	e.printStackTrace();
		}
	}
	public String modifyList(String filename, String ip_addr, int chunk){
                System.out.println("modify list called");
		String[] value =  new String[2];
		value[0] = ip_addr;
		value[1] = Integer.toString(chunk);
		addChunk(filename, value);
		for (String addr: nodeList){
			sendChunkList(addr);
		}	
		return getChunkList().toString();
       	}
       	public String addNode(String ip_addr){
                updateNodelist(ip_addr);
                sendChunkList(ip_addr);
		return "nodelist updated";

        }
       	private static Master obj;
        public static void main(String[] args) {
                obj = new Master();
                obj.start();
        //String host = (args.length < 1) ? null : args[0];

                //try {
                //      Registry registry = LocateRegistry.getRegistry("54.209.66.61", 8084);
                //      ClientInterface stub = (ClientInterface) registry.lookup("Node");
                //      String response = stub.updateList();
                //      System.out.println("response: " + response);
                //} catch (Exception e) {
                //      System.err.println("Client exception: " + e.toString());
                //      e.printStackTrace();
                //}
        }
}
                                                   
