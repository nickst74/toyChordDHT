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
//import java.util.List;

import toyChord.messages.*;

public class Node {
    private NodeAddress myAddress, prevAddress, nextAddress;
    private ServerSocket socket;
    private boolean isLeader;

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
        // prev and next node is the only one in the network
        this.prevAddress = null;
        this.nextAddress = null;
        this.isLeader = true;
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
        try {
            System.out.println("Creating server socket.");
            this.socket = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println("Unable to create socket. Please verify that socket number is not in used.");
            System.exit(0);
        }
        this.isLeader = false;
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

    private void notify(String targetIp, int targetPort, NodeAddress newAddress, MessageType type)
            throws UnknownHostException, IOException, ClassNotFoundException {
        Message response;
        Socket s = new Socket(targetIp, targetPort);
        ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream is = new ObjectInputStream(s.getInputStream());
        os.writeObject(new Notify(newAddress, type));
        response = (Message) is.readObject();
        if(response.getType() != MessageType.OK){
            System.out.println("Some shit went wrong while notifying neighbors.");
        }
        s.close();

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
            System.out.println("I am second node.");
            // I become his next and prev
            this.prevAddress = prev;
            this.nextAddress = prev;
            notify(ip, port, this.myAddress, MessageType.UPDATE_NEXT);
            notify(ip, port, this.myAddress, MessageType.UPDATE_PREV);
        } else {
            String myId = this.getMyAddress().getId();
            // More than one Node in chord so i have to find my place
            while(true){
                System.out.println("Asking Node.");
                System.out.println(hashIsBigger(prev.getId(), next.getId()));///////////////
                
                if(
                    //if my id is between next and prev node
                    (hashIsBigger(myId, prev.getId()) && hashIsBigger(next.getId(), myId))
                    ||
                    // or next is smaller than prev and i have to go between them
                    (hashIsBigger(prev.getId(), next.getId()) && (hashIsBigger(myId, prev.getId()) || hashIsBigger(next.getId(), myId)))){
                    System.out.println("Found my place.");
                    break;
                }
                // ask who next node is and continue searching
                s = new Socket(next.getIp(), next.getPort());
                os = new ObjectOutputStream(s.getOutputStream());
                is = new ObjectInputStream(s.getInputStream());
                // send who is it message to find next id range
                os.writeObject(new WhoIsIt());
                // read response with two node addresses and close socket
                response = (ItIsMe) is.readObject();
                s.close();
                prev = response.getTargetAddress();
                next = response.getNextAddress();
            }
            // set my prev and next node addresses
            this.prevAddress = prev;
            this.nextAddress = next;
            // Notify prev and next for my arrival
            notify(next.getIp(), next.getPort(), this.myAddress, MessageType.UPDATE_PREV);
            notify(prev.getIp(), prev.getPort(), this.myAddress, MessageType.UPDATE_NEXT);
        }
        System.out.println("Joined successfully.");
        // TODO: here goes the data transfer upon join
    }


    public void depart() throws UnknownHostException, ClassNotFoundException, IOException {
        System.out.println("Node departing...");
        if(!this.isLeader){
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
        }
    }

    public void overlay(ArrayList<NodeAddress> message) throws UnknownHostException, IOException,
            ClassNotFoundException {
        if(message.contains(this.getMyAddress())){
            System.out.println("Chord DHT network overlay starting from me:");
            for(NodeAddress a : message){
                System.out.println(a.toString());
            }
        } else {
            message.add(this.getMyAddress());
            Socket s = new Socket(this.nextAddress.getIp(), this.nextAddress.getPort());
            ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream is = new ObjectInputStream(s.getInputStream());
            os.writeObject(new Overlay(message));
            Message response = (Message) is.readObject();
            if(response.getType() != MessageType.OK){
                System.out.println("Some shit went wrong while passing on overlay message.");
            }
            s.close();
        }
    }
    
}