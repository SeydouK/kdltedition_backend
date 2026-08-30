package com.kdlt.platform.user.controller;

import com.kdlt.platform.user.entity.Invite;
import com.kdlt.platform.user.service.InviteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
