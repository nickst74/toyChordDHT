package toyChord.messages;

import toyChord.NodeAddress;

public class Query extends Message {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private String key;
    private String value;

    private final NodeAddress origin;
    private NodeAddress responder;

    public MessageType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public NodeAddress getOrigin() {
        return origin;
    }

    public NodeAddress getResponder() {
        return responder;
    }

    public Query(NodeAddress addr, String key){
        this.type = MessageType.QUERY_REQUEST;
        this.origin = addr;
        this.key = key;
        this.responder = null;
        this.value = null;
    }

    public void answer(NodeAddress addr, String value){
        this.type = MessageType.QUERY_RESPONSE;
        this.responder = addr;
        this.value = value;
    }
}
