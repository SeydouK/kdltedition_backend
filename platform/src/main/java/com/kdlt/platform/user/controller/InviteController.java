package com.kdlt.platform.user.controller;

import com.kdlt.platform.user.dto.CreateInviteRequest;
import com.kdlt.platform.user.entity.Invite;
import com.kdlt.platform.user.service.InviteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/invites")
public class InviteController {
    private final InviteService inviteService;

    public InviteController(InviteService inviteService){
        this.inviteService = inviteService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> validate(@PathVariable String token){
        try{
            Invite invite = inviteService.validateToken(token);
            return ResponseEntity.ok(Map.of("valide", true, "email", invite.getEmail()));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("valid", false, "message", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Void> createInvite(@Valid @RequestBody CreateInviteRequest request){
        inviteService.createInvite(request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
