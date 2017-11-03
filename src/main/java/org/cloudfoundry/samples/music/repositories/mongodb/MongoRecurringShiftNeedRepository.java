package org.cloudfoundry.samples.music.repositories.mongodb;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblesolutions.accessiblescheduling.domain.RecurringShiftNeed;

@Repository
@Profile("mongodb")
public interface MongoRecurringShiftNeedRepository extends MongoRepository<RecurringShiftNeed, String> {
}