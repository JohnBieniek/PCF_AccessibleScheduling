package org.cloudfoundry.samples.music.repositories.mongodb;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblesolutions.accessiblescheduling.domain.Event;

@Repository
@Profile("mongodb")
public interface MongoEventRepository extends MongoRepository<Event, String> {
}