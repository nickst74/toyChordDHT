package toyChord.messages;

import toyChord.KVPair;
import toyChord.NodeAddress;
import toyChord.config.Config;

public class AddReplica extends Message {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    private final MessageType type = MessageType.ADD_REPLICA;
    private int ttl;
    private final KVPair pair;
    private final NodeAddress origin, respondTo;

    public AddReplica(NodeAddress origin, KVPair pair, NodeAddress addr) {
        this.pair = pair;
        this.origin = origin;
        this.respondTo = addr;
        this.ttl = Config.K-1;
    }

    public KVPair getPair() {
        return pair;
    }

    public NodeAddress getOrigin() {
        return origin;
    }

    public int getTtl() {
        return ttl;
    }

    public NodeAddress getRespondTo() {
        return respondTo;
    }

    public MessageType getType() {
        return type;
    }

    public void reduceTtl() {
        this.ttl--;
    }
}
