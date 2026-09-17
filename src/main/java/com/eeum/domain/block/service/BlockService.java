package com.eeum.domain.block.service;

import com.eeum.domain.block.dto.request.BlockUserRequest;
import com.eeum.domain.block.dto.request.UnblockUserRequest;
import com.eeum.domain.block.dto.response.BlockUserResponse;
import com.eeum.domain.block.dto.response.UnblockUserResponse;
import com.eeum.domain.block.entity.Block;
import com.eeum.domain.block.repository.BlockQueryModelRepository;
import com.eeum.domain.block.repository.BlockRepository;
import com.eeum.domain.user.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final BlockQueryModelRepository blockQueryModelRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BlockUserResponse block(Long userId, BlockUserRequest request) {
        boolean existsByUserId = userRepository.existsById(request.blockedUserId());

        validateExistsUser(existsByUserId);

        Block block = Block.of(userId, request.blockedUserId());
        blockRepository.save(block);

        blockQueryModelRepository.add(userId, request.blockedUserId());

        return BlockUserResponse.of(block);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UnblockUserResponse unblock(Long userId, UnblockUserRequest request) {
        boolean existsByUserId = userRepository.existsById(request.unblockedUserId());

        validateExistsUser(existsByUserId);
        Block block = Block.of(userId, request.unblockedUserId());
        blockRepository.delete(block);

        blockQueryModelRepository.remove(userId, request.unblockedUserId());
        return UnblockUserResponse.of(block);
    }

    public Set<Long> getBlockedUserIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }

        return blockQueryModelRepository.read(userId)
            .orElseGet(() -> {
                List<Long> blockedUserIds = blockRepository.findBlockedUserIdsByBlockerUserId(
                    userId);
                blockQueryModelRepository.create(userId, blockedUserIds);
                return new HashSet<>(blockedUserIds);
            });
    }

    private static void validateExistsUser(boolean existsByUserId) {
        if (!existsByUserId) {
            throw new IllegalArgumentException("The user id doesn't exist");
        }
    }
}
