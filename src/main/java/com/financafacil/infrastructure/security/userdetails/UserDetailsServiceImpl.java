package com.financafacil.infrastructure.security.userdetails;

import com.financafacil.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .filter(u -> u.isActive())
            .map(u -> new org.springframework.security.core.userdetails.User(
                u.getId().toString(),
                u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))))
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
