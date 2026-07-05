package com.jiteen.claims.claim.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Spring configuration for the AWS S3 client, activated only when
 * {@code storage.provider=s3} is set in the application properties.
 *
 * <p>
 * When static credentials ({@code aws.access-key-id} and
 * {@code aws.secret-access-key}) are provided, they are used directly.
 * Otherwise the AWS Default Credential Provider Chain is used, supporting
 * IAM roles, EC2 instance profiles, ECS task roles, and environment variables
 * — the recommended approach for production deployments on AWS infrastructure.
 * </p>
 *
 * <p>
 * This bean is intentionally excluded from the Spring context when
 * {@code storage.provider=local} (the default), preventing any AWS SDK
 * initialization overhead in local development environments.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3Config {

    @Value("${aws.region:ap-south-1}")
    private String region;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    /**
     * Constructs and configures the AWS {@link S3Client} bean for the platform.
     *
     * <p>
     * Credential resolution priority:
     * <ol>
     *   <li>Static credentials if both {@code aws.access-key-id} and
     *       {@code aws.secret-access-key} are non-blank</li>
     *   <li>AWS Default Credential Provider Chain (IAM roles, env vars, ~/.aws/credentials)</li>
     * </ol>
     * </p>
     *
     * @return a fully configured {@link S3Client} instance
     */
    @Bean
    public S3Client s3Client() {
        if (accessKeyId != null && !accessKeyId.isBlank()
                && secretAccessKey != null && !secretAccessKey.isBlank()) {
            log.info("Initializing S3Client with static credentials for region: {}", region);
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                    .build();
        }

        log.info("Initializing S3Client using default credential provider chain for region: {}", region);
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
