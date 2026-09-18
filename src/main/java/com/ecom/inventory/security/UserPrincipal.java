package com.ecom.inventory.security;



import com.ecom.inventory.entity.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author SohanBappy
 */
public class UserPrincipal implements UserDetails {

    private AppUser user;
//    private List<UserCampusPermission> campusPermissions;

    public UserPrincipal() {
    }

    public UserPrincipal(AppUser user) {
        this.user = user;
//        this.campusPermissions = campusPermissions;
    }

    public AppUser getUser() {
        return user;
    }

//    public List<UserCampusPermission> getCampusPermissions() {
//        return campusPermissions;
//    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }



    @Override
    public String getPassword() {
        if(user==null){
            return "na";
        }
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        if(user==null){
            return "na";
        }
        return user.getUsername();
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
