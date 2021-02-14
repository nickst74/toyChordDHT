package toyChord;

import java.io.Serializable;

public class KVPair implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    private String key;
    private String value;

    public KVPair(String key, String value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return "<"+this.key+","+this.value+">";
    }

    // we only depend on key for equality
    @Override
    public boolean equals(Object a) {
        try{
            KVPair p = (KVPair) a;
            if(this.getKey().equals(p.getKey())) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
