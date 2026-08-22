package com.eeum.domain.user.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

@Table(name = "block",
    indexes = {
        @Index(name = "idx_blocker_user_id_blocked_user_id", columnList = "blockerUserId, blockedUserId", unique = true)
    }
)
@Getter
@Entity
@ToString
@SoftDelete(columnName = "deleted", strategy = SoftDeleteType.TIMESTAMP)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Block {

    @Id
    @Tsid
    private Long id;

    private Long blockerUserId;

    private Long blockedUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static Block of(Long blockerUserId, Long blockedUserId) {
        LocalDateTime now = LocalDateTime.now();
        return Block.builder()
            .blockerUserId(blockerUserId)
            .blockedUserId(blockedUserId)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public Block(Long blockerUserId, Long blockedUserId, LocalDateTime createdAt,
        LocalDateTime updatedAt) {
        this.blockerUserId = blockerUserId;
        this.blockedUserId = blockedUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
