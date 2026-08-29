package com.kdlt.platform.auth.service;

import com.kdlt.platform.auth.dto.JwtResponse;
import com.kdlt.platform.auth.dto.LoginRequest;
import com.kdlt.platform.auth.security.JwtUtil;
import com.kdlt.platform.exceptions.BadRequestException;
import com.kdlt.platform.user.entity.User;
import com.kdlt.platform.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Email ou mot de passe incorrect."));

        if (!user.isActive()) {
            throw new BadRequestException("Ce compte est désactivé.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getMotDePasseHash())){
            throw new BadRequestException("Email ou mot de passe incorrect.");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new JwtResponse(token, user.getRole().name());
    }

}
