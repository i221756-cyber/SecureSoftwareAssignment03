package edu.nu.owaspapivulnlab.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AppUserRepository users;

    public UserController(AppUserRepository users) {
        this.users = users;
    }

    // Task 3: Resource Ownership - Helper method to validate user ownership
    private void validateUserOwnership(Long userId, Authentication auth) {
        AppUser currentUser = users.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.getId().equals(userId) && !currentUser.isAdmin()) {
            throw new RuntimeException("Access denied: Can only access your own user data");
        }
    }

    // Task 3: Resource Ownership - Helper endpoint to get current authenticated user's profile
    @GetMapping("/mine")
    public ResponseEntity<?> mine(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        AppUser me = users.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        // Task 4: Data Exposure Control - return DTO that excludes sensitive fields
        SafeUserDto dto = new SafeUserDto(me.getId(), me.getUsername(), me.getEmail());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public SafeUserDto get(@PathVariable("id") Long id, Authentication auth) {
        // Task 3: Resource Ownership - Ensure users can only access their own data
        validateUserOwnership(id, auth);
        AppUser u = users.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return new SafeUserDto(u.getId(), u.getUsername(), u.getEmail());
    }

    // Task 6: Mass Assignment Prevention - Only map allowed fields from request
    @PostMapping
    public SafeUserDto create(@Valid @RequestBody AppUser body) {
        // Task 6: Prevent mass assignment by ignoring any client-supplied role/isAdmin
        AppUser user = new AppUser();
        user.setUsername(body.getUsername());
        user.setPassword(body.getPassword());
        user.setEmail(body.getEmail());
        user.setAdmin(false); // Always set by server
        user.setRole("USER"); // Always set by server
        // Optionally: validate fields further here if needed
        AppUser saved = users.save(user);
        // Task 4: Data Exposure Control - return DTO without sensitive fields
        return new SafeUserDto(saved.getId(), saved.getUsername(), saved.getEmail());
    }

    @GetMapping("/search")
    public List<SafeUserDto> search(@RequestParam String q, Authentication auth) {
        // Task 3: Resource Ownership - Only admins can search users
        AppUser currentUser = users.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.isAdmin()) {
            throw new RuntimeException("Access denied: Search is restricted to administrators");
        }
        return users.search(q).stream()
            .map(u -> new SafeUserDto(u.getId(), u.getUsername(), u.getEmail()))
            .toList();
    }

    @GetMapping
    public List<SafeUserDto> list(Authentication auth) {
        // Task 3: Resource Ownership - Only admins can list all users
        AppUser currentUser = users.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.isAdmin()) {
            throw new RuntimeException("Access denied: User listing is restricted to administrators");
        }
        return users.findAll().stream()
            .map(u -> new SafeUserDto(u.getId(), u.getUsername(), u.getEmail()))
            .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        // Task 3: Resource Ownership - Only allow self-deletion or admin deletion
        validateUserOwnership(id, auth);
        users.deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "deleted");
        return ResponseEntity.ok(response);
    }

    // Task 4: Data Exposure Control - DTO used to return safe user data (no password/role/isAdmin)
    private static class SafeUserDto {
        private Long id;
        private String username;
        private String email;

        public SafeUserDto() {}

        public SafeUserDto(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
    }
}
