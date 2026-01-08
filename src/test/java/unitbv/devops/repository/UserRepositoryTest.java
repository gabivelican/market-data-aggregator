package unitbv.devops.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unitbv.devops.DatabaseTestBase;
import unitbv.devops.entity.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest extends DatabaseTestBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindUserByUsername_Admin() {
        Optional<User> user = userRepository.findByUsername("admin");
        assertTrue(user.isPresent(), "Admin user should exist in seed data");
        assertEquals("admin", user.get().getUsername());
    }

    @Test
    public void testFindUserByUsername_Trader() {
        Optional<User> user = userRepository.findByUsername("trader1");
        assertTrue(user.isPresent(), "Trader1 user should exist in seed data");
        assertEquals("trader1", user.get().getUsername());
    }

    @Test
    public void testFindUserByUsername_Analyst() {
        Optional<User> user = userRepository.findByUsername("analyst");
        assertTrue(user.isPresent(), "Analyst user should exist in seed data");
        assertEquals("analyst", user.get().getUsername());
    }

    @Test
    public void testFindUserNotFound() {
        Optional<User> user = userRepository.findByUsername("nonexistent");
        assertFalse(user.isPresent(), "Non-existent user should not be found");
    }

    @Test
    public void testCreateNewUser() {
        User newUser = new User("testuser", "hashed_password_test");
        User saved = userRepository.save(newUser);
        assertNotNull(saved.getId(), "Saved user should have an ID");

        Optional<User> found = userRepository.findByUsername("testuser");
        assertTrue(found.isPresent(), "Newly created user should be findable");
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    public void testUserCreatedAtTimestamp() {
        User newUser = new User("timestamptest", "hashed_password_test");
        User saved = userRepository.save(newUser);
        assertNotNull(saved.getCreatedAt(), "User should have created_at timestamp");
    }

    @Test
    public void testFindAllUsers() {
        Iterable<User> users = userRepository.findAll();
        int count = 0;
        for (User user : users) {
            count++;
        }
        assertTrue(count >= 3, "Should have at least 3 users from seed data");
    }
}
