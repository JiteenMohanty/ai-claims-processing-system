package com.jiteen.claims.claim.application.mapper;

import com.jiteen.claims.claim.application.dto.response.DocumentResponse;
import com.jiteen.claims.claim.application.dto.response.UploadDocumentResponse;
import com.jiteen.claims.claim.domain.entity.Claim;
import com.jiteen.claims.claim.domain.entity.Document;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-22T20:00:54+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class DocumentMapperImpl implements DocumentMapper {

    @Override
    public UploadDocumentResponse toUploadResponse(Document document) {
        if ( document == null ) {
            return null;
        }

        UploadDocumentResponse.UploadDocumentResponseBuilder uploadDocumentResponse = UploadDocumentResponse.builder();

        uploadDocumentResponse.documentId( document.getId() );
        uploadDocumentResponse.claimId( documentClaimId( document ) );
        uploadDocumentResponse.uploadedAt( document.getCreatedAt() );
        uploadDocumentResponse.originalFileName( document.getOriginalFileName() );
        uploadDocumentResponse.contentType( document.getContentType() );
        uploadDocumentResponse.fileSize( document.getFileSize() );
        uploadDocumentResponse.status( document.getStatus() );

        return uploadDocumentResponse.build();
    }

    @Override
    public DocumentResponse toResponse(Document document) {
        if ( document == null ) {
            return null;
        }

        DocumentResponse.DocumentResponseBuilder documentResponse = DocumentResponse.builder();

        documentResponse.documentId( document.getId() );
        documentResponse.claimId( documentClaimId( document ) );
        documentResponse.originalFileName( document.getOriginalFileName() );
        documentResponse.contentType( document.getContentType() );
        documentResponse.fileSize( document.getFileSize() );
        documentResponse.status( document.getStatus() );
        documentResponse.createdAt( document.getCreatedAt() );
        documentResponse.updatedAt( document.getUpdatedAt() );

        return documentResponse.build();
    }

    private UUID documentClaimId(Document document) {
        Claim claim = document.getClaim();
        if ( claim == null ) {
            return null;
        }
        return claim.getId();
    }
}
