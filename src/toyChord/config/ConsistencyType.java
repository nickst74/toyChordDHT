package toyChord.config;

// The two consistency types implemented
public enum ConsistencyType {
    LINEARIZABILITY, // uses Chain replication
    EVENTUAL
}
