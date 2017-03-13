package com.ge.predix.acs.attribute.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile({ "cloud-redis", "redis" })
public final class RedisResourceAttributeCache extends RedisAttributeCache implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisResourceAttributeCache.class);

    private final RedisTemplate<String, String> resourceCache;

    @Autowired
    public RedisResourceAttributeCache(final RedisTemplate<String, String> resourceCache) {
        super(resourceCache);
        this.resourceCache = resourceCache;
    }

    @Override
    public void afterPropertiesSet() {
        LOGGER.info("Starting Redis resource attribute cache");

        try {
            String ping = this.resourceCache.getConnectionFactory().getConnection().ping();
            LOGGER.info("Redis server ping: {}", ping);
        } catch (RedisConnectionFailureException e) {
            LOGGER.error("Redis server ping failed", e);
        }
    }
}
