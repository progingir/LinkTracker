package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.service.TgChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/tg-chat")
@RequiredArgsConstructor
public class TgChatController {

    private final TgChatService tgChatService;

    @PostMapping("/{id}")
    public void registerChat(@PathVariable Long id) {
        log.atInfo().addKeyValue("chat_id", id).log("Поступил запрос на регистрацию чата");
        tgChatService.registerChat(id);
        log.atInfo().addKeyValue("chat_id", id).log("Чат успешно зарегистрирован");
    }

    @DeleteMapping("/{id}")
    public void deleteChat(@PathVariable Long id) {
        log.atInfo().addKeyValue("chat_id", id).log("Поступил запрос на удаление чата");
        tgChatService.deleteChat(id);
        log.atInfo().addKeyValue("chat_id", id).log("Чат успешно удален");
    }
}
