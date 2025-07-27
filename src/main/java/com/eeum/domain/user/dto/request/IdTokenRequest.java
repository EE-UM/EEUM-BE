package com.eeum.domain.user.dto.request;

import lombok.Builder;

@Builder
public record IdTokenRequest(
        String idToken,
        String provider
) {
}
