package com.eventpulse.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // Hardcoded users for demonstration
    private final Map<String, UserDetails> users = new HashMap<>();

    public CustomUserDetailsService() {
        // Create hardcoded users
        users.put("admin", User.builder()
                .username("admin")
                .password("$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi") // password
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build());

        users.put("user", User.builder()
                .username("user")
                .password("$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi") // password
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build());

        users.put("eventpulse", User.builder()
                .username("eventpulse")
                .password("$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi") // password
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return user;
    }

    /**
     * Get all available usernames (for documentation purposes)
     */
    public String[] getAvailableUsernames() {
        return users.keySet().toArray(new String[0]);
    }

    /**
     * Check if user exists
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
}
