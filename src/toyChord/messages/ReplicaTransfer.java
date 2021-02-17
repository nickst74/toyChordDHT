package toyChord.messages;

import java.util.ArrayList;

import toyChord.KVPair;

public class ReplicaTransfer extends Message {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    private final MessageType type = MessageType.REPLICA_TRANSFER;
    private final ArrayList<KVPair> data;
    

    public ReplicaTransfer(ArrayList<KVPair> data){
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public ArrayList<KVPair> getData() {
        return data;
    }
}
