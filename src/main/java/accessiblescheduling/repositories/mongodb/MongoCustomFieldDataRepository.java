package accessiblescheduling.repositories.mongodb;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblescheduling.domain.CustomFieldData;
import accessiblescheduling.domain.Shift;

@Repository
@Profile("mongodb")
public interface MongoCustomFieldDataRepository extends MongoRepository<CustomFieldData, String> {
	CustomFieldData findByCustomFieldIdAndOwnerId(String customFieldId, String ownerId);
	List<CustomFieldData> findByCustomFieldId(String customFieldId);
	List<CustomFieldData> findByOwnerId(String ownerId);
}