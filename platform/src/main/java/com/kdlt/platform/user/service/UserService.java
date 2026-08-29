package com.kdlt.platform.user.service;

import com.kdlt.platform.exceptions.EmailAlreadyExistsException;
import com.kdlt.platform.user.dto.UserCreateDto;
import com.kdlt.platform.user.dto.UserProfileDTO;
import com.kdlt.platform.user.dto.UserUpdateDto;
import com.kdlt.platform.user.entity.Role;
import com.kdlt.platform.user.entity.User;
import com.kdlt.platform.user.repository.UserRepository;
import org.hibernate.cache.spi.support.StorageAccess;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

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

    public UserProfileDTO createUser(UserCreateDto dto){
        if (userRepository.findByEmail(dto.getEmail()).isPresent()){
            throw new EmailAlreadyExistsException("The email already exists.");
        }
        User user = new User();
        String motDePasse = passwordEncoder.encode(dto.getMotDePasse());

        user.setPhotoUrl(dto.getPhotoUrl());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setMotDePasseHash(motDePasse);
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(dto.getEmail());
        user.setActive(true);
        LocalDateTime maintenant = LocalDateTime.now();
        user.setDateCreation(maintenant);
        user.setUpdatedAt(maintenant);

        userRepository.save(user);
        return mapToUserProfiledTO(user);
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
        String motDePasse = passwordEncoder.encode(dto.getMotDePasse());
        user.setMotDePasseHash(motDePasse);
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPhotoUrl(dto.getPhotoUrl());

        userRepository.save(user);
        return mapToUserProfiledTO(user);
    }

    public UserProfileDTO findByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return mapToUserProfiledTO(user);
    }

    public void deactivateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        user.setActive(false);
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void changeRole(String email, Role newRole){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setRole(newRole);
        userRepository.save(user);
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


}
