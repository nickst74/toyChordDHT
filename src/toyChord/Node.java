package toyChord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import toyChord.config.*;
import toyChord.messages.*;

public class Node {
    private final NodeAddress myAddress;
    private NodeAddress prevAddress, nextAddress;
    private ServerSocket socket;
    private ArrayList<KVPair> data;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReadLock rl = lock.readLock();
    private final WriteLock wl = lock.writeLock();

    // (Every record is replicated to the next K nodes)
    // A list that contains all replicas that the node keeps
    private ArrayList<KVPair> replicas;
    // the id range that the (K-1)th node before is responsible for
    // (in linearizability I have to answer for that key range as
    // I am the last of that Key-Range replication sequence)
    private String tailLowId;
    private String tailHighId;
    // private boolean isLeader;

    // Just some setters/getters needed
    public String getTailLowId() {
        return tailLowId;
    }

    public String getTailHighId() {
        return tailHighId;
    }

    public ArrayList<KVPair> getData() {
        return data;
    }

    public ArrayList<KVPair> getReplicas() {
        return replicas;
    }

    public NodeAddress getMyAddress() {
        return myAddress;
    }

    public NodeAddress getNextAddress() {
        return nextAddress;
    }

    public NodeAddress getPrevAddress() {
        return prevAddress;
    }

    public void setNextAddress(NodeAddress nextAddress) {
        this.nextAddress = nextAddress;
    }

    public void setPrevAddress(NodeAddress prevAddress) {
        this.prevAddress = prevAddress;
    }

    public void setTailLowId(String tailLowId) {
        this.tailLowId = tailLowId;
    }

    public void setTailHighId(String tailHighId) {
        this.tailHighId = tailHighId;
    }

    // compare hash values/strings
    public static boolean hashIsBigger(String a, String b) {
        int result = a.compareTo(b);
        if(result > 0){
            return true;
        } else {
            return false;
        }
    }

    public static boolean hashInRange(String low, String high, String hash) {
        // if higher than low and smaller or equal to high
        if(low.compareTo(hash) < 0 && high.compareTo(hash) > -1){
            return true;
        }
        // if low is higher than high (last range of values in the circle)
        // check if hash is higher than low or lower or equal than high
        if(low.compareTo(high) > 0 && (low.compareTo(hash) < 0 || high.compareTo(hash) > -1)){
            return true;
        }
        // else return false (not in range)
        return false;
    }

    // if the hash of the key is between my id and previous id
    // then it belongs to my data (not replicas)
    public boolean belongsToMe(String key) throws NoSuchAlgorithmException {
        String hash = NodeAddress.sha1(key);
        if(this.getNextAddress() == null || hashInRange(this.getPrevAddress().getId(), this.getMyAddress().getId(), hash)){
            return true;
        }
        return false;
    }

