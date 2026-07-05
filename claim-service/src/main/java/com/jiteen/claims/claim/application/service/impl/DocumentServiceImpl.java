package com.jiteen.claims.claim.application.service.impl;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.jiteen.claims.claim.api.exception.ClaimNotFoundException;
import com.jiteen.claims.claim.api.exception.DocumentNotFoundException;
import com.jiteen.claims.claim.api.exception.FileSizeExceededException;
import com.jiteen.claims.claim.api.exception.FileStorageException;
import com.jiteen.claims.claim.api.exception.InvalidFileTypeException;
import com.jiteen.claims.claim.application.dto.response.DocumentResponse;
import com.jiteen.claims.claim.application.dto.response.UploadDocumentResponse;
import com.jiteen.claims.claim.application.event.ClaimEventPublisher;
import com.jiteen.claims.claim.application.mapper.DocumentMapper;
import com.jiteen.claims.claim.application.service.DocumentService;
import com.jiteen.claims.claim.application.service.StorageService;
import com.jiteen.claims.claim.domain.entity.Claim;
import com.jiteen.claims.claim.domain.entity.Document;
import com.jiteen.claims.claim.domain.enums.DocumentStatus;
import com.jiteen.claims.claim.domain.event.DocumentUploadedEvent;
import com.jiteen.claims.claim.domain.repository.ClaimRepository;
import com.jiteen.claims.claim.domain.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

/**
 * Enterprise service implementation managing the orchestrations, lifecycle
 * validation rules, and persistence tracking logic for insurance claim
 * supporting documents.
 * <p>
 * This component acts as a transactional coordinator bridging the presentation
 * controller layer, the persistent data repository framework, and the target
 * storage engine abstractions. It enforces operational constraints including
 * validation thresholds for structural data metrics, allowed media formats,
 * secure file placement, and clean soft-deletion pipelines.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final ClaimRepository claimRepository;
    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final DocumentMapper documentMapper;
    private final ClaimEventPublisher claimEventPublisher;

    @Value("${storage.provider:local}")
    private String storageProvider;

    /**
     * Threshold maximum byte capacity for individual document uploads,
     * calibrated exactly to 10 MB.
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * White-listed set of officially permitted Multipurpose Internet Mail
     * Extensions (MIME) types.
     */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    /**
     * Processes, validates, and archives an incoming file asset against an
     * active insurance claim transaction.
     * <p>
     * Performs a strict sequential validation check checking claim existence,
     * payload non-emptiness, media verification, and storage layout safety
     * parameters prior to triggering binary file transfers and recording entity
     * updates.
     * </p>
     *
     * @param claimId the unique {@link UUID} primary key token of the targeted
     * parent insurance claim
     * @param file the multi-part multipart object payload wrapper transmitting
     * the file context
     * @return an {@link UploadDocumentResponse} detailing the recorded
     * processing state vectors
     * @throws ClaimNotFoundException if the parent claim target resource does
     * not exist or is soft-deleted
     * @throws FileStorageException if the incoming document payload data is
     * empty or uninitialized
     * @throws InvalidFileTypeException if the data content type does not map
     * into white-listed MIME extensions
     * @throws FileSizeExceededException if the data content footprint crosses
     * the 10 MB volume barrier
     */
    @Override
    public UploadDocumentResponse uploadDocument(UUID claimId, MultipartFile file) {
        // A. Verify claim existence within active records
        Claim claim = claimRepository.findByIdAndDeletedAtIsNull(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        // B. Validate multi-part boundary content integrity
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Uploaded file is empty.");
        }

        // C. Validate content media specification type
        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new InvalidFileTypeException(contentType);
        }

        // D. Validate absolute maximum sizing quotas
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeExceededException(file.getSize(), MAX_FILE_SIZE);
        }

        // E. Stream binary content down onto physical destination layers
        String storagePath = storageService.store(file);

        // F. Assemble new document domain entity container profiles
        String storedFileName = Paths.get(storagePath).getFileName().toString();

        Document document = Document.builder()
                .claim(claim)
                .originalFileName(file.getOriginalFilename())
                .contentType(contentType)
                .fileSize(file.getSize())
                .storagePath(storagePath)
                .storedFileName(storedFileName)
                .status(DocumentStatus.UPLOADED)
                .storageProvider(storageProvider)
                .build();

        // G. Commit metadata updates to database records
        Document savedDocument = documentRepository.save(document);

        // H. Publish event to trigger downstream AI document analysis pipeline
        claimEventPublisher.publishDocumentUploaded(DocumentUploadedEvent.builder()
                .claimId(claimId)
                .documentId(savedDocument.getId())
                .storagePath(storagePath)
                .storageProvider(storageProvider)
                .originalFileName(file.getOriginalFilename())
                .contentType(contentType)
                .uploadedAt(savedDocument.getCreatedAt())
                .build());

        // I. Map entity layout securely into client data contracts
        return documentMapper.toUploadResponse(savedDocument);
    }

    /**
     * Compiles a listing collection of metadata tracking information
     * summarizing all active non-deleted documents associated with a specified
     * parent insurance claim target resource.
     *
     * @param claimId the unique {@link UUID} tracking identity key mapping the
     * target claim record
     * @return a {@link List} containing transformed outbound
     * {@link DocumentResponse} items
     */
    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByClaimId(UUID claimId) {

        claimRepository.findByIdAndDeletedAtIsNull(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        return documentRepository.findByClaimIdAndDeletedAtIsNull(claimId).stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    /**
     * Extracts full transaction metadata for a single specific document lookup
     * key from active tracking layers.
     *
     * @param documentId the sequence-based surrogate numeric database key
     * identifier tracking the attachment
     * @return the matching target mapped outbound {@link DocumentResponse}
     * layout representation
     * @throws DocumentNotFoundException if the target document instance cannot
     * be verified or found
     */
    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long documentId) {
        return documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .map(documentMapper::toResponse)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    /**
     * Executes the lifecycle decommissioning parameters and soft-deletion
     * constraints for an active document resource.
     * <p>
     * Permanently purges the physical file instance from backend binary system
     * assets before tagging metadata columns with the appropriate structural
     * tracking markers to insulate rows from ongoing data layer selections.
     * </p>
     *
     * @param documentId the sequence-based surrogate numeric database key
     * identifier tracking the attachment
     * @throws DocumentNotFoundException if the target document instance cannot
     * be verified or found within active layers
     */
    @Override
    public void deleteDocument(Long documentId) {
        // Locate active record resource parameters matching identification targets
        Document document = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        // Purge physical disk or cloud resource allocations matching location tokens
        storageService.delete(document.getStoragePath());

        // Update database configuration to execute soft delete state compliance mechanisms
        document.setDeletedAt(LocalDateTime.now());
        documentRepository.save(document);
    }
}
