package com.jiteen.claims.notification.config;

/**
 * Centralized registry of Kafka topic name constants consumed by the
 * Notification Service messaging layer.
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public final class KafkaTopics {

    private KafkaTopics() {}

    /**
     * Topic from which the Notification Service consumes claim approval events.
     * Published by the Claim Service when a claim officer approves a claim.
     */
    public static final String CLAIM_APPROVED = "claim-approved-topic";

    /**
     * Topic from which the Notification Service consumes claim rejection events.
     * Published by the Claim Service when a claim officer rejects a claim.
     */
    public static final String CLAIM_REJECTED = "claim-rejected-topic";
}
