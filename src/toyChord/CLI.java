package toyChord;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import toyChord.messages.*;

public class CLI extends Thread {
    private Node node;
    private String userInput;

    // Initialize needed variables to run
    public CLI(Node node, String s) {
        this.node = node;
        this.userInput = s;
    }

    @Override
    public void run() {
        if(userInput != null && !userInput.isEmpty()) {
            try {
                execute();
            } catch (Exception e) {
                //TODO: handle exception
            }
        }
    }

    /////////// ALL POSSIBLE USER INPUT COMMANDS ///////////
    public void execute() throws UnknownHostException, ClassNotFoundException, NoSuchAlgorithmException, IOException {
        String[] tokens = userInput.split(" ");
        switch(tokens[0]){
            case "depart": {
                this.node.depart();
                break;
            }
            case "overlay": {
                ArrayList<NodeAddress> list = new ArrayList<NodeAddress>();
                if(node.getNextAddress() == null){
                    list.add(this.node.getMyAddress());
                }
                node.overlay(new Overlay(list));
                break;
            }
            case "query": {
                String [] tmp = userInput.split("\"");
                if(tokens[1].equals("*")){
                    QueryAll m = new QueryAll();
                    if(this.node.getNextAddress() == null){
                        m.addAddrData(this.node.getMyAddress(), this.node.getData());
                    }
                    this.node.queryAll(m);
                } else if(tmp.length < 2){
                    System.out.println("Query takes one string as argument wraped in quotes.");
                } else {
                    this.node.query(new Query(this.node.getMyAddress(), tmp[1]));
                }
                break;
            }
            case "insert": {
                String [] tmp = userInput.split("\"");
                if(tmp.length < 4){
                    System.out.println("Insert takes two strings as arguments wraped in quotes.");
                } else {
                    this.node.insert(new Insert(this.node.getMyAddress(), new KVPair(tmp[1], tmp[3])));
                }
                break;
            }
            case "delete": {
                String [] tmp = userInput.split("\"");
                if(tmp.length < 2){
                    System.out.println("Delete takes one strings as argument wraped in quotes.");
                } else {
                    this.node.delete(new Delete(this.node.getMyAddress(), tmp[1]));
                }
                break;
            }
            // only used for debugging reasons
            //case "printKR": {
            //    System.out.println("My key range is:\nfrom -> "+this.node.getTailLowId()+"\nto -> "+this.node.getTailHighId());
            //    break;
            //}
            // only used for debugging reasons but can still be usefull so we leave it be
            case "printAll":{
                System.out.println("Data in Node:");
                for(KVPair i : this.node.getData()){
                    System.out.println(" " + i.toString());
                }
                System.out.println("Replicas in Node:");
                for(KVPair i : this.node.getReplicas()){
                    System.out.println(" " + i.toString());
                }
                break;
            }
            case "help": {
                System.out.println("- delete <key> : Delete record with given key from DHT. <key> must a be string wrapped int quotes.\n"+
                                    "- depart : Local node will depart from chord DHT network.\n"+
                                    "- insert <key> <value> : Insert a key-value pair in DHT. Both arguments must be strings wrapped int quotes.\n"+
                                    "- overlay : Returns all nodes connected to the network in the correct order.\n"+
                                    "- printAll : Returns all data and replica that the node contains (for debugging and test reasons).\n"+
                                    "- query <key> : Search for record of the spesified key in DHT. <key> must a be string wrapped int quotes."+
                                    " If '*' is given as key, it returns all key-value pairs from all nodes in DHT.");
                break;
            }
            default:{
                System.out.println("Unkown command '"+ tokens[0] +"'. Press help for list of available commands.");
            }
        }
    }
}
