package com.jiteen.claims.notification.application.service;

import com.jiteen.claims.notification.domain.event.ClaimApprovedEvent;
import com.jiteen.claims.notification.domain.event.ClaimRejectedEvent;

/**
 * Application service contract defining email notification dispatch operations
 * for insurance claim lifecycle events within the Notification Service.
 *
 * <p>
 * Implementations of this interface encapsulate all email composition and
 * SMTP delivery logic. The interface abstraction allows the email backend
 * (Spring Mail, AWS SES, SendGrid, etc.) to be swapped without modifying
 * the consumer layer.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public interface EmailNotificationService {

    /**
     * Composes and dispatches an approval notification email to the claimant
     * identified within the provided event payload.
     *
     * @param event the {@link ClaimApprovedEvent} containing claimant contact
     *              details and approval context for email composition
     */
    void sendClaimApprovedNotification(ClaimApprovedEvent event);

    /**
     * Composes and dispatches a rejection notification email to the claimant
     * identified within the provided event payload, including the documented
     * rejection reason and guidance for next steps.
     *
     * @param event the {@link ClaimRejectedEvent} containing claimant contact
     *              details, rejection reason, and context for email composition
     */
    void sendClaimRejectedNotification(ClaimRejectedEvent event);
}
