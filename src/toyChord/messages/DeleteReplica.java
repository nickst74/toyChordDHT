package toyChord.messages;

import toyChord.NodeAddress;
import toyChord.config.Config;

public class DeleteReplica extends Message {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final MessageType type = MessageType.DELETE_REPLICA;
    private int ttl;
    private final String key;
    private final NodeAddress origin, respondTo;

    public DeleteReplica(NodeAddress origin, String key, NodeAddress addr) {
        this.key = key;
        this.origin = origin;
        this.respondTo = addr;
        this.ttl = Config.K-1;
    }

    public String getKey() {
        return key;
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
