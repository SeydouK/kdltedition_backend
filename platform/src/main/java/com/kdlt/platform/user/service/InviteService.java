package com.kdlt.platform.user.service;

import com.kdlt.platform.user.entity.Invite;
import com.kdlt.platform.user.repository.InviteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.kdlt.platform.exceptions.ResourceNotFoundException;
import com.kdlt.platform.exceptions.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.kdlt.platform.user.entity.Role;
import com.kdlt.platform.user.repository.UserRepository;
import java.util.Set;

@Service
public class InviteService {

    private final InviteRepository inviteRepository;
    private final EmailService emailService;
    public static final Set<Role> INVITABLE_ROLES = Set.of(Role.STAFF, Role.ADMIN);

    @Value("${app.invite.expiration-hours:48}")
    private long expirationHours;

    private final UserRepository userRepository;
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public InviteService(InviteRepository inviteRepository,
                         EmailService emailService, UserRepository userRepository){
        this.inviteRepository = inviteRepository;
        this.emailService = emailService;

        this.userRepository = userRepository;
    }

    public Invite createInvite(String email, Role role) {
        if (!INVITABLE_ROLES.contains(role)) {
            throw new BadRequestException("Ce rôle ne peut pas être attribué par invitation.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException(
                    "Cette adresse a déjà un compte. Modifiez son rôle depuis la liste des utilisateurs.");
        }

        Invite invite = new Invite();
        invite.setEmail(normalizedEmail);
        invite.setRole(role);
        invite.setToken(UUID.randomUUID().toString());
        invite.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));

        Invite saved = inviteRepository.save(invite);

        emailService.sendInviteEmail(normalizedEmail, frontendBaseUrl + saved.getToken(), role, expirationHours);
        return saved;
    }

    public Invite validateToken(String token){
        Invite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation introuvable."));

        if (invite.isUsed()){
            throw new BadRequestException("Cette invitation a déjà été utilisée.");
        }
        if (invite.isExpired()){
            throw new BadRequestException("Cette invitation a expiré. Demandew-en une nouvelle.");
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
