package toyChord.supplementary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import toyChord.config.Config;
import toyChord.messages.Delete;

public class Deletions {
    // opens th "query.txt" nad deletes from DHT all the keysfrom that file
    // just so we can delete all files from dht without having to
    // restart it between experiments

    public static void main(String[] args) throws IOException, InterruptedException {
        // for query throughput we should not block 
        DemoValues val = new DemoValues(false);
        
        new DemoListener(val).start();

        BufferedReader reader = new BufferedReader(new FileReader(Config.insertFile));

        // record start time
        val.start = System.nanoTime();
        // start sending the query requests
        // every line in file defines a key
        String line = reader.readLine();
		while (line != null) {
			//System.out.println(line);
            val.issued++;
            String key = line.split(",")[0];
            // pick a node from the network randomly
            int randomNum = ThreadLocalRandom.current().nextInt(0, 10);
            new DemoMessageSender(Config.nodes[randomNum], new Delete(val.myAddr, key)).start();
			// read next line
			line = reader.readLine();
		}
        val.eof = true;
        Thread.sleep(5000);        

        reader.close();
    }
}
