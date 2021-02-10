package toyChord.messages;

import toyChord.NodeAddress;

public class ItIsMe extends Message{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public final MessageType type = MessageType.IT_IS_ME;

    private NodeAddress targetAddress, nextAddress;

    public ItIsMe(NodeAddress my, NodeAddress next){
        this.targetAddress = my;
        this.nextAddress = next;
    }

    public MessageType getType() {
        return this.type;
    }

    public NodeAddress getTargetAddress() {
        return this.targetAddress;
    }

    public NodeAddress getNextAddress() {
        return this.nextAddress;
    }

}
