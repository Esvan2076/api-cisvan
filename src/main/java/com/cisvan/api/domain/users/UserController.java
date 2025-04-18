package com.cisvan.api.domain.users;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.domain.users.dto.AuthRequest;
import com.cisvan.api.domain.users.dto.UserDTO;
import com.cisvan.api.services.JwtService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody AuthRequest request) {
        UserDTO userDTO = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        String token = userService.verify(request);
        if (token.equals("fail")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMe(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            String username = jwtService.extractUserName(token);
            Boolean admin = jwtService.extractAdmin(token);

            return ResponseEntity.ok(new UserDTO(userId, username, admin));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}