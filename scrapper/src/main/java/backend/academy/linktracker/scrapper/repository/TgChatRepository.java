package backend.academy.linktracker.scrapper.repository;

public interface TgChatRepository {
    boolean addChat(Long chatId);

    boolean removeChat(Long chatId);

    boolean existsChat(Long chatId);
}
