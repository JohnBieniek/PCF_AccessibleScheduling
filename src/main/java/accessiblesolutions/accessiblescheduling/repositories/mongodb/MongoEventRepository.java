package accessiblescheduling.repositories.mongodb;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblescheduling.domain.Event;

@Repository
@Profile("mongodb")
public interface MongoEventRepository extends MongoRepository<Event, String> {
}