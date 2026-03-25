package backend.academy.linktracker.scrapper.repository;

import java.util.List;

public interface TgChatRepository {
    void addChat(Long chatId);

    void removeChat(Long chatId);

    boolean existsChat(Long chatId);

    List<Long> findAll(int limit, int offset);
}
