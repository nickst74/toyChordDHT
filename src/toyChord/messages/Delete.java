package toyChord.messages;

import toyChord.NodeAddress;

public class Delete extends Message {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private String key;
    private String result;

    private final NodeAddress origin;
    private NodeAddress responder;

    // TODO : for experiments only
    private long endTime;

    public Delete(NodeAddress addr, String key) {
        this.type = MessageType.DELETE_REQUEST;
        this.key = key;
        this.origin = addr;
    }

    public void answer(NodeAddress addr, String result){
        this.type = MessageType.DELETE_RESPONSE;
        this.responder = addr;
        this.result = result;
        this.endTime = System.nanoTime();
    }

    public MessageType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getResult() {
        return result;
    }

    public NodeAddress getOrigin() {
        return origin;
    }

    public NodeAddress getResponder() {
        return responder;
    }

    public long getEndTime() {
        return endTime;
    }

}
