package unitbv.devops.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configurație pentru WebSocket cu STOMP protocol
 * Permite streaming în timp real de prețuri și alerte către clienți
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    /**
     * Configurează message broker-ul pentru rutarea mesajelor
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple in-memory broker pentru topic-uri (broadcasting)
        registry.enableSimpleBroker("/topic");

        // Prefix pentru mesajele trimise de la client către server
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Înregistrează endpoint-ul WebSocket unde clienții se pot conecta
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Permite toate originile (pentru dezvoltare)
                .withSockJS();  // Fallback pentru browsere care nu suportă WebSocket nativ
    }
}

