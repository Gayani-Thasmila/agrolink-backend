package com.agrolink.backend.service;

import com.agrolink.backend.dto.AuthResponse;
import com.agrolink.backend.dto.AdminAuthResponse;
import com.agrolink.backend.dto.LoginRequest;
import com.agrolink.backend.dto.RegisterRequest;
import com.agrolink.backend.dto.UpdateProfileRequest;
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

    public User getUserById(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User saveUser(User user) {
        if (hasRegisteredEmail(user.getEmail())) throw new IllegalArgumentException("Email already registered");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }


    public void updateFcmToken(String email, String token) {
        userRepository.findAllByEmailIgnoreCaseOrderByIdAsc(email.trim().toLowerCase())
                .stream().findFirst().ifPresent(user -> {
                    user.setFcmToken(token);
                    userRepository.save(user);
                });
    }

    public AuthResponse register(RegisterRequest request) {
        if (request == null) return new AuthResponse(false, "Request body required", null, null);
        if (request.getEmail() == null || request.getPassword() == null) {
            return new AuthResponse(false, "Email and password required", null, null);
        }
        if (hasRegisteredEmail(request.getEmail())) return new AuthResponse(false, "Email registered", null, null);

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        applyLocation(user, request.getLatitude(), request.getLongitude());
        user.setRole(request.getRole() == null ? "USER" : request.getRole());
        User saved = userRepository.save(user);
        return new AuthResponse(true, "Registration successful", null, saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = authenticate(request);
        if (user == null) return new AuthResponse(false, "Invalid credentials", null, null);

        // ✅ දැන් සාමාන්‍ය ලොගින් එකේදීත් ටෝකන් එකක් ජෙනරේට් වෙනවා
        String token = adminTokenService.generateToken(user);
        return new AuthResponse(true, "Login successful", token, user);
    }

    public AdminAuthResponse adminLogin(LoginRequest request) {
        User user = authenticate(request);
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return new AdminAuthResponse(false, "Admin access denied", null, null);
        }
        String token = adminTokenService.generateToken(user);
        return new AdminAuthResponse(true, "Admin login successful", token, user);
    }

    public User updateProfile(int userId, UpdateProfileRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Profile request is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getName() != null) user.setName(request.getName().trim());
        if (request.getPhone() != null) user.setPhone(request.getPhone().trim());
        if (request.getAddress() != null) user.setAddress(request.getAddress().trim());
        applyLocation(user, request.getLatitude(), request.getLongitude());

        return userRepository.save(user);
    }

    private void applyLocation(User user, Double latitude, Double longitude) {
        if (latitude == null && longitude == null) {
            return;
        }
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Both latitude and longitude are required");
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid latitude or longitude");
        }
        user.setLatitude(latitude);
        user.setLongitude(longitude);
    }

    private User authenticate(LoginRequest request) {
        if (request == null || request.getEmail() == null) return null;
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        return userRepository.findAllByEmailIgnoreCaseOrderByIdAsc(email).stream()
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .findFirst().orElse(null);
    }

    public long countUsers() { return userRepository.count(); }
    private boolean hasRegisteredEmail(String email) { return userRepository.countByEmailIgnoreCase(email.trim().toLowerCase()) > 0; }
}
