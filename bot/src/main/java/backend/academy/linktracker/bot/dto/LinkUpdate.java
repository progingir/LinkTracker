package backend.academy.linktracker.bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

public record LinkUpdate(
        @NotNull(message = "id не может быть null") Long id,

        @NotNull(message = "url не может быть null") URI url,

        @NotBlank(message = "description не может быть пустым")
        String description,

        @NotEmpty(message = "список tgChatIds не может быть пустым")
        List<Long> tgChatIds) {}
