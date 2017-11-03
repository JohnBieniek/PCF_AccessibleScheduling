package org.cloudfoundry.samples.music.repositories.mongodb;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblesolutions.accessiblescheduling.domain.ShiftRequest;

@Repository
@Profile("mongodb")
public interface MongoShiftRequestRepository extends MongoRepository<ShiftRequest, String> {
}