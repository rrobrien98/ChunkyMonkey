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
	 * takes in both the new key(filename) and the value for that key (the list of chunks belonging to that filename)
	 * synchronized to avoid concurrency modification of chunklist
	 */
	private static synchronized void addChunk(String key, String[] value){	
		ArrayList<String[]> old_val = chunkList.get(key);	
		
		//create new entry if filename not already in chunklist
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
	 * This is where we have the server side of our master running
	 * This runs in a different thread so that the main method can be responsible for health checking the nodes
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
	 * is synchronized for the nodelist to avoid concurrency issues for chunklist distribution
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
	 *
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

		//needs to be synchronized to avoid concurrent modification with node churn
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
	 * responds by sending a chunklist to the new node it can use to download files
	 */
       	public String addNode(String ip_addr){
                updateNodelist(ip_addr);
                this.sendChunkList(ip_addr);
		return "nodelist updated";

        }
	
       	private static Master obj;
	
	/**
	 * Main method which runs the master server. 
	 * the first task of this method is to start the master server running in its own thread
	 * Next, it enters an infinite for loop of periodically checking on the health of its member nodes
	 */
        public static void main(String[] args) {
                obj = new Master();
                obj.start();

                //will check for failures every 10 seconds
                while(true){

                        int changed = 0;
                        ArrayList<String> badList = new ArrayList<String>();
                        synchronized(nodeList){

                                //attempt to contact every node in node list, if we dont recieve reply, delete this node from the node list
                                for (String addr : nodeList){
                                        try {
                                                Registry registry = LocateRegistry.getRegistry(addr, 8087);
                                                ClientInterface stub = (ClientInterface) registry.lookup("Node");
                                                String response = stub.heartbeat();
                                        }
                                        catch (Exception e) {




                                                badList.add(addr);
                                                changed = 1;
                                        }
                                }
                                //done outside loop through nodelist to avoid concurrency issues
                                for(String addr: badList){
                                        nodeList.remove(addr);
                                }
                        }
                        if (changed == 1){
                                ArrayList<String> newLists = new ArrayList<String>();
                                synchronized (chunkList){

                                        //find all chunks that are held on the nodes discovered to be bad
                                        for (String file: chunkList.keySet()){
                                                ArrayList<String[]> badChunks = new ArrayList<String[]>();

                                                for (String[] chunk : chunkList.get(file)){

                                                        if (badList.contains(chunk[0])){
                                                                badChunks.add(chunk);
                                                        }
                                                }

                                                //again all modification to chunklists must be done outside loop to avoid concurrency issues
                                                for (String[] chunk: badChunks){
                                                        ArrayList<String[]> old_val = chunkList.get(file);

                                                        old_val.remove(chunk);
                                                }
                                        }


                                        //now find all the files that need to be removed from the file list, ie the ones who dont have any chunks
                                        //stored on good nodes now
                                        ArrayList<String> toRemove = new ArrayList<String>();


                                        for (String file: chunkList.keySet()){
                                                if(chunkList.get(file).size() == 0){
                                                        toRemove.add(file);

                                                }
                                        }

                                        for (String file: toRemove){
                                                chunkList.remove(file);
                                        }
                                        //send out new chunk lists to every node in system
                                        synchronized (nodeList){

                                                for (String addr: getNodeList()){

                                                        obj.sendChunkList(addr);
                                                }

                                        }
                                }

                        }
                        try{
                                TimeUnit.SECONDS.sleep(10);

                        }
                        catch (Exception e){
                                System.out.print("sleep didnt work");
                        }
              	}

	}
}

