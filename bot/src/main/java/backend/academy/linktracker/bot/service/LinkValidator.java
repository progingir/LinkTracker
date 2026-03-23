package backend.academy.linktracker.bot.service;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.stereotype.Component;

@Component
public class LinkValidator {

    public URI validate(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Пустой текст");
        }

        try {
            URI uri = new URI(text.trim());
            if (uri.getScheme() == null || !uri.getScheme().startsWith("http")) {
                throw new IllegalArgumentException("Отсутствует или неверный протокол (http/https)");
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Некорректный синтаксис ссылки");
        }
    }
}
