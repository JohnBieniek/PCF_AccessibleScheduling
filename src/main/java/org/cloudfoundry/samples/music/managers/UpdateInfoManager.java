package org.cloudfoundry.samples.music.managers;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.UpdateInfo;

@Component
public class UpdateInfoManager {
	@Autowired
	private CrudRepository<UpdateInfo,String> crud;
	
    public UpdateInfoManager() {
    }
    
    public LocalDateTime get(String id) {
    	return crud.findOne(id).getTime();
	}
    
    public UpdateInfo set(String id) {
    	UpdateInfo updateInfo = crud.findOne(id);
    	updateInfo.setTime(LocalDateTime.now());
    	return crud.save(updateInfo);
	}
}