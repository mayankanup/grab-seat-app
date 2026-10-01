package com.grabseat.service;

import com.grabseat.exception.ConflictException;
import com.grabseat.exception.UnauthorizedException;
import com.grabseat.model.UserAccount;
import com.grabseat.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount register(String username, String password) {
        String id = requireUsername(username);
        requirePassword(password);
        if (users.findByUsername(id).isPresent()) {
            throw new ConflictException("User already exists: " + id);
        }
        return users.save(new UserAccount(id, passwordEncoder.encode(password)));
    }

    @Transactional(readOnly = true)
    public UserAccount authenticate(String username, String password) {
        String id = username == null ? "" : username.trim();
        UserAccount user = users.findByUsername(id)
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (password == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return user;
    }

    private String requireUsername(String username) {
        String id = username == null ? "" : username.trim();
        if (!id.matches("[A-Za-z0-9._-]{3,32}")) {
            throw new IllegalArgumentException(
                "userId must be 3-32 chars: letters, digits, . _ -");
        }
        return id;
    }

    private void requirePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("password must be 8-72 chars");
        }
    }
}
