package com.agrolink.backend.service;

import com.agrolink.backend.dto.AuthResponse;
import com.agrolink.backend.dto.AdminAuthResponse;
import com.agrolink.backend.dto.LoginRequest;
import com.agrolink.backend.dto.RegisterRequest;
import com.agrolink.backend.model.User;
import com.agrolink.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminTokenService adminTokenService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveUser(User user) {
        if (hasRegisteredEmail(user.getEmail())) throw new IllegalArgumentException("Email already registered");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // ✅ FCM Token එක Update කරන මෙතඩ් එක
    public void updateFcmToken(String email, String token) {
        userRepository.findAllByEmailIgnoreCaseOrderByIdAsc(email.trim().toLowerCase())
                .stream().findFirst().ifPresent(user -> {
                    user.setFcmToken(token);
                    userRepository.save(user);
                });
    }

    public AuthResponse register(RegisterRequest request) {
        if (request == null) return new AuthResponse(false, "Request body required", null, null);
        if (hasRegisteredEmail(request.getEmail())) return new AuthResponse(false, "Email registered", null, null);

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() == null ? "USER" : request.getRole());
        User saved = userRepository.save(user);
        return new AuthResponse(true, "Registration successful", null, saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = authenticate(request);
        if (user == null) return new AuthResponse(false, "Invalid credentials", null, null);

        // ✅ දැන් සාමාන්‍ය ලොගින් එකේදීත් ටෝකන් එකක් ජෙනරේට් වෙනවා
        String token = adminTokenService.generateToken(user);
        return new AuthResponse(true, token, user, "Login successful");
    }

    public AdminAuthResponse adminLogin(LoginRequest request) {
        User user = authenticate(request);
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return new AdminAuthResponse(false, "Admin access denied", null, null);
        }
        String token = adminTokenService.generateToken(user);
        return new AdminAuthResponse(true, "Admin login successful", token, user);
    }

    private User authenticate(LoginRequest request) {
        if (request == null || request.getEmail() == null) return null;
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        return userRepository.findAllByEmailIgnoreCaseOrderByIdAsc(email).stream()
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .findFirst().orElse(null);
    }

    public long countUsers() { return userRepository.count(); }
    private boolean hasRegisteredEmail(String email) { return userRepository.countByEmailIgnoreCase(email) > 0; }
}