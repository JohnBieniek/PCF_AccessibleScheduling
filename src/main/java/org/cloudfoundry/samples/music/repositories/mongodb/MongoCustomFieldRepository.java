package org.cloudfoundry.samples.music.repositories.mongodb;

import org.cloudfoundry.samples.music.domain.CustomField;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongodb")
public interface MongoCustomFieldRepository extends MongoRepository<CustomField, String> {
}