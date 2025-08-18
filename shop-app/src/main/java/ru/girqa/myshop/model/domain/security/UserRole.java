package ru.girqa.myshop.model.domain.security;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
  ANONYMOUS, USER, ADMIN;

  @Override
  public String getAuthority() {
    return "ROLE_" + this.name();
  }
}
