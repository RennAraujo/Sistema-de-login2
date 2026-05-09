package com.iamportfolio.security;

import com.iamportfolio.model.User;
import com.iamportfolio.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        logger.debug("Carregando usuÃ¡rio: {}", usernameOrEmail);
        
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> {
                    logger.error("UsuÃ¡rio nÃ£o encontrado: {}", usernameOrEmail);
                    return new UsernameNotFoundException("UsuÃ¡rio nÃ£o encontrado: " + usernameOrEmail);
                });

        logger.debug("UsuÃ¡rio encontrado: {}", user.getUsername());
        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        logger.debug("Carregando usuÃ¡rio por ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("UsuÃ¡rio nÃ£o encontrado com ID: {}", id);
                    return new UsernameNotFoundException("UsuÃ¡rio nÃ£o encontrado com ID: " + id);
                });

        return UserPrincipal.create(user);
    }

    // Classe interna para representar o principal do usuÃ¡rio
    public static class UserPrincipal implements UserDetails {
        private Long id;
        private String username;
        private String email;
        private String password;
        private boolean enabled;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;
        private Collection<? extends GrantedAuthority> authorities;

        public UserPrincipal(Long id, String username, String email, String password, 
                            boolean enabled, boolean accountNonExpired, boolean accountNonLocked, 
                            boolean credentialsNonExpired, Collection<? extends GrantedAuthority> authorities) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.enabled = enabled;
            this.accountNonExpired = accountNonExpired;
            this.accountNonLocked = accountNonLocked;
            this.credentialsNonExpired = credentialsNonExpired;
            this.authorities = authorities;
        }

        public static UserPrincipal create(User user) {
            Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
            );

            return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired(),
                authorities
            );
        }

        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public boolean isAccountNonExpired() {
            return accountNonExpired;
        }

        @Override
        public boolean isAccountNonLocked() {
            return accountNonLocked;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return credentialsNonExpired;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserPrincipal that = (UserPrincipal) o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
} 