package kz.legeal.ease.backend.jwt;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PersonDetails implements UserDetails {

    private final User user;
    private final UserRole userRole;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getCode()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !user.isDeleted() && user.isActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isDeleted() && user.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !user.isDeleted() && user.isActive();
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
