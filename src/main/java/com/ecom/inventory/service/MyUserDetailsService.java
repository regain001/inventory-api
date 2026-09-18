package com.ecom.inventory.service;


import com.ecom.inventory.dao.Dao;
import com.ecom.inventory.entity.AppUser;
import com.ecom.inventory.security.UserPrincipal;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyUserDetailsService extends Dao implements UserDetailsService {

    @Autowired
    private UserService userService;

    public MyUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserPrincipal loadUserByUsername(String userName) throws UsernameNotFoundException {
        AppUser activeUser = userService.getUserByUserName(userName);
        if (activeUser == null) {
            throw new UsernameNotFoundException("Not a valid username");
        }
        //get user campus permission
//        List<UserCampusPermission> campusPermissions = campusPermissionRepository.findAllByUserId(activeUser.getId());

        return new UserPrincipal(activeUser);
    }


}
