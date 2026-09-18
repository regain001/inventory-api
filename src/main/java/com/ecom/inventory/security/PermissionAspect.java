//package com.ecom.inventory.security;
//
//import com.commlink.ems.entity.enums.authorization.PermissionEnum;
//import com.commlink.ems.service.JwtDecoder;
//import com.commlink.ems.service.authorization.AuthorizationService;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.Arrays;
//
//@Aspect
//@Component
//@Order(1)
//public class PermissionAspect {
//
//    @Autowired
//    private AuthorizationService authorizationService;
//
//    @Autowired
//    private JwtDecoder jwtDecoder;
//
//
//    @Before("@annotation(requireAnyPermission)")
//    public void checkAnyPermission(JoinPoint joinPoint, RequireAnyPermission requireAnyPermission) {
//        PermissionEnum[] requiredPermissions = requireAnyPermission.value();
//
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//
//        if (attributes == null) {
//            throw new AccessDeniedException("User is not authenticated");
//        }
//
//        HttpServletRequest request = attributes.getRequest();
//
//        Long userId = jwtDecoder.getUserId(request);
//
//        if (userId == null) {
//            throw new AccessDeniedException("User is not authenticated");
//        }
//
//        String[] permissionStrings = Arrays.stream(requiredPermissions)
//                .map(PermissionEnum::getPermissionString)
//                .toArray(String[]::new);
//
//        if (!authorizationService.hasAnyPermission(userId, permissionStrings)) {
//            throw new AccessDeniedException(
//                    "Access denied. Insufficient permission"
//            );
//        }
//    }
//}