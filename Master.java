import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Master extends Thread implements MasterInterface{

        private Master() {}
        public void run(){

                try
                {
                        //System.out.println("before stub");
                        MasterInterface stub = (MasterInterface) UnicastRemoteObject.exportObject(this, 8104);

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



        public String modifyList(String filename, String ip_addr, int chunk){
                return "rmi working";
        }
       public String addNode(String ip_addr){
                System.out.println(ip_addr);
                return "worked";

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
                                                   
