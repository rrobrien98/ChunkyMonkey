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

/*
 * This class runs the client program on a machine
 * It allows a user to add them self to the ChunkyMonkey system, and gives them the ability to declair files for download and to
 * download files from other nodes
 * Implements Thread so node can run as server and client. 
 * To run this program, the command line argument: java -Djava.rmi.server.hostname=[your public IP] Node [your public IP] [master servers public IP]                                                                             54.153.0.234 100.26.104.102
 */
public class Node extends Thread implements ClientInterface{
   	
	//this stores all the chunk data from all nodes in system
	public static ConcurrentHashMap<String, ArrayList<String[]>> chunkList = new ConcurrentHashMap<String, ArrayList<String[]>>();
	public static final int CHUNK_SIZE = 500;
	//this stores the times it takes to download a chunk from other known nodes in the system
	public static Hashtable<String, Long> latencyList = new Hashtable<String, Long>(); 		
        
	/**
	 * Constructor 
	 */
	public Node() {
		
	}
	
        /*
	 * Updates this clients chunk list to be the new one distributed by the master in the even of a write to the system
	 * Args: ConcurrentHashMap<String, ArrayList<String[]>> newList is the new index of chunks to replace the old one
	 */
	public void setChunkList(ConcurrentHashMap<String, ArrayList<String[]>> newList){
                //synchronized so that we dont get concurrent modification errors when looping through chunklist for downloads
		synchronized(chunkList){
			chunkList = newList;
		}
		
	}
        
	/*
	 * Simple getter for the chunklist
	 */
	public ConcurrentHashMap<String, ArrayList<String[]>> getChunkList(){
		return this.chunkList;
	}
	
	/*
	 * Called by the master to check if node is alive 
	 */
	public String heartbeat(){
		return "Im not dead yet";
	}
	
	/**
	 * 
	 */
	public void run(){

                try{

                        ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(this, 8002);

                        Registry registry = LocateRegistry.createRegistry(8087);
                        registry.bind("Node", stub);

                        System.err.println("Server ready");

                }
                catch (Exception e){                                                                                         
                        System.out.println ("Exception is caught" + e.toString());
                }
        }
	
	/**
	 * Method that downloads specified file chunk from specified node and writes
	 * it to local machine. 
	 *
	 * does so by calling download chunk on that method from the chunk information taken in as arguments
	 * gets a byte array of file data from this method, which it then writes into local storage
	 */
	public void getChunk(String ip_addr, String filename, int chunk){
		byte[] chunkData = new byte[CHUNK_SIZE];

		try{
			Registry registry = LocateRegistry.getRegistry(ip_addr, 8087);
			ClientInterface stub = (ClientInterface) registry.lookup("Node");
			chunkData = stub.downloadChunk(filename, chunk);
			
			//seek into the proper position in the new file and write the recieved array there
			RandomAccessFile file = new RandomAccessFile(filename, "rw");
			file.seek(chunk * CHUNK_SIZE);
			
			file.write(chunkData);
			file.close();
		}
		catch (Exception e){
			System.out.println("Exception is caught" + e.toString());
		}
	}
	
	/**
	 * Method called by peer nodes to download specified file chunk from this node. 
	 *
	 * only called by get chunk
	 * asks a remote node to read a chunk of data from its local filesystem, then return it as a byte array
	 *
	 */
	public byte[] downloadChunk(String filename, int chunk){
		byte[] data = new byte[CHUNK_SIZE];
		try{	
			
			RandomAccessFile file = new RandomAccessFile(filename, "rw");
			file.seek(chunk * CHUNK_SIZE);
			
			file.read(data);
			
			file.close();
			
		}
		catch (Exception e){
			System.out.println("Exception is caught" + e.toString());
			
		}
		return data;
	}
	
	/**
	 * Method called by the master which updates node's chunkList.
	 * This is how the node stays up to date on which files are available to download 
	 */
        public  String updateList(ConcurrentHashMap<String, ArrayList<String[]>> list) {
                
		setChunkList(list);
                return "new list recieved";
        }
	
	/**
	 * Inner Class that represents a file chunk download. 
	 * Used so we can simultaneously download chunks.
	 */
	private class Downloader implements Runnable{
		String ip_addr;
		String filename;
		int chunk;
		Node obj;
		long time;	
		
