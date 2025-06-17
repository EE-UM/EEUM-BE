package com.eeum.common.securitycore.oidc;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class OidcProviderFactory {

    private final Map<Provider, OidcProvider> authProviderMap;
    private final KakaoOidcProvider kakaoOidcProvider;

    public OidcProviderFactory(KakaoOidcProvider kakaoOidcProvider) {
        authProviderMap = new EnumMap<>(Provider.class);
        this.kakaoOidcProvider = kakaoOidcProvider;
        initialize();
    }

    private void initialize() {
        authProviderMap.put(Provider.KAKAO, kakaoOidcProvider);
    }

    public String getProviderId(Provider provider, String idToken) {
        System.out.println("getProvider(provider).getProviderId(idToken) = " + getProvider(provider).getProviderId(idToken));
        return getProvider(provider).getProviderId(idToken);
    }


    private OidcProvider getProvider(Provider provider) {
        OidcProvider oidcProvider = authProviderMap.get(provider);
        System.out.println("oidcProvider = " + oidcProvider);
        System.out.println("oidcProvider.getProviderId = " + oidcProvider.getProviderId("eyJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxYzNjNDNiZTViYmYxMWVkNjI1ZTMyMDZhODI2ZTUzZSIsInN1YiI6IjM2Mzc0OTc0ODkiLCJhdXRoX3RpbWUiOjE3MjE5MTI2NzcsImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwiZXhwIjoxNzIxOTU1ODc3LCJpYXQiOjE3MjE5MTI2NzcsImVtYWlsIjoic3NtamgwMzAxQG5hdmVyLmNvbSJ9.cIMebTedEde8BrUUasnRJivdFJmyL62kvUDepnK7NlHp5dl9ygYqZNnoerDT5YU6bta503Mzp9-7U_MBRdqNPr7NXwvJ76NEAJcp3UwCV7DJJRlKMZv1PySqUJmpZiCwmCYhJV-UvEU2G9NF0mK_1CQ6-c2YnbAzxtf8DJ9jib6M8jyHD2-_FUM21IX8_0Uvb4TbwCwfSh8GaBLZKNgSAseq5Pf_vnVWbt38loRvfYhGBNcXey-BFQF3aiG8v0E9SaH4D93UhueFotXplrGg2I2UP7VEkkL1vDhb0I2sO82zELKBxYWaadzyCtv8dobB_blxOy5lagy6L4w2iBBMsw"));
        if (oidcProvider == null) {
            throw new RuntimeException("Wrong provider");
        }
        return oidcProvider;
    }
}
