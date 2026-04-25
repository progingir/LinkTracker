package backend.academy.linktracker.bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.net.URI;
import java.util.List;

public record LinkUpdate(
        Long id,
        URI url,

        @NotBlank(message = "description не может быть пустым")
        String description,

        @NotEmpty(message = "список tgChatIds не может быть пустым")
        List<Long> tgChatIds,

        boolean isSystemReport) {
    public static LinkUpdate systemReport(String description, List<Long> tgChatIds) {
        return new LinkUpdate(null, null, description, tgChatIds, true);
    }
}
