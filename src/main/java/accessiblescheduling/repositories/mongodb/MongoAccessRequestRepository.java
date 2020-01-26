package accessiblescheduling.repositories.mongodb;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblescheduling.domain.AccessRequest;

@Repository
@Profile("mongodb")
public interface MongoAccessRequestRepository extends MongoRepository<AccessRequest, String> {
	AccessRequest findById(String id);
}