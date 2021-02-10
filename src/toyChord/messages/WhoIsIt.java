package toyChord.messages;

// Used to get target and next node address
public class WhoIsIt extends Message {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final MessageType type = MessageType.WHO_IS_IT;

    public MessageType getType() {
        return type;
    }
}
