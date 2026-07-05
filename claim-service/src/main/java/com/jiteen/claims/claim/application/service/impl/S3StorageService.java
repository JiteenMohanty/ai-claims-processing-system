package com.jiteen.claims.claim.application.service.impl;

import com.jiteen.claims.claim.api.exception.FileStorageException;
import com.jiteen.claims.claim.application.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * AWS S3 implementation of the {@link StorageService} contract.
 *
 * <p>
 * Uploads insurance claim supporting documents to a designated S3 bucket using
 * the AWS SDK v2 synchronous client. Object keys follow the pattern
 * {@code claims/<UUID>.<extension>} to eliminate filename collisions while
 * preserving the original file extension for MIME-type inference.
 * </p>
 *
 * <p>
 * This implementation is activated via {@code storage.provider=s3} in the
 * application configuration. When inactive (default: {@code storage.provider=local}),
 * the {@link LocalStorageService} is used instead and no S3 infrastructure
 * connection is established.
 * </p>
 *
 * <p>
 * <strong>Production Security Notes:</strong> The S3Client is configured via
 * {@link com.jiteen.claims.claim.config.S3Config} using the AWS Default Credential
 * Provider Chain, which supports IAM roles and instance profiles — no credentials
 * should be embedded in source code or Docker images.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3StorageService implements StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Uploads the provided multipart file to S3 under the configured bucket.
     *
     * <p>
     * A UUID-based object key is generated to prevent filename collisions. The
     * original file extension is preserved for downstream MIME inference by
     * Textract and other consumers.
     * </p>
     *
     * @param file the {@link MultipartFile} to upload
     * @return the S3 object key (e.g., {@code claims/abc-123.pdf})
     * @throws FileStorageException if the file stream cannot be read or the S3
     *                              upload fails
     */
    @Override
    public String store(MultipartFile file) {
        String extension = extractExtension(file.getOriginalFilename());
        String s3Key = "claims/" + UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

            log.info("Document uploaded to S3: bucket={}, key={}, size={} bytes",
                    bucketName, s3Key, file.getSize());

            return s3Key;

        } catch (IOException e) {
            throw new FileStorageException("Failed to read file stream for S3 upload.", e);
        } catch (S3Exception e) {
            throw new FileStorageException(
                    "S3 upload failed for bucket=" + bucketName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deletes the object identified by the given S3 key from the configured bucket.
     *
     * <p>
     * If the key is blank or null, the operation is silently skipped. S3 delete
     * operations are idempotent — deleting a non-existent key does not raise an error.
     * </p>
     *
     * @param s3Key the S3 object key to delete
     * @throws FileStorageException if the S3 delete operation fails
     */
    @Override
    public void delete(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build());

            log.info("Document deleted from S3: bucket={}, key={}", bucketName, s3Key);

        } catch (S3Exception e) {
            throw new FileStorageException(
                    "Failed to delete S3 object key=" + s3Key + ": " + e.getMessage(), e);
        }
    }

    /**
     * Checks whether an object with the given S3 key exists in the configured bucket.
     *
     * @param s3Key the S3 object key to check
     * @return {@code true} if the object exists, {@code false} otherwise
     */
    @Override
    public boolean exists(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return false;
        }

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build());
            return true;

        } catch (NoSuchKeyException e) {
            return false;

        } catch (S3Exception e) {
            log.warn("Error checking S3 object existence: bucket={}, key={} — {}",
                    bucketName, s3Key, e.getMessage());
            return false;
        }
    }

    private String extractExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }
}
