package com.eeum.global.redis;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "redis_switch")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RedisSwitch {

    @Id
    @Enumerated(EnumType.STRING)
    private RedisFeature feature;

    private boolean enabled;

    private LocalDateTime updatedAt;
}
