package com.kdlt.platform.user.dto;

import com.kdlt.platform.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateInviteRequest {

    @NotBlank(message = "L'adresse email est obligatoire.")
    @Email(message = "Cette adresse email n'est pas valide.")
    private String email;

    @NotNull(message = "Le rôle est obligatoire.")
    private Role role;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}