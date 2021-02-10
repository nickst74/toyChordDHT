package toyChord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class CLI extends Thread {
    private Node node;

    // Initialize needed variables to run
    public CLI(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        // Just loop while listening to user input
        while (true) {
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String input;
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
    private void execute(String userInput) throws UnknownHostException, ClassNotFoundException, IOException {
        String[] tokens = userInput.split(" ");
        switch(tokens[0]){
            case "depart": {
                node.depart();
                break;
            }
            case "overlay": {
                node.overlay(new ArrayList<NodeAddress>());
                break;
            }
            case "help": {
                System.out.println("- depart : Local node will depart from chord dht network.");
                break;
            }
            default:{
                System.out.println("Unkown command. Press help for list of available commands.");
            }
        }
    }
}
