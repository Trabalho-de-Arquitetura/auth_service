package com.authentication.service;

import com.authentication.entity.AuthUser;
import com.authentication.repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User; // Import correto
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList; // Para authorities vazias

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthUserRepository userRepository;

    public CustomUserDetailsService(AuthUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AuthUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        // Para simplicidade, não estamos usando roles aqui, mas você poderia adicioná-las
        return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}