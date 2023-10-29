package app.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TestContainersBase {

    @Value("${spring.data.mongodb.port}")
    private int PORT;

    @Bean
    public int getPort() {
        return PORT;
    }
}
