# DHT
This project implements a Distributed Hash Table called Chord which uses consistent hashing to make data available accross different nodes


STEPS TO RUN:

1. Compile all the java files using javac \*.java
2. On the PC which you wish to use as a server, find the IP address using the "ifconfig" command on Linux or "ipconfig" on windows
3. Start the server using the command java Server
4. Start any number of nodes on different PCs from 1 to 15 by using the command java Node
5. It will prompt for the ID of the node, enter the ID (1 to 15), it will then require the server's IP address, enter the IP obtained in step 2
6. Follow the menu, create a file, retrieve files, display the finger table etc.
7. Exit a node, and when you start that node again with the same ID, all the data is recovered
