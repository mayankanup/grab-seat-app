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
    public UserAccount register(String login, String password, String fullName, String email) {
        String id = requireLogin(login);
        requirePassword(password);
        String name = requireFullName(fullName);
        String mail = requireEmail(email);
        if (users.findByLogin(id).isPresent()) {
            throw new ConflictException("Login already taken: " + id);
        }
        if (users.findByEmail(mail).isPresent()) {
            throw new ConflictException("Email already registered: " + mail);
        }
        return users.save(new UserAccount(id, passwordEncoder.encode(password), name, mail));
    }

    @Transactional(readOnly = true)
    public UserAccount authenticate(String login, String password) {
        String id = login == null ? "" : login.trim();
        UserAccount user = users.findByLogin(id)
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (password == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return user;
    }

    private String requireLogin(String login) {
        String id = login == null ? "" : login.trim();
        if (!id.matches("[A-Za-z0-9._-]{3,32}")) {
            throw new IllegalArgumentException(
                "login must be 3-32 chars: letters, digits, . _ -");
        }
        return id;
    }

    private void requirePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("password must be 8-72 chars");
        }
    }

    private String requireFullName(String fullName) {
        String name = fullName == null ? "" : fullName.trim();
        if (name.isEmpty() || name.length() > 100) {
            throw new IllegalArgumentException("fullName is required (max 100 chars)");
        }
        return name;
    }

    private String requireEmail(String email) {
        String mail = email == null ? "" : email.trim().toLowerCase();
        if (!mail.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+") || mail.length() > 254) {
            throw new IllegalArgumentException("email must be a valid address");
        }
        return mail;
    }
}
