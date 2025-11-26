package com.supercontrol.backend.config;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIoConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public SocketIOServer socketIOServer() {

        com.corundumstudio.socketio.Configuration socketConfig = new com.corundumstudio.socketio.Configuration();

        socketConfig.setHostname("0.0.0.0");
        socketConfig.setPort(8081);

        return new SocketIOServer(socketConfig);
    }
}
