package toyChord.supplementary;

import java.net.SocketException;

import toyChord.Node;
import toyChord.NodeAddress;
import toyChord.config.Config;

public class DemoValues {
    public int issued, completed;
    public boolean eof, blocking;
    public long start, end;
    public NodeAddress myAddr;

    public DemoValues(boolean blocking) throws SocketException {
        this.blocking = blocking;
        this.issued = 0;
        this.completed = 0;
        this.eof = false;
        this.myAddr = new NodeAddress(Node.ipDiscovery(), Config.serverSocket);
    }
    
}
