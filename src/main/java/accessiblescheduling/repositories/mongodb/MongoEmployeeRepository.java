package accessiblescheduling.repositories.mongodb;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblescheduling.domain.Employee;

@Repository
@Profile("mongodb")
public interface MongoEmployeeRepository extends MongoRepository<Employee, String> {
	Employee findByUserId(String userId);
}