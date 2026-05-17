package backend.academy.linktracker.bot.repository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationInboxRepository {
    private final Set<String> processedNotifications = ConcurrentHashMap.newKeySet();

    public boolean isProcessed(String updateKey, Long chatId) {
        return processedNotifications.contains(generateKey(updateKey, chatId));
    }

    public void markAsProcessed(String updateKey, Long chatId) {
        processedNotifications.add(generateKey(updateKey, chatId));
    }

    private String generateKey(String updateKey, Long chatId) {
        return updateKey + ":" + chatId;
    }
}
