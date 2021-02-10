package toyChord.messages;

import toyChord.NodeAddress;

public class Notify extends Message {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private MessageType type;
    private NodeAddress address;

    public Notify(NodeAddress address, MessageType type){
        this.type = type;
        this.address = address;
    }

    public MessageType getType() {
        return type;
    }

    public NodeAddress getAddress() {
        return address;
    }
}
