package toyChord.messages;

import java.util.ArrayList;

import toyChord.NodeAddress;

public class Overlay extends Message {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final MessageType type = MessageType.OVERLAY;

    private ArrayList<NodeAddress> addresses;

    public Overlay(ArrayList<NodeAddress> addresses){
        this.addresses = addresses;
    }

    public MessageType getType() {
        return type;
    }

    public ArrayList<NodeAddress> getAddresses() {
        return addresses;
    }
    
}
