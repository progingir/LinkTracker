package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.grpc.BotServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfiguration {

    @Bean
    public BotServiceGrpc.BotServiceBlockingStub botStub(GrpcChannelFactory channelFactory) {
        return BotServiceGrpc.newBlockingStub(channelFactory.createChannel("bot-channel"));
    }
}
