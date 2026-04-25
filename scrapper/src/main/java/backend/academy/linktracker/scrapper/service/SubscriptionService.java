package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public List<Long> getChatIdsByLinkId(Long linkId) {
        return subscriptionRepository.findChatIdsByLinkId(linkId);
    }
}
