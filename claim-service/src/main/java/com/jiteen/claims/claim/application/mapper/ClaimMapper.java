package com.jiteen.claims.claim.application.mapper;

import com.jiteen.claims.claim.application.dto.request.CreateClaimRequest;
import com.jiteen.claims.claim.application.dto.request.UpdateClaimRequest;
import com.jiteen.claims.claim.application.dto.response.ClaimResponse;
import com.jiteen.claims.claim.domain.entity.Claim;
import com.jiteen.claims.claim.domain.enums.ClaimStatus;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Enterprise compile-time component responsible for high-performance object-to-object
 * data transformation mapping constraints across the Claim service boundary tiers.
 * <p>
 * This interface utilizes MapStruct to automate generation of thread-safe mapper implementations,
 * decoupling the API application layer data transfer presentation contracts (DTOs) from the
 * relational domain entity models. It handles structural translation optimizations, forces
 * default state engine configurations, and natively supports safe partial delta state patching.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy =
        NullValuePropertyMappingStrategy.IGNORE
)
public interface ClaimMapper {

    /**
     * Maps a structured validation-passed ingestion request directly into a persistent domain entity lifecycle instance.
     * <p>
     * Explicitly ignores primary auto-generation identifiers and system-managed audit timestamps to prevent
     * manual manipulation constraints. Natively configures the state tracking engine status variable explicitly
     * to {@link ClaimStatus#SUBMITTED} via expressions mapping parameters.
     * </p>
     *
     * @param request the inbound parsed client ingestion data request mapping variables
     * @return an un-persisted, populated {@link Claim} relational domain model entity instance
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(com.jiteen.claims.claim.domain.enums.ClaimStatus.SUBMITTED)")
    Claim toEntity(CreateClaimRequest request);

    /**
     * Maps an enterprise persistent relational entity instance into a decoupled client-facing transfer object contract.
     * <p>
     * Automatically coordinates direct scalar transformations and primitive configurations across matching variable signatures,
     * securely omitting internal system infrastructure tracking fields like soft-deletion timelines.
     * </p>
     *
     * @param claim the underlying database storage backing domain entity record context model
     * @return an immutable structural client response delivery presentation payload representation
     */
    ClaimResponse toResponse(Claim claim);

    /**
     * Synchronizes dynamic selective modifications from an incremental patch contract onto an active domain instance target.
     * <p>
     * Configured with a strategy layer enforcing {@link NullValuePropertyMappingStrategy#IGNORE}, meaning any null fields
     * present inside the incoming change parameter request are fully ignored and completely preserve the existing state values
     * resting inside the transaction-managed database entity.
     * </p>
     *
     * @param request the parsed payload contract containing client-specified delta modification properties
     * @param claim the operational active reference object currently attached to the persistence boundary layer context
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateClaimFromRequest(UpdateClaimRequest request, @MappingTarget Claim claim);
}