//package com.ecom.inventory.security;
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.commlink.ems.dto.credentials.StudentForgetPasswordOtpVerificationTokenDto;
//import com.commlink.ems.dto.credentials.StudentForgetPasswordTokenDto;
//import com.commlink.ems.entity.*;
//import com.commlink.ems.entity.courseoffering.Student;
//import com.commlink.ems.entity.hr.HrEmployeeProfile;
//import com.commlink.ems.repository.hr.HrEmployeeProfileRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//@Service
//public class JwtUtil {
//
//    @Autowired
//    private HrEmployeeProfileRepository hrEmployeeProfileRepository;
//
//    public static final String KEY_APP_TYPE = "applicationType";
//    public static final String KEY_SESSION_ID = "sessionId";
//    public static final String KEY_USER_NAME = "userName";
//
//    @Value("${token.expireTimeInMilli}")
//    private int tokenExpire;
//    @Value("${token.secret}")
//    private String tokenSecret;
//    @Value("${token.refreshExpirationDateInDays}")
//    private int refreshExpirationDateInDays;
//
//    @Value("${token.admin.access.expireTimeMilli}")
//    private int adminExpiryMilli;
//    @Value("${token.admin.refresh.expireTimeMilli}")
//    private int refreshExpiryMilli;
//
//    public String generateToken(UserPrincipal userDetails, Campus campus, String sessionId, Integer isFacultyCoEvaluationPending) {
//        return generateToken(userDetails, campus, userDetails.getUser().getIsPasswordResetMandatory(), isFacultyCoEvaluationPending, sessionId);
//    }
//
//    public String generateToken(UserPrincipal userDetails, Campus campus, Integer isPasswordMandatory, Integer isFacultyCoEvaluationPending, String sessionId) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "accessToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_EMS);
//        claims.put("userId", userDetails.getUser().getId());
//        claims.put("campusId", campus.getId());
//        claims.put("campusCode", campus.getCampusCode());
//        claims.put("isPasswordResetMandatory", isPasswordMandatory);
//        claims.put("isFacultyCoEvaluationPending", isFacultyCoEvaluationPending);
//        if(sessionId!=null){
//            claims.put(KEY_SESSION_ID, sessionId);
//        }
//        if(userDetails.getUser().getUsername()!=null){
//            claims.put(KEY_USER_NAME, userDetails.getUser().getUsername());
//        }
//
//        //Checking HR profile
//        HrEmployeeProfile profile = hrEmployeeProfileRepository.findByUserIdAndEmploymentStatus(userDetails.getUser().getId(), "Active");
//        if (profile != null) {
//            claims.put("isEmployee", true);
//            claims.put("employeeId", profile.getId());
//            claims.put("isProfileComplete", profile.getIsUserInputComplete());
//            claims.put("isProfileMandatory", profile.getIsProfileMandatory());
//        } else {
//            claims.put("isEmployee", false);
//        }
//
//        Date expiryTime = new Date(System.currentTimeMillis() + adminExpiryMilli);
//        return JWT.create()
//                .withSubject(userDetails.getUsername())
//                .withExpiresAt(expiryTime)
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)//If we try to put null values in claims, then will return(java.lang.IllegalArgumentException)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateTokenPreReg(StudentAdmissionStatus e) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "accessToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_PREREG);
//        claims.put("rollNo", e.getAdmissionRoll());
//        claims.put("userId", e.getId());
//        claims.put("circularId", e.getCircularId());
//        return JWT.create()
//                .withSubject(String.valueOf(e.getApplicationId()))
//                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpire))
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateRefreshTokenPreReg(StudentAdmissionStatus e) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "refreshToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_PREREG);
//        claims.put("rollNo", e.getAdmissionRoll());
//        claims.put("userId", e.getId());
//        claims.put("circularId", e.getCircularId());
//        return JWT.create()
//                .withSubject(String.valueOf(e.getApplicationId()))
//                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpire))
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateTokenForFiles(String webPath) {
//        return JWT.create()
//                .withSubject(webPath)
//                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpire))
//                .withIssuedAt(new Date())
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateRefreshToken(UserPrincipal userDetails, Campus campus, String sessionId, Integer isFacultyCoEvaluationPending) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "refreshToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_EMS);
//        claims.put("userId", userDetails.getUser().getId());
//        claims.put("campusId", campus.getId());
//        claims.put("campusCode", campus.getCampusCode());
//        claims.put("isPasswordResetMandatory", userDetails.getUser().getIsPasswordResetMandatory());
//        claims.put("isFacultyCoEvaluationPending", isFacultyCoEvaluationPending);
//        if(sessionId!=null){
//            claims.put(KEY_SESSION_ID, sessionId);
//        }
//        if(userDetails.getUser().getUsername()!=null){
//            claims.put(KEY_USER_NAME, userDetails.getUser().getUsername());
//        }
//
//        //Checking HR profile
//        HrEmployeeProfile profile = hrEmployeeProfileRepository.findByUserIdAndEmploymentStatus(userDetails.getUser().getId(), "Active");
//
//        if (profile != null) {
//            claims.put("isEmployee", true);
//            claims.put("employeeId", profile.getId());
//            claims.put("isProfileComplete", profile.getIsUserInputComplete());
//            claims.put("isProfileMandatory", profile.getIsProfileMandatory());
//        } else {
//            claims.put("isEmployee", false);
//        }
//
//        Date expiryTime = new Date(System.currentTimeMillis() + refreshExpiryMilli);
//        return JWT.create()
//                .withSubject(userDetails.getUsername())
//                .withExpiresAt(expiryTime)
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String genRefreshSpecialCand(SpecialEnrollment entity) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "refreshToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_SPECIAL_ENROLLMENT);
//        // user id is the respective table's primary key, number
//        // otherwise there will be perse error
//        claims.put("userId", entity.getId());
//        return JWT.create()
//                .withSubject(entity.getUserId())
//                .withExpiresAt(new Date(System.currentTimeMillis() + (refreshExpirationDateInDays * 24 * 60 * 60 * 1000)))
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateTokenSpecialCand(SpecialEnrollment entity) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "accessToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_SPECIAL_ENROLLMENT);
//        claims.put("userId", entity.getId());
//        return JWT.create()
//                .withSubject(entity.getUserId())
//                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpire))
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateTokenForBaustCand(Application application) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "accessToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_BAUST_ADMISSION);
//        claims.put("userId", application.getId());//Application Id
//        claims.put("appId", application.getId());
//        claims.put("circularId", application.getCircularId());
//        return JWT.create()
//                .withSubject(application.getUserId())
//                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpire))
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateRefreshTokenForBaustCand(Application application) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "refreshToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_BAUST_ADMISSION);
//        claims.put("userId", application.getId());//Application Id
//        claims.put("appId", application.getId());
//        claims.put("circularId", application.getCircularId());
//        return JWT.create()
//                .withSubject(application.getUserId())
//                .withExpiresAt(new Date(System.currentTimeMillis() + (refreshExpirationDateInDays * 24 * 60 * 60 * 1000)))
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateTokenForBaustStudent(Student student, Integer isFacultyEvaluationPending, Integer isCoEvaluationPending) {
//        return generateTokenForBaustStudent(student, student.getIsPasswordResetMandatory(), isFacultyEvaluationPending, isCoEvaluationPending);
//    }
//
//    public String generateTokenForBaustStudent(Student student, Integer isPasswordResetMandatory, Integer isFacultyEvaluationPending, Integer isCoEvaluationPending) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "accessToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_STUDENT_PORTAL);
//        addToClaims(claims, "userId", student.getId());//default
//        addToClaims(claims, "studentId", student.getId());
//        addToClaims(claims, "username", student.getUsername());
//        addToClaims(claims, "batchId", student.getBatchId());
//        addToClaims(claims, "syllabusId", student.getSyllabusId());
//        addToClaims(claims, "deptId", student.getDepartmentId());
//        addToClaims(claims, "isPasswordResetMandatory", isPasswordResetMandatory);
//        addToClaims(claims, "isFacultyEvaluationPending", isFacultyEvaluationPending);
//        addToClaims(claims, "isCoEvaluationPending", isCoEvaluationPending);
//        return JWT.create()
//                .withSubject(student.getUsername()) //student's username
//                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpire))
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateRefreshTokenForBaustStudent(Student student, Integer isFacultyEvaluationPending, Integer isCoEvaluationPending) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "refreshToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_STUDENT_PORTAL);
//        addToClaims(claims, "userId", student.getId());//default
//        addToClaims(claims, "studentId", student.getId());
//        addToClaims(claims, "username", student.getUsername());
//        addToClaims(claims, "batchId", student.getBatchId());
//        addToClaims(claims, "syllabusId", student.getSyllabusId());
//        addToClaims(claims, "deptId", student.getDepartmentId());
//        addToClaims(claims, "isPasswordResetMandatory", student.getIsPasswordResetMandatory());
//        addToClaims(claims, "isFacultyEvaluationPending", isFacultyEvaluationPending);
//        addToClaims(claims, "isCoEvaluationPending", isCoEvaluationPending);
//        return JWT.create()
//                .withSubject(student.getUsername()) //student's username
//                .withExpiresAt(new Date(System.currentTimeMillis() + (refreshExpirationDateInDays * 24 * 60 * 60 * 1000)))
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateTokenForBupUser(User user) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "accessToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_BUP_AUTHORITY);
//        addToClaims(claims, "userId", user.getId());//default
//        addToClaims(claims, "username", user.getUsername());
//        addToClaims(claims, "campusId", 1L);    //for BUP, campusId = 1 hardcoded, it can be refactored to take input in login page in future
//        addToClaims(claims, "campusCode", "MIST");    //for BUP, campusCode = MIST hardcoded, it can be refactored to take input in login page in future
//        return JWT.create()
//                .withSubject(user.getUsername())
//                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpire))
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateRefreshTokenForBupUser(User user) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "refreshToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_BUP_AUTHORITY);
//        addToClaims(claims, "userId", user.getId());//default
//        addToClaims(claims, "username", user.getUsername());
//        addToClaims(claims, "campusId", 1L);    //for BUP, campusId = 1 hardcoded, it can be refactored to take input in login page in future
//        addToClaims(claims, "campusCode", "MIST");    //for BUP, campusCode = MIST hardcoded, it can be refactored to take input in login page in future
//        return JWT.create()
//                .withSubject(user.getUsername())
//                .withExpiresAt(new Date(System.currentTimeMillis() + (refreshExpirationDateInDays * 24 * 60 * 60 * 1000)))
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateForgetPasswordToken(StudentForgetPasswordTokenDto obj) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "forgetPasswordToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_STUDENT_PORTAL);
//        addToClaims(claims, "classRoll", obj.getClassRoll());//default
//
//
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        String dateInString = obj.getOtpExpirationTime();
//        Date dateSessionExp = new Date();
//        try {
//            dateSessionExp = formatter.parse(dateInString);
//        } catch (ParseException ex) {
//            Logger.getLogger(JwtUtil.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//
//        addToClaims(claims, "otpExpiryDate", obj.getOtpExpirationTime());
//        addToClaims(claims, "isOtpVerified", obj.getIsOtpVerified());
//        addToClaims(claims, "sTime", obj.getsTime());
//        return JWT.create()
//                .withSubject(obj.getClassRoll()) //student's username
//                .withExpiresAt(dateSessionExp)
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//    public String generateForgetPasswordOtpVerificationToken(StudentForgetPasswordOtpVerificationTokenDto obj) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("tokenType", "forgetPasswordOtpVerificationToken");
//        claims.put(KEY_APP_TYPE, AppConstants.APP_TYPE_STUDENT_PORTAL);
//        addToClaims(claims, "classRoll", obj.getClassRoll());//default
//
//        addToClaims(claims, "note", obj.getNote());
//        addToClaims(claims, "isOtpVerified", obj.getIsOtpVerified());
//        return JWT.create()
//                .withSubject(obj.getClassRoll()) //student's username
//                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpire))
//                .withIssuedAt(new Date())
//                .withClaim("claims", claims)
//                .sign(Algorithm.HMAC256(tokenSecret));
//    }
//
//    public String generateSessionId(){
//        UUID uuid = UUID.randomUUID();
//        return uuid.toString();
//    }
//
//    private Boolean addToClaims(Map<String, Object> claims, String key, Object value) {
//        if (key == null || key.isEmpty()) {
//            return false;
//        }
//        if (value == null) {
//            return false;
//        }
//        claims.put(key, value);
//        return true;
//    }
//}
