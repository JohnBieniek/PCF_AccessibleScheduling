package org.cloudfoundry.samples.music.repositories.mongodb;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;
import accessiblesolutions.accessiblescheduling.domain.ScheduleStatus;

@Repository
@Profile("mongodb")
public interface ScheduleStatusRepository extends MongoRepository<ScheduleStatus, String> {
	void deleteByMonth(String month);
}