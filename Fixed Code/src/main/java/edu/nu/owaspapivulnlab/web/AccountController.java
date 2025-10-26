package edu.nu.owaspapivulnlab.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import edu.nu.owaspapivulnlab.model.Account;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AccountRepository;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
// Task 9: Input Validation
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accounts;
    private final AppUserRepository users;

    public AccountController(AccountRepository accounts, AppUserRepository users) {
        this.accounts = accounts;
        this.users = users;
    }

    // Task 3: Resource Ownership - Validate account ownership before operations
    private void validateAccountOwnership(Account account, Authentication auth) {
        AppUser user = users.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (!account.getOwnerUserId().equals(user.getId()) && !user.isAdmin()) {
            throw new RuntimeException("Access denied: Account belongs to another user");
        }
    }

    @GetMapping("/{id}/balance")
    public Double balance(@PathVariable Long id, Authentication auth) {
        Account a = accounts.findById(id).orElseThrow(() -> new RuntimeException("Account not found"));
        // Task 3: Resource Ownership - Ensure user can only access their own account balance
        validateAccountOwnership(a, auth);
        return a.getBalance();
    }

    // Task 9: Input Validation - Validate transfer amount
    @PostMapping("/{id}/transfer")
    public ResponseEntity<?> transfer(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "Amount is required")
            @Min(value = 1, message = "Amount must be positive")
            @Max(value = 1000000, message = "Amount too large") Double amount,
            Authentication auth) {
        Account a = accounts.findById(id).orElseThrow(() -> new RuntimeException("Account not found"));
        // Task 3: Resource Ownership - Ensure user can only transfer from their own account
        validateAccountOwnership(a, auth);
        // Task 9: Reject negative, zero, or excessively large values
        if (amount == null || amount <= 0 || amount > 1_000_000) {
            return ResponseEntity.badRequest().body("Invalid transfer amount");
        }
        if (a.getBalance() < amount) {
            return ResponseEntity.badRequest().body("Insufficient funds");
        }
        a.setBalance(a.getBalance() - amount);
        accounts.save(a);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("remaining", a.getBalance());
        return ResponseEntity.ok(response);
    }

// Task 3: Safe-ish helper to view my accounts (returns selected user fields + total balance)
    @GetMapping("/mine")
    public ResponseEntity<?> mine(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        AppUser me = users.findByUsername(auth.getName())
                        .orElseThrow(() -> new RuntimeException("User not found"));

        // Task 3: Aggregate balance across all accounts owned by the current user
        double totalBalance = accounts.findByOwnerUserId(me.getId()).stream()
                .mapToDouble(a -> a.getBalance() == null ? 0.0 : a.getBalance()).sum();

        // Task 4: Data Exposure Control - return DTO without sensitive fields (no password/role/isAdmin)
        AccountOwnerDto dto = new AccountOwnerDto(me.getId(), me.getUsername(), me.getEmail(), totalBalance);
        return ResponseEntity.ok(dto);  // return selected user fields
    }

    // Task 4: DTO for account owner view (no password, no role/isAdmin)
    private static class AccountOwnerDto {
        private Long id;
        private String username;
        private String email;
        private Double balance;

        public AccountOwnerDto() {}

        public AccountOwnerDto(Long id, String username, String email, Double balance) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.balance = balance;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public Double getBalance() { return balance; }
    }
}
