package com.eeum.global.securitycore.oidc;

import com.auth0.jwk.GuavaCachedJwkProvider;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class AppleOidcProvider implements OidcProvider {

    private static final String ISSUER = "https://appleid.apple.com";
    private static final String JWKS_URL = "https://appleid.apple.com/auth/keys";

    private final String clientId;
    private final JwkProvider jwkProvider;

    public AppleOidcProvider(@Value("${oauth.apple.client-id}") String clientId) {
        this.clientId = clientId;
        try {
            JwkProvider base = new UrlJwkProvider(new URL(JWKS_URL));
            this.jwkProvider = new GuavaCachedJwkProvider(base, 10, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create JwkProvider", e);
        }
    }

    @Override
    public String getProviderId(String idToken) {
        return verifyAndDecode(idToken).getSubject();
    }

    private DecodedJWT verifyAndDecode(String idToken) {
        DecodedJWT decoded = JWT.decode(idToken);

        if (!ISSUER.equals(decoded.getIssuer())) {
            throw new IllegalArgumentException("Invalid iss for Apple ID token");
        }
        List<String> aud = decoded.getAudience();
        if (aud == null || aud.isEmpty() || !aud.contains(clientId)) {
            throw new IllegalArgumentException("Invalid aud for Apple ID token");
        }

        try {
            Jwk jwk = jwkProvider.get(decoded.getKeyId());
            RSAPublicKey key = (RSAPublicKey) jwk.getPublicKey();

            Algorithm alg = Algorithm.RSA256(key, null);
            JWTVerifier verifier = JWT.require(alg)
                    .withIssuer(ISSUER)
                    .withAudience(clientId)
                    .acceptLeeway(60) // 시계 오차 허용
                    .build();

            return verifier.verify(idToken); // 서명/만료 검증 포함
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to verify Apple ID token", e);
        }
    }

}
