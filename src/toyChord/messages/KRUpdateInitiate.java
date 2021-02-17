package toyChord.messages;

import toyChord.NodeAddress;

// This message is forwarded  backwards until ttl = 0 or origin is reached
public class KRUpdateInitiate extends Message {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    private final MessageType type = MessageType.KR_UPDATE_INITIATE;
    // keep the node from where the updates started (no overlapping circles if K > count(nodes))
    private final NodeAddress origin;
    private int ttl;

    public KRUpdateInitiate(NodeAddress addr, int ttl) {
        this.origin = addr;
        this.ttl = ttl;
    }

    public MessageType getType() {
        return type;
    }

    public NodeAddress getOrigin() {
        return origin;
    }

    public int getTtl() {
        return ttl;
    }

    public void reduceTtl() {
        this.ttl--;
    }

}
