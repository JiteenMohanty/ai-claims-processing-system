package com.jiteen.claims.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test verifying the AI Processing Service Spring application context
 * loads successfully in a test environment with Kafka broker disabled.
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=localhost:9092",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class AiProcessingServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
