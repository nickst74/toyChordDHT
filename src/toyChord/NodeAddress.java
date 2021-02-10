package toyChord;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NodeAddress implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String ip;
    private int port;
    private String id;

    public NodeAddress(String ip, int port) throws NoSuchAlgorithmException {
        this.ip = ip;
        this.port = port;
        this.id = sha1(ip + ':' + String.valueOf(port));
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return ip+":"+String.valueOf(port);
    }

    @Override
    public boolean equals(Object a){
        try{
            if(this.getId().equals(((NodeAddress) a).getId())){
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Encode String with SHA1 hash function and get desired id
	public static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }         
        return sb.toString();
    }
}