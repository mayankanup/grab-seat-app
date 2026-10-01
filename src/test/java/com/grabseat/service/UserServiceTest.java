package com.grabseat.service;

import com.grabseat.exception.ConflictException;
import com.grabseat.exception.UnauthorizedException;
import com.grabseat.model.UserAccount;
import com.grabseat.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserAccountRepository users;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private UserService service() {
        return new UserService(users, encoder);
    }

    private UserAccount account() {
        return new UserAccount("anup", encoder.encode("password123"),
            "Anup Kumar", "anup@example.com");
    }

    @Test
    void registerHashesPasswordAndSaves() {
        when(users.findByLogin("anup")).thenReturn(Optional.empty());
        when(users.findByEmail("anup@example.com")).thenReturn(Optional.empty());
        when(users.save(any(UserAccount.class))).thenAnswer(i -> i.getArgument(0));

        UserAccount user =
            service().register("anup", "password123", "Anup Kumar", "anup@example.com");

        assertThat(user.getLogin()).isEqualTo("anup");
        assertThat(user.getFullName()).isEqualTo("Anup Kumar");
        assertThat(user.getEmail()).isEqualTo("anup@example.com");
        assertThat(user.getPasswordHash()).isNotEqualTo("password123");
        assertThat(encoder.matches("password123", user.getPasswordHash())).isTrue();
    }

    @Test
    void registerDuplicateLoginThrows409() {
        when(users.findByLogin("anup")).thenReturn(Optional.of(account()));

        assertThatThrownBy(
            () -> service().register("anup", "password123", "Anup Kumar", "anup@example.com"))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void registerDuplicateEmailThrows409() {
        when(users.findByLogin("anup2")).thenReturn(Optional.empty());
        when(users.findByEmail("anup@example.com")).thenReturn(Optional.of(account()));

        assertThatThrownBy(
            () -> service().register("anup2", "password123", "Anup Two", "anup@example.com"))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void registerShortPasswordThrows400() {
        assertThatThrownBy(
            () -> service().register("anup", "short", "Anup Kumar", "anup@example.com"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registerBadEmailThrows400() {
        assertThatThrownBy(
            () -> service().register("anup", "password123", "Anup Kumar", "not-an-email"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void authenticateSucceedsWithCorrectPassword() {
        when(users.findByLogin("anup")).thenReturn(Optional.of(account()));

        assertThat(service().authenticate("anup", "password123").getLogin())
            .isEqualTo("anup");
    }

    @Test
    void authenticateFailsWithWrongPassword() {
        when(users.findByLogin("anup")).thenReturn(Optional.of(account()));

        assertThatThrownBy(() -> service().authenticate("anup", "wrongpass1"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void authenticateFailsForUnknownLogin() {
        when(users.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().authenticate("ghost", "password123"))
            .isInstanceOf(UnauthorizedException.class);
    }
}
