package com.texastoc.common;

import com.texastoc.module.player.model.Role;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationHelper {

  public boolean isLoggedInUserHaveRole(Role.Type roleType) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
    for (GrantedAuthority grantedAuthority : grantedAuthorities) {
      if (("ROLE_" + roleType.name()).equals(grantedAuthority.toString())) {
        return true;
      }
    }

    return false;
  }

  public String getLoggedInUserEmail() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetails) {
      return ((UserDetails) principal).getUsername();
    }
    return null;
  }

}
