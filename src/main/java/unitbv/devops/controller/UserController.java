package unitbv.devops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import unitbv.devops.dto.UserDTO;
import unitbv.devops.service.UserService;

import java.util.List;

/**
 * REST Controller pentru operații pe utilizatori
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "API pentru gestionarea utilizatorilor")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * GET /api/users - Obține toți utilizatorii
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Returnează lista cu toți utilizatorii")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/{id} - Obține utilizator după ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Returnează detaliile unui utilizator specific")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/users/username/{username} - Obține utilizator după username
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Returnează detaliile unui utilizator după username")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/users - Creează un utilizator nou
     */
    @PostMapping
    @Operation(summary = "Create new user", description = "Creează un nou utilizator")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        UserDTO created = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/users/{id} - Actualizează un utilizator
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Actualizează detaliile unui utilizator")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/users/{id} - Șterge un utilizator
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Șterge un utilizator din sistem")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
