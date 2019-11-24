package accessiblescheduling.repositories.mongodb;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblescheduling.domain.CustomField;

@Repository
@Profile("mongodb")
public interface MongoCustomFieldRepository extends MongoRepository<CustomField, String> {
}