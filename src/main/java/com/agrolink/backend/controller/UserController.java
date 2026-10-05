package com.agrolink.backend.controller;

import com.agrolink.backend.dto.*;
import com.agrolink.backend.model.User;
import com.agrolink.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    // ✅ FCM Token එක සේව් කරන API එක
    @PostMapping("/user/update-fcm-token")
    public ResponseEntity<?> updateFcmToken(@RequestParam String email, @RequestParam String fcmToken) {
        userService.updateFcmToken(email, fcmToken);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public List<User> getUsers() { return userService.getAllUsers(); }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @RequestMapping(value = {"/users/{id}", "/user/{id}", "/user/update/{id}"}, method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateProfile(@PathVariable int id, @RequestBody UpdateProfileRequest request) {
        try {
            return ResponseEntity.ok(userService.updateProfile(id, request));
        } catch (IllegalArgumentException ex) {
            HttpStatus status = "User not found".equals(ex.getMessage()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(ex.getMessage());
        }
    }

    @PostMapping({"/auth/register", "/user/save"})
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED).body(response);
    }
}
