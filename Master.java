import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
/**
 * This class runs the master program on a machine. Master monitors 
 * active nodes in the system. In charge of adding nodes to the 
 * system, and the distribution of file chunk index locations to nodes. 
 * Includes methods that will be called upon by nodes in the system. 
 * Extends Thread because master acts as a server and a client. 
 */
public class Master extends Thread implements MasterInterface{
	
	//This stores all the chunk data from all nodes in system
	private static ConcurrentHashMap<String, ArrayList<String[]>> chunkList = new ConcurrentHashMap<String, ArrayList<String[]>>();
        
	//This stores all active nodes in the system
	private static ArrayList<String> nodeList = new ArrayList<String>();
	
	/**
	 * Synchronized method that adds a file chunk to chunkList.
	 */
	private static synchronized void addChunk(String key, String[] value){	
		ArrayList<String[]> old_val = chunkList.get(key);	
		if(old_val == null){
			old_val = new ArrayList<String[]>();
			old_val.add(value);
			chunkList.put(key, old_val);
		}
		else{
			old_val.add(value);
			chunkList.put(key, old_val);
                }
	}
	
	/**
	 * Constructor 
	 */
	private Master() {
		this.chunkList = chunkList;
	}
	
	/**
	 * 
	 */
        public void run(){
                try {
                        MasterInterface stub = (MasterInterface) UnicastRemoteObject.exportObject(this, 8106);
                        Registry registry = LocateRegistry.createRegistry(8087);
                        registry.bind("Master", stub);
                        System.err.println("Master Server ready");
                }
                catch (Exception e){                                                                                         
                        System.out.println (e.toString());
                }	
	}
	
	/**
	 * Getter method to get nodeList
	 */
	public static ArrayList<String> getNodeList(){
		return nodeList;
	}
	
	/**
	 * Setter method to set nodeList
	 */
        public static void setNodeList(ArrayList<String> newList){
                nodeList = newList;
        }
	
	/**
	 * Getter method to get chunkList
	 */
	public static ConcurrentHashMap<String, ArrayList<String[]>> getChunkList(){
		return chunkList;
	}
	
	/**
	 * Method that updates nodeList
	 */
	public void updateNodelist(String ip_addr){
		synchronized(nodeList){
			ArrayList<String> newList = getNodeList();
			newList.add(ip_addr);
			setNodeList(newList);
		}
	}
	
	/**
	 * Method that sends master's chunkList to node of specified IP address.
	 */
	public void sendChunkList(String ip_addr){
		try {
                	      	
			Registry registry = LocateRegistry.getRegistry(ip_addr, 8087);
			ClientInterface stub = (ClientInterface) registry.lookup("Node");
			String response = stub.updateList(this.getChunkList());                  	
                } 
		catch (Exception e) {
                      	System.err.println("Client exception: " + e.toString());
                      	e.printStackTrace();
		}
	}
	
	/**
	 * Method called by nodes which modify master's chunkList.
	 * Master broadcast distributes chunkList to system. 
	 */
	public String modifyList(String filename, String ip_addr, int chunk){
		String[] value =  new String[2];
		value[0] = ip_addr;
		value[1] = Integer.toString(chunk);
		addChunk(filename, value);
		synchronized (nodeList){
			for (String addr: getNodeList()){
				sendChunkList(addr);
			}	
		}	
		try {
			System.out.println("waiting");
		
		}
		catch(Exception e){
			System.out.println("sleep didnt work");
		}
		return getChunkList().toString();
       	}
	
	/**
	 * Method called by nodes which adds node to network 
	 */
       	public String addNode(String ip_addr){
                updateNodelist(ip_addr);
                this.sendChunkList(ip_addr);
		return "nodelist updated";

        }
	
       	private static Master obj;
	
	/**
	 * Main method which runs the master server. Includes constant health pings 
	 * to nodes in system. 
	 */
        public static void main(String[] args) {
                obj = new Master();
                obj.start();

		while(true){	
			int changed = 0;
			ArrayList<String> badList = new ArrayList<String>();
			synchronized(nodeList){
				for (String addr : nodeList){
					try {
						Registry registry = LocateRegistry.getRegistry(addr, 8087);
						ClientInterface stub = (ClientInterface) registry.lookup("Node");
						String response = stub.heartbeat();
					}
					catch (Exception e) {
						nodeList.remove(addr);
						badList.add(addr);
						changed = 1;
					}
				}
			}
			if (changed == 1){
				for (String file: chunkList.keySet()){
					for (String[] chunk : chunkList.get(file)){
						if (badList.contains(chunk[0])){
				        	        ArrayList<String[]> old_val = chunkList.get(file);
                      					old_val.remove(chunk);
						}
					}
				}
			}
			try {
				TimeUnit.SECONDS.sleep(10000);
				//Sleep so master only pings nodes every 10 seconds
			}
			catch (Exception e){
				System.out.print("sleep didnt work");
			}
		}
		    
	}
}
                                                   
