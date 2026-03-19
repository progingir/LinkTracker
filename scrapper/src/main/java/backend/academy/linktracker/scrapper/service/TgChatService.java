package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TgChatService {

    private final TgChatRepository tgChatRepository;
    private final LinkRepository linkRepository;

    public void registerChat(Long chatId) {
        validateId(chatId);
        if (tgChatRepository.existsChat(chatId)) {
            throw new ChatAlreadyExistsException(chatId);
        }
        tgChatRepository.addChat(chatId);
    }

    public void deleteChat(Long chatId) {
        validateId(chatId);
        if (!tgChatRepository.existsChat(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        tgChatRepository.removeChat(chatId);
        linkRepository.removeAllByChatId(chatId);
    }

    private void validateId(Long chatId) {
        if (chatId == null || chatId <= 0) {
            throw new IllegalArgumentException("ID чата должен быть положительным числом");
        }
    }
}
