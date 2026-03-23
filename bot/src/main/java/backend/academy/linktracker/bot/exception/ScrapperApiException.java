package backend.academy.linktracker.bot.exception;

public class ScrapperApiException extends RuntimeException {
    private final int statusCode;

    public ScrapperApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
