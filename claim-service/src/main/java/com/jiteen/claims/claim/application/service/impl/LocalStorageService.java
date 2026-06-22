package com.jiteen.claims.claim.application.service.impl;

import com.jiteen.claims.claim.api.exception.FileStorageException;
import com.jiteen.claims.claim.application.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Local file system implementation of the {@link StorageService} core contract.
 * <p>
 * This service fulfills Phase 4 architecture requirements by persisting binary claim attachments
 * inside a configurable local storage pool directory structure. It shields consuming orchestration 
 * layers from concrete I/O boundaries and incorporates strict filename sanitization routines to eliminate 
 * path traversal vulnerabilities and multi-part upload tracking exploits.
 * </p>
 * <p>
 * This implementation can be fully substituted with a cloud-native object store service bean (such as AWS S3) 
 * in subsequent iterations without causing any breaking interface modifications across application tiers.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Service
public class LocalStorageService implements StorageService {

    private final Path rootLocation;

    /**
     * Constructs a new {@code LocalStorageService} injection instance, resolving and normalizing 
     * the configured base directory properties.
     *
     * @param uploadDir the base filesystem directory string extracted from application properties configuration context
     */
    public LocalStorageService(@Value("${storage.local.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * Safely captures an inbound file stream, generates an invariant UUID tracking token alias, 
     * and streams the binary content directly onto the designated local storage path.
     * <p>
     * Employs definitive path traversal defense configurations by completely disregarding parent path tokens 
     * from client-provided filenames and validating relative node alignment criteria.
     * </p>
     *
     * @param file the inbound {@link MultipartFile} data transaction transport reference stream
     * @return the fully qualified absolute storage path string indicating where the binary file is allocated
     * @throws FileStorageException if structural folder generation fails or an I/O exception breaks stream copy procedures
     */
    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Failed to persist empty or uninitialized multi-part payload resource context.");
        }

        try {
            // Guarantee that the base operational directory matrix exists safely ahead of streaming data
            if (!Files.exists(this.rootLocation)) {
                Files.createDirectories(this.rootLocation);
            }

            // Extract the original file extension securely while completely discarding the customer-supplied name string
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Generate an immutable unique identifier filename mapping to eliminate collision parameters on disk
            String storedFileName = UUID.randomUUID().toString() + extension;
            Path destinationFile = this.rootLocation.resolve(Paths.get(storedFileName)).normalize();

            // Guard against absolute and relative directory escape patterns (Path Traversal Ingestion Check)
            if (!destinationFile.startsWith(this.rootLocation)) {
                throw new FileStorageException("Security Exception: Target storage path vector escapes root directory boundaries.");
            }

            // Stream and lock the binary block directly into target storage tracking blocks
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return destinationFile.toString();

        } catch (IOException e) {
            throw new FileStorageException("Critical execution failure transpired while compiling filesystem binary data streams.", e);
        }
    }

    /**
     * Permanently purges an active binary document resource block from the storage pool using its structural file path string.
     * <p>
     * Validates that the targeted deletion path is securely within the verified directory structure before executing file elimination.
     * </p>
     *
     * @param storagePath the fully qualified string path locator referencing the file resource target on disk
     * @throws FileStorageException if security assertions fail or disk access constraints block file erasure operations
     */
    @Override
    public void delete(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return;
        }

        try {
            Path targetFile = Paths.get(storagePath).toAbsolutePath().normalize();

            // Enforce explicit path containment rules to prevent arbitrary host-level erasure actions
            if (!targetFile.startsWith(this.rootLocation)) {
                throw new FileStorageException("Security Exception: Requested deletion path coordinates escape root directory boundaries.");
            }

            Files.deleteIfExists(targetFile);

        } catch (IOException e) {
            throw new FileStorageException("Failed to execute physical file system cleanup parameters targeting the resource element.", e);
        }
    }

    /**
     * Checks for the presence and system accessibility of a specific binary asset tracking reference within the file storage layer.
     *
     * @param storagePath the fully qualified string path locator mapping the queried document entity asset
     * @return {@code true} if the targeted item exists and represents an active file layer; {@code false} otherwise
     */
    @Override
    public boolean exists(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return false;
        }
        Path targetFile = Paths.get(storagePath).toAbsolutePath().normalize();
        return targetFile.startsWith(this.rootLocation) && Files.exists(targetFile) && Files.isRegularFile(targetFile);
    }
}