package com.jiteen.claims.claim;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring Boot integration test that verifies the complete application context
 * loads successfully with all beans, configurations, and infrastructure wiring
 * resolved. Runs against an embedded H2 database and embedded Kafka broker
 * to avoid requiring external infrastructure during CI.
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {
            "claim-created-topic",
            "ai-analysis-completed-topic",
            "claim-approved-topic",
            "claim-rejected-topic",
            "document-uploaded-topic"
        }
)
class ClaimServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
