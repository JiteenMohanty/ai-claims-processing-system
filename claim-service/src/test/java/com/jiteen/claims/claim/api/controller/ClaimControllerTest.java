package com.jiteen.claims.claim.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiteen.claims.claim.application.dto.request.CreateClaimRequest;
import com.jiteen.claims.claim.application.dto.response.ClaimResponse;
import com.jiteen.claims.claim.application.service.ClaimService;
import com.jiteen.claims.claim.api.exception.ClaimNotFoundException;
import com.jiteen.claims.claim.domain.enums.ClaimStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Enterprise MVC slice verification test suite for {@link ClaimController}.
 * <p>
 * This class isolates the API controller layout tier by mocking the application's core service layer execution limits.
 * It exercises explicit operational validations over inbound parameter contracts, payload syntax schemas, JSR-380 validation,
 * mapping outcomes, and corporate REST protocol rules across both successful scenarios and negative failure contexts.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@WebMvcTest(controllers = ClaimController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
})
@DisplayName("ClaimController Web MVC Slice Tests")
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClaimService claimService;

    /**
     * Asserts that a well-formed {@link CreateClaimRequest} returns an HTTP 201 Created status code 
     * accompanied by a populated JSON body tracking the initial state vectors.
     */
    @Test
    @DisplayName("POST /api/claims - Should create a claim record successfully and return HTTP 201")
    void shouldCreateClaimSuccessfully() throws Exception {
        // Given
        CreateClaimRequest request = CreateClaimRequest.builder()
                .policyNumber("POL-ENTERPRISE-100")
                .claimantName("Alexander Pierce")
                .claimType("AUTO")
                .incidentDate(LocalDateTime.of(2026, 6, 12, 14, 0))
                .claimAmount(new BigDecimal("4500.75"))
                .description("Accidental front fender intersection tracking collision anomaly.")
                .build();

        UUID generatedId = UUID.randomUUID();
        ClaimResponse simulatedResponse = ClaimResponse.builder()
                .id(generatedId)
                .policyNumber(request.getPolicyNumber())
                .claimantName(request.getClaimantName())
                .claimType(request.getClaimType())
                .incidentDate(request.getIncidentDate())
                .claimAmount(request.getClaimAmount())
                .status(ClaimStatus.SUBMITTED)
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(claimService.createClaim(any(CreateClaimRequest.class))).thenReturn(simulatedResponse);

        // When & Then
        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(generatedId.toString()))
                .andExpect(jsonPath("$.policyNumber").value("POL-ENTERPRISE-100"))
                .andExpect(jsonPath("$.claimantName").value("Alexander Pierce"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.claimAmount").value(4500.75));

        verify(claimService, times(1)).createClaim(any(CreateClaimRequest.class));
    }

    /**
     * Asserts that submitting an invalid creation configuration (e.g. blank attributes) immediately fails 
     * JSR-380 schema interceptors and drops out an HTTP 400 Bad Request fault mapping.
     */
    @Test
    @DisplayName("POST /api/claims - Should return HTTP 400 Bad Request when incoming parameters break constraint limits")
    void shouldReturn400ForInvalidCreateRequest() throws Exception {
        // Given - Breaking structural constraints using a blank policy and a non-positive financial valuation
        CreateClaimRequest invalidRequest = CreateClaimRequest.builder()
                .policyNumber("")
                .claimantName("Alexander Pierce")
                .claimType("AUTO")
                .incidentDate(LocalDateTime.now())
                .claimAmount(new BigDecimal("-100.00"))
                .description("Invalid data format validation test tracking vector.")
                .build();

        // When & Then
        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Asserts that querying an active, non-deleted claim resource primary identifier transitions an HTTP 200 OK 
     * metadata layout string.
     */
    @Test
    @DisplayName("GET /api/claims/{claimId} - Should return active claim record details and HTTP 200 when located")
    void shouldGetClaimByIdSuccessfully() throws Exception {
        // Given
        UUID targetId = UUID.fromString("6a2f483c-1b70-4299-a1b4-52d87b3200ff");
        ClaimResponse matchingResponse = ClaimResponse.builder()
                .id(targetId)
                .policyNumber("POL-STABLE-999")
                .claimantName("Eleanor Vance")
                .claimType("PROPERTY")
                .incidentDate(LocalDateTime.of(2026, 5, 20, 8, 30))
                .claimAmount(new BigDecimal("125000.00"))
                .status(ClaimStatus.UNDER_REVIEW)
                .description("Structural ceiling leakage damage narrative statement tracking scenario.")
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(claimService.getClaimById(targetId)).thenReturn(matchingResponse);

        // When & Then
        mockMvc.perform(get("/api/claims/{claimId}", targetId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(targetId.toString()))
                .andExpect(jsonPath("$.policyNumber").value("POL-STABLE-999"))
                .andExpect(jsonPath("$.claimantName").value("Eleanor Vance"))
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        verify(claimService, times(1)).getClaimById(targetId);
    }

    /**
     * Asserts that querying a soft-deleted or non-existent unique resource accurately triggers 
     * a tailored {@link ClaimNotFoundException} error block translating directly into an HTTP 404 response payload matrix.
     */
    @Test
    @DisplayName("GET /api/claims/{claimId} - Should return HTTP 404 Not Found when lookup key doesn't map to any active entry")
    void shouldReturn404WhenClaimNotFound() throws Exception {
        // Given
        UUID absentId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        when(claimService.getClaimById(absentId)).thenThrow(new ClaimNotFoundException(absentId));

        // When & Then
        mockMvc.perform(get("/api/claims/{claimId}", absentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Claim not found with id: " + absentId));

        verify(claimService, times(1)).getClaimById(absentId);
    }

    /**
     * Asserts that extracting global listings compiles arrays cleanly underneath an HTTP 200 OK wrapper envelope.
     */
    @Test
    @DisplayName("GET /api/claims - Should retrieve an array structure of active claims and return HTTP 200")
    void shouldGetAllClaimsSuccessfully() throws Exception {
        // Given
        ClaimResponse firstClaim = ClaimResponse.builder()
                .id(UUID.randomUUID())
                .policyNumber("POL-LIST-01")
                .claimantName("Corporate Entity Alpha")
                .status(ClaimStatus.SUBMITTED)
                .build();

        ClaimResponse secondClaim = ClaimResponse.builder()
                .id(UUID.randomUUID())
                .policyNumber("POL-LIST-02")
                .claimantName("Corporate Entity Beta")
                .status(ClaimStatus.APPROVED)
                .build();

        when(claimService.getAllClaims()).thenReturn(List.of(firstClaim, secondClaim));

        // When & Then
        mockMvc.perform(get("/api/claims")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].policyNumber").value("POL-LIST-01"))
                .andExpect(jsonPath("$[1].policyNumber").value("POL-LIST-02"));

        verify(claimService, times(1)).getAllClaims();
    }

    /**
     * Asserts that putting an active reference through the approval route maps adjustments onto an APPROVED status definition.
     */
    @Test
    @DisplayName("PUT /api/claims/{claimId}/approve - Should execute status advancement and return transitioned APPROVED model metadata")
    void shouldApproveClaimSuccessfully() throws Exception {
        // Given
        UUID claimId = UUID.fromString("fd3a9211-c884-4861-bb27-6f14b98cf931");
        ClaimResponse approvedResponse = ClaimResponse.builder()
                .id(claimId)
                .policyNumber("POL-WF-001")
                .claimantName("Thomas Wayne")
                .status(ClaimStatus.APPROVED)
                .build();

        when(claimService.approveClaim(claimId)).thenReturn(approvedResponse);

        // When & Then
        mockMvc.perform(put("/api/claims/{claimId}/approve", claimId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(claimId.toString()))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(claimService, times(1)).approveClaim(claimId);
    }

    /**
     * Asserts that putting an active reference through the rejection channel maps adjustments onto a REJECTED status definition.
     */
    @Test
    @DisplayName("PUT /api/claims/{claimId}/reject - Should execute status reversal denial rules and return terminal REJECTED model metadata")
    void shouldRejectClaimSuccessfully() throws Exception {
        // Given
        UUID claimId = UUID.fromString("991b5c2c-8824-4f40-84a1-0731f2bc8802");
        ClaimResponse rejectedResponse = ClaimResponse.builder()
                .id(claimId)
                .policyNumber("POL-WF-002")
                .claimantName("Bruce Wayne")
                .status(ClaimStatus.REJECTED)
                .build();

        when(claimService.rejectClaim(claimId)).thenReturn(rejectedResponse);

        // When & Then
        mockMvc.perform(put("/api/claims/{claimId}/reject", claimId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(claimId.toString()))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(claimService, times(1)).rejectClaim(claimId);
    }
}