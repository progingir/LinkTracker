package backend.academy.linktracker.scrapper.service.formatter;

import backend.academy.linktracker.scrapper.constant.NotificationConstants;
import backend.academy.linktracker.scrapper.dto.UpdateResult;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MarkdownNotificationFormatter implements NotificationFormatter {

    @Override
    public String formatUpdate(List<UpdateResult> updates) {
        return updates.stream()
                .map(UpdateResult::description)
                .collect(Collectors.joining(NotificationConstants.UPDATE_SEPARATOR));
    }

    @Override
    public String formatErrorReport(List<String> failedUrls) {
        return NotificationConstants.ERROR_REPORT_HEADER
                + NotificationConstants.ERROR_REPORT_BODY
                + failedUrls.stream()
                        .map(url -> NotificationConstants.LIST_ITEM_PREFIX + url)
                        .collect(Collectors.joining("\n"));
    }
}
