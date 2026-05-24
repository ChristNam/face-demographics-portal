package com.facedetect;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final FaceStreamHandler faceStreamHandler;

    // Inject the handler that contains our AI engine
    public WebSocketConfig(FaceStreamHandler faceStreamHandler) {
        this.faceStreamHandler = faceStreamHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register the injected handler to the "/stream" endpoint
        registry.addHandler(faceStreamHandler, "/stream")
                .setAllowedOrigins("*");
    }

    // ✅ Expand the memory buffer to allow large Base64 image frames
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // Increase the text and binary message limits to ~2 Megabytes
        container.setMaxTextMessageBufferSize(2000000);
        container.setMaxBinaryMessageBufferSize(2000000);
        return container;
    }
}