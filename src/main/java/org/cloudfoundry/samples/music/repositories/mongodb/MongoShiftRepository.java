package org.cloudfoundry.samples.music.repositories.mongodb;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblesolutions.accessiblescheduling.domain.Shift;

@Repository
@Profile("mongodb")
public interface MongoShiftRepository extends MongoRepository<Shift, String> {
	List<Shift> findByStartMonth(int month);
	List<Shift> removeByStartMonth(int month);
}