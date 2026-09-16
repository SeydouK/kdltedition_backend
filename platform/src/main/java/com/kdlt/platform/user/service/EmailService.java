package com.kdlt.platform.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.util.HashMap;
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

    public void sendInviteEmail(String toEmail, String inviteLink){
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");

        Map<String, Object> body = Map.of(
                "sender", Map.of("email", fromAddress, "name", "TKARD GO"),
                "to", new Object[]{ Map.of("email", toEmail) },
                "subject", "Votre invitation TKARD GO",
                "htmlContent", "<p>Bonjour,</p>" +
                        "<p>Vous avez été invité(e) à rejoindre TKARD GO.</p>" +
                        "<p><a href=\"" + inviteLink + "\">Cliquez ici pour créer votre compte</a> (lien valable 30 minutes).</p>" +
                        "<p>Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>" +
                        "<p>L'équipe TKARD GO</p>"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try{
            restTemplate.postForEntity(BREVO_API_URL, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Échec de l'envoi de l'email via Brevo : " + e.getMessage(), e);
        }
    }

    public void sendNewQuoteNotificationToStaff(String staffEmail, String customerName, String productName, int quantity) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sender", Map.of("name", "KDLT Éditions", "email", mailFrom));
        payload.put("to", List.of(Map.of("email", staffEmail)));
        payload.put("subject", "Nouvelle demande de devis - " + productName);
        payload.put("htmlContent",
                "<p>Nouvelle demande de devis reçue :</p>" +
                        "<p><strong>Client :</strong> " + customerName + "</p>" +
                        "<p><strong>Produit :</strong> " + productName + "</p>" +
                        "<p><strong>Quantité :</strong> " + quantity + "</p>" +
                        "<p>Connectez-vous à l'espace admin pour y répondre.</p>"
        );

        sendEmail(payload);
    }

    public void sendQuoteResponseToCustomer(String customerEmail, String productName, boolean accepted, String proposedPrice, String staffResponse) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sender", Map.of("name", "KDLT Éditions", "email", mailFrom));
        payload.put("to", List.of(Map.of("email", customerEmail)));
        payload.put("subject", "Réponse à votre demande de devis - " + productName);

        String content = accepted
                ? "<p>Bonne nouvelle ! Voici notre proposition pour votre demande concernant <strong>" + productName + "</strong> :</p>" +
                "<p><strong>Prix proposé :</strong> " + proposedPrice + " FCFA</p>" +
                (staffResponse != null ? "<p>" + staffResponse + "</p>" : "")
                : "<p>Concernant votre demande de devis pour <strong>" + productName + "</strong> :</p>" +
                (staffResponse != null ? "<p>" + staffResponse + "</p>" : "<p>Nous ne sommes malheureusement pas en mesure d'honorer cette demande pour le moment.</p>");

        payload.put("htmlContent", content);

        sendEmail(payload);
    }
}
