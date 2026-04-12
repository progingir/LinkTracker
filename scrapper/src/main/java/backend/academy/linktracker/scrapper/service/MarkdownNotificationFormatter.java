package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.UpdateResult;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MarkdownNotificationFormatter implements NotificationFormatter {

    private static final String UPDATE_SEPARATOR = "\n\n---\n\n";

    @Override
    public String formatUpdate(List<UpdateResult> updates) {
        return updates.stream().map(UpdateResult::description).collect(Collectors.joining(UPDATE_SEPARATOR));
    }

    @Override
    public String formatErrorReport(List<String> failedUrls) {
        return "⚠️ *Отчет о стабильных сбоях*\n\n"
                + "Ресурсы недоступны длительное время (рекомендуем проверить их или отписаться):\n"
                + failedUrls.stream().map(url -> "• " + url).collect(Collectors.joining("\n"));
    }
}
