package com.ge.predix.acs.attribute.cache;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ge.predix.acs.config.InMemoryDataSourceConfig;
import com.ge.predix.acs.config.RedisConfig;
import com.ge.predix.acs.model.Attribute;

@ContextConfiguration(classes = { RedisConfig.class, InMemoryDataSourceConfig.class })
@ActiveProfiles({ "h2", "redis" })
public class RedisAttributeCacheIT extends AbstractTestNGSpringContextTests {

    @Autowired
    private RedisTemplate<String, String> resourceCache;

    @Autowired
    private RedisTemplate<String, String> subjectCache;

    @Autowired
    private AbstractAttributeCache resourceAttributeCache;

    @Autowired
    private AbstractAttributeCache subjectAttributeCache;

    private static final String ZONE_ID = "test-zone";
    private static final String RESOURCE_ID = "aircraftPart/13a71359-db68-4602-aac5-a8fa401c3194";
    private static final String SUBJECT_ID = "organizations/37178fb9-7171-4df4-bd6b-9b5c04593d07";

    @BeforeClass
    public void beforeClass() {
        this.resourceAttributeCache.flushAll();
        this.subjectAttributeCache.flushAll();
    }

    @Test
    public void testMultipleCaches() throws Exception {
        Set<Attribute> expectedResourceAttributes = Collections
                .singleton(new Attribute("https://resource/attr/issuer", "uri", '/' + RESOURCE_ID));
        Set<Attribute> expectedSubjectAttributes = Collections
                .singleton(new Attribute("https://subject/attr/issuer", "owner", '/' + SUBJECT_ID));

        String resourceKey = this.resourceAttributeCache.keyName(ZONE_ID, RESOURCE_ID);
        String subjectKey = this.subjectAttributeCache.keyName(ZONE_ID, SUBJECT_ID);

        this.resourceAttributeCache.set(resourceKey, expectedResourceAttributes);
        this.subjectAttributeCache.set(subjectKey, expectedSubjectAttributes);

        Set<Attribute> actualResourceAttributes = this.resourceAttributeCache.get(resourceKey);
        Set<Attribute> actualSubjectAttributes = this.subjectAttributeCache.get(subjectKey);

        long elapsedTimeInSeconds = 2L;
        Thread.sleep(elapsedTimeInSeconds * 1000L);

        long resourceTtl = this.resourceAttributeCache.getRemainingTtlInSeconds(resourceKey);
        Assert.assertTrue(resourceTtl > 0L && resourceTtl <= (this.resourceAttributeCache.getConfiguredTtlInSeconds()
                - elapsedTimeInSeconds));

        long subjectTtl = this.subjectAttributeCache.getRemainingTtlInSeconds(subjectKey);
        Assert.assertTrue(subjectTtl > 0L && subjectTtl <= (this.subjectAttributeCache.getConfiguredTtlInSeconds()
                - elapsedTimeInSeconds));

        Assert.assertEquals(expectedResourceAttributes, actualResourceAttributes);
        Assert.assertEquals(expectedSubjectAttributes, actualSubjectAttributes);

        Assert.assertEquals(this.resourceCache.getConnectionFactory().getConnection().dbSize().longValue(), 1L);
        Assert.assertEquals(this.subjectCache.getConnectionFactory().getConnection().dbSize().longValue(), 1L);
    }
}
