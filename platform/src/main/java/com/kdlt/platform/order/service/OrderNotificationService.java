package com.kdlt.platform.order.service;

import com.kdlt.platform.order.entity.OrderStatus;
import com.kdlt.platform.order.event.OrderStatusChangedEvent;
import com.kdlt.platform.user.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Component
public class OrderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationService.class);

    private final EmailService emailService;

    public OrderNotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    // Exécuté après la validation de la transaction : un échec d'email ne peut jamais annuler
    // la commande ni son changement de statut. fallbackExecution : s'exécute aussi quand
    // l'appelant n'a pas de transaction (cas du changement de statut).
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        notifyCustomer(event);
        alertStaff(event);
    }

    private void notifyCustomer(OrderStatusChangedEvent event) {
        try {
            emailService.sendHtmlEmail(event.customerEmail(), customerSubject(event), buildCustomerHtml(event));
        } catch (Exception e) {
            log.error("Échec de l'email client de la commande n°{} (statut {}) : {}",
                    event.orderId(), event.status(), e.getMessage());
        }
    }

    private void alertStaff(OrderStatusChangedEvent event) {
        if (event.staffEmails().isEmpty()) return;

        String subject = "Nouvelle commande n°" + event.orderId();
        String html = buildStaffHtml(event);

        for (String staffEmail : event.staffEmails()) {
            try {
                emailService.sendHtmlEmail(staffEmail, subject, html);
            } catch (Exception e) {
                log.error("Échec de l'alerte équipe pour la commande n°{} : {}", event.orderId(), e.getMessage());
            }
        }
    }

    private String customerSubject(OrderStatusChangedEvent event) {
        String ref = "Commande n°" + event.orderId();
        return switch (event.status()) {
            case PENDING -> ref + " enregistrée";
            case CONFIRMED -> ref + " confirmée";
            case IN_PRODUCTION -> ref + " en production";
            case SHIPPED -> ref + " expédiée";
            case DELIVERED -> ref + " livrée";
            case CANCELLED -> ref + " annulée";
        };
    }

    private String headline(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Nous avons bien reçu votre commande";
            case CONFIRMED -> "Votre commande est confirmée";
            case IN_PRODUCTION -> "Votre commande est en production";
            case SHIPPED -> "Votre commande a été expédiée";
            case DELIVERED -> "Votre commande a été livrée";
            case CANCELLED -> "Votre commande a été annulée";
        };
    }

    private String message(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Elle est en attente de confirmation. Vous recevrez un nouvel email à chaque étape.";
            case CONFIRMED -> "Nous avons confirmé votre commande et allons la préparer.";
            case IN_PRODUCTION -> "Vos produits sont en cours de fabrication.";
            case SHIPPED -> "Elle est en cours de livraison.";
            case DELIVERED -> "Merci de votre confiance. Nous espérons que vos produits vous donneront entière satisfaction.";
            case CANCELLED -> "Si vous pensez qu'il s'agit d'une erreur, contactez KDLT Éditions.";
        };
    }

    private String buildCustomerHtml(OrderStatusChangedEvent event) {
        return layout(
                "<p>Bonjour " + HtmlUtils.htmlEscape(event.customerFirstName()) + ",</p>"
                        + "<h2 style=\"font-size:20px;margin:16px 0 8px;\">" + headline(event.status()) + "</h2>"
                        + "<p style=\"margin:0 0 20px;\">" + message(event.status()) + "</p>"
                        + orderSummary(event));
    }

    private String buildStaffHtml(OrderStatusChangedEvent event) {
        String customerName = event.customerFirstName() + " " + event.customerLastName();

        return layout(
                "<h2 style=\"font-size:20px;margin:0 0 12px;\">Nouvelle commande n°" + event.orderId() + "</h2>"
                        + "<p style=\"margin:0 0 20px;\"><strong>Client :</strong> "
                        + HtmlUtils.htmlEscape(customerName) + " (" + HtmlUtils.htmlEscape(event.customerEmail()) + ")</p>"
                        + orderSummary(event)
                        + "<p style=\"margin-top:24px;\">Connectez-vous à l'espace d'administration pour la confirmer.</p>");
    }

    private String orderSummary(OrderStatusChangedEvent event) {
        StringBuilder rows = new StringBuilder();
        for (OrderStatusChangedEvent.Line line : event.lines()) {
            rows.append("<tr>")
                    .append("<td style=\"padding:8px 0;border-bottom:1px solid #E8E2D6;\">")
                    .append(HtmlUtils.htmlEscape(line.productName()))
                    .append(" × ").append(line.quantity())
                    .append("</td>")
                    .append("<td style=\"padding:8px 0;border-bottom:1px solid #E8E2D6;text-align:right;\">")
                    .append(formatPrice(line.lineTotal()))
                    .append("</td></tr>");
        }

        return "<p style=\"margin:0 0 8px;font-weight:bold;\">Commande n°" + event.orderId() + "</p>"
                + "<table style=\"width:100%;border-collapse:collapse;font-size:14px;\">" + rows + "</table>"
                + "<p style=\"text-align:right;font-weight:bold;margin:16px 0 0;\">Total : "
                + formatPrice(event.total()) + "</p>";
    }

    private String layout(String content) {
        return "<div style=\"font-family:Arial,sans-serif;max-width:560px;margin:0 auto;color:#1F1C17;\">"
                + "<p style=\"font-size:20px;font-weight:bold;color:#1E4B78;margin:0 0 24px;\">KDLT Éditions</p>"
                + content
                + "<p style=\"font-size:12px;color:#8A8174;margin-top:32px;\">"
                + "Cet email est envoyé automatiquement, merci de ne pas y répondre.</p>"
                + "</div>";
    }

    private String formatPrice(BigDecimal value) {
        return NumberFormat.getInstance(Locale.FRANCE).format(value) + " FCFA";
    }
}