//package com.ecom.inventory.security;
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.JWTVerifier;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.exceptions.JWTVerificationException;
//import com.auth0.jwt.interfaces.DecodedJWT;
//import com.commlink.ems.dto.common.ResponseDto;
//import com.commlink.ems.service.JwtDecoder;
//import com.commlink.ems.util.common.JwtDetail;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
///**
// *
// * @author SohanBappy
// */
//@Component
//public class JwtRequestFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private JwtDecoder jwtDecoder;
//
//    @Value("${token.secret}")
//    private String tokenSecret;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//        System.out.println("servlet path: " + request.getServletPath());
//        String requestPath = request.getServletPath();
//        if (UriMatcher.matchedAuthWhiteList(requestPath)) {
//            //skip filtering, target URI is in the WHITELIST
//            System.out.println("Skipping auth filtering, target URI is in the WHITELIST");
//        } else {
//            try {
//                final String authorizationHeader = request.getHeader("Authorization");
//                String jwt = null;
//                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//                    DecodedJWT decodedJWT = null;
//                    try {
//                        jwt = authorizationHeader.substring(7);
//                        Algorithm algo = Algorithm.HMAC256(tokenSecret.getBytes());
//                        JWTVerifier verifier = JWT.require(algo).build();
//                        decodedJWT = verifier.verify(jwt);
//                    } catch (JWTVerificationException | IllegalArgumentException | UsernameNotFoundException ex) {
//                        writeResponse(response, HttpStatus.UNAUTHORIZED, "Invalid JWT token");
//                        return;
//                    }
//
//                    JwtDetail jwtDetail = jwtDecoder.getJwtDetailByDecodedJWT(decodedJWT);
//                    String applicationType = jwtDetail.getApplicationType();
//                    if (applicationType == null) {
//                        writeResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token, application type not defined");
//                        return;
//                    } else {
//                        if (applicationType.equalsIgnoreCase(AppConstants.APP_TYPE_EMS)) {
//                            if (UriMatcher.matched(requestPath, SecurityConstants.PREREG_URLS)) {
//                                writeResponse(response, HttpStatus.UNAUTHORIZED, "Trying to access Pre-registration API with EMS token");
//                                return;
//                            }
//                            if (UriMatcher.matched(requestPath, SecurityConstants.SPECIAL_URLS)) {
//                                writeResponse(response, HttpStatus.UNAUTHORIZED, "Trying to access Special-enrollment API with EMS token");
//                                return;
//                            }
//                        } else if (applicationType.equalsIgnoreCase(AppConstants.APP_TYPE_PREREG)) {
//                            if (!UriMatcher.matched(requestPath, SecurityConstants.PREREG_URLS)) {
//                                writeResponse(response, HttpStatus.UNAUTHORIZED, "Trying to access restricted API with pre-registration token");
//                                return;
//                            }
//                        } else if (applicationType.equalsIgnoreCase(AppConstants.APP_TYPE_SPECIAL_ENROLLMENT)) {
//                            if (!UriMatcher.matched(requestPath, SecurityConstants.SPECIAL_URLS)) {
//                                writeResponse(response, HttpStatus.UNAUTHORIZED, "Trying to access restricted API with special-enrollment token");
//                                return;
//                            }
//                        } else if (applicationType.equalsIgnoreCase(AppConstants.APP_TYPE_BAUST_ADMISSION)) {
//                            if (!UriMatcher.matched(requestPath, SecurityConstants.BAUST_URLS)) {
//                                writeResponse(response, HttpStatus.UNAUTHORIZED, "Trying to access restricted API with baust-admission token");
//                                return;
//                            }
//                        } else if (applicationType.equalsIgnoreCase(AppConstants.APP_TYPE_STUDENT_PORTAL)) {
//                            if (!UriMatcher.matched(requestPath, SecurityConstants.STUDENT_PORTAL_URLS)) {
//                                writeResponse(response, HttpStatus.UNAUTHORIZED, "Trying to access restricted API with student-portal token");
//                                return;
//                            }
//                        } else if (applicationType.equalsIgnoreCase(AppConstants.APP_TYPE_BUP_AUTHORITY)) {
//                            if (!UriMatcher.matched(requestPath, SecurityConstants.BUP_URLS)) {
//                                writeResponse(response, HttpStatus.UNAUTHORIZED, "Trying to access restricted API with BUP token");
//                                return;
//                            }
//                        } else {
//                            writeResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token, unknown application type: " + applicationType);
//                            return;
//                        }
//                    }
//
//                    String tokenType = jwtDetail.getTokenType();
//                    if (tokenType != null && tokenType.equals("accessToken")) {
//                        if (requestPath.contains("/refreshtoken")) {
//                            writeResponse(response, HttpStatus.UNAUTHORIZED, "Invalid use of access token");
//                            return;
//                        }
//                    } else if (tokenType != null && tokenType.equals("refreshToken")) {
//                        if (!requestPath.contains("/refreshtoken")) {
//                            writeResponse(response, HttpStatus.UNAUTHORIZED, "Invalid use of refresh token");
//                            return;
//                        }
//                    } else {
//                        writeResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token type, must be access token or refresh token");
//                        return;
//                    }
//
//                    UserPrincipal dummy = new UserPrincipal();
//                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(dummy, null, dummy.getAuthorities());
//                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                    SecurityContextHolder.getContext().setAuthentication(authToken);
//
//                    //update access tracker where required
//                    updateAccessTime(jwtDetail, requestPath);
//
//                } else {
//                    writeResponse(response, HttpStatus.UNAUTHORIZED, "Bearer token missing");
//                    return;
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                writeResponse(response, HttpStatus.UNAUTHORIZED, "Token processing failed");
//                return;
//            }
//        }
//        chain.doFilter(request, response);
//    }
//
//    private void updateAccessTime(JwtDetail jwtDetail, String requestPath){
//        try {
//            if (!requestPath.contains("/refreshtoken")) {   //use of refresh token is excluded from treating the request as user activity
//                if(jwtDetail.getApplicationType().equalsIgnoreCase(AppConstants.APP_TYPE_EMS)){
//                    AccessTracker.accessed(jwtDetail.getSessionId());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void writeResponse(HttpServletResponse response, HttpStatus status, String message) {
//        ResponseDto responseDto = new ResponseDto();
//        responseDto.setError(message);
//        responseDto.setStatus(status.value());
//        response.setStatus(status.value());
//        try {
//            new ObjectMapper().writeValue(response.getOutputStream(), responseDto);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//}
