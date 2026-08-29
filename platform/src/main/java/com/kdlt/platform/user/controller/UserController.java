package com.kdlt.platform.user.controller;

import com.kdlt.platform.user.dto.UserCreateDto;
import com.kdlt.platform.user.dto.UserProfileDTO;
import com.kdlt.platform.user.dto.UserUpdateDto;
import com.kdlt.platform.user.entity.Invite;
import com.kdlt.platform.user.entity.User;
import com.kdlt.platform.user.service.InviteService;
import com.kdlt.platform.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import com.kdlt.platform.user.dto.ChangePasswordDto;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final InviteService inviteService;

    public UserController(UserService userService, InviteService inviteService){
        this.userService = userService;
        this.inviteService = inviteService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getMyProfile(@AuthenticationPrincipal User currentUser){
        UserProfileDTO user = userService.getProfile(currentUser.getId());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me/role")
    public ResponseEntity<Map<String, String>> getMyRole(@AuthenticationPrincipal User currentUser){
        return ResponseEntity.ok(Map.of("role", currentUser.getRole().name()));
    }

    @PostMapping("/me/photo")
    public ResponseEntity<Map<String, String>> uploadPhoto(@AuthenticationPrincipal User currentUser,
                                                           @RequestParam("file") MultipartFile file){
        String photoUrl = userService.uploadPhoto(currentUser.getId(), file);
        return ResponseEntity.ok(Map.of("photoUrl", photoUrl));
    }

    @PostMapping
    public ResponseEntity<UserProfileDTO> createUser(@RequestParam String token,
                                                     @Valid @RequestBody UserCreateDto dto){
        Invite invite = inviteService.validateToken(token);
        if(dto.getEmail() == null || !dto.getEmail().equalsIgnoreCase(invite.getEmail())){
            throw new RuntimeException(("L'email ne correspond pas a l'invitation."));
        }

        UserProfileDTO user = userService.createUser(dto);
        inviteService.markUsed(invite);

        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> updateUser(@AuthenticationPrincipal User currentUser,
                                                     @RequestBody UserUpdateDto dto){
        UserProfileDTO user = userService.updateProfile(currentUser.getId(), dto);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User currentUser,
                                               @Valid @RequestBody ChangePasswordDto dto){
        userService.changePassword(currentUser.getId(), dto);
        return ResponseEntity.noContent().build();
    }
}
