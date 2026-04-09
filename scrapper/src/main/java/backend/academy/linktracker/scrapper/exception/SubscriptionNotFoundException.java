package backend.academy.linktracker.scrapper.exception;

import java.net.URI;

public class SubscriptionNotFoundException extends RuntimeException {
    public SubscriptionNotFoundException(Long chatId, URI url) {
        super("Чат с ID " + chatId + " не подписан на ссылку: " + url);
    }
}
