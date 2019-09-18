package org.cloudfoundry.samples.music.repositories.mongodb;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import accessiblesolutions.accessiblescheduling.domain.UpdateInfo;

@Repository
@Profile("mongodb")
public interface MongoUpdateInfoRepository extends MongoRepository<UpdateInfo, String> {
	List<UpdateInfo> findById(String id);
}