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

    @Test
    void registerHashesPasswordAndSaves() {
        when(users.findByUsername("anup")).thenReturn(Optional.empty());
        when(users.save(any(UserAccount.class))).thenAnswer(i -> i.getArgument(0));

        UserAccount user = service().register("anup", "password123");

        assertThat(user.getUsername()).isEqualTo("anup");
        assertThat(user.getPasswordHash()).isNotEqualTo("password123");
        assertThat(encoder.matches("password123", user.getPasswordHash())).isTrue();
    }

    @Test
    void registerDuplicateThrows409() {
        when(users.findByUsername("anup")).thenReturn(
            Optional.of(new UserAccount("anup", "hash")));

        assertThatThrownBy(() -> service().register("anup", "password123"))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void registerShortPasswordThrows400() {
        assertThatThrownBy(() -> service().register("anup", "short"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void authenticateSucceedsWithCorrectPassword() {
        when(users.findByUsername("anup")).thenReturn(
            Optional.of(new UserAccount("anup", encoder.encode("password123"))));

        assertThat(service().authenticate("anup", "password123").getUsername())
            .isEqualTo("anup");
    }

    @Test
    void authenticateFailsWithWrongPassword() {
        when(users.findByUsername("anup")).thenReturn(
            Optional.of(new UserAccount("anup", encoder.encode("password123"))));

        assertThatThrownBy(() -> service().authenticate("anup", "wrongpass1"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void authenticateFailsForUnknownUser() {
        when(users.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().authenticate("ghost", "password123"))
            .isInstanceOf(UnauthorizedException.class);
    }
}
