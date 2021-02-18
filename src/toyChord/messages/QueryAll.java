package toyChord.messages;

import java.io.Serializable;
import java.util.ArrayList;

import toyChord.KVPair;
import toyChord.NodeAddress;

public class QueryAll extends Message {
    /**
     *
     */
    private class AddrData implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private NodeAddress addr;
        private ArrayList<KVPair> pairs;

        public AddrData(NodeAddress addr, ArrayList<KVPair> pairs) {
            this.addr = addr;
            this.pairs = pairs;
        }

        public NodeAddress getAddr() {
            return addr;
        }

        public String toString() {
            String response = this.addr.toString() + " contains:\n";
            for (KVPair i : this.pairs) {
                try {
                    response = response + " " + i.toString() + " with id: " + NodeAddress.sha1(i.getKey()) + "\n";
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return response;
        }
    }

    private static final long serialVersionUID = 1L;

    private final MessageType type = MessageType.QUERY_ALL;

    private ArrayList<AddrData> data;

    public QueryAll(){
        this.data = new ArrayList<AddrData>();
    }

    public MessageType getType() {
        return type;
    }

    public boolean contains(NodeAddress addr) {
        for(AddrData i : data){
            if(i.getAddr().equals(addr)){
                return true;
            }
        }
        return false;
    }

    public void addAddrData(NodeAddress addr, ArrayList<KVPair> ls) {
        this.data.add(new AddrData(addr, ls));
    }

    public String toString() {
        String response = "";
        for(AddrData i : this.data) {
            response = response + i.toString();
        }
        return response;
    }
}
