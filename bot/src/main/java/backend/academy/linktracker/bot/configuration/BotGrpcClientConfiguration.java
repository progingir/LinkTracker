package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.properties.ScrapperClientProperties;
import backend.academy.linktracker.grpc.ScrapperServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class BotGrpcClientConfiguration {

    @Bean
    public ScrapperServiceGrpc.ScrapperServiceBlockingStub scrapperStub(
            GrpcChannelFactory channelFactory, ScrapperClientProperties properties) {
        return ScrapperServiceGrpc.newBlockingStub(channelFactory.createChannel(properties.getGrpcChannelName()));
    }
}
