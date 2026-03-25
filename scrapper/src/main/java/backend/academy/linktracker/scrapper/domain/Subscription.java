package backend.academy.linktracker.scrapper.domain;

import java.net.URI;
import java.util.List;

public record Subscription(Long chatId, Long linkId, URI url, List<String> tags) {}
