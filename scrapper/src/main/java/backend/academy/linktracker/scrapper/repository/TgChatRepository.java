package backend.academy.linktracker.scrapper.repository;

public interface TgChatRepository {
    void addChat(Long chatId);

    void removeChat(Long chatId);

    boolean existsChat(Long chatId);
}
