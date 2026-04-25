package backend.academy.linktracker.scrapper.configuration;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SchedulerConfiguration {

    @Value("${app.scheduler.threads-count}")
    private int threadsCount;

    @Bean(name = "linkUpdaterExecutor")
    public Executor linkUpdaterExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadsCount);
        executor.setMaxPoolSize(threadsCount);
        executor.setThreadNamePrefix("LinkUpdater-");
        executor.initialize();
        return executor;
    }
}
