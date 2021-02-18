package toyChord.supplementary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import toyChord.KVPair;
import toyChord.config.Config;
import toyChord.messages.Insert;

public class InsertValues {
    // open file "insert.txt"
    // and insert all given Key-Value pairs in the DHT
    // every request is sent to a random node in the network
    // also keep track of time

    public static void main(String[] args) throws IOException, InterruptedException {
        // for inserts we should not block 
        DemoValues val = new DemoValues(false);
        
        new DemoListener(val).start();

        BufferedReader reader = new BufferedReader(new FileReader(Config.insertFile));

        // record start time
        val.start = System.nanoTime();
        // start sending the insert requests

        String line = reader.readLine();
		while (line != null) {
			//System.out.println(line);
            val.issued++;
            String[] tokens = line.split(",");
            String key = tokens[0];
            String value = tokens[1].substring(1);
            // pick a node from the network randomly
            int randomNum = ThreadLocalRandom.current().nextInt(0, 10);
            new DemoMessageSender(Config.nodes[randomNum], new Insert(val.myAddr, new KVPair(key, value))).start();
			// read next line
			line = reader.readLine();
		}
        val.eof = true;
        Thread.sleep(5000);        

        reader.close();
    }
}
