package toyChord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import toyChord.messages.Delete;
import toyChord.messages.Insert;
import toyChord.messages.Overlay;
import toyChord.messages.Query;
import toyChord.messages.QueryAll;

public class CLI extends Thread {
    private Node node;

    // Initialize needed variables to run
    public CLI(Node node) {
        this.node = node;
    }

    // TODO: Maybe we need a client thread to execute commands simultaneously
    @Override
    public void run() {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String input;
        // Just loop while listening to user input
        while (true) {
            try {
                input = stdIn.readLine();
                if (input != null && !input.isEmpty()) {
                    execute(input);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /////////// ALL POSSIBLE USER INPUT COMMANDS ///////////
    private void execute(String userInput)
            throws UnknownHostException, ClassNotFoundException, IOException, NoSuchAlgorithmException {
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
            case "help": {
                System.out.println("- delete <key> : Delete record with given key from DHT. <key> must a be string wrapped int quotes.\n"+
                                    "- depart : Local node will depart from chord DHT network.\n"+
                                    "- insert <key> <value> : Insert a key-value pair in DHT. Both arguments must be strings wrapped int quotes.\n"+
                                    "- overlay : Returns all nodes connected to the network in the correct order."+
                                    "- query <key> : Search for record of the spesified key in DHT. <key> must a be string wrapped int quotes."+
                                    " If '*' is given as key, it returns all key-value pairs from all nodes in DHT.\n");
                break;
            }
            default:{
                System.out.println("Unkown command '"+ tokens[0] +"'. Press help for list of available commands.");
            }
        }
    }
}
