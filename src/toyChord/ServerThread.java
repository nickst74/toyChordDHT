package toyChord;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import toyChord.messages.*;

public class ServerThread extends Thread {
    private Socket socket;
    private Node  node;

    public ServerThread(Socket socket, Node node){
        this.socket = socket;
        this.node = node;
    }

    public void  run() {
        try {
            Message request = null;
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            request = (Message) is.readObject();
            // decode message and act accordingly
            switch(request.getType()){
                case WHO_IS_IT: {
                    // send next node's and your address
                    os.writeObject(new ItIsMe(node.getMyAddress(), node.getNextAddress()));
                    break;
                }
                case UPDATE_NEXT: {
                    Notify m = (Notify) request;
                    if(m.getAddress() == null){
                        System.out.println("I am last.");
                    } else{
                        System.out.println("My next is:"+m.getAddress().toString());
                    }
                    this.node.setNextAddress(m.getAddress());
                    os.writeObject(new OK());
                    break;
                }
                case UPDATE_PREV: {
                    Notify m = (Notify) request;
                    if(m.getAddress() == null){
                        System.out.println("I am last.");
                    } else{
                        System.out.println("My prev is:"+m.getAddress().toString());
                    }
                    this.node.setPrevAddress(m.getAddress());
                    os.writeObject(new OK());
                    break;
                }
                case OVERLAY: {
                    System.out.println("Recieved overlay message.");
                    Overlay m = (Overlay) request;
                    os.writeObject(new OK());
                    this.node.overlay(m.getAddresses());
                    break;
                }
                default: {
                    System.out.println("Some shit went wrong at recieved message type.");
                }
            }
            // message exchange completed, close socket
            this.socket.close();
            } catch (Exception e) {
                //TODO: handle exception
                e.printStackTrace();
            } finally {
        }
    }
}
