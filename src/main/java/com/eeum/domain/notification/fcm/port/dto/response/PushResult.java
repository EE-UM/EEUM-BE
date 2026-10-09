package com.eeum.domain.notification.fcm.port.dto.response;

import java.util.List;

public record PushResult(
    int successCount,
    List<String> invalidTokens
) {

    public boolean isAllFailed() {
        return successCount == 0;
    }
}
