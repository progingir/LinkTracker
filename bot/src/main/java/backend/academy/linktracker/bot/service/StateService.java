package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.state.DefaultState;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.repository.StateRepository.UserContext;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import backend.academy.linktracker.bot.state.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateService {

    private final StateRepository stateRepository;

    public void setState(Long chatId, UserState state) {
        log.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("newState", state)
                .log("Изменение состояния пользователя");

        Optional<UserContext> current = stateRepository.findById(chatId);

        UserContext nextContext;
        if (current.isEmpty()) {
            nextContext = new UserContext(state, null, List.of());
        } else {
            UserContext v = current.orElseThrow();
            nextContext = new UserContext(state, v.getPendingLink(), v.getPendingTags());
        }

        stateRepository.save(chatId, nextContext);
    }

    public void setPendingLink(Long chatId, URI link) {
        log.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("link", link)
                .log("Обновление временной ссылки в контексте");

        Optional<UserContext> current = stateRepository.findById(chatId);

        UserContext context;
        if (current.isEmpty()) {
            context = new UserContext(DefaultState.NONE, link, List.of());
        } else {
            context = current.orElseThrow();
            context.setPendingLink(link);
        }

        stateRepository.save(chatId, context);
    }

    public void setPendingTags(Long chatId, List<String> tags) {
        log.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("tagsCount", tags != null ? tags.size() : 0)
                .log("Обновление временных тегов в контексте");

        stateRepository.findById(chatId).ifPresent(v -> {
            v.setPendingTags(tags != null ? tags : List.of());
            stateRepository.save(chatId, v);
        });
    }

    public UserContext getContext(Long chatId) {
        return stateRepository.findById(chatId).orElseGet(() -> new UserContext(DefaultState.NONE, null, List.of()));
    }

    public void clear(Long chatId) {
        log.atInfo().addKeyValue("chatId", chatId).log("Удаление контекста пользователя");

        stateRepository.delete(chatId);
    }
}
