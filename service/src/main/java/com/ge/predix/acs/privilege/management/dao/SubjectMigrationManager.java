package com.ge.predix.acs.privilege.management.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("titan")
public class SubjectMigrationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectMigrationManager.class);

    public void doSubjectMigration(final SubjectRepository subjectRepository,
            final GraphSubjectRepository subjectHierarchicalRepository, final int pageSize) {
        int numOfSubjectsSaved = 0;
        Pageable pageRequest = new PageRequest(0, pageSize, new Sort("id"));
        long numOfSubjectEntitiesToMigrate = subjectRepository.count();
        Page<SubjectEntity> pageOfSubjects;

        do {
            pageOfSubjects = subjectRepository.findAll(pageRequest);
            List<SubjectEntity> subjectListToSave = pageOfSubjects.getContent();
            numOfSubjectsSaved += pageOfSubjects.getNumberOfElements();
            subjectListToSave.forEach(item -> {
                item.setId(0);
                LOGGER.trace("doSubjectMigration Subject-Id : " + item.getSubjectIdentifier() + " Zone-name : "
                    + item.getZone().getName() + " Zone-id:" + item.getZone().getId());
            });
            
            subjectHierarchicalRepository.save(subjectListToSave);
            LOGGER.info("Total subjects migrated so far: " + numOfSubjectsSaved + "/" + numOfSubjectEntitiesToMigrate);
            pageRequest = pageOfSubjects.nextPageable();
        } while (pageOfSubjects.hasNext());

        LOGGER.info("Number of subject entities migrated: " + numOfSubjectsSaved);
        LOGGER.info("Subject migration to Titan completed.");
    }

    public void rollbackMigratedData(final GraphSubjectRepository subjectHierarchicalRepository) {
        LOGGER.info("Initiating rollback for subjectHierarchicalRepository");
        subjectHierarchicalRepository.deleteAll();
    }

}
