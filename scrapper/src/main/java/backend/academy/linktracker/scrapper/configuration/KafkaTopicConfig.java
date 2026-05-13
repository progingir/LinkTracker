package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public NewTopic linkUpdatesTopic() {
        return TopicBuilder.name(kafkaProperties.topic())
            .partitions(kafkaProperties.partitions())
            .replicas(kafkaProperties.replicas())
            .configs(Map.of("min.insync.replicas", kafkaProperties.minInsyncReplicas()))
            .build();
    }
}
