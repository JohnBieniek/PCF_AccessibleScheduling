package org.cloudfoundry.samples.music.repositories.mongodb;

import java.util.List;

import org.cloudfoundry.samples.music.domain.CustomFieldData;
import org.cloudfoundry.samples.music.domain.Shift;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongodb")
public interface MongoCustomFieldDataRepository extends MongoRepository<CustomFieldData, String> {
	CustomFieldData findByCustomFieldIdAndOwnerId(String customFieldId, String ownerId);
	List<CustomFieldData> findByOwnerId(String ownerId);
}