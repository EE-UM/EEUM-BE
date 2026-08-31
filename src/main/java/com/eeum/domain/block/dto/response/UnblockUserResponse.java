package com.eeum.domain.block.dto.response;

import com.eeum.domain.block.entity.Block;
import lombok.AccessLevel;
import lombok.Builder;

public record UnblockUserResponse(
    Long blockerUserId,
    Long blockedUserId
) {

    public static UnblockUserResponse of(Block block) {
        return UnblockUserResponse
            .builder()
            .blockerUserId(block.getBlockerUserId())
            .blockedUserId(block.getBlockedUserId())
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public UnblockUserResponse(Long blockerUserId, Long blockedUserId) {
        this.blockerUserId = blockerUserId;
        this.blockedUserId = blockedUserId;
    }
}
