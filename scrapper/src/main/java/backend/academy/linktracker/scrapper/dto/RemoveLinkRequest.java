package backend.academy.linktracker.scrapper.dto;

import jakarta.validation.constraints.NotNull;
import java.net.URI;

public record RemoveLinkRequest(
    @NotNull(message = "Ссылка не должна быть пустой")
    URI link
) {}
