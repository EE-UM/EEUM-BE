package com.eeum.domain.block.dto.response;

import com.eeum.domain.block.entity.Block;
import lombok.AccessLevel;
import lombok.Builder;

public record BlockUserResponse(
    Long blockerUserId,
    Long blockedUserId
) {

    public static BlockUserResponse of(Block block) {
        return BlockUserResponse
            .builder()
            .blockerUserId(block.getBlockerUserId())
            .blockedUserId(block.getBlockedUserId())
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public BlockUserResponse(Long blockerUserId, Long blockedUserId) {
        this.blockerUserId = blockerUserId;
        this.blockedUserId = blockedUserId;
    }
}
