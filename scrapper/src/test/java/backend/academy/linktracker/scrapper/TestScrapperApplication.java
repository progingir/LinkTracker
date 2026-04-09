package backend.academy.linktracker.scrapper;

import org.springframework.boot.SpringApplication;

public class TestScrapperApplication {

    public static void main(String[] args) {
        SpringApplication.from(ScrapperApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
