package toyChord.messages;

import toyChord.KVPair;
import toyChord.NodeAddress;

public class Insert extends Message {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private MessageType type;
    private KVPair pair;
    private final NodeAddress origin;
    private NodeAddress responder;

    public Insert(NodeAddress addr, KVPair pair) {
        this.type = MessageType.INSERT_REQUEST;
        this.pair = pair;
        this.origin = addr;
    }

    public KVPair getPair() {
        return pair;
    }

    public MessageType getType() {
        return type;
    }

    public NodeAddress getOrigin() {
        return origin;
    }

    public NodeAddress getResponder() {
        return responder;
    }

    public void answer(NodeAddress addr) {
        this.type = MessageType.INSERT_RESPONSE;
        this.responder = addr;
    }

}
