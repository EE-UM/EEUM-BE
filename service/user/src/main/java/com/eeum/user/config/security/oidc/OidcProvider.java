package com.eeum.user.config.security.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

import static org.apache.tomcat.util.codec.binary.Base64.decodeBase64;

public interface OidcProvider {

    String getProviderId(String idToken);

    default Map<String, String> parseHeaders(String token) {
        String header = token.split("\\.")[0];

        try {
            String decodedHeader = new String(decodeBase64(header), "UTF-8");
            return new ObjectMapper().readValue(decodedHeader, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
