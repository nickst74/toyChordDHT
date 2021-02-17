package toyChord;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerListenerThread extends Thread {
    private final ServerSocket socket;
    private final Node node;

    public ServerListenerThread(ServerSocket socket, Node node) {
        this.socket = socket;
        this.node = node;
    }

    @Override
    public void run() {
        // just loop listening on incoming connections
        while(true) {
            try {
                // accept and generate a new thread for every connection
                Socket s = this.socket.accept();
                new ServerThread(s, this.node).start();
            } catch (Exception e) {
                //TODO: handle exception
            }
        }
    }
}
