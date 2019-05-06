import os
import time
nodes = ["54.209.66.61","3.14.64.40","3.94.170.64","54.153.0.234","3.83.10.22"]

os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@100.26.104.102 killall -9 java")
for node in nodes:
    os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@"+ node + " killall -9 java")


os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@100.26.104.102 rm Master.class")
os.system("scp -i /home/rrobrien/rrobrien-keypair Master.class rrobrien@100.26.104.102:/home/rrobrien")
for node in nodes:
    
    os.system("scp -i /home/rrobrien/rrobrien-keypair ClientInterface.class rrobrien@" + node +":/home/rrobrien")
    os.system("scp -i /home/rrobrien/rrobrien-keypair MasterInterface.class rrobrien@" + node +":/home/rrobrien")

    os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@"+ node + " rm Node.java")
    os.system("scp -i /home/rrobrien/rrobrien-keypair Node.java rrobrien@" + node +":/home/rrobrien")
    os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@"+ node + " javac Node.java")
    os.system("scp -i /home/rrobrien/rrobrien-keypair bigfile.txt rrobrien@" + node +":/home/rrobrien")


n = os.fork()
if n > 0:
    
    os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@100.26.104.102 java -Djava.rmi.server.hostname=100.26.104.102 Master ")
else:
    time.sleep(2)
    
    #os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@3.14.64.40 java Node 3.14.64.40 100.26.104.102 3")
         
    counter = 0
    while counter < (len(nodes)):
        i = os.fork()
        if i == 0:
            print(counter)
            os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@" + nodes[counter] + " java Node "+nodes[counter] +" 100.26.104.102 bigfile.txt "+ str(counter))
            os._exit(0)
        
        counter += 1
            #os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@54.209.66.61 java Node "+ nodes[counter] +" 100.26.104.102 bigfile.txt 5")
            #counter += 1
     
    #os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@" + nodes[counter] + " java Node "+ nodes[counter] +" 100.26.104.102 bigfile.txt")


