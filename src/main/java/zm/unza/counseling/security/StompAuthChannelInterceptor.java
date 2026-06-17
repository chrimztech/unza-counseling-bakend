package zm.unza.counseling.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import zm.unza.counseling.service.JwtService;

/**
 * Authenticates STOMP CONNECT frames using the JWT carried in the
 * "Authorization" STOMP header. WebSocket handshake requests can't reliably
 * carry an Authorization HTTP header (browsers/native WebSocket clients
 * don't support custom handshake headers), so authentication happens at the
 * STOMP frame level instead, which works the same way over plain WebSocket
 * or SockJS.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String jwt = authHeader.substring(7);
                    String username = jwtService.extractUsername(jwt);

                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (jwtService.isTokenValid(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(authToken);
                            log.debug("Authenticated STOMP CONNECT for user: {}", username);
                        } else {
                            log.warn("Invalid JWT on STOMP CONNECT for user: {}", username);
                        }
                    }
                } catch (Exception e) {
                    log.error("STOMP CONNECT authentication failed: {}", e.getMessage());
                }
            } else {
                log.warn("STOMP CONNECT received without Bearer Authorization header");
            }
        }

        return message;
    }
}
