package toyChord;

import java.io.IOException;
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

import toyChord.messages.*;

public class Node {
    private NodeAddress myAddress, prevAddress, nextAddress;
    private ServerSocket socket;
    private ArrayList<KVPair> data;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReadLock rl = lock.readLock();
    private final WriteLock wl = lock.writeLock();

    // TODO : Replicas not implimented yet
    // (Every record is replicated to the next K nodes)
    // A list that contains all replicas that the node keeps
    //private ArrayList<KVPair> replicas;
    // The last node I have replicas from, is responsible for
    // the ids in this key range
    //private String tailLow;
    //private String tailHigh;
    //private boolean isLeader;

    // Simple function to find local ip (only for 192.168.0.0/16 networks)
    public String ipDiscovery() throws SocketException {
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
        return null;
    }

    public ArrayList<KVPair> getData() {
        return data;
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

    // if first node in network
    public Node(String ip, int port) throws NoSuchAlgorithmException, SocketException {
        // specify this Node's address
        if (ip == null || ip.isEmpty()) {
            ip = ipDiscovery();
        }
        this.myAddress = new NodeAddress(ip, port);
        System.out.println("Node starting with id: "+this.myAddress.getId());
        // try creating server socket on specified port
        try {
            System.out.println("Creating server socket.");
            this.socket = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println("Unable to create socket. Please verify that socket number is not in used.");
            System.exit(0);
        }
        // first node has no data at startup
        this.data = new ArrayList<KVPair>();
        // prev and next node is the only one in the network
        this.prevAddress = null;
        this.nextAddress = null;
        //this.isLeader = true;
    }

    // if not first node in network, start join from given bootstrap node
    public Node(String ip, int port, String leaderIp, int leaderPort)
            throws NoSuchAlgorithmException, UnknownHostException, IOException {
        // specify this Node's address and the leader Node address (they dont change
        // later on)
        if (ip == null || ip.isEmpty()) {
            ip = ipDiscovery();
        }
        this.myAddress = new NodeAddress(ip, port);
        System.out.println("Node starting with id: "+this.myAddress.getId());
        // Itialize it but keep write lock until you get your values from next node TODO LOCKKKK
        this.data = new ArrayList<KVPair>();
        try {
            System.out.println("Creating server socket.");
            this.socket = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println("Unable to create socket. Please verify that socket number is not in used.");
            System.exit(0);
        }
        //this.isLeader = false;
        // start from leader node and find correct position in chord
        try {
            this.join(leaderIp, leaderPort);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to join network. Please verify leader node address.");
            System.exit(0);
        }
    }

    public static boolean hashIsBigger(String a, String b){
        int result = a.compareTo(b);
        if(result > 0){
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws NumberFormatException, NoSuchAlgorithmException, IOException {
        //// INITIALIZE NODE ////
        // TODO: Check given IP address and Port number not implemented
        Node node = null;
        switch (args.length) {
            case 1: {
                node = new Node(null, Integer.parseInt(args[0]));
                break;
            }
            case 2: {
                node = new Node(args[0], Integer.parseInt(args[1]));
                break;
            }
            case 3: {
                node = new Node(null, Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
                break;
            }
            case 4: {
                node = new Node(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
                break;
            }
            default: {
                System.out.println("Argument for first node is ip and port number. If newtwork exist then provide"
                        + " additionally leader node ip and port number in this specific order.");
                System.exit(0);
            }
        }
        // System.out.println(node.myAddress.toString());
        // System.out.println(node.leaderAdderss.toString());
        // System.out.println(node.myAddress.getId());
        //// START A USER INPUT-SCANNER THREAD ////
        new CLI(node).start();
        //// LOOP WHILE ACCEPTING INCOMING CONNECTIONS ON OPEN SERVER SOCKET ////
        System.out.println("Listening on server socket.");
        while (true) {
            // ACCEPT INCOMING CONNECTIONS ON OPEN SOCKET
            Socket serverSocket = node.socket.accept();
            //System.out.println("Connection incoming.");
            new ServerThread(serverSocket, node).start();
        }
    }

    // Send message and wait for an OK response (because it is use quite a lot)
    public static void sendDemoMessage(NodeAddress addr, Message message) throws UnknownHostException, IOException,
            ClassNotFoundException {
        Socket s = new Socket(addr.getIp(), addr.getPort());
        ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream is = new ObjectInputStream(s.getInputStream());
        os.writeObject(message);
        Message response = (Message) is.readObject();
        if(response.getType() != MessageType.OK){
            System.out.println("Some shit went wrong while passing on message.");
        }
        s.close();
    }

    private void notify(NodeAddress dst, NodeAddress newAddress, MessageType type)
            throws UnknownHostException, IOException, ClassNotFoundException {
        sendDemoMessage(dst, new Notify(newAddress, type));
    }

    private void join(String ip, int port) throws UnknownHostException, IOException,
            ClassNotFoundException {
        System.out.println("Joining...");
        NodeAddress prev, next = null;
        ItIsMe response;
        // connect to leader to bootstrap node join protocol
        System.out.println("Contacting leader node.");
        Socket s = new Socket(ip, port);
        ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream is = new ObjectInputStream(s.getInputStream());
        // send who is it message to find next id range
        os.writeObject(new WhoIsIt());
        // read response with two node addresses and close socket
        response = (ItIsMe) is.readObject();
        s.close();
        prev = response.getTargetAddress();
        next = response.getNextAddress();
        System.out.println("Attempting to join network.");
        // if only one node exists in network
        if(next == null){
            //System.out.println("I am second node.");
            // I become his next and prev
            this.prevAddress = prev;
            this.nextAddress = prev;
            notify(prev, this.myAddress, MessageType.UPDATE_NEXT);
            notify(prev, this.myAddress, MessageType.UPDATE_PREV);
        } else {
            String myId = this.getMyAddress().getId();
            // More than one Node in chord so i have to find my place
            while(true){
                //System.out.println("Asking Node.");
                //System.out.println(hashIsBigger(prev.getId(), next.getId()));///////////////
                
                if(
                    //if my id is between next and prev node
                    (hashIsBigger(myId, prev.getId()) && hashIsBigger(next.getId(), myId))
                    ||
                    // or next is smaller than prev and i have to go between them
                    (hashIsBigger(prev.getId(), next.getId()) && (hashIsBigger(myId, prev.getId()) || hashIsBigger(next.getId(), myId)))){
                    //System.out.println("Found my place.");
                    break;
                }
                // ask who next node is and continue searching
                s = new Socket(next.getIp(), next.getPort());
                os = new ObjectOutputStream(s.getOutputStream());
                is = new ObjectInputStream(s.getInputStream());
                // send who is it message to find next id range
                os.writeObject(new WhoIsIt());
                // read response with two node addresses and close socket
                // (NO CHECK DONE IF IT FAIL... but it should not in the perfect world that we live in)
                response = (ItIsMe) is.readObject();
                s.close();
                prev = response.getTargetAddress();
                next = response.getNextAddress();
            }
            // set my prev and next node addresses
            this.prevAddress = prev;
            this.nextAddress = next;
            // Notify prev and next for my arrival
            notify(next, this.myAddress, MessageType.UPDATE_PREV);
            notify(prev, this.myAddress, MessageType.UPDATE_NEXT);
        }
        // TODO: here goes the data transfer upon join
        System.out.println("Joined successfully.");
    }


    public void depart() throws UnknownHostException, ClassNotFoundException, IOException, NoSuchAlgorithmException {
        System.out.println("Node departing...");
        if(prevAddress == null && this.nextAddress == null){
            System.out.println("Network shutdown successfull.");
            System.exit(0);
        } else {
            // TODO: re-arrange files as needed before departure
            NodeAddress prev = this.getPrevAddress();
            NodeAddress next = this.getNextAddress();

            // Transfer my data to the next node before leaving
            this.transferAll(next);
            
            // if ONLY one node will be present after departure
            if(prev.equals(next)){
                notify(prev, null, MessageType.UPDATE_NEXT);
                notify(prev, null, MessageType.UPDATE_PREV);
            } else {
                // else update next and previous accordingly
                notify(prev, next, MessageType.UPDATE_NEXT);
                notify(next, prev, MessageType.UPDATE_PREV);
            }
            System.out.println("Node shutdown successfull.");
            System.exit(0);
        }
        /*if(!this.isLeader){
            // TODO: re-arrange files as needed before departure
            NodeAddress prev = this.getPrevAddress();
            NodeAddress next = this.getNextAddress();
            // if ONLY leader node will be present after departure
            if(prev.equals(next)){
                notify(prev.getIp(), prev.getPort(), null, MessageType.UPDATE_NEXT);
                notify(prev.getIp(), prev.getPort(), null, MessageType.UPDATE_PREV);
            } else {
                // else update next and previous accordingly
                notify(prev.getIp(), prev.getPort(), next, MessageType.UPDATE_NEXT);
                notify(next.getIp(), next.getPort(), prev, MessageType.UPDATE_PREV);
            }
            System.out.println("Node shutdown successfull.");
            System.exit(0);
        }
        else if(this.isLeader && prevAddress == null && this.nextAddress == null){
            System.out.println("Network shutdown successfull.");
            System.exit(0);
        } else{
            System.out.println("Cannot shutdown network while other nodes are connected.");
        }*/
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

    // just check 
    public boolean belongsToMe(String key) throws NoSuchAlgorithmException {
        String hash = NodeAddress.sha1(key);
        if(this.getNextAddress() == null
            ||
            (hashIsBigger(this.myAddress.getId(), hash) && hashIsBigger(hash, this.prevAddress.getId()))
            ||
            (hashIsBigger(this.getPrevAddress().getId(), this.getMyAddress().getId())
                &&
                (hashIsBigger(hash, this.getPrevAddress().getId()) || hashIsBigger(this.getMyAddress().getId(), hash)))){
            return true;
        }
        return false;
    }

    public void query(Query message)
            throws NoSuchAlgorithmException, UnknownHostException, ClassNotFoundException, IOException {
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

            // TODO : check something like min replica id for eventual consistency
        } else {
            sendDemoMessage(this.nextAddress, message);
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

    // TODO: INSERT AND FIX REARRANGE FILES ON JOIN/DEPART

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
            message.answer(this.myAddress);
            sendDemoMessage(message.getOrigin(), message);
        } else {
            sendDemoMessage(this.nextAddress, message);
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
            } else {
                result = "Not Found";
            }
            message.answer(this.myAddress, result);
            sendDemoMessage(message.getOrigin(), message);
        } else {
            sendDemoMessage(this.nextAddress, message);
        }
    }

    public void printDeleteResponse(Delete response) {
        System.out.println("\"" + response.getKey() + "\" " + response.getResult() + ". Response from Node : " + response.getResponder().toString());
    }

    // TODO: **************** FIXFIXFIXFIX
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
        System.out.println("Collected data for transfer.");
        // send data to dst Node
        sendDemoMessage(dst, new DataTransfer(toSend));
        System.out.println("Transfer complete");
        // remove those records from my list
        this.wl.lock();
        this.data.removeAll(toSend);
        this.wl.unlock();
    }

    // transfer all my data before I depart
    public void transferAll(NodeAddress dst) throws UnknownHostException, ClassNotFoundException, IOException {
        //this.wl.lock();
        sendDemoMessage(dst, new DataTransfer(this.data));
        // no need to unlock I am leaving
    }

    public void dataReceive(DataTransfer message) {
        this.data.addAll(message.getData());
        //this.wl.unlock();
    }
    
}