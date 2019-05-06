import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
public class Master extends Thread implements MasterInterface{
	private static Hashtable<String, ArrayList<String[]>> chunkList = new Hashtable<String, ArrayList<String[]>>();
        private static ArrayList<String> nodeList = new ArrayList<String>();
	private static void addChunk(String key, String[] value){
		
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
		
	private Master() {
		this.chunkList = chunkList;
	}
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

	public static Hashtable<String, ArrayList<String[]>> getChunkList(){
		return chunkList;
	}
	public void updateNodelist(String ip_addr){
		
		nodeList.add(ip_addr);
	}
	public void sendChunkList(String ip_addr){
		System.out.println("Send chunk list called");
		try {
                      	
			Registry registry = LocateRegistry.getRegistry(ip_addr, 8087);
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
		System.out.println("got here");
		addChunk(filename, value);
		System.out.println("nodelist " + nodeList.toString());
		for (String addr: nodeList){
			System.out.println(addr);
			sendChunkList(addr);
		}	
		return getChunkList().toString();
       	}
       	public String addNode(String ip_addr){
                updateNodelist(ip_addr);
                this.sendChunkList(ip_addr);
		return "nodelist updated";

        }
       	private static Master obj;
        public static void main(String[] args) {
                obj = new Master();
                obj.start();

		while(true){
			int changed = 0;
			ArrayList<String> badList = new ArrayList<String>();
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
			if (changed == 1){
				for (String file: chunkList.keySet()){
					for (String[] chunk : chunkList.get(file)){
						if (badList.contains(chunk[0])){
				        	        ArrayList<String[]> old_val = chunkList.get(file);
				                
                        				old_val.remove(chunk);
                        				chunkList.put(file, old_val);
						}
					}
				}
			}
			try{
				TimeUnit.SECONDS.sleep(10000);
		
			}
			catch (Exception e){
				System.out.print("sleep didnt work");
			}
		}
                


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
                                                   
