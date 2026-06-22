package com.jiteen.claims.claim.application.mapper;

import com.jiteen.claims.claim.application.dto.response.DocumentResponse;
import com.jiteen.claims.claim.application.dto.response.UploadDocumentResponse;
import com.jiteen.claims.claim.domain.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Enterprise compile-time component responsible for high-performance object-to-object
 * data transformation mapping constraints across the claim document ecosystem tiers.
 * <p>
 * This interface utilizes MapStruct to generate optimized, type-safe, and thread-safe mapper
 * implementations at compile time. It completely isolates internal data-tier entity fields from public out-facing 
 * client response DTO configurations, mirroring the architectural mapping conventions established 
 * by {@code ClaimMapper}.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DocumentMapper {

    /**
     * Translates an ingested database-backed {@link Document} entity model configuration into 
     * a tailored, lightweight {@link UploadDocumentResponse} acknowledgment payload.
     * <p>
     * Explicitly maps nested relational object references and complex property keys to their 
     * respective flat primitive DTO field names.
     * </p>
     *
     * @param document the underlying persistent {@link Document} relational domain entity record context
     * @return an instantiated client-facing {@link UploadDocumentResponse} transfer object context mapping metrics
     */
    @Mapping(target = "documentId", source = "id")
    @Mapping(target = "claimId", source = "claim.id")
    @Mapping(target = "uploadedAt", source = "createdAt")
    UploadDocumentResponse toUploadResponse(Document document);

    /**
     * Automatically projects an active {@link Document} data entity record context onto an immutable
     * standard customer delivery data structure contract matching {@link DocumentResponse}.
     * <p>
     * Direct scalar properties are synchronized across matching signatures, while explicit identity 
     * keys and primary reference handles undergo isolated structural mapping project adjustments.
     * </p>
     *
     * @param document the underlying persistent {@link Document} relational domain entity record context
     * @return a fully populated, decoupled client delivery tier {@link DocumentResponse} contract mapping summary
     */
    @Mapping(target = "documentId", source = "id")
    @Mapping(target = "claimId", source = "claim.id")
    DocumentResponse toResponse(Document document);
}