    // Simple function to find local ip (only for 192.168.0.0/16 networks)
    public static String ipDiscovery() throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration<InetAddress> ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement();
                String ip = i.getHostAddress();
                // System.out.println(ip);
                if (ip.startsWith("192.168.")) {
                    return ip;
                }
            }
        }
        System.out.println("Ip not recognised please try specifying it.");
        System.exit(0);
        // just for compiling reasons return null (it never happens :-))
        return null;
    }

    // it's used quite a lot so keep it as a separate function
    public static Message sendMessageGetResponse(NodeAddress dst, Message message)
            throws UnknownHostException, IOException, ClassNotFoundException {
        Socket s = new Socket(dst.getIp(), dst.getPort());
        ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream is = new ObjectInputStream(s.getInputStream());
        os.writeObject(message);
        Message response = (Message) is.readObject();
        s.close();
        return response;
    }

    // only receives OK as response
    public static void sendDemoMessage(NodeAddress dst, Message message)
            throws UnknownHostException, ClassNotFoundException, IOException {
        Message response = (Message) sendMessageGetResponse(dst, message);
        if (response.getType() != MessageType.OK) {
            System.out.println("Something went wrong, message received not OK.");
        }
    }

    public Node(String ip, int port) throws SocketException, NoSuchAlgorithmException {
        // specify this Node's address
        if (ip == null || ip.isEmpty()) {
            ip = ipDiscovery();
        }
        this.myAddress = new NodeAddress(ip, port);
        System.out.println("Initializing Node...");
        // try creating server socket on specified port
        try {
            this.socket = new ServerSocket(port);
            System.out.println("Created server socket successfully.");
        } catch (Exception e) {
            System.out.println("Unable to create socket. Please verify that socket number is not in used.");
            System.exit(0);
        }
        // first node has no data at startup
        this.data = new ArrayList<KVPair>();
        this.replicas = new ArrayList<KVPair>();
        System.out.println("Node starting with on: " + this.getMyAddress().toString() + "\nwith id: "
                + this.getMyAddress().getId());
    }

    public static void main(String[] args) throws NumberFormatException, NoSuchAlgorithmException, UnknownHostException,
            ClassNotFoundException, IOException {
        // Printing out the LOGO :D
        Config.printLogo();
        Node node = null;
        String leaderIp = null;
        int leaderPort = 0;
        // Initialize node on startup
        switch (args.length) {
            case 1: {
                // only my port is given and i am first
                node = new Node(null, Integer.parseInt(args[0]));
                break;
            }
            case 2: {
                // my port and ip are given and i am first
                node = new Node(args[0], Integer.parseInt(args[1]));
                break;
            }
            case 3: {
                // only my port and leader identity given
                node = new Node(null, Integer.parseInt(args[0]));
                leaderIp = args[1];
                leaderPort = Integer.parseInt(args[2]);
                break;
            }
            case 4: {
                // both mine ip and port and leader identity are given
                node = new Node(args[0], Integer.parseInt(args[1]));
                leaderIp = args[2];
                leaderPort = Integer.parseInt(args[3]);
                break;
            }
            default: {
                System.out.println("Argument for first node is ip and port number. If newtwork exist then provide"
                        + " additionally leader node ip and port number in this specific order.");
                System.exit(0);
            }
        }
        // start or join network accordingly
        if(leaderIp == null && leaderPort == 0) {
            // if first in network, every pointers set to null
            // prev and next node is the only one in the network
            node.setPrevAddress(null);
            node.setNextAddress(null);
            // Set key range to null if it is the only node in network
            node.setTailLowId(null);
            node.setTailHighId(null);
            System.out.println("Chord DHT network started successfully.");
        } else {
            // if i am not first, join existing network
            node.join(leaderIp, leaderPort);
        }
        // start server thread to listen for incoming requests
        new ServerListenerThread(node.socket, node).start();
        // update key ranges as needed
        // if I am not first and we are using replication
        if(node.getNextAddress() != null && Config.K > 1){
            // then start a key range update
            sendDemoMessage(node.getMyAddress(), new KRUpdateInitiate(node.getMyAddress(), Config.K+1));
        }
        // start listening to user input
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String input;
        // Just loop while listening to user input
        while (true) {
            try {
                input = stdIn.readLine();
                new CLI(node, input).start();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void join(String ip, int port) throws UnknownHostException, IOException,
            ClassNotFoundException, NoSuchAlgorithmException {
        NodeAddress prev, next = null;
        ItIsMe response;
        // connect to leader/bootstrap node join protocol
        System.out.println("Contacting leader node.");
        // read response with two node addresses and close socket
        response = (ItIsMe) sendMessageGetResponse(new NodeAddress(ip, port), new WhoIsIt());
        prev = response.getTargetAddress();
        next = response.getNextAddress();
        System.out.println("Attempting to join network.");
        // if only one node exists in network
        if(next == null){
            // I become his next and prev, and he becomes mine
            this.prevAddress = prev;
            this.nextAddress = prev;
            sendDemoMessage(prev, new Notify(this.getMyAddress(), MessageType.UPDATE_NEXT));
            sendDemoMessage(prev, new Notify(this.getMyAddress(), MessageType.UPDATE_PREV));
        } else {
            String myId = this.getMyAddress().getId();
            // More than one Node in chord so i have to find my place
            while(true){
                //System.out.println("Asking Node.");
                //System.out.println(hashIsBigger(prev.getId(), next.getId()));///////////////
                
                if(hashInRange(prev.getId(), next.getId(), myId)){
                    // if I find my place break the loop
                    break;
                }
                // ask who next node is and continue searching
                response = (ItIsMe) sendMessageGetResponse(next, new WhoIsIt());
                prev = response.getTargetAddress();
                next = response.getNextAddress();
            }
            // set my prev and next node addresses
            this.prevAddress = prev;
            this.nextAddress = next;
            // Notify prev and next for my arrival (TODO : CHECK IF NEEDS REVERSE ORDER)
            sendDemoMessage(prev, new Notify(this.getMyAddress(), MessageType.UPDATE_NEXT));
            sendDemoMessage(next, new Notify(this.getMyAddress(), MessageType.UPDATE_PREV));
        }
        // also set low and high tailKR on prev NodeId
        // so we can fetch all replicas needed later
        this.tailLowId = this.getPrevAddress().getId();
        this.tailHighId = this.getPrevAddress().getId();
        System.out.println("Joined successfully.");
    }

    public void depart() throws UnknownHostException, ClassNotFoundException, IOException, NoSuchAlgorithmException {
        System.out.println("Node departing...");
        if(prevAddress == null && this.nextAddress == null){
            System.out.println("Network shutdown successfull.");
            System.exit(0);
        } else {
            NodeAddress prev = this.getPrevAddress();
            NodeAddress next = this.getNextAddress();

            if(Config.K == 1){
                // Transfer my data to the next node before leaving
                // but only needed if no replication is used
                this.transferAll(next);
            }
            // if ONLY one node will be present after departure
            if(prev.equals(next)){
                sendDemoMessage(prev, new Notify(null, MessageType.UPDATE_NEXT));
                sendDemoMessage(prev, new Notify(null, MessageType.UPDATE_PREV));
                // if replication is used update his key range to null (last node)
                if(Config.K > 1){
                    sendDemoMessage(prev, new UpdateKR(null, null));
                }
            } else {
                // else update next and previous accordingly
                sendDemoMessage(prev, new Notify(next, MessageType.UPDATE_NEXT));
                sendDemoMessage(next, new Notify(prev, MessageType.UPDATE_PREV));
                if(Config.K > 1){
                    // start key range update for the next K (at most) nodes if replication is used
                    sendDemoMessage(this.getNextAddress(), new KRUpdateInitiate(this.getNextAddress(), Config.K));
                }
            }
            System.out.println("Node shutdown successfull.");
            System.exit(0);
        }
    }

    // if someone joined before me
    public void dataTransfer(NodeAddress dst) throws NoSuchAlgorithmException, UnknownHostException,
            ClassNotFoundException, IOException {
        //find what to transfer
        ArrayList<KVPair> toSend = new ArrayList<KVPair>();
        this.rl.lock();
        for(KVPair p : this.data){
            if(!belongsToMe(p.getKey())){
                toSend.add(p);
            }
        }
        this.rl.unlock();
        this.wl.lock();
        this.data.removeAll(toSend);
        // if we have data replication, also put them in your replicas
        if(Config.K > 1){
            this.replicas.addAll(toSend);
        }
        this.wl.unlock();
        //System.out.println("Collected data for transfer.");
        // send data to dst Node
        sendDemoMessage(dst, new DataTransfer(toSend));
        //System.out.println("Transfer complete");
    }

    // transfer all my data before I depart
    public void transferAll(NodeAddress dst) throws UnknownHostException, ClassNotFoundException, IOException {
        this.wl.lock();
        sendDemoMessage(dst, new DataTransfer(this.data));
        // no need to unlock I am leaving
    }

    public void dataReceive(DataTransfer message) {
        this.wl.lock();
        this.data.addAll(message.getData());
        this.wl.unlock();
    }

    // the next <ttl> nodes need to find their new tailKeyRange
    public void findKeyRange(KRUpdateInitiate message)
            throws UnknownHostException, ClassNotFoundException, IOException {
        // throw a request backwards to ask for my tail key range
        sendDemoMessage(this.prevAddress, new AskKeyRange(this.getMyAddress()));
        // continue forwarding if more nodes need to update their key range
        if(message.getTtl() > 1 && !message.getOrigin().equals(this.getNextAddress())) {
            message.reduceTtl();
            sendDemoMessage(this.getNextAddress(), message);
        }
    }

    // if i am responsible then send key range to origin else forward it
    public void tellKeyRange(AskKeyRange message) throws UnknownHostException, ClassNotFoundException, IOException {
        if(message.getTtl() == 1 || message.getOrigin().equals(this.getPrevAddress())) {
            sendDemoMessage(message.getOrigin(), new UpdateKR(this.getPrevAddress().getId(), this.getMyAddress().getId()));
        } else {
            message.reduceTtl();
            sendDemoMessage(this.getPrevAddress(), message);
        }
    }

    public void updateKR(UpdateKR message)
            throws NoSuchAlgorithmException, UnknownHostException, ClassNotFoundException, IOException {
        String oldLow = this.tailLowId;
        this.tailLowId = message.getLow();
        this.tailHighId = message.getHigh();
        // after updating you tail key range fix the replica list as needed
        // (KR updates happen only if using replication so need to check that)
        fixReplicas(oldLow, message.getLow());
    }

    public void overlay(Overlay message) throws UnknownHostException, IOException,
            ClassNotFoundException {
        if(message.contains(this.getMyAddress())){
            System.out.println("Chord DHT network overlay starting from me:");
            for(NodeAddress a : message.getAddresses()){
                System.out.println(a.toString());
            }
        } else {
            message.addAddress(this.getMyAddress());
            sendDemoMessage(this.nextAddress, message);
        }
    }

    public void query(Query message)
            throws NoSuchAlgorithmException, UnknownHostException, ClassNotFoundException, IOException {
        String hash = NodeAddress.sha1(message.getKey());
        if(Config.K == 1 || this.getNextAddress() == null){
            if(belongsToMe(message.getKey())){
                String value = null;
                this.rl.lock();
                for(KVPair p : this.data){
                    if(p.getKey().equals(message.getKey())){
                        value = p.getValue();
                        break;
                    }
                }
                this.rl.unlock();
                message.answer(this.myAddress, value);
                sendDemoMessage(message.getOrigin(), message);
            } else {
                sendDemoMessage(this.nextAddress, message);
            }
        } else {
            if(Config.repType == ConsistencyType.LINEARIZABILITY && hashInRange(this.tailLowId, tailHighId, hash)){
                // if linearizability and I am last replica manager for Key Value pair then respond
                String value = null;
                this.rl.lock();
                for(KVPair p : this.replicas){
                    if(p.getKey().equals(message.getKey())){
                        value = p.getValue();
                        break;
                    }
                }
                this.rl.unlock();
                message.answer(this.myAddress, value);
                sendDemoMessage(message.getOrigin(), message);
            } else if(Config.repType == ConsistencyType.EVENTUAL && hashInRange(this.tailLowId, this.getMyAddress().getId(), hash)){
                String value = null;
                this.rl.lock();
                for(KVPair p: this.data){
                    if(p.getKey().equals(message.getKey())){
                        value = p.getValue();
                        break;
                    }
                }
                for(KVPair p: this.replicas){
                    if(p.getKey().equals(message.getKey())){
                        value = p.getValue();
                        break;
                    }
                }
                this.rl.unlock();
                message.answer(this.myAddress, value);
                sendDemoMessage(message.getOrigin(), message);
            } else {
                sendDemoMessage(this.nextAddress, message);
            }
        }
    }

    public void printQueryResponse(Query response){
        String result;
        if(response.getValue() == null){
            result = "\"" + response.getKey() + "\" Not Found. ";
        } else {
            result = "<" + response.getKey() + "," + response.getValue() + "> ";
        }
        System.out.println(result + "Response from Node : " + response.getResponder().toString());
    }

    // read-locks data before accessing them
    public void queryAll(QueryAll message) throws UnknownHostException, ClassNotFoundException, IOException {
        if(message.contains(this.getMyAddress())){
            System.out.println("Chord DHT contains:");
            System.out.print(message.toString());
        } else {
            this.rl.lock();
            message.addAddrData(this.myAddress, this.data);
            this.rl.unlock();
            sendDemoMessage(this.nextAddress, message);
        }
    }

    public void insert(Insert message) throws UnknownHostException, ClassNotFoundException, IOException,
            NoSuchAlgorithmException {
        if(belongsToMe(message.getPair().getKey())){
            KVPair pair = message.getPair();
            // check if it already exists (after taking read lock)
            this.rl.lock();
            boolean exists = this.data.contains(pair);
            this.rl.unlock();
            // firstly get write lock. if exists then remove
            this.wl.lock();
            if(exists){
                this.data.remove(pair);
            }
            // add new pair and unlock
            this.data.add(pair);
            this.wl.unlock();
            // if eventual consistency or no replication then reply to user (or if no other node exists)
            if(Config.K == 1 || Config.repType == ConsistencyType.EVENTUAL || this.getNextAddress() == null){
                message.answer(this.myAddress);
                sendDemoMessage(message.getOrigin(), message);
            }
            // if replication is used forward replica to k-1 nodes
            if(Config.K > 1 && this.getNextAddress() != null){
                sendDemoMessage(this.getNextAddress(), new AddReplica(this.getMyAddress(), pair, message.getOrigin()));
            }
        } else {
            sendDemoMessage(this.nextAddress, message);
        }
    }

    public void insertReplica(AddReplica message) throws UnknownHostException, ClassNotFoundException, IOException {
        // insert into your replicas
        KVPair pair = message.getPair();
            // check if it already exists (after taking read lock)
            this.rl.lock();
            boolean exists = this.replicas.contains(pair);
            this.rl.unlock();
            // firstly get write lock. if exists then remove
            this.wl.lock();
            if(exists){
                this.replicas.remove(pair);
            }
            // add new pair and unlock
            this.replicas.add(pair);
            this.wl.unlock();
        if(message.getTtl() > 1 && !this.getNextAddress().equals(message.getOrigin())){
            // forward it if you have to
            message.reduceTtl();
            sendDemoMessage(this.getNextAddress(), message);
        } else if(Config.repType == ConsistencyType.LINEARIZABILITY) {
            // else you are last to receive and use linearizability then respond to client
            Insert response = new Insert(message.getRespondTo(), pair);
            response.answer(this.getMyAddress());
            sendDemoMessage(message.getRespondTo(), response);
        }
    }

    public void printInsertResponse(Insert response) {
        System.out.println(response.getPair().toString() + " inserted. Response from Node : " + response.getResponder().toString());
    }

    public void delete(Delete message)
            throws NoSuchAlgorithmException, UnknownHostException, ClassNotFoundException, IOException {
        if(belongsToMe(message.getKey())){
            String result;
            KVPair tmp = new KVPair(message.getKey(),"");
            this.rl.lock();
            boolean exists = this.data.contains(tmp);
            this.rl.unlock();
            if(exists){
                this.wl.lock();
                this.data.remove(tmp);
                this.wl.unlock();
                result = "Removed Succefully";

                // if eventual consistency or no replication then reply to user (or if no other node exists)
                if(Config.K == 1 || Config.repType == ConsistencyType.EVENTUAL || this.getNextAddress() == null){
                    message.answer(this.myAddress, result);
                    sendDemoMessage(message.getOrigin(), message);
                }
                // if replication is used delete the next k-1 replicas
                if(Config.K > 1 && this.getNextAddress() != null){
                    sendDemoMessage(this.getNextAddress(), new DeleteReplica(this.getMyAddress(), message.getKey(), message.getOrigin()));
                }

            } else {
                // if it does not exist no need to check for replicas on next nodes to delete
                result = "Not Found";
                message.answer(this.myAddress, result);
                sendDemoMessage(message.getOrigin(), message);
            }
            
        } else {
            sendDemoMessage(this.nextAddress, message);
        }
    }

    public void deleteReplica(DeleteReplica message) throws UnknownHostException, ClassNotFoundException, IOException {
        String result = "Removed Succefully";
        KVPair tmp = new KVPair(message.getKey(),"");
        // remove pair from replicas
        this.wl.lock();
        this.replicas.remove(tmp);
        this.wl.unlock();
        // forward if needed, or else answer to client
        if(message.getTtl() > 1 && !this.getNextAddress().equals(message.getOrigin())){
            // forward it if you have to
            message.reduceTtl();
            sendDemoMessage(this.getNextAddress(), message);
        } else if(Config.repType == ConsistencyType.LINEARIZABILITY) {
            // else you are last to receive and use linearizability then respond to client
            Delete response = new Delete(message.getRespondTo(), message.getKey());
            response.answer(this.getMyAddress(), result);
            sendDemoMessage(message.getRespondTo(), response);
        }
    }

    public void printDeleteResponse(Delete response) {
        System.out.println("\"" + response.getKey() + "\" " + response.getResult() + ". Response from Node : " + response.getResponder().toString());
    }

    public void fixReplicas(String oldLow, String newLow) throws NoSuchAlgorithmException, UnknownHostException,
            ClassNotFoundException, IOException {
        // (transfer from data to replicas si done upon data transfer to joining node so no need to check that here)
        if(newLow == null){
            // if I am last, all my replicas go to my data
            this.wl.lock();
            this.data.addAll(this.replicas);
            this.replicas = new ArrayList<KVPair>();
            this.wl.unlock();
        } else if(oldLow != null) { // if I was alone and someone joined no need for me to update anything, (already had all data)
            // if someone departed, then we need to transfer pairs from replicas to data
            // (no need for him to send the data as we have them as replicas)
            ArrayList<KVPair> fromReplicaToData = new ArrayList<KVPair>();
            ArrayList<KVPair> replicasToDelete = new ArrayList<KVPair>();
            this.rl.lock();
            // get every pair that needs transfer from replicas to data
            for(KVPair p : this.replicas){
                if(belongsToMe(p.getKey())){
                    fromReplicaToData.add(p);
                    replicasToDelete.add(p);
                } else if(!hashInRange(newLow, this.getPrevAddress().getId(), NodeAddress.sha1(p.getKey()))){
                    replicasToDelete.add(p);
                }
            }
            this.rl.unlock();
            this.wl.lock();
            // add to data
            this.data.addAll(fromReplicaToData);
            // remove not needed replicas
            this.replicas.removeAll(replicasToDelete);
            this.wl.unlock();
            // also request from previous node any replicas that you need but don't have
            if(!hashInRange(oldLow, this.getPrevAddress().getId(), newLow)){
                //System.out.println("Asking for replicas after joining");
                sendDemoMessage(this.getPrevAddress(), new RequestDataOfRange(newLow, oldLow));
            }
        } 
    }

    public ArrayList<KVPair> fetchDataOfRange(String low, String high) throws NoSuchAlgorithmException {
        ArrayList<KVPair> ret = new ArrayList<KVPair>();
        this.rl.lock();
        for(KVPair p : this.data) {
            if(hashInRange(low, high, NodeAddress.sha1(p.getKey()))){
                ret.add(p);
            }
        }
        for(KVPair p : this.replicas) {
            if(hashInRange(low, high, NodeAddress.sha1(p.getKey()))){
                ret.add(p);
            }
        }
        this.rl.unlock();
        return ret;
    }

    public void receiveReplicas(ReplicaTransfer m) {
        this.wl.lock();
        this.replicas.addAll(m.getData());
        this.wl.unlock();
    }

}