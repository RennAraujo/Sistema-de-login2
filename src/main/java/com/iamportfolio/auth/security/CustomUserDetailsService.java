package com.iamportfolio.auth.security;

import com.iamportfolio.identity.model.User;
import com.iamportfolio.identity.repository.UserRepository;
import com.iamportfolio.rbac.model.Permission;
import com.iamportfolio.rbac.model.Role;
import com.iamportfolio.rbac.model.RoleAssignment;
import com.iamportfolio.rbac.repository.RoleAssignmentRepository;
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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        logger.debug("Loading user: {}", usernameOrEmail);

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));

        return UserPrincipal.create(user, resolveAuthorities(user));
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found by id: " + id));
        return UserPrincipal.create(user, resolveAuthorities(user));
    }

    /**
     * Effective authorities = ROLE_USER (always) + role names + permission names,
     * collected from active direct role assignments and from groups the user belongs to.
     */
    private Collection<GrantedAuthority> resolveAuthorities(User user) {
        Set<Role> effectiveRoles = new HashSet<>();

        // 1. Direct grants via RoleAssignment (skip expired ones).
        List<RoleAssignment> active = roleAssignmentRepository.findActiveByUser(user, LocalDateTime.now());
        active.forEach(ra -> effectiveRoles.add(ra.getRole()));

        // 2. Group-mediated grants.
        user.getGroups().forEach(g -> effectiveRoles.addAll(g.getRoles()));

        Set<GrantedAuthority> authorities = effectiveRoles.stream()
                .flatMap(r -> {
                    Set<GrantedAuthority> a = new HashSet<>();
                    a.add(new SimpleGrantedAuthority(r.getName()));
                    for (Permission p : r.getPermissions()) {
                        a.add(new SimpleGrantedAuthority(p.getName()));
                    }
                    return a.stream();
                })
                .collect(Collectors.toSet());

        // Default authority for any authenticated user.
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }

    public static class UserPrincipal implements UserDetails {
        private final Long id;
        private final String username;
        private final String email;
        private final String password;
        private final boolean enabled;
        private final boolean accountNonExpired;
        private final boolean accountNonLocked;
        private final boolean credentialsNonExpired;
        private final Collection<? extends GrantedAuthority> authorities;

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

        public static UserPrincipal create(User user, Collection<GrantedAuthority> authorities) {
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

        public Long getId() { return id; }
        public String getEmail() { return email; }

        @Override public String getUsername() { return username; }
        @Override public String getPassword() { return password; }
        @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
        @Override public boolean isAccountNonExpired() { return accountNonExpired; }
        @Override public boolean isAccountNonLocked() { return accountNonLocked; }
        @Override public boolean isCredentialsNonExpired() { return credentialsNonExpired; }
        @Override public boolean isEnabled() { return enabled; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserPrincipal that)) return false;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
