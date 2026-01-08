package unitbv.devops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unitbv.devops.entity.User;
import unitbv.devops.repository.UserRepository;
import unitbv.devops.dto.UserDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer pentru operații pe utilizatori
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Obține toți utilizatorii
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obține utilizator după ID
     */
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Obține utilizator după username
     */
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO);
    }

    /**
     * Creează un utilizator nou
     */
    public UserDTO createUser(UserDTO userDTO) {
        User user = new User(userDTO.getUsername(), "");
        User saved = userRepository.save(user);
        return convertToDTO(saved);
    }

    /**
     * Actualizează un utilizator
     */
    public Optional<UserDTO> updateUser(Long id, UserDTO userDTO) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(userDTO.getUsername());
                    User updated = userRepository.save(user);
                    return convertToDTO(updated);
                });
    }

    /**
     * Șterge un utilizator
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Convertor de Entity la DTO
     */
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername()
        );
    }
}
