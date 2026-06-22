package com.jiteen.claims.claim.application.mapper;

import com.jiteen.claims.claim.application.dto.request.CreateClaimRequest;
import com.jiteen.claims.claim.application.dto.request.UpdateClaimRequest;
import com.jiteen.claims.claim.application.dto.response.ClaimResponse;
import com.jiteen.claims.claim.domain.entity.Claim;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-22T20:00:53+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ClaimMapperImpl implements ClaimMapper {

    @Override
    public Claim toEntity(CreateClaimRequest request) {
        if ( request == null ) {
            return null;
        }

        Claim.ClaimBuilder claim = Claim.builder();

        claim.policyNumber( request.getPolicyNumber() );
        claim.claimantName( request.getClaimantName() );
        claim.claimType( request.getClaimType() );
        claim.incidentDate( request.getIncidentDate() );
        claim.claimAmount( request.getClaimAmount() );
        claim.description( request.getDescription() );

        claim.status( com.jiteen.claims.claim.domain.enums.ClaimStatus.SUBMITTED );

        return claim.build();
    }

    @Override
    public ClaimResponse toResponse(Claim claim) {
        if ( claim == null ) {
            return null;
        }

        ClaimResponse.ClaimResponseBuilder claimResponse = ClaimResponse.builder();

        claimResponse.id( claim.getId() );
        claimResponse.policyNumber( claim.getPolicyNumber() );
        claimResponse.claimantName( claim.getClaimantName() );
        claimResponse.claimType( claim.getClaimType() );
        claimResponse.incidentDate( claim.getIncidentDate() );
        claimResponse.claimAmount( claim.getClaimAmount() );
        claimResponse.status( claim.getStatus() );
        claimResponse.description( claim.getDescription() );
        claimResponse.createdAt( claim.getCreatedAt() );
        claimResponse.updatedAt( claim.getUpdatedAt() );

        return claimResponse.build();
    }

    @Override
    public void updateClaimFromRequest(UpdateClaimRequest request, Claim claim) {
        if ( request == null ) {
            return;
        }

        if ( request.getPolicyNumber() != null ) {
            claim.setPolicyNumber( request.getPolicyNumber() );
        }
        if ( request.getClaimantName() != null ) {
            claim.setClaimantName( request.getClaimantName() );
        }
        if ( request.getClaimType() != null ) {
            claim.setClaimType( request.getClaimType() );
        }
        if ( request.getIncidentDate() != null ) {
            claim.setIncidentDate( request.getIncidentDate() );
        }
        if ( request.getClaimAmount() != null ) {
            claim.setClaimAmount( request.getClaimAmount() );
        }
        if ( request.getDescription() != null ) {
            claim.setDescription( request.getDescription() );
        }
    }
}
