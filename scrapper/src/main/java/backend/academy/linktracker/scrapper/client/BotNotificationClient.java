package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class BotNotificationClient {
    private final RestClient restClient;

    public BotNotificationClient(RestClient.Builder builder, BotProperties properties) {
        this.restClient = builder.baseUrl(properties.getUrl()).build();
    }

    public void sendUpdate(LinkUpdate update) {
        log.atInfo().addKeyValue("link", update.url()).log("Отправка уведомления в Бот");
        try {
            restClient.post()
                .uri("/updates")
                .body(update)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.atError().setCause(e).log("Не удалось доставить уведомление в Бот");
        }
    }
}
