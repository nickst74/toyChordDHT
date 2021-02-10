package toyChord.messages;

public class OK extends Message {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final MessageType type = MessageType.OK;

    public MessageType getType() {
        return type;
    }
}
