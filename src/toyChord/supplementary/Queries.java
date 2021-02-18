package toyChord.supplementary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import toyChord.config.Config;
import toyChord.messages.Query;

public class Queries {
    // open file "query.txt"
    // and query all given Keys in the DHT
    // every request is sent to a random node in the network
    // also keep track of time

    public static void main(String[] args) throws IOException, InterruptedException {
        // for query throughput we should not block 
        DemoValues val = new DemoValues(false);
        
        new DemoListener(val).start();

        BufferedReader reader = new BufferedReader(new FileReader(Config.queryFile));

        // record start time
        val.start = System.nanoTime();
        // start sending the query requests
        // every line in file defines a key
        String line = reader.readLine();
		while (line != null) {
			//System.out.println(line);
            val.issued++;
            String key = line;
            // pick a node from the network randomly
            int randomNum = ThreadLocalRandom.current().nextInt(0, 10);
            new DemoMessageSender(Config.nodes[randomNum], new Query(val.myAddr, key)).start();
			// read next line
			line = reader.readLine();
		}
        val.eof = true;
        Thread.sleep(5000);        

        reader.close();
    }
}
