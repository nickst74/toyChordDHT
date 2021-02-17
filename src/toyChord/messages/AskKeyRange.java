package toyChord.messages;

import toyChord.NodeAddress;
import toyChord.config.Config;

public class AskKeyRange extends Message {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final MessageType type = MessageType.KR_ASK;
    private final NodeAddress origin;
    private int ttl;

    public AskKeyRange(NodeAddress addr) {
        this.origin = addr;
        this.ttl = Config.K - 1;
    }
    
    public int getTtl() {
        return ttl;
    }

    public MessageType getType() {
        return type;
    }

    public NodeAddress getOrigin() {
        return origin;
    }

    public void reduceTtl() {
        this.ttl--;
    }
}
