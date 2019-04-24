import os

os.system("java Master &")
os.system("ssh -i /home/rrobrien/rrobrien-keypair rrobrien@54.209.66.61 java Node")

