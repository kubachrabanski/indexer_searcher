package indexer.parse;

public final class ParseException extends Exception {

    public ParseException(String message) {
        super(message);
    }

    public ParseException(Exception exception) {
        super(exception);
    }
}
