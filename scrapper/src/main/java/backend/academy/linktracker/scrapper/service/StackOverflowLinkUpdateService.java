package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StackOverflowLinkUpdateService implements LinkUpdateService {
    private final StackOverflowClient client;
    private final LinkParser parser;

    @Override
    public boolean supports(URI url) {
        return parser.parseStackOverflow(url) != null;
    }

    @Override
    public Optional<OffsetDateTime> fetchUpdateDate(URI url) {
        Long id = parser.parseStackOverflow(url);
        return client.fetchQuestion(id).map(StackOverflowResponse.Item::lastActivityDate);
    }

    @Override
    public String getUpdateDescription(URI url, OffsetDateTime date) {
        return "Обнаружена новая активность в вопросе на StackOverflow!";
    }
}
