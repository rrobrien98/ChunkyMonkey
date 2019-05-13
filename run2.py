import os
import time
import random
nodes = ["54.153.0.234","54.209.66.61","3.14.64.40","3.94.170.64","3.83.10.22","54.193.125.205","3.14.67.164", "54.193.125.205", "52.24.143.203" , "34.221.171.231", "35.183.5.27", "35.182.195.196", "13.233.35.167", "13.233.83.84", "13.229.131.254" ,"3.0.94.77", "54.252.169.133", "13.54.28.88", "54.250.199.120", "18.185.138.61", "52.59.249.174","34.249.192.207","54.229.234.118", "35.181.59.157", "35.181.43.210","18.130.245.254","35.178.15.57","54.233.223.52","18.228.190.7"]


random.shuffle(nodes)
print(nodes)
"""
os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@100.26.104.102 killall -9 java")
for node in nodes:
    os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@"+ node + " killall -9 java")
"""
os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@100.26.104.102 rm Master.java")
os.system("scp -i /home/rrobrien/rrobrien-keypair ClientInterface.class rrobrien@100.26.104.102:/home/rrobrien")
os.system("scp -i /home/rrobrien/rrobrien-keypair Master.class rrobrien@100.26.104.102:/home/rrobrien")
os.system("scp -i /home/rrobrien/rrobrien-keypair MasterInterface.class rrobrien@100.26.104.102:/home/rrobrien")

for node in nodes:
    #os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@"+ node + " rm ClientInterface.class")
    #os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@"+ node + " rm MasterInterface.class")


    #os.system("scp -i /home/rrobrien/rrobrien-keypair ClientInterface.java rrobrien@" + node +":/home/rrobrien")
    #os.system("scp -i /home/rrobrien/rrobrien-keypair MasterInterface.java rrobrien@" + node +":/home/rrobrien")

    os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@"+ node + " rm Node.java")
    os.system("scp -i /home/rrobrien/rrobrien-keypair Node.java rrobrien@" + node +":/home/rrobrien")
    os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@"+ node + " javac Node.java")
    #os.system("scp -i /home/rrobrien/rrobrien-keypair bigfile.txt rrobrien@" + node +":/home/rrobrien")
"""

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
        #    print(counter)
            os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@" + nodes[counter] + " java -Djava.rmi.server.hostname=" + nodes[counter] + " Node "+nodes[counter] +" 100.26.104.102 bigfile.txt " + str(counter))
            os._exit(0)
        
        counter += 1
            
    print("done")
"""
