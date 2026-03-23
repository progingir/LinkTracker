package backend.academy.linktracker.scrapper.repository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTgChatRepository implements TgChatRepository {

    private final Set<Long> chats = ConcurrentHashMap.newKeySet();

    @Override
    public void addChat(Long chatId) {
        chats.add(chatId);
    }

    @Override
    public void removeChat(Long chatId) {
        chats.remove(chatId);
    }

    @Override
    public boolean existsChat(Long chatId) {
        return chats.contains(chatId);
    }
}
