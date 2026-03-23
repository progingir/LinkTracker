package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.state.UserState;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

@Repository
public class StateRepository {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserContext {
        private UserState state;
        private URI pendingLink;
        private List<String> pendingTags;
    }

    private final Map<Long, UserContext> states = new ConcurrentHashMap<>();

    public void save(Long chatId, UserContext context) {
        states.put(chatId, context);
    }

    public Optional<UserContext> findById(Long chatId) {
        return Optional.ofNullable(states.get(chatId));
    }

    public void delete(Long chatId) {
        states.remove(chatId);
    }
}
