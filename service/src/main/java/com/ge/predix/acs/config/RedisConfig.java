/*******************************************************************************
 * Copyright 2016 General Electric Company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.ge.predix.acs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.ge.predix.acs.attribute.cache.AbstractAttributeCache;
import com.ge.predix.acs.attribute.cache.RedisResourceAttributeCache;
import com.ge.predix.acs.attribute.cache.RedisSubjectAttributeCache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

/**
 * DataSourceConfig used for all cloud profiles.
 *
 * @author 212406427
 */
interface JedisProvider {
    default String getJedisUrl(int port, int portOffset) {
        return "redis://localhost:" + (port + portOffset);
    }

    default Jedis getJedisInstance() {
        Jedis jedis = new Jedis(this.getJedisUrl(this.getJedisPort(), this.getJedisPortOffset()));
        jedis.connect();
        return jedis;
    }

    int getJedisPort();

    int getJedisPortOffset();
}

class PolicyEvaluationJedisConnectionFactory extends JedisConnectionFactory implements JedisProvider {

    private final int port;

    private static final int CACHE_PORT_OFFSET = 0;

    PolicyEvaluationJedisConnectionFactory(final int port, final JedisPoolConfig jedisPoolConfig) {
        super(jedisPoolConfig);
        this.port = port;
    }

    @Override
    protected Jedis fetchJedisConnector() {
        return this.getJedisInstance();
    }

    @Override
    public int getJedisPort() {
        return this.port;
    }

    @Override
    public int getJedisPortOffset() {
        return CACHE_PORT_OFFSET;
    }
}

class ResourceAttributeJedisConnectionFactory extends JedisConnectionFactory implements JedisProvider {

    private final int port;

    private static final int CACHE_PORT_OFFSET = 1;

    ResourceAttributeJedisConnectionFactory(final int port, final JedisPoolConfig jedisPoolConfig) {
        super(jedisPoolConfig);
        this.port = port;
    }

    @Override
    protected Jedis fetchJedisConnector() {
        return this.getJedisInstance();
    }

    @Override
    public int getJedisPort() {
        return this.port;
    }

    @Override
    public int getJedisPortOffset() {
        return CACHE_PORT_OFFSET;
    }
}

class SubjectAttributeJedisConnectionFactory extends JedisConnectionFactory implements JedisProvider {

    private final int port;

    private static final int CACHE_PORT_OFFSET = 2;

    SubjectAttributeJedisConnectionFactory(final int port, final JedisPoolConfig jedisPoolConfig) {
        super(jedisPoolConfig);
        this.port = port;
    }

    @Override
    protected Jedis fetchJedisConnector() {
        return this.getJedisInstance();
    }

    @Override
    public int getJedisPort() {
        return this.port;
    }

    @Override
    public int getJedisPortOffset() {
        return CACHE_PORT_OFFSET;
    }
}

@Configuration
@EnableAutoConfiguration
@Profile({ "redis" })
public class RedisConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${REDIS_HOST:localhost}")
    private String hostName;

    @Value("${REDIS_PORT:6379}")
    private int port;

    @Value("${REDIS_MIN_ACTIVE:0}")
    private int minIdle;

    @Value("${REDIS_MAX_ACTIVE:100}")
    private int maxTotal;

    @Value("${REDIS_MAX_WAIT_TIME:2000}")
    private int maxWaitMillis;

    @Value("${REDIS_SOCKET_TIMEOUT:3000}")
    private int timeout;

    private JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(this.maxTotal);
        jedisPoolConfig.setMinIdle(this.minIdle);
        jedisPoolConfig.setMaxWaitMillis(this.maxWaitMillis);
        jedisPoolConfig.setTestOnBorrow(false);
        return jedisPoolConfig;
    }

    private void setUpJedisConnectionFactory(final JedisConnectionFactory jedisConnectionFactory) {
        jedisConnectionFactory.setUsePool(false);
        jedisConnectionFactory.setTimeout(this.timeout);
        jedisConnectionFactory.setHostName(this.hostName);
    }

    private JedisConnectionFactory policyEvaluationJedisConnectionFactory() {
        JedisPoolConfig jedisPoolConfig = this.jedisPoolConfig();

        JedisConnectionFactory jedisConnectionFactory =
                new PolicyEvaluationJedisConnectionFactory(this.port, jedisPoolConfig);
        this.setUpJedisConnectionFactory(jedisConnectionFactory);

        LOGGER.info("Successfully created policy evaluation Redis connection factory");

        return jedisConnectionFactory;
    }

    private JedisConnectionFactory resourceJedisConnectionFactory() {
        JedisPoolConfig jedisPoolConfig = this.jedisPoolConfig();

        JedisConnectionFactory jedisConnectionFactory =
                new ResourceAttributeJedisConnectionFactory(this.port, jedisPoolConfig);
        this.setUpJedisConnectionFactory(jedisConnectionFactory);

        LOGGER.info("Successfully created resource attribute Redis connection factory");

        return jedisConnectionFactory;
    }

    private JedisConnectionFactory subjectJedisConnectionFactory() {
        JedisPoolConfig jedisPoolConfig = this.jedisPoolConfig();

        JedisConnectionFactory jedisConnectionFactory =
                new SubjectAttributeJedisConnectionFactory(this.port, jedisPoolConfig);
        this.setUpJedisConnectionFactory(jedisConnectionFactory);

        LOGGER.info("Successfully created subject attribute Redis connection factory");

        return jedisConnectionFactory;
    }

    private RedisTemplate<String, String> stringRedisTemplate(final JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        LOGGER.info("Successfully created policy evaluation Redis template");
        return this.stringRedisTemplate(this.policyEvaluationJedisConnectionFactory());
    }

    @Bean
    public RedisTemplate<String, String> resourceCache() {
        LOGGER.info("Successfully created resource attribute Redis template");
        return this.stringRedisTemplate(this.resourceJedisConnectionFactory());
    }

    @Bean
    public RedisTemplate<String, String> subjectCache() {
        LOGGER.info("Successfully created subject attribute Redis template");
        return this.stringRedisTemplate(this.subjectJedisConnectionFactory());
    }

    @Bean
    public AbstractAttributeCache resourceAttributeCache() {
        return new RedisResourceAttributeCache(this.resourceCache());
    }

    @Bean
    public AbstractAttributeCache subjectAttributeCache() {
        return new RedisSubjectAttributeCache(this.subjectCache());
    }
}
