package com.jiteen.claims.claim.application.mapper;

import com.jiteen.claims.claim.application.dto.request.CreateClaimRequest;
import com.jiteen.claims.claim.application.dto.request.UpdateClaimRequest;
import com.jiteen.claims.claim.application.dto.response.ClaimResponse;
import com.jiteen.claims.claim.domain.entity.Claim;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-03T18:17:51+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ClaimMapperImpl implements ClaimMapper {

    @Override
    public Claim toEntity(CreateClaimRequest request) {
        if ( request == null ) {
            return null;
        }

        Claim.ClaimBuilder claim = Claim.builder();

        claim.claimAmount( request.getClaimAmount() );
        claim.claimType( request.getClaimType() );
        claim.claimantName( request.getClaimantName() );
        claim.description( request.getDescription() );
        claim.incidentDate( request.getIncidentDate() );
        claim.policyNumber( request.getPolicyNumber() );

        claim.status( com.jiteen.claims.claim.domain.enums.ClaimStatus.SUBMITTED );

        return claim.build();
    }

    @Override
    public ClaimResponse toResponse(Claim claim) {
        if ( claim == null ) {
            return null;
        }

        ClaimResponse.ClaimResponseBuilder claimResponse = ClaimResponse.builder();

        claimResponse.claimAmount( claim.getClaimAmount() );
        claimResponse.claimType( claim.getClaimType() );
        claimResponse.claimantName( claim.getClaimantName() );
        claimResponse.createdAt( claim.getCreatedAt() );
        claimResponse.description( claim.getDescription() );
        claimResponse.id( claim.getId() );
        claimResponse.incidentDate( claim.getIncidentDate() );
        claimResponse.policyNumber( claim.getPolicyNumber() );
        claimResponse.status( claim.getStatus() );
        claimResponse.updatedAt( claim.getUpdatedAt() );

        return claimResponse.build();
    }

    @Override
    public void updateClaimFromRequest(UpdateClaimRequest request, Claim claim) {
        if ( request == null ) {
            return;
        }

        if ( request.getClaimAmount() != null ) {
            claim.setClaimAmount( request.getClaimAmount() );
        }
        if ( request.getClaimType() != null ) {
            claim.setClaimType( request.getClaimType() );
        }
        if ( request.getClaimantName() != null ) {
            claim.setClaimantName( request.getClaimantName() );
        }
        if ( request.getDescription() != null ) {
            claim.setDescription( request.getDescription() );
        }
        if ( request.getIncidentDate() != null ) {
            claim.setIncidentDate( request.getIncidentDate() );
        }
        if ( request.getPolicyNumber() != null ) {
            claim.setPolicyNumber( request.getPolicyNumber() );
        }
    }
}
