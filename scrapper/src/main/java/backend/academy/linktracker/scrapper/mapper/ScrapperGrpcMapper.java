package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.grpc.LinkResponseMsg;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ScrapperGrpcMapper {

    public LinkResponseMsg toMsg(LinkResponse dto) {
        return LinkResponseMsg.newBuilder()
                .setId(dto.id())
                .setUrl(dto.url().toString())
                .addAllTags(dto.tags())
                .build();
    }

    public List<LinkResponseMsg> toListMsg(List<LinkResponse> dtos) {
        return dtos.stream().map(this::toMsg).toList();
    }
}
