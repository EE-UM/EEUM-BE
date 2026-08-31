package com.eeum.domain.block.dto.request;

import com.eeum.domain.block.entity.Block;
import lombok.AccessLevel;
import lombok.Builder;

public record UnblockUserRequest(
    Long unblockerUserID,
    Long unblockedUserId
) {

    public static UnblockUserRequest of(Block block) {
        return UnblockUserRequest
            .builder()
            .unblockerUserID(block.getBlockerUserId())
            .unblockedUserId(block.getBlockedUserId())
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public UnblockUserRequest(Long unblockerUserID, Long unblockedUserId) {
        this.unblockerUserID = unblockerUserID;
        this.unblockedUserId = unblockedUserId;
    }
}
