package com.financafacil.infrastructure.security.userdetails;

import com.financafacil.domain.model.User;
import com.financafacil.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_returnsUserDetails_whenActiveUserFound() {
        var userId = UUID.randomUUID();
        var user = User.builder()
            .id(userId).name("Test User").email("test@test.com")
            .passwordHash("encodedPassword").createdAt(Instant.now())
            .updatedAt(Instant.now()).emailVerified(true).active(true).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        var userDetails = userDetailsService.loadUserByUsername("test@test.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(userId.toString());
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@test.com"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found");
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenUserInactive() {
        var user = User.builder()
            .id(UUID.randomUUID()).name("Inactive").email("inactive@test.com")
            .passwordHash("hash").createdAt(Instant.now())
            .updatedAt(Instant.now()).emailVerified(true).active(false).build();

        when(userRepository.findByEmail("inactive@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("inactive@test.com"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found");
    }
}
