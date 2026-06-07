package piotrholda.portfoliomanager.quotation;

public class AlphaVantageRateLimitException extends RuntimeException {

    public AlphaVantageRateLimitException(String message) {
        super(message);
    }
}
