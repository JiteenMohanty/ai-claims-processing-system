package com.jiteen.claims.claim.application.mapper;

import com.jiteen.claims.claim.application.dto.response.AiAnalysisResultResponse;
import com.jiteen.claims.claim.domain.entity.AiAnalysisResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-03T18:17:52+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class AiAnalysisResultMapperImpl implements AiAnalysisResultMapper {

    @Override
    public AiAnalysisResultResponse toResponse(AiAnalysisResult result) {
        if ( result == null ) {
            return null;
        }

        AiAnalysisResultResponse.AiAnalysisResultResponseBuilder aiAnalysisResultResponse = AiAnalysisResultResponse.builder();

        aiAnalysisResultResponse.claimAmountExtracted( result.getClaimAmountExtracted() );
        aiAnalysisResultResponse.claimId( result.getClaimId() );
        aiAnalysisResultResponse.createdAt( result.getCreatedAt() );
        aiAnalysisResultResponse.customerNameExtracted( result.getCustomerNameExtracted() );
        List<String> list = result.getFraudIndicators();
        if ( list != null ) {
            aiAnalysisResultResponse.fraudIndicators( new ArrayList<String>( list ) );
        }
        aiAnalysisResultResponse.id( result.getId() );
        aiAnalysisResultResponse.incidentDateExtracted( result.getIncidentDateExtracted() );
        List<String> list1 = result.getMissingDocuments();
        if ( list1 != null ) {
            aiAnalysisResultResponse.missingDocuments( new ArrayList<String>( list1 ) );
        }
        aiAnalysisResultResponse.policyNumber( result.getPolicyNumber() );
        aiAnalysisResultResponse.processedAt( result.getProcessedAt() );
        aiAnalysisResultResponse.recommendedAction( result.getRecommendedAction() );
        aiAnalysisResultResponse.riskScore( result.getRiskScore() );
        aiAnalysisResultResponse.summary( result.getSummary() );

        aiAnalysisResultResponse.riskCategory( resolveRiskCategory(result.getRiskScore()) );

        return aiAnalysisResultResponse.build();
    }
}
