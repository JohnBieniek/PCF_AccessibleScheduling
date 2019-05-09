package org.cloudfoundry.samples.music.repositories.mongodb;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblesolutions.accessiblescheduling.domain.ClientRequest;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;

@Repository
@Profile("mongodb")
public interface MongoClientRequestRepository extends MongoRepository<ClientRequest, String> {
	List<ClientRequest> findByClientId(String clientId);
}