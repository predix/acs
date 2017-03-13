package com.ge.predix.acs.attribute.cache;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.predix.acs.model.Attribute;

public class RedisAttributeCache extends AbstractAttributeCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisAttributeCache.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RedisTemplate<String, String> cache;

    public RedisAttributeCache(final RedisTemplate<String, String> cache) {
        this.cache = cache;
    }

    @Override
    public void set(final String key, final Set<Attribute> value) {
        if (!this.valueValid(key, value)) {
            return;
        }

        try {
            this.cache.opsForValue().set(key, OBJECT_MAPPER.writeValueAsString(value), this.getConfiguredTtlInSeconds(),
                    TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            LOGGER.error("Couldn't serialize JSON to attribute cache key: " + key, e);
        }
    }

    @Override
    public Set<Attribute> get(final String key) {
        String value = this.cache.opsForValue().get(key);
        if (StringUtils.isEmpty(value)) {
            return Collections.emptySet();
        }

        try {
            return OBJECT_MAPPER.readValue(value, new TypeReference<Set<Attribute>>() {
            });
        } catch (IOException e) {
            LOGGER.error("Couldn't deserialize JSON from attribute cache key: " + key, e);
            return Collections.emptySet();
        }
    }

    @Override
    public long getRemainingTtlInSeconds(final String key) {
        return this.cache.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public void flushAll() {
        this.cache.getConnectionFactory().getConnection().flushAll();
    }
}
