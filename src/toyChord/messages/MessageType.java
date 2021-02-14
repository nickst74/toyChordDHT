package toyChord.messages;

public enum MessageType {
    OK,
    // exchanged during node joins
    WHO_IS_IT,
    IT_IS_ME,
    // update pointers after join/depart
    UPDATE_NEXT,
    UPDATE_PREV,
    // transfer data after join/depart
    DATA_TRANSFER,
    DATA_DEMAND,
    // for overlay command
    OVERLAY,
    // Query Message type
    QUERY_REQUEST,
    QUERY_RESPONSE,
    QUERY_ALL,
    // Insert Message Type
    INSERT_REQUEST,
    INSERT_RESPONSE,
    // Delete Message Type
    DELETE_REQUEST,
    DELETE_RESPONSE,
}
