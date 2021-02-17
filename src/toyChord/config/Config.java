package toyChord.config;

public class Config {
    // Choose consistency type and replication factor
    // then re-compile (make the jar file and execute)
    public static final ConsistencyType repType = ConsistencyType.LINEARIZABILITY; // replication type
    public static final int K = 3; // replication factor
}
