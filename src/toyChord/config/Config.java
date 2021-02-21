package toyChord.config;

import toyChord.NodeAddress;

public class Config {
    // Choose consistency type and replication factor
    // then re-compile (make the jar file and execute)
    public static final ConsistencyType repType = ConsistencyType.LINEARIZABILITY; // replication type
    public static final int K = 1; // replication factor

    public static void printLogo(){
        System.out.print("\n"+
                        "          TTTTTTTT     OOOO      YY    YY\n"+
                        "             TT      OO    OO     YY  YY\n"+
                        "             TT     OO      OO     YYYY\n"+
                        "             TT     OO      OO      YY\n"+
                        "             TT      OO    OO       YY\n"+
                        "             TT        OOOO         YY\n"+
                        "\n"+
                        "   CCCCC  HH    HH     OOOO     RRRRRRR   DDDDD\n"+
                        " CC       HH    HH   OO    OO   RR    RR  DD   DD\n"+
                        "CC        HHHHHHHH  OO      OO  RRRRRRR   DD    DD\n"+
                        "CC        HH    HH  OO      OO  RR  RR    DD    DD\n"+
                        " CC       HH    HH   OO    OO   RR   RR   DD   DD\n"+
                        "   CCCCC  HH    HH     OOOO     RR    RR  DDDDD\n\n");
    }

    // only needed for supplementary code (used for the experiments/measurements for the project)
    public static final String insertFile = "./transactions/insert.txt";
    public static final String queryFile = "./transactions/query.txt";
    public static final String requestsFile = "./transactions/requests.txt";
    public static final int serverSocket = 8500;

    // an array with all node addresses in the DHT (again used only for measurements for the report)
    public static final NodeAddress[] nodes = new NodeAddress[]{new NodeAddress("192.168.1.1", 8700),
                                                                new NodeAddress("192.168.1.1", 8800),
                                                                new NodeAddress("192.168.1.2", 8700),
                                                                new NodeAddress("192.168.1.2", 8800),
                                                                new NodeAddress("192.168.1.3", 8700),
                                                                new NodeAddress("192.168.1.3", 8800),
                                                                new NodeAddress("192.168.1.4", 8700),
                                                                new NodeAddress("192.168.1.4", 8800),
                                                                new NodeAddress("192.168.1.5", 8700),
                                                                new NodeAddress("192.168.1.5", 8800)};

}
