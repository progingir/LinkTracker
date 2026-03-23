package backend.academy.linktracker.scrapper.exception;

public class InvalidLinkException extends RuntimeException {
    public InvalidLinkException(String link) {
        super("Некорректный формат ссылки: " + link);
    }
}
