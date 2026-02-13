package kz.legeal.ease.backend.jwt;

import kz.legeal.ease.backend.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
public class PersonDetails implements UserDetails {

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final var role = user.getRoles();
        final var authority = new ArrayList<GrantedAuthority>();
        authority.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
        return authority;
    }

    Override

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
