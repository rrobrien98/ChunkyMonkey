import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Scanner;
public class Node extends Thread implements ClientInterface{
        public static Hashtable<String, String[]> chunkList = new Hashtable<String, String[]>();
	public static final int CHUNK_SIZE = 500;
        public Node() {}
        public void setChunkList(Hashtable<String, String[]> newList){
                chunkList = newList;
        }
        public Hashtable<String, String[]> getChunkList(){
		return this.chunkList;
	}
	public void run(){

                try
                {

                        ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(this, 8002);

                        //Bind the remote object's stub in the registry

                        Registry registry = LocateRegistry.createRegistry(8087);
                        registry.bind("Node", stub);

                        System.err.println("Server ready");



                }
                catch (Exception e){                                                                                         // Throwing an exception
                        System.out.println ("Exception is caught" + e.toString());
                }
        }
	public void getChunk(String ip_addr, String filename, int chunk){
		byte[] chunkData = new byte[CHUNK_SIZE];

		try{
			Registry registry = LocateRegistry.getRegistry(ip_addr, 8087);
			ClientInterface stub = (ClientInterface) registry.lookup("Node");
			chunkData = downloadChunk(filename, chunk);

			RandomAccessFile file = new RandomAccessFile(filename, "rw");
			file.seek(chunk * CHUNK_SIZE);
			String chunkString = Arrays.toString(chunkData);
			//System.out.println(chunkString);
			file.write(chunkData);
			file.close();
		}
		catch (Exception e){
			System.out.println("Exception is caught" + e.toString());
		}
	}		
	public byte[] downloadChunk(String filename, int chunk){
		byte[] data = new byte[CHUNK_SIZE];
		try{	
			//File data_file = new File(filename);
			RandomAccessFile file = new RandomAccessFile(filename, "rw");
			//file.seek(chunk * CHUNK_SIZE);
			Scanner scanner = new Scanner(new File(filename)); 
			//while (scanner.hasNextLine()) {	
				System.out.println(scanner.nextLine());//scanner.nextLine());
			//}
			file.read(data);
			
			file.close();
			//System.out.println(Arrays.toString(data));
		}
		catch (Exception e){
			System.out.println("Exception is caught" + e.toString());
			
		}
		return data;
	}
        public String updateList(Hashtable<String, String[]> list) {
                setChunkList(list);
                return "new list recieved";
        }
	private static Node obj;
        public static void main(String args[]) {
           	obj = new Node();
                obj.start();
		//Registry registry = LocateRegistry.getRegistry("100.26.104.102", 8087);
                //ClientInterface stub = (ClientInterface) registry.lookup("Master");
		//String response = stub.addNode(args[whatever]);
		//while(true){
		        //Scanner input= new Scanner(System.in); 
  
        		// String input 
			//String name = input.nextLine();	
			//System.out.println("sent request");
                	if (args.length == 1 && args[0].equals("test")){
				try {
                        		//Registry registry = LocateRegistry.getRegistry("100.26.104.102", 8087);
                       			//ClientInterface stub = (ClientInterface) registry.lookup("Master");
                        		//String response = stub.addNode("54.209.66.61");
         			  	obj.getChunk("54.209.66.61", "/home/rrobrien/test3.txt",0);
				
				
					//System.out.println("response from other node: " + response);
                			//String response2 = stub.modifyList("test name", "test ip", 1);	
					//System.out.println("new list = " + chunkList.toString());
				} catch (Exception e) {
                        		System.err.println("Client exception: " + e.toString());
                        		e.printStackTrace();
                		}
			}
		//}
        }	

}

