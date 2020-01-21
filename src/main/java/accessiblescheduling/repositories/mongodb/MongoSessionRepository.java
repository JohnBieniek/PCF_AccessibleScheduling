package accessiblescheduling.repositories.mongodb;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblescheduling.domain.Session;

@Repository
@Profile("mongodb")
public interface MongoSessionRepository extends MongoRepository<Session, String> {
	Session findByToken(String token);
}