package com.kdlt.platform.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${MAIL_FROM:no-reply@kdedition.com}")
    private String fromAddress;

    @Value("${BREVO_API_KEY:}")
    private String brevoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    public void sendInviteEmail(String toEmail, String inviteLink) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("email", fromAddress, "name", "KDLT Éditions"),
                "to", List.of(Map.of("email", toEmail)),
                "subject", "Votre invitation à rejoindre KDLT Éditions",
                "htmlContent", "<p>Bonjour,</p>" +
                        "<p>Vous avez été invité(e) à rejoindre la plateforme KDLT Éditions.</p>" +
                        "<p><a href=\"" + inviteLink + "\">Cliquez ici pour créer votre compte</a> (lien valable 30 minutes).</p>" +
                        "<p>Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>" +
                        "<p>L'équipe KDLT Éditions</p>"
        );

        sendEmail(body);
    }

    public void sendNewQuoteNotificationToStaff(String staffEmail, String customerName, String productName, int quantity) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("email", fromAddress, "name", "KDLT Éditions"),
                "to", List.of(Map.of("email", staffEmail)),
                "subject", "Nouvelle demande de devis - " + productName,
                "htmlContent",
                "<p>Nouvelle demande de devis reçue :</p>" +
                        "<p><strong>Client :</strong> " + escape(customerName) + "</p>" +
                        "<p><strong>Produit :</strong> " + escape(productName) + "</p>" +
                        "<p><strong>Quantité :</strong> " + quantity + "</p>" +
                        "<p>Connectez-vous à l'espace admin pour y répondre.</p>"
        );

        sendEmail(body);
    }

    public void sendQuoteResponseToCustomer(String customerEmail, String productName, boolean accepted, String proposedPrice, String staffResponse) {
        String response = staffResponse != null ? "<p>" + escape(staffResponse) + "</p>" : "";

        String content = accepted
                ? "<p>Bonne nouvelle ! Voici notre proposition pour votre demande concernant <strong>" + escape(productName) + "</strong> :</p>" +
                "<p><strong>Prix proposé :</strong> " + escape(proposedPrice) + " FCFA</p>" +
                response
                : "<p>Concernant votre demande de devis pour <strong>" + escape(productName) + "</strong> :</p>" +
                (staffResponse != null ? response : "<p>Nous ne sommes malheureusement pas en mesure d'honorer cette demande pour le moment.</p>");

        Map<String, Object> body = Map.of(
                "sender", Map.of("email", fromAddress, "name", "KDLT Éditions"),
                "to", List.of(Map.of("email", customerEmail)),
                "subject", "Réponse à votre demande de devis - " + productName,
                "htmlContent", content
        );

        sendEmail(body);
    }

    private String escape(String text) {
        return HtmlUtils.htmlEscape(text == null ? "" : text).replace("\n", "<br>");
    }

    private void sendEmail(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(BREVO_API_URL, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Échec de l'envoi de l'email via Brevo : " + e.getMessage(), e);
        }
    }

    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("email", fromAddress, "name", "KDLT Éditions"),
                "to", List.of(Map.of("email", toEmail)),
                "subject", subject,
                "htmlContent", htmlContent
        );

        sendEmail(body);
    }
}