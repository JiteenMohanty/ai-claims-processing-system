package com.jiteen.claims.notification.application.service.impl;

import com.jiteen.claims.notification.application.service.EmailNotificationService;
import com.jiteen.claims.notification.domain.event.ClaimApprovedEvent;
import com.jiteen.claims.notification.domain.event.ClaimRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Spring JavaMail implementation of the {@link EmailNotificationService} contract.
 *
 * <p>
 * Composes and dispatches transactional email notifications to insurance claimants
 * using the configured SMTP provider (supports Gmail, AWS SES, SendGrid, or any
 * standard SMTP relay). Email composition uses {@link SimpleMailMessage} for
 * plaintext bodies in Version 1; HTML templates via Thymeleaf are available for
 * future enhancement.
 * </p>
 *
 * <p>
 * If the SMTP provider is unavailable or misconfigured, the send operation throws
 * a {@link org.springframework.mail.MailException} which the caller (Kafka consumer)
 * is responsible for handling. Notification failures are logged at ERROR level
 * without blocking claim processing.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from:jiteen.dev@gmail.com}")
    private String fromAddress;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * {@inheritDoc}
     *
     * <p>
     * Composes an approval email containing the policy number, claim ID, and
     * a confirmation that the claim has been cleared for payment processing.
     * </p>
     */
    @Override
    public void sendClaimApprovedNotification(ClaimApprovedEvent event) {
        log.info("Preparing approval notification for claimId: {}, policy: {}",
                event.getClaimId(), event.getPolicyNumber());

        if (!emailEnabled) {
            log.info("[SIMULATION] Claim APPROVED email — claimId: {}, policy: {}, claimant: {}",
                    event.getClaimId(), event.getPolicyNumber(), event.getClaimantName());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setSubject(String.format("Your Insurance Claim Has Been Approved — Policy %s",
                    event.getPolicyNumber()));
            message.setText(buildApprovalEmailBody(event));

            mailSender.send(message);

            log.info("Approval notification dispatched successfully for claimId: {}", event.getClaimId());

        } catch (Exception ex) {
            log.error("Failed to dispatch approval notification for claimId: {} — {}",
                    event.getClaimId(), ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Composes a rejection email containing the policy number, rejection reason,
     * and guidance on resubmission or appeal procedures.
     * </p>
     */
    @Override
    public void sendClaimRejectedNotification(ClaimRejectedEvent event) {
        log.info("Preparing rejection notification for claimId: {}, policy: {}",
                event.getClaimId(), event.getPolicyNumber());

        if (!emailEnabled) {
            log.info("[SIMULATION] Claim REJECTED email — claimId: {}, policy: {}, claimant: {}, reason: {}",
                    event.getClaimId(), event.getPolicyNumber(), event.getClaimantName(), event.getReason());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setSubject(String.format("Update on Your Insurance Claim — Policy %s",
                    event.getPolicyNumber()));
            message.setText(buildRejectionEmailBody(event));

            mailSender.send(message);

            log.info("Rejection notification dispatched successfully for claimId: {}", event.getClaimId());

        } catch (Exception ex) {
            log.error("Failed to dispatch rejection notification for claimId: {} — {}",
                    event.getClaimId(), ex.getMessage(), ex);
        }
    }

    /**
     * Constructs the plaintext body for a claim approval email.
     *
     * @param event the approval event containing claim context
     * @return the formatted email body string
     */
    private String buildApprovalEmailBody(ClaimApprovedEvent event) {
        return String.format("""
                Dear %s,

                We are pleased to inform you that your insurance claim has been approved.

                Claim Reference ID: %s
                Policy Number: %s
                Decision Date: %s

                Your approved claim amount will be processed and disbursed according to your
                policy terms within 5–7 business days.

                If you have any questions, please contact our claims support team quoting
                your claim reference ID above.

                Thank you for choosing our services.

                Regards,
                Claims Processing Team
                AI-Powered Insurance Platform
                """,
                event.getClaimantName(),
                event.getClaimId(),
                event.getPolicyNumber(),
                event.getApprovedAt() != null ? event.getApprovedAt().toLocalDate() : "N/A"
        );
    }

    /**
     * Constructs the plaintext body for a claim rejection email.
     *
     * @param event the rejection event containing claim context and reason
     * @return the formatted email body string
     */
    private String buildRejectionEmailBody(ClaimRejectedEvent event) {
        return String.format("""
                Dear %s,

                We regret to inform you that your insurance claim has not been approved
                following our review process.

                Claim Reference ID: %s
                Policy Number: %s
                Decision Date: %s
                Reason: %s

                You have the right to appeal this decision within 30 days of receiving
                this notification. To initiate an appeal, please contact our claims
                support team with your claim reference ID and any additional documentation.

                We understand this may be disappointing and appreciate your understanding.

                Regards,
                Claims Processing Team
                AI-Powered Insurance Platform
                """,
                event.getClaimantName(),
                event.getClaimId(),
                event.getPolicyNumber(),
                event.getRejectedAt() != null ? event.getRejectedAt().toLocalDate() : "N/A",
                event.getReason() != null ? event.getReason() : "Please contact support for details"
        );
    }
}
