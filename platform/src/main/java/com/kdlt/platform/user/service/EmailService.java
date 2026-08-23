package com.kdlt.platform.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

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
}
