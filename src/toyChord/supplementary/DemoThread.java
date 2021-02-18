package toyChord.supplementary;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import toyChord.messages.*;

public class DemoThread extends Thread {
    private Socket socket;

    public DemoThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // just find what is the result and print it after sending OK
            Message request = null;
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            request = (Message) is.readObject();
            // decode message and act accordingly
            switch(request.getType()) {
                case INSERT_RESPONSE: {
                    Insert m = (Insert) request;
                    os.writeObject(new OK());
                    System.out.println(m.getPair().toString() + " inserted. Response from Node : " + m.getResponder().toString());
                    break;
                }
                case DELETE_RESPONSE : {
                    Delete m = (Delete) request;
                    os.writeObject(new OK());
                    System.out.println("\"" + m.getKey() + "\" " + m.getResult() + ". Response from Node : " + m.getResponder().toString());
                    break;
                }
                case QUERY_RESPONSE: {
                    Query m = (Query) request;
                    os.writeObject(new OK());
                    String result;
                    if(m.getValue() == null){
                        result = "\"" + m.getKey() + "\" Not Found. ";
                    } else {
                        result = "<" + m.getKey() + "," + m.getValue() + "> ";
                    }
                    System.out.println(result + "Response from Node : " + m.getResponder().toString());
                    break;
                }
                default: {
                    System.out.println("Something went wrong at recieved message type.");
                }
            }
            this.socket.close();
        } catch (ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
