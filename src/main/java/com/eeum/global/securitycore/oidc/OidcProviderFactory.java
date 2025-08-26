package com.eeum.global.securitycore.oidc;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class OidcProviderFactory {

    private final Map<Provider, OidcProvider> authProviderMap;
    private final KakaoOidcProvider kakaoOidcProvider;
    private final AppleOidcProvider appleOidcProvider;

    public OidcProviderFactory(KakaoOidcProvider kakaoOidcProvider, AppleOidcProvider appleOidcProvider) {
        authProviderMap = new EnumMap<>(Provider.class);
        this.kakaoOidcProvider = kakaoOidcProvider;
        this.appleOidcProvider = appleOidcProvider;
        initialize();
    }

    private void initialize() {
        authProviderMap.put(Provider.KAKAO, kakaoOidcProvider);
        authProviderMap.put(Provider.APPLE, appleOidcProvider);
    }

    public String getProviderId(Provider provider, String idToken) {
        return getProvider(provider).getProviderId(idToken);
    }


    private OidcProvider getProvider(Provider provider) {
        OidcProvider oidcProvider = authProviderMap.get(provider);
        if (oidcProvider == null) {
            throw new RuntimeException("Wrong provider");
        }
        return oidcProvider;
    }
}
