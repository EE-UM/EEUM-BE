package com.eeum.domain.block.repository;

import com.eeum.domain.block.entity.Block;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockRepository extends JpaRepository<Block, Long> {

    @Query(value = """
        select b.blocked_user_id from block b where b.blocker_user_id = :blockerUserId
        """,
        nativeQuery = true)
    List<Long> findBlockedUserIdsByBlockerUserId(@Param("blockerUserId") Long blockerUserId);
}
