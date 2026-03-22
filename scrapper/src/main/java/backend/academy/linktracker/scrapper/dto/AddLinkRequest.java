package backend.academy.linktracker.scrapper.dto;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

public record AddLinkRequest(
        @NotNull(message = "Ссылка не должна быть пустой") URI link, List<String> tags) {}
