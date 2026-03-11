package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.BotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BotController {

    private final BotService botService;

    @PostMapping("/updates")
    public void sendUpdate(@Valid @RequestBody LinkUpdate update) {
        log.atInfo()
            .addKeyValue("url", update.url())
            .log("Получено обновление от Scrapper");
        botService.sendNotification(update);
    }
}
