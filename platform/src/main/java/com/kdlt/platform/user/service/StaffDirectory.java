package com.kdlt.platform.user.service;

import com.kdlt.platform.user.entity.Role;
import com.kdlt.platform.user.entity.User;
import com.kdlt.platform.user.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaffDirectory {

    // Rôles alertés lors d'une nouvelle commande ou d'une nouvelle demande de devis.
    private static final List<Role> NOTIFIED_ROLES = List.of(Role.OWNER, Role.ADMIN, Role.STAFF);

    private final UserRepository userRepository;

    public StaffDirectory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<String> activeStaffEmails() {
        return userRepository.findByRoleInAndActiveTrue(NOTIFIED_ROLES).stream()
                .map(User::getEmail)
                .toList();
    }
}