package com.eeum.global.config.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AppleMusicRestClientConfig {

  @Bean
  public RestClient appleMusicClient(
      @Value("${apple-music.base-url}") String baseUrl,
      @Value("${apple-music.connect-timeout-ms}") int connectionTimeoutMs,
      @Value("${apple-music.read-timeout-ms}") int readTimeoutMs
  ) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(connectionTimeoutMs);
    factory.setReadTimeout(readTimeoutMs);

    return RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(factory)
        .build();
  }
}
