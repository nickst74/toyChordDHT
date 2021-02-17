package toyChord.messages;

public class RequestDataOfRange extends Message {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final MessageType type = MessageType.REPLICA_REQUEST;
    private final String low, high;

    public RequestDataOfRange(String low, String high) {
        this.low = low;
        this.high = high;
    }

    public MessageType getType() {
        return type;
    }

    public String getLow() {
        return low;
    }

    public String getHigh() {
        return high;
    }
    
}
