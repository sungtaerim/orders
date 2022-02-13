package com.lepse.orders.config;

import com.lepse.orders.service.CredentialManager;
import com.lepse.orders.service.FindOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource({"classpath:application.properties"})
public class Config {

    final Environment environment;

    @Autowired
    public Config(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public CredentialManager credentialManagerConfig() {
        final String user = environment.getProperty("tc.server.login");
        final String password = environment.getProperty("tc.server.password");
        final String tcServer = environment.getProperty("tc.server.address");

        return new CredentialManager(user, password, tcServer);
    }

    @Bean
    public FindOrder findOrder() {
        return new FindOrder(credentialManagerConfig());
    }
}
