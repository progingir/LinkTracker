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
        if (!tgChatRepository.addChat(chatId)) {
            throw new ChatAlreadyExistsException(chatId);
        }
    }

    public void deleteChat(Long chatId) {
        if (!tgChatRepository.removeChat(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        linkRepository.removeAllByChatId(chatId);
    }
}
