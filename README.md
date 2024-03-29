# toyChordDHT

A peer-to-peer Distributed Hash Table implementation in Java, based on a simplified version of the [Chord DHT](https://en.wikipedia.org/wiki/Chord_(peer-to-peer)). The system designed to store, delete and query key-value pairs on a cluster of computers (also defined as "nodes"). Every node in the network will store the values for all the keys for which it is responsible. The way a key is assigned to a node is specified through the use of a Hash function (there are other ways available too), in order to achieve load balancing.

Every node is aware of his previous and next node in the sequence (like a Ring Network topology), while every transaction he receives is either handle by him (if possible), or forwarded to the next node. Consequently all of the transactions can start from any point of the network and are always forwarded towards the same direction.

Any new node can join the network, while an existing one can depart at any time (Byzantine failures are not considered yet), without the loss of any existing data stored. The system also supports the existence of replicas in multiple nodes of the network, and the data consistency type can be set to either linearizability or eventual consistency. Both of the options can be defined in the [config file](https://github.com/nickst74/toyChordDHT/blob/main/src/toyChord/config/Config.java).

More information regarding the system's operations and the conducted experiments can be found in the [report](https://github.com/nickst74/toyChordDHT/blob/main/report.pdf).

# Instructions

At first run the **compile.sh** script to compile the project.  
./compile.sh


Then the **make_jar.sh** script to aggregate all files into one jar file.  
./make_jar.sh


To start the first node of the DHT:  
./start_network <myPort>


To add more nodes to the DHT you need to know the ip and port of an existing node in the network (any of those nodes, no specific bootstrap/leader node needed):  
./start_node <myPort> <targetNodeIp> <targetNodePort>


After the node starts, it automaticaly attempts to join the network. Then the user is able to input the desired commands through the CLI. For the list of the available commands you can always use the command "help" in the CLI. An example usage of the commands available:

insert "Hello World" "200"  
(inserts the "Hello World" key with value "200" into the DHT)

query "Hello World"  
(searches and returns the value for the key "Hello World" inside the DHT if it exists)

query *  
(returns all the key-value pairs from all the nodes, only the original data stored but not the replicas)

delete "Hello World"  
(deletes the "Hello World" key from the DHT if it exists)

overlay  
(prints all the nodes in the DHT with their ip:post and id in the correct order, starting from the one that executed the command)

depart  
(the node leaves the network and then shuts down)

# Experiments

In order to run the experiments defined in the project requirements we use the files in the "supplementary" folder (the timestamps we take will not work of course in a distributed project due to synchronization issues but we are running it on VMs with host time sync option enabled, so it does not matter that much). Three more scripts are also created to automate the process of inserting the key-values pairs from the "insert.txt", querying the keys of the "query.txt", and execute the requests from the "requests.txt" files. One more is being used to delete all key that we inserted in the whole process. To run those 4 programs we first need to define in the "Config.java" file the filepaths for the transaction files, the addresses of the nodes in the network (from those addresses we pick one randomly every time to execute a transaction in those 4 programs), along with the replication factor (number of replicas for every key-value pair), along with the replication strategy that implements leniarizability or eventual consistency (linearizability uses chain replication). Then we need to recompile the project of course.