		Downloader(String ip_addr, String filename, int chunk, Node obj){
			this.ip_addr = ip_addr;
			this.filename = filename;
			this.chunk = chunk;
			this.obj = obj;
			this.time = 0;
		}	
		
		/*
		 * starts a download of a single chunk, uses instance variables of class as chunk info
		 */
		public void run(){
			long start = System.currentTimeMillis();
			
			this.obj.getChunk(this.ip_addr, this.filename, this.chunk);
		
			//records time taken to download the chunk and saves as instance variable	
			this.time = (System.currentTimeMillis() - start);
			System.out.println("Downloading Chunk " + this.chunk);
			
			return;
		}
		/*
		 * getter for time taken to download chunk
		 * used for latency list data
		 */
		public long getTime(){
			return this.time;
		}
		
		/*
		 * getter for ip address downloaded from
		 */
		public String getIp(){
			return this.ip_addr;
		}
	}
	
	private static Node obj;
        
	/**
	 * Main method which runs client server. Contains simple user interface. 
	 */
	public static void main(String args[]) {
           	obj = new Node();
                obj.start();
		
                if (args.length != 2){
			System.out.println("Need IP address of this node and master as arguments");
			return;
		}

		String thisIp = args[0];
                String masterIp = args[1];
		
		try{
	        	Registry registry = LocateRegistry.getRegistry(masterIp, 8087);
	        	MasterInterface stub = (MasterInterface) registry.lookup("Master");
			String response = stub.addNode(thisIp); 
		
			//Node gets added to network, only done once per node 
			while(true){
		
				System.out.println("Enter operation: \n1) Make file available to system for download \n2) Download a file \n3) See files available");	
				Scanner op = new Scanner(System.in);
				int operation = op.nextInt();
	
				switch (operation) {
					case 1:
						System.out.println("Enter file info in format of [filename] [chunk]");
						Scanner fileInfo = new Scanner(System.in);
						String info = fileInfo.nextLine();
						String[] infoArr = info.split(" ");
					
	                                        
	                                        response = stub.modifyList(infoArr[0],thisIp,Integer.parseInt(infoArr[1]));
						//File chunk gets added to system
						
						break;
					case 2:
						
						System.out.println("Enter name of File you would like to download");
						Scanner filename = new Scanner(System.in);
						String file = filename.nextLine();
						ArrayList<String[]> chunks = chunkList.get(file);
						//List of all chunk index locations for file
						
						String[][] toDownload = new String[100][2]; 
						//Array to hold all the chunks to be downloaded, assumed that no file will be larger than 100 chunks
						
						
						synchronized(chunkList){
							
							//loop through chunks of file to find ones we want to download
							for(String[] chunk: chunks){
								if (latencyList.get(chunk[0]) == null){
									long initLatency = 0;
									latencyList.put(chunk[0],initLatency);
								}
								
								//intelligent selection: grabs a chunk to download if we still need that chunk or if a 
								//faster location to download from is found
								if (toDownload[Integer.parseInt(chunk[1])][0] == null  || latencyList.get(toDownload[Integer.parseInt(chunk[1])][0]) > latencyList.get(chunk[0]) ){
								
									toDownload[Integer.parseInt(chunk[1])] = chunk;
									
								}
							}
						}
						ArrayList<Node.Downloader> downloads = new ArrayList<Node.Downloader>();
						ArrayList<Thread> threads = new ArrayList<Thread>();
						
						//create downloader objects and start downloads for all selected nodes
						for (int i = 0; i < toDownload.length; i++){
							if (toDownload[i][0] !=  null) {
								
								Node.Downloader download = obj.new Downloader(toDownload[i][0], file, Integer.parseInt(toDownload[i][1]), obj);
								downloads.add(download);
								Thread downloader = new Thread(download);
								threads.add(downloader);
								downloader.start();
							}
						}
						
						//wait for all downloads to finish				
						for (Thread thread : threads){
							thread.join();
						}
						
						//retrieve download time info from all downloaders, and update latency list
						for (Node.Downloader download : downloads){
							latencyList.put(download.getIp(),download.getTime());
						}
					
						break;
					case 3:
						
						for(String key: chunkList.keySet()){
							System.out.println(key);
						}
											
						break;
					default:
						System.out.println("Invalid Command");
						
						break;
				}
	
			}	
        	
		}
		catch(Exception e){
			System.out.println("node exception " + e.toString());
			e.printStackTrace();
		}
	}
}

