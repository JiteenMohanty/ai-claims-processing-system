package com.jiteen.claims.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test verifying the Notification Service Spring application context
 * loads successfully with all beans wired, using an embedded Kafka broker
 * and a mocked {@link JavaMailSender} to avoid external infrastructure dependencies.
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
            "claim-approved-topic",
            "claim-rejected-topic"
        }
)
class NotificationServiceApplicationTests {

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void contextLoads() {
    }
}
