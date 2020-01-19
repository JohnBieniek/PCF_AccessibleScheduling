package accessiblescheduling.repositories.mongodb;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblescheduling.domain.Shift;

@Repository
@Profile("mongodb")
public interface MongoShiftRepository extends MongoRepository<Shift, String> {
	List<Shift> findByStartMonthAndStartYear(int month,int startYear);
	List<Shift> findByClientId(String clientId);
	List<Shift> findByStaffId(String staffId);
	List<Shift> findByRequestedStaffId(String requestedStaffId);
	List<Shift> findByStartMonthAndStartYearAndClientId(int month,int startYear, String clientId);
	List<Shift> findByStartMonthAndStartYearAndStaffId(int month,int startYear, String employeeId);
	long deleteByStartMonthAndStartYear(int month,int startYear);
}