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
import java.util.ArrayList;
import java.lang.Runnable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
public class Node extends Thread implements ClientInterface{
        public static ConcurrentHashMap<String, ArrayList<String[]>> chunkList = new ConcurrentHashMap<String, ArrayList<String[]>>();
	public static final int CHUNK_SIZE = 500;
	private static volatile long time;	
        public Node() {
		time = 0;
	}
		
        public synchronized void setChunkList(ConcurrentHashMap<String, ArrayList<String[]>> newList){
                chunkList = newList;
        	//System.out.println("set the new chunklist");
	}
        public synchronized ConcurrentHashMap<String, ArrayList<String[]>> getChunkList(){
		return this.chunkList;
	}
	public String heartbeat(){
		return "Im not dead yet";
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
			chunkData = stub.downloadChunk(filename, chunk);

			RandomAccessFile file = new RandomAccessFile(filename, "rw");
			file.seek(chunk * CHUNK_SIZE);
			//String chunkString = Arrays.toString(chunkData);
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
			file.seek(chunk * CHUNK_SIZE);
			//Scanner scanner = new Scanner(new File(filename)); 
			//while (scanner.hasNextLine()) {	
			//	System.out.println(scanner.nextLine());//scanner.nextLine());
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
        public synchronized String updateList(ConcurrentHashMap<String, ArrayList<String[]>> list) {
                //System.out.println("called update list");
		setChunkList(list);
                return "new list recieved";
        }
	
	
	private class Downloader implements Runnable{
		String ip_addr;
		String filename;
		int chunk;
		Node obj;
			
		Downloader(String ip_addr, String filename, int chunk, Node obj){
			this.ip_addr = ip_addr;
			this.filename = filename;
			this.chunk = chunk;
			this.obj = obj;
			
		}	
		public void run(){
			long start = System.currentTimeMillis();
			System.out.println("START: " + start);
			this.obj.getChunk(this.ip_addr, this.filename, this.chunk);
			System.out.println("END: " + System.currentTimeMillis());
			this.obj.time = (System.currentTimeMillis() - start);
			System.out.println("DOWNLOAD TIME: " + this.obj.time);
			
			return;
		}
		
	}
	
	private static Node obj;
        public static void main(String args[]) {
           	obj = new Node();
                obj.start();
		
			
		
                if (args.length < 2){
			System.out.println("Need IP address of node as argument");
			return;
		}

		String thisIp = args[0];
                String masterIp = args[1];
		//System.out.println("our ip: " + thisIp);
		try{
		//long start = System.currentTimeMillis();
	        Registry registry = LocateRegistry.getRegistry(masterIp, 8087);
	        MasterInterface stub = (MasterInterface) registry.lookup("Master");
	        
		String response = stub.addNode(thisIp);
	        //System.out.println("TIME: " + (System.currentTimeMillis() - start));

		//System.out.println(response);



		//while(true){
		//	System.out.println("Enter operation");	
		//	Scanner op = new Scanner(System.in);
		//	int operation = op.nextInt();
			

			//user specifies what type of command they want to perform
			switch (args.length) {
				case 4:
					//Scanner fileInfo = new Scanner(System.in);
					//String info = fileInfo.nextLine();
					//String[] infoArr = info.split(" ");
				
                                        //long start = System.currentTimeMillis();
                                        response = stub.modifyList(args[2],thisIp,Integer.parseInt(args[3]));
					//System.out.println("TIME: " + (System.currentTimeMillis() - start));

					break;
				case 3:
					
					
					//Scanner filename = new Scanner(System.in);
					//String file = filename.nextLine();
					ArrayList<String[]> chunks = chunkList.get(args[2]);
					ArrayList<String> done = new ArrayList<String>();
					long start = System.currentTimeMillis();
					for(String[] chunk: chunks){
						if (!done.contains(chunk[1])){
							//obj.getChunk(chunk[0], file, Integer.parseInt(chunk[1]));
							
							Node.Downloader download = obj.new Downloader(chunk[0], args[2], Integer.parseInt(chunk[1]), obj);
							Thread downloader = new Thread(download);
							downloader.start();
							System.out.println("finished running");
							done.add(chunk[1]);
							downloader.join();
							System.out.println("download time from thread: " + obj.time);
						}
					}
										
					break;
				case 2:
					
					for(String key: chunkList.keySet()){
						System.out.println(key);
					}
							
					
					
					break;
				default:
					System.out.println("Invalid Command");
					
					break;
				}
				
			}
		
        	//}	
		
		catch(Exception e){
			System.out.println("node exception " + e.toString());
		}
		}
}

