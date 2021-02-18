package toyChord.supplementary;

import java.net.ServerSocket;
import java.net.Socket;

import toyChord.config.Config;

public class DemoListener extends Thread {
    private DemoValues val;
    private ServerSocket socket;

    public DemoListener(DemoValues val) {
        try {
            this.socket = new ServerSocket(Config.serverSocket);
        } catch (Exception e) {
            //TODO: handle exception
            e.printStackTrace();
            System.exit(0);
        }
        this.val = val;
    }

    @Override
    public void run() {
        //System.out.println("Sent message");
        // just loop listening on incoming connections
        while (true) {
            try {
                //System.out.println("received response.");
                // accept and generate a new thread for every connection
                Socket s = this.socket.accept();
                Thread t = new DemoThread(s);
                t.start();
                // if we need all output to be printed in the correct sequence, wait for thread to finish
                if(val.blocking){
                    t.join();
                }
                // increase completed transactions
                val.completed++;
                // ckeck if transactions ended
                if (val.eof && !(val.issued > val.completed)) {
                    val.end = System.nanoTime();
                    break;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        // sleep two second so last request should not fail and then print result
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("End time:" + val.end);
        long duration = val.end - val.start;
        System.out.println("Duration in nano-seconds:" + duration);
    }
}
