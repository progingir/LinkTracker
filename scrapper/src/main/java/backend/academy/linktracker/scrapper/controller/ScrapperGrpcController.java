package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.grpc.*;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.TgChatService;
import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ScrapperGrpcController extends ScrapperServiceGrpc.ScrapperServiceImplBase {

    private final LinkService linkService;
    private final TgChatService tgChatService;

    @Override
    public void registerChat(ChatRequest request, StreamObserver<Empty> responseObserver) {
        tgChatService.registerChat(request.getId());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteChat(ChatRequest request, StreamObserver<Empty> responseObserver) {
        tgChatService.deleteChat(request.getId());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getLinks(ChatRequest request, StreamObserver<ListLinksResponseMsg> responseObserver) {
        ListLinksResponse response = linkService.getLinksResponse(request.getId());

        List<LinkResponseMsg> messages =
                response.links().stream().map(this::dtoToMsg).toList();

        responseObserver.onNext(ListLinksResponseMsg.newBuilder()
                .addAllLinks(messages)
                .setSize(response.size())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void addLink(AddLinkRequestMsg request, StreamObserver<LinkResponseMsg> responseObserver) {
        LinkResponse resp = linkService.addLinkFromExternal(
                request.getChatId(), request.getLink(), request.getTagsList(), request.getFiltersList());

        responseObserver.onNext(dtoToMsg(resp));
        responseObserver.onCompleted();
    }

    @Override
    public void removeLink(RemoveLinkRequestMsg request, StreamObserver<LinkResponseMsg> responseObserver) {
        LinkResponse resp = linkService.removeLinkFromExternal(request.getChatId(), request.getLink());

        responseObserver.onNext(dtoToMsg(resp));
        responseObserver.onCompleted();
    }

    private LinkResponseMsg dtoToMsg(LinkResponse dto) {
        return LinkResponseMsg.newBuilder()
                .setId(dto.id())
                .setUrl(dto.url().toString())
                .addAllTags(dto.tags())
                .addAllFilters(dto.filters())
                .build();
    }
}
