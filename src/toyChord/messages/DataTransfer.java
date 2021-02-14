package toyChord.messages;

import java.util.ArrayList;

import toyChord.KVPair;

public class DataTransfer extends Message {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final MessageType type = MessageType.DATA_TRANSFER;

    private ArrayList<KVPair> data;

    public DataTransfer(ArrayList<KVPair> data){
        this.data = data;
    }
    
    public MessageType getType() {
        return type;
    }

    public ArrayList<KVPair> getData() {
        return data;
    }
}
