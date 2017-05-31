package com.ge.predix.acs.privilege.management;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ge.predix.acs.policy.evaluation.cache.PolicyEvaluationCache;
import com.ge.predix.acs.privilege.management.dao.SubjectEntity;
import com.ge.predix.acs.zone.management.dao.ZoneEntity;
import com.ge.predix.acs.zone.resolver.ZoneResolver;

@Test
public class PrivilegeManagementConstraintViolationTest {
    @InjectMocks
    private PrivilegeManagementService service;
    @Mock
    private ZoneResolver zoneResolver;
    @Mock
    private PolicyEvaluationCache cache;

    @BeforeMethod
    public void setupMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(new ZoneEntity(0L, "testzone")).when(this.zoneResolver).getZoneEntityOrFail();
        Mockito.doNothing().when(this.cache).resetForSubjects(Mockito.anyString(),
                Mockito.anyListOf(SubjectEntity.class));
    }

    public void testCreateDuplicateResource() {

    }

    public void testCreateDuplicateResources() {

    }

    public void testCreateDuplicateSubject() {

    }

    public void testCreateDuplicateSubjects() {

    }
}
