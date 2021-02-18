package toyChord.supplementary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import toyChord.KVPair;
import toyChord.config.Config;

import toyChord.messages.Insert;
import toyChord.messages.Query;
import toyChord.messages.Message;

public class Requests {
    // open file "requests.txt"
    // and send all of the defined requests into the DHT
    // every request is sent to a random node in the network
    // also keep track of time (although not needed this time)

    public static void main(String[] args) throws IOException, InterruptedException {
        // this time block waiting for output of every thread
        // we look after the sequence that the transactions are completed
        DemoValues val = new DemoValues(false);
        
        new DemoListener(val).start();

        BufferedReader reader = new BufferedReader(new FileReader(Config.requestsFile));
        // just for compiling...
        Message message = null;
        // record start time
        val.start = System.nanoTime();
        // start sending the query requests
        // every line in file defines a key
        String line = reader.readLine();
		while (line != null) {
			//System.out.println(line);
            val.issued++;
            // decode request and create the correct message
            String[] tokens = line.split(",");
            switch(tokens[0]){
                case "insert": {
                    message = new Insert(val.myAddr, new KVPair(tokens[1].substring(1), tokens[2].substring(1)));
                    break;
                }
                case "query": {
                    message = new Query(val.myAddr, tokens[1].substring(1));
                    break;
                }
            }
            // pick a node from the network randomly
            int randomNum = ThreadLocalRandom.current().nextInt(0, 10);
            new DemoMessageSender(Config.nodes[randomNum], message).start();
			// read next line
			line = reader.readLine();
		}
        val.eof = true;
        Thread.sleep(5000);
        reader.close();
    }
}
