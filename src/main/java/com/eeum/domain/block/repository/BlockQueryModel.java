package com.eeum.domain.block.repository;

import java.util.List;
import lombok.Getter;

@Getter
public class BlockQueryModel {

    private Long blockerId;
    private List<Long> blockedUsersId;

    public static BlockQueryModel create(Long blockerId, List<Long> blockedUsersId) {
        BlockQueryModel blockQueryModel = new BlockQueryModel();
        blockQueryModel.blockerId = blockerId;
        blockQueryModel.blockedUsersId = blockedUsersId;

        return blockQueryModel;
    }
}
