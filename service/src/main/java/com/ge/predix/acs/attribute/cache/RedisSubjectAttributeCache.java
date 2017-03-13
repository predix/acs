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
public final class RedisSubjectAttributeCache extends RedisAttributeCache implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSubjectAttributeCache.class);

    private final RedisTemplate<String, String> subjectCache;

    @Autowired
    public RedisSubjectAttributeCache(final RedisTemplate<String, String> subjectCache) {
        super(subjectCache);
        this.subjectCache = subjectCache;
    }

    @Override
    public void afterPropertiesSet() {
        LOGGER.info("Starting Redis subject attribute cache");

        try {
            String ping = this.subjectCache.getConnectionFactory().getConnection().ping();
            LOGGER.info("Redis server ping: {}", ping);
        } catch (RedisConnectionFailureException e) {
            LOGGER.error("Redis server ping failed", e);
        }
    }
}
