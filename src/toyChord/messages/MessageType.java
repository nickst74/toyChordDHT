package toyChord.messages;

public enum MessageType {
    OK,
    // exchanged during node joins
    WHO_IS_IT,
    IT_IS_ME,
    UPDATE_NEXT,
    UPDATE_PREV,
    // for overlay command
    OVERLAY
}
