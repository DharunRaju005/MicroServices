package org.example.service;

import org.example.entities.UserRole;
import org.example.entities.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails extends Users implements UserDetails {

    Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Users byUserName) {

        this.setPassword(byUserName.getPassword());
        this.setUserId(byUserName.getUserId());
        this.setUserName(byUserName.getUserName());

        List<GrantedAuthority>auths=new ArrayList<>();
        for(UserRole role:byUserName.getRoles()){
            auths.add(new SimpleGrantedAuthority(role.getName().toUpperCase()));
        }
        this.authorities=auths;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }



    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public String getUsername() {
        return super.getUserName();
    }

    @Override
    public String getUserId() {
        return super.getUserId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }




}
