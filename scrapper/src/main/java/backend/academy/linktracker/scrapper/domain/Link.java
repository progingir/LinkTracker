package backend.academy.linktracker.scrapper.domain;

import java.net.URI;
import java.util.List;

public record Link(Long id, URI url, List<String> tags, List<String> filters) {}
