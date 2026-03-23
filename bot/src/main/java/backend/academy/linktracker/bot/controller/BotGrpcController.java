package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.mapper.BotGrpcMapper;
import backend.academy.linktracker.bot.service.BotService;
import backend.academy.linktracker.grpc.BotServiceGrpc;
import backend.academy.linktracker.grpc.Empty;
import backend.academy.linktracker.grpc.LinkUpdateMsg;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotGrpcController extends BotServiceGrpc.BotServiceImplBase {

    private final BotService botService;
    private final BotGrpcMapper mapper;

    @Override
    public void sendUpdate(LinkUpdateMsg request, StreamObserver<Empty> responseObserver) {
        botService.sendNotification(mapper.toDto(request));

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
