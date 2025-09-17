package com.eeum.global.config;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class ApnsClientConfig {

    @Value("${app.apns.p8-path}")
    private String p8Path;
    @Value("${app.apns.team-id}")
    private String teamId;
    @Value("${app.apns.key-id}")
    private String keyId;
    @Value("${app.apns.production}")
    private boolean production;

    @Bean
    public ApnsClient apnsClient() throws Exception {
        InputStream keyStream = new ByteArrayInputStream(p8Path.getBytes(StandardCharsets.UTF_8));
        ApnsSigningKey key = ApnsSigningKey.loadFromInputStream(keyStream, teamId, keyId);
        return new ApnsClientBuilder()
                .setApnsServer(production ? ApnsClientBuilder.PRODUCTION_APNS_HOST
                        : ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                .setSigningKey(key)
                .build();
    }
}
