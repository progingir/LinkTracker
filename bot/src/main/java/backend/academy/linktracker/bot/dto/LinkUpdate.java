package backend.academy.linktracker.bot.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.net.URI;
import java.util.List;

public record LinkUpdate(
        @JsonProperty("id") Long id,
        @JsonProperty("url") URI url,

        @NotBlank(message = "description не может быть пустым")
        @JsonProperty("description") String description,

        @NotEmpty(message = "список tgChatIds не может быть пустым")
        @JsonProperty("tgChatIds") List<Long> tgChatIds,

        @JsonProperty("isSystemReport") Boolean isSystemReport) {

    @JsonCreator
    public LinkUpdate {
        if (isSystemReport == null) {
            isSystemReport = false;
        }
    }

    public static LinkUpdate systemReport(@JsonProperty("description") String description, List<Long> tgChatIds) {
        return new LinkUpdate(null, null, description, tgChatIds, true);
    }
}
