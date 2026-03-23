package backend.academy.linktracker.bot.mapper;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.grpc.LinkUpdateMsg;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class BotGrpcMapper {

    public LinkUpdate toDto(LinkUpdateMsg msg) {
        return new LinkUpdate(msg.getId(), URI.create(msg.getUrl()), msg.getDescription(), msg.getTgChatIdsList());
    }
}
