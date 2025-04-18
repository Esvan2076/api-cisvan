package com.cisvan.api.domain.users;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.users.dto.AuthRequest;
import com.cisvan.api.domain.users.dto.UserDTO;
import com.cisvan.api.services.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserDTO register(AuthRequest request) {
        Users user = Users.builder()
                .username(request.getUsername())
                .password(encoder.encode(request.getPassword()))
                .admin(false) // nunca dejamos que el cliente mande esto
                .build();

        usersRepository.save(user);

        return new UserDTO(user.getId(), user.getUsername(), user.getAdmin());
    }

    public String verify(AuthRequest request) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            Users user = usersRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return jwtService.generateToken(user.getId(), user.getUsername(), user.getAdmin());
        }

        return "fail";
    }
}
