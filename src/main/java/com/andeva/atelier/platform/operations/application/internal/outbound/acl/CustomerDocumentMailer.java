package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

/**
 * Outbound port emailing a document to a customer as a PDF attachment.
 */
public interface CustomerDocumentMailer {

    /**
     * @return true when the email was accepted by the provider (or simulated without credentials)
     */
    boolean send(String recipientEmail, String subject, String html, String fileName, byte[] pdf);

    /**
     * Same as {@link #send} without attachment.
     */
    boolean sendWithoutAttachment(String recipientEmail, String subject, String html);
}
