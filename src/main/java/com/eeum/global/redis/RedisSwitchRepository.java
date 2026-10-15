package com.eeum.global.redis;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RedisSwitchRepository extends JpaRepository<RedisSwitch, RedisFeature> {

}
