package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TgChatService {

    private final TgChatRepository tgChatRepository;

    @Transactional
    public void registerChat(Long chatId) {
        validateId(chatId);
        try {
            tgChatRepository.addChat(chatId);
        } catch (DataIntegrityViolationException e) {
            throw new ChatAlreadyExistsException(chatId);
        }
    }

    @Transactional
    public void deleteChat(Long chatId) {
        validateId(chatId);
        if (!tgChatRepository.existsChat(chatId)) {
            throw new ChatNotFoundException(chatId);
        }

        tgChatRepository.removeChat(chatId);
    }

    private void validateId(Long chatId) {
        if (chatId == null || chatId <= 0) {
            throw new IllegalArgumentException("ID чата должен быть положительным числом");
        }
    }
}
