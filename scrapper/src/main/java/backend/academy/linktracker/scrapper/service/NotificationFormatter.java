package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.UpdateResult;
import java.util.List;

public interface NotificationFormatter {

    String formatUpdate(List<UpdateResult> updates);

    String formatErrorReport(List<String> failedUrls);
}
