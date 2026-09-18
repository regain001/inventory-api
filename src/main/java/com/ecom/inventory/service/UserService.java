/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.service;


import com.ecom.inventory.dao.Dao;
import com.ecom.inventory.dto.common.ResponseDto;
import com.ecom.inventory.entity.AppUser;
import com.ecom.inventory.exception.UserInputValidationException;
import com.ecom.inventory.repository.AppUserRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;


/**
 * @author SohanBappy
 */
@Service
public class UserService extends Dao {

    @Value("${comm.isDevMode}")
    private Boolean isDevMode;

    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;

    private final String DEFAULT_INI_PASS_HASH = "$2a$10$sO6YVh35gkacXLwlEtdZNebxukFx6RH14RHahiFFg3hTepRuQm5Vu";
    private final String ACTIVE_USER_STATAUS = "Active";

    public UserService(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public List<AppUser> getAllUser() {
        return userRepository.findAll();
    }

    public Optional<AppUser> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public AppUser saveUser(AppUser user) {
        String plainPass = user.getPasswordHash();
        user.setPasswordHash(encoder.encode(plainPass));
        return userRepository.save(user);
    }



    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public AppUser getUserByUserName(String username) {
        AppUser user = null;
        String query = "from User where username =:username AND status = '" + ACTIVE_USER_STATAUS + "'";//Hibernate param
        Session session = getCurrentSession();
        user = (AppUser) session.createQuery(query).setParameter("username", username).uniqueResult();
        return user;
    }

    public List<BigInteger> getUserRoleIds(Long userId) {
        String query = "select role_id from user_role  where user_id =  " + userId;
        Session session = getCurrentSession();
//        List<BigInteger> ids = session.createSQLQuery(query).list();
        List<BigInteger> ids = new ArrayList<>();
        return ids;
    }

    public List<String> getUserRoles(Long userId) {
        String query = "select distinct r.role from user_role ur join role_tb r on ur.role_id = r.id where ur.user_id =  " + userId;
        Session session = getCurrentSession();
//        List<String> roles = session.createSQLQuery(query).list();
        List<String> roles = new ArrayList<>();
        return roles;
    }

//    public ResponseDto generatePasswordForUser(UserIdRangeDto params) throws UserInputValidationException {
//        ResponseDto retDto = new ResponseDto();
//        //validate
//        if (params.getFromUserId() == null || params.getFromUserId() < 0) {
//            throw new UserInputValidationException("fromId", "Invalid from id.");
//        }
//        if (params.getToUserId() == null || params.getToUserId() < 0) {
//            throw new UserInputValidationException("toId", "Invalid to id.");
//        } else if (params.getToUserId() < params.getFromUserId()) {
//            throw new UserInputValidationException("toId", "To User id must be greater than or equal to from user id");
//        } else {
//
//        }
//        Session session = getCurrentSession();
////        List<User> smsToBeSentUsers = new ArrayList<>();
//        try {
//            String query = "from User where id >= " + params.getFromUserId() + " AND id <= " + params.getToUserId() + " ";
//            List<User> userList = getListByQuery(session, query);
//            int count = 0;
//            if (userList != null) {
//                for (User user : userList) {
////                    String currPass = user.getPassword();
////                    if (currPass == null ? DEFAULT_INI_PASS_HASH == null : currPass.equals(DEFAULT_INI_PASS_HASH)) {
//                        Random random = new Random(new Date().getTime());
//                        String plainPass = String.format("%06d", random.nextInt(1000000));
//                        String hashedPass = bCryptPasswordEncoder.encode(plainPass);
//
//                        //update
//                        user.setOtp(plainPass);
//                        user.setPassword(hashedPass);
//                        user.setPasswordLastResetTime(new Date());
//                        user.setIsPasswordResetMandatory(1);
//                        userRepository.save(user);
//                        ++count;
////                        smsToBeSentUsers.add(user);
////                    }
//
//                }
//            }
//
//            // sms code
////            for (User user : smsToBeSentUsers) {
////                Boolean isSentOtp = false;
////                if (user.getIsPasswordResetMandatory() == 1) {
////                    isSentOtp = true;
////                }
////                passwordSmsUtil.sendSmsAfterPasswordGeneration(user, isSentOtp);
////            }
//
//            // final response data
//            String finalMsg = "";
//
//            if (count > 1) {
//                finalMsg = "Password generated for " + count + " users.";
//            } else if (count == 1) {
//                finalMsg = "Password generated for " + count + " user.";
//            } else if (count == 0) {
//                finalMsg = "No password generated for any user.";
//            }
//            retDto.setData(finalMsg);
//            retDto.setMessage(finalMsg);
//        } catch (Exception e) {
//            e.printStackTrace();
//            retDto.setData("Error in password generation.");
//            retDto.setError("Error in password generation.");
//        }
//
//        return retDto;
//    }
}
