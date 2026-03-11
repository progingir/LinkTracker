package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.service.BotService;
import backend.academy.linktracker.grpc.*;
import io.grpc.stub.StreamObserver;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotGrpcController extends BotServiceGrpc.BotServiceImplBase {
    private final BotService botService;

    @Override
    public void sendUpdate(LinkUpdateMsg request, StreamObserver<Empty> responseObserver) {
        var update = new backend.academy.linktracker.bot.dto.LinkUpdate(
                request.getId(), URI.create(request.getUrl()),
                request.getDescription(), request.getTgChatIdsList());
        botService.sendNotification(update);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
