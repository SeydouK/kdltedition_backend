package com.kdlt.platform.user.service;

import com.kdlt.platform.auth.dto.CustomerRegisterDto;
import com.kdlt.platform.exceptions.EmailAlreadyExistsException;
import com.kdlt.platform.user.dto.UserCreateDto;
import com.kdlt.platform.user.dto.UserProfileDTO;
import com.kdlt.platform.user.dto.UserUpdateDto;
import com.kdlt.platform.user.entity.Role;
import com.kdlt.platform.user.entity.User;
import com.kdlt.platform.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.kdlt.platform.exceptions.ResourceNotFoundException;
import com.kdlt.platform.exceptions.BadRequestException;
import com.kdlt.platform.user.dto.ChangePasswordDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, StorageService storageService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
    }

    private UserProfileDTO mapToUserProfiledTO(User user){
        UserProfileDTO dto = new UserProfileDTO();

        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setId(user.getId());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());

        return dto;
    }

    public UserProfileDTO getProfile(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return mapToUserProfiledTO(user);
    }

    public String uploadPhoto(Long userId, MultipartFile file){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(("User not found.")));

        String photoUrl = storageService.store(file, "profile-photos");

        user.setPhotoUrl(photoUrl);
        userRepository.save(user);

        return photoUrl;
    }

    public UserProfileDTO createUser(UserCreateDto dto, Role role){
        String email = dto.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)){
            throw new EmailAlreadyExistsException("Cet email est déjà utilisé.");
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(email);
        user.setMotDePasseHash(passwordEncoder.encode(dto.getMotDePasse()));
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(role);
        user.setActive(true);

        User saved = userRepository.save(user);
        return mapToUserProfiledTO(saved);
    }

    public UserProfileDTO updateProfile(Long userId, UserUpdateDto dto){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        if (!user.getEmail().equalsIgnoreCase(dto.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("Cet email est déjà utilisé.");
        }
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());

        userRepository.save(user);
        return mapToUserProfiledTO(user);
    }

    public UserProfileDTO findByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return mapToUserProfiledTO(user);
    }

    private User loadManageableUser(Long userId, User actor) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));

        if (target.getId().equals(actor.getId())) {
            throw new BadRequestException("Vous ne pouvez pas modifier votre propre compte.");
        }
        if (target.getRole() == Role.OWNER) {
            throw new BadRequestException("Le compte propriétaire ne peut pas être modifié depuis l'application.");
        }
        return target;
    }

    public void changePassword(Long userId, ChangePasswordDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getMotDePasseHash())) {
            throw new BadRequestException("Mot de passe actuel incorrect.");
        }

        user.setMotDePasseHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public List<UserProfileDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserProfiledTO)
                .toList();
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public UserProfileDTO changeRole(Long userId, Role newRole, User actor) {
        if (!InviteService.INVITABLE_ROLES.contains(newRole)) {
            throw new BadRequestException("Ce rôle ne peut pas être attribué depuis l'application.");
        }

        User target = loadManageableUser(userId, actor);
        target.setRole(newRole);
        return mapToUserProfiledTO(userRepository.save(target));
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public UserProfileDTO setActive(Long userId, boolean active, User actor) {
        User target = loadManageableUser(userId, actor);
        target.setActive(active);
        return mapToUserProfiledTO(userRepository.save(target));
    }

    public UserProfileDTO registerCustomer(CustomerRegisterDto dto) {
        String normalizedEmail = dto.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Cet email est déjà utilisé.");
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(normalizedEmail);
        user.setMotDePasseHash(passwordEncoder.encode(dto.getMotDePasse()));
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        User saved = userRepository.save(user);
        return mapToUserProfiledTO(saved);
    }


}
