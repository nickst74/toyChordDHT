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

    // TODO: REVERSE ORDER OF OPERATION AND RESPONSE //////// WTF DID I DO O.o //////// or maybe not, RE-EVALUATE APPROACH
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
                        System.out.println("My next is:"+m.getAddress().toString() + " with id: " + m.getAddress().getId());
                    }
                    this.node.setNextAddress(m.getAddress());
                    os.writeObject(new OK());
                    break;
                }
                case UPDATE_PREV: {
                    Notify m = (Notify) request;
                    // check if someone joined before me
                    boolean transferNeeded = m.getAddress() != null && (this.node.getPrevAddress() == null || this.node.belongsToMe(m.getAddress().toString()));
                    this.node.setPrevAddress(m.getAddress());
                    os.writeObject(new OK());
                    if(m.getAddress() == null){
                        System.out.println("I am last.");
                    } else{
                        System.out.println("My prev is:"+m.getAddress().toString() + " with id: " + m.getAddress().getId());
                    }
                    // if someone joined send him his data
                    if(transferNeeded){
                        this.node.dataTransfer(m.getAddress());
                    }
                    break;
                }
                case OVERLAY: {
                    System.out.println("Recieved overlay message.");
                    Overlay m = (Overlay) request;
                    os.writeObject(new OK());
                    this.node.overlay(m);
                    break;
                }
                case QUERY_REQUEST :  {
                    System.out.println("Recieved query request.");
                    Query m = (Query) request;
                    os.writeObject(new OK());
                    this.node.query(m);
                    break;
                }
                case QUERY_RESPONSE : {
                    System.out.println("Recieved query response.");
                    Query m = (Query) request;
                    os.writeObject(new OK());
                    this.node.printQueryResponse(m);
                    break;
                }
                case QUERY_ALL : {
                    System.out.println("Recieved query all message.");
                    QueryAll m = (QueryAll) request;
                    os.writeObject(new OK());
                    this.node.queryAll(m);
                    break;
                }
                case INSERT_REQUEST : {
                    System.out.println("Recieved insert request.");
                    Insert m = (Insert) request;
                    os.writeObject(new OK());
                    this.node.insert(m);
                    break; 
                }
                case INSERT_RESPONSE : {
                    System.out.println("Recieved insert response.");
                    Insert m = (Insert) request;
                    os.writeObject(new OK());
                    this.node.printInsertResponse(m);
                    break;
                }
                case DELETE_REQUEST : {
                    System.out.println("Recieved delete request.");
                    Delete m = (Delete) request;
                    os.writeObject(new OK());
                    this.node.delete(m);
                    break;
                }
                case DELETE_RESPONSE : {
                    System.out.println("Recieved delete response.");
                    Delete m = (Delete) request;
                    os.writeObject(new OK());
                    this.node.printDeleteResponse(m);
                    break;
                }
                case DATA_TRANSFER : {
                    System.out.println("Received data transfer message");
                    DataTransfer m = (DataTransfer) request;
                    os.writeObject(new OK());
                    this.node.dataReceive(m);
                    break;
                }
                case DATA_DEMAND : {
                    System.out.println("Received data demand request.");
                    //TODO LOGIC
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
