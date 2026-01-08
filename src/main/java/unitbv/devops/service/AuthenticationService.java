package unitbv.devops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import unitbv.devops.dto.LoginRequest;
import unitbv.devops.dto.LoginResponse;
import unitbv.devops.dto.RegisterRequest;
import unitbv.devops.entity.User;
import unitbv.devops.repository.UserRepository;
import unitbv.devops.security.JwtTokenProvider;

import java.util.Optional;

/**
 * Service pentru autentificare și autorizare
 */
@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Înregistrează un utilizator nou
     */
    public boolean register(RegisterRequest registerRequest) {
        // Verifică dacă utilizatorul deja există
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return false;  // Utilizator deja există
        }

        // Creează utilizator nou cu parolă criptată
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        userRepository.save(user);
        return true;
    }

    /**
     * Autentifică utilizator și generează JWT token
     */
    public Optional<LoginResponse> login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isEmpty()) {
            return Optional.empty();  // Utilizator nu există
        }

        User user = userOptional.get();

        // Verifică parola
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            return Optional.empty();  // Parolă incorectă
        }

        // Generează JWT token
        String token = jwtTokenProvider.generateToken(user.getUsername());
        LoginResponse response = new LoginResponse(token, user.getUsername(), "Bearer");

        return Optional.of(response);
    }

    /**
     * Verifică dacă un token este valid
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}

