package com.eeum.global.redis;

import java.util.Set;

public record RedisRecoveredEvent(Set<RedisFeature> features) {

}
