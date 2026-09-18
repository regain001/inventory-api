/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.security;

/**
 * @author SohanBappy
 */
public class SecurityConstants {

    public static final String[] AUTH_WHITELIST = new String[]{
//        "**/reports/**",
            "/api/authenticate",
            "/authenticate/**",
            "/api/makeToken",
            "/files/download/**",
            "/files/view/**",
            "/admission/applications/home",
            "/admission/applications/meta",
            "/admission/applications/submit",
            "/baust/admission/applications/submit",
            "/baust/admission/applications/meta",
            "/baust/admission/applications/home",
            "/baust/admission/applications/forget-credentials",
            "/baust/admission/applications/apply-offline",
            "/baust/admission/applications/apply-offline",
            "/password/forget-password",
            "/password/student-forget-password",
            "/password/student-otp-verification-of-forget-password",
            "/password/student-forget-password-submission",
            "/address/**",
            "/candidate/login",
            "/recover/*",
            "/sslcommerz/*",
            "/public-misc/**",
            "/content/**",
            "/web-socket"
    };

    public static final String[] SPECIAL_URLS = new String[]{
            "/special-enrollment/*",
            "notused"
    };

    public static final String[] PREREG_URLS = new String[]{
            "/prereg/*",
            "notused"
    };

    public static final String[] BAUST_URLS = new String[]{
            "/baust/*",
            "notused"
    };

    public static final String[] STUDENT_PORTAL_URLS = new String[]{
            "/student-portal/*",
            "notused"
    };

    public static final String[] BUP_URLS = new String[]{
            "/affiliate-institute/*",
            "notused"
    };
}
