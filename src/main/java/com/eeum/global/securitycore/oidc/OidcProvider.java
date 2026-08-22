package com.eeum.global.securitycore.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public interface OidcProvider {

    String getProviderId(String idToken);

    default Map<String, String> parseHeaders(String token) {
        String header = token.split("\\.")[0];

        try {
            String decodedHeader = new String(Base64.getUrlDecoder().decode(header),
                StandardCharsets.UTF_8);
            return new ObjectMapper().readValue(decodedHeader, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
