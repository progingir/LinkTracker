package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.grpc.BotServiceGrpc;
import backend.academy.linktracker.scrapper.properties.BotClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class BotClientConfiguration {

    @Bean
    public BotServiceGrpc.BotServiceBlockingStub botServiceStub(
            GrpcChannelFactory channelFactory, BotClientProperties properties) {
        return BotServiceGrpc.newBlockingStub(channelFactory.createChannel(properties.getGrpcChannelName()));
    }
}
