package toyChord.supplementary;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import toyChord.NodeAddress;
import toyChord.messages.*;

public class DemoMessageSender extends Thread {

    private NodeAddress dst;
    private Message message;

    public DemoMessageSender(NodeAddress dst, Message message){
        this.dst = dst;
        this.message = message;
    }

    // it's used quite a lot so keep it as a separate function
    public static Message sendMessageGetResponse(NodeAddress dst, Message message)
            throws UnknownHostException, IOException, ClassNotFoundException {
        Socket s = new Socket(dst.getIp(), dst.getPort());
        ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream is = new ObjectInputStream(s.getInputStream());
        os.writeObject(message);
        Message response = (Message) is.readObject();
        s.close();
        return response;
    }

    // only receives OK as response
    public static void sendDemoMessage(NodeAddress dst, Message message)
            throws UnknownHostException, ClassNotFoundException, IOException {
        Message response = (Message) sendMessageGetResponse(dst, message);
        if (response.getType() != MessageType.OK) {
            System.out.println("Something went wrong, message received not OK.");
        }
    }

    @Override
    public void run() {
        try {
            sendDemoMessage(dst, message);
        } catch (Exception e) {
            //TODO: handle exception
            e.printStackTrace();
        }
    }
    
}
