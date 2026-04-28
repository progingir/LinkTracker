package backend.academy.linktracker.scrapper.configuration;

import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic linkUpdatesTopic(
            @Value("${app.kafka.topic}") String topicName,
            @Value("${app.kafka.partitions:3}") int partitions,
            @Value("${app.kafka.replicas:3}") int replicas,
            @Value("${app.kafka.min-insync-replicas:2}") String minInsyncReplicas) {

        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicas)
                .configs(Map.of("min.insync.replicas", minInsyncReplicas))
                .build();
    }
}
