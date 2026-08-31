package com.eeum.domain.block.dto.request;

import lombok.AccessLevel;
import lombok.Builder;

public record BlockUserRequest(
    Long blockedUserId
) {

    public static BlockUserRequest of(Long blockedUserId) {
        return BlockUserRequest
            .builder()
            .blockedUserId(blockedUserId)
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public BlockUserRequest(Long blockedUserId) {
        this.blockedUserId = blockedUserId;
    }
}