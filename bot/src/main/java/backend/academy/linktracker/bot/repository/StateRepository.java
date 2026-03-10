package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.service.UserState;
import java.net.URI;
import java.util.Map;
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
    }

    private final Map<Long, UserContext> states = new ConcurrentHashMap<>();

    public void setState(Long chatId, UserState state) {
        states.compute(
                chatId,
                (k, v) -> v == null ? new UserContext(state, null) : new UserContext(state, v.getPendingLink()));
    }

    public void setPendingLink(Long chatId, URI link) {
        states.compute(chatId, (k, v) -> {
            if (v == null) {
                return new UserContext(UserState.NONE, link);
            }
            v.setPendingLink(link);
            return v;
        });
    }

    public UserContext getContext(Long chatId) {
        return states.getOrDefault(chatId, new UserContext(UserState.NONE, null));
    }

    public void clear(Long chatId) {
        states.remove(chatId);
    }
}
