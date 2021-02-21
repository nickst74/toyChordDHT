# toyChordDHT

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
