//package com.ecom.inventory.config;
//
//import com.commlink.ems.service.JwtDecoder;
//import com.commlink.ems.service.MyUserDetailsService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.MessagingException;
//import org.springframework.messaging.simp.config.ChannelRegistration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.messaging.support.MessageHeaderAccessor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.util.MimeType;
//import org.springframework.util.MimeTypeUtils;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
//@Order(Ordered.HIGHEST_PRECEDENCE + 99)
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//    private final static String WEB_SOCKET_URL = "/web-socket";
//    public static final String APPLICATION_DESTINATION_PREFIX = "/app";
//    public static final String TOPIC = "/topic";
//    public static final String QUEUE = "/queue";
//
//
//    @Autowired
//    JwtDecoder jwtDecoder;
//
//    @Autowired
//    MyUserDetailsService userDetailsService;
//
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint(WEB_SOCKET_URL).setAllowedOrigins("*");
//        registry.addEndpoint(WEB_SOCKET_URL).setAllowedOrigins("*").withSockJS();
//
//    }
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        config.enableSimpleBroker(TOPIC, QUEUE);
//        config.setApplicationDestinationPrefixes(APPLICATION_DESTINATION_PREFIX);
//        System.out.println();
//    }
//
//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(new ChannelInterceptor() {
//            @Override
//            public Message<?> preSend(Message<?> message, MessageChannel channel) {
//                StompHeaderAccessor accessor =
//                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//
//                    String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
//                    assert authorizationHeader != null;
//                    try {
//                        String username = jwtDecoder.getJwtDetail(authorizationHeader).getUsername();
//
//                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
//                                = new UsernamePasswordAuthenticationToken(userDetails, null,
//                                userDetails.getAuthorities());
//                        //  SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//
//                        accessor.setUser(usernamePasswordAuthenticationToken);
//                    } catch (Exception e) {
//                        String invalid_access = "Invalid Access";
//                        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
//                        headerAccessor.setMessage("Invalid access");
//                        headerAccessor.setContentLength(invalid_access.length());
//                        headerAccessor.setContentType(MimeType.valueOf(MimeTypeUtils.TEXT_PLAIN_VALUE));
//
//                        // simpMessagingTemplate.convertAndSend(MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders()));
//                        throw new MessagingException("Invalid Access");
//                    }
//                }
//                return message;
//            }
//        });
//    }
//}
