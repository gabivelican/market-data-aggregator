package unitbv.devops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import unitbv.devops.dto.LoginRequest;
import unitbv.devops.dto.LoginResponse;
import unitbv.devops.dto.RegisterRequest;
import unitbv.devops.dto.AuthResponse;
import unitbv.devops.service.AuthenticationService;

import java.util.Optional;

/**
 * REST Controller pentru autentificare și autorizare
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API pentru autentificare și autorizare")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * POST /api/auth/register - Înregistrează un utilizator nou
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creează un cont nou cu username și parolă")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null || registerRequest.getUsername().isEmpty() ||
            registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new AuthResponse("Username și password sunt obligatorii", null, false)
            );
        }

        boolean registered = authenticationService.register(registerRequest);

        if (registered) {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new AuthResponse("Utilizator înregistrat cu succes", registerRequest.getUsername(), true)
            );
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new AuthResponse("Username deja utilizat", registerRequest.getUsername(), false)
            );
        }
    }

    /**
     * POST /api/auth/login - Autentifică utilizator și returnează JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Autentifică utilizatorul și returnează JWT token")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty() ||
            loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new AuthResponse("Username și password sunt obligatorii", null, false)
            );
        }

        Optional<LoginResponse> result = authenticationService.login(loginRequest);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new AuthResponse("Username sau parolă incorectă", loginRequest.getUsername(), false)
            );
        }
    }

    /**
     * POST /api/auth/validate - Validează JWT token
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate token", description = "Verifică dacă un JWT token este valid")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                    new AuthResponse("Authorization header nu este valid", null, false)
            );
        }

        String token = authHeader.substring(7);
        boolean isValid = authenticationService.validateToken(token);

        if (isValid) {
            return ResponseEntity.ok(
                    new AuthResponse("Token este valid", null, true)
            );
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new AuthResponse("Token nu este valid", null, false)
            );
        }
    }
}
