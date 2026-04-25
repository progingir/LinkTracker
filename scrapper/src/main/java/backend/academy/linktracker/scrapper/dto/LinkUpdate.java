package backend.academy.linktracker.scrapper.dto;

import java.net.URI;
import java.util.List;

public record LinkUpdate(Long id, URI url, String description, List<Long> tgChatIds, boolean isSystemReport) {
    public static LinkUpdate systemReport(String description, List<Long> tgChatIds) {
        return new LinkUpdate(null, null, description, tgChatIds, true);
    }
}
