package com.kdlt.platform.user.service;

import com.kdlt.platform.user.entity.Invite;
import com.kdlt.platform.user.repository.InviteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InviteService {

    private final InviteRepository inviteRepository;
    private final EmailService emailService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public InviteService(InviteRepository inviteRepository,
                         EmailService emailService){
        this.inviteRepository = inviteRepository;
        this.emailService = emailService;

    }

    public Invite createInvite(String email){
        Invite invite = new Invite();
        invite.setEmail(email);
        invite.setToken(UUID.randomUUID().toString());
        invite.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        Invite saved = inviteRepository.save(invite);

        String inviteLink = frontendBaseUrl + "" + saved.getToken();
        emailService.sendInviteEmail(email, inviteLink);

        return saved;
    }

    public Invite validateToken(String token){
        Invite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invitation introuvable."));

        if (invite.isUsed()){
            throw new RuntimeException("Cette invitation a déjà été utilisée.");
        }
        if (invite.isExpired()){
            throw new RuntimeException("Cette invitation a expiré. Demandew-en une nouvelle.");
        }

        return invite;
    }

    public void markUsed(Invite invite){
        invite.setUsed(true);
        inviteRepository.save(invite);
    }

    public List<Invite> ListAll() {
        return inviteRepository.findAllByOrderByDateCreationDesc();
    }
}
