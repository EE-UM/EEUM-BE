package com.eeum.common.applemusickit;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class AppleMusickit {

    @Value("${apple-music.key}")
    private String privateKeyString;

    @Value("${apple-music.key-id}")
    private String keyId;

    @Value("${apple-music.team-id}")
    private String teamId;

    public String generateToken() {
        try {
            ECPrivateKey privateKey = loadPrivateKeyFromString(privateKeyString);
            Instant now = Instant.now();

            return Jwts.builder()
                    .setHeaderParam("alg", "ES256")
                    .setHeaderParam("kid", keyId)
                    .setIssuer(teamId)
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(now.plusSeconds(60 * 60 * 24 * 180))) // 6 months
                    .signWith(privateKey, SignatureAlgorithm.ES256)
                    .compact();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Apple Music developer token", e);
        }
    }

    private ECPrivateKey loadPrivateKeyFromString(String keyString) {
        try {
            String privateKeyPEM = keyString
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

            return (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse EC private key from string.", e);
        }
    }
}
