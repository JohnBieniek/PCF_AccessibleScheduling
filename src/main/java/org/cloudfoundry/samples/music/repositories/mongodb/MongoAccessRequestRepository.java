package org.cloudfoundry.samples.music.repositories.mongodb;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblesolutions.accessiblescheduling.domain.AccessRequest;

@Repository
@Profile("mongodb")
public interface MongoAccessRequestRepository extends MongoRepository<AccessRequest, String> {
	AccessRequest findByUserId(String userId);
}