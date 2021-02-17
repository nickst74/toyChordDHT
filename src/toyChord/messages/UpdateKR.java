package toyChord.messages;

public class UpdateKR extends Message {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    private final MessageType type = MessageType.KR_UPDATE;
    private final String low, high;

    public UpdateKR(String low, String high) {
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
