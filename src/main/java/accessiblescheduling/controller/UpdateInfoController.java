package accessiblescheduling.controller;

import java.time.LocalDateTime;
import java.util.List;

import javax.security.sasl.AuthenticationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accessiblescheduling.constants.Constants;
import accessiblescheduling.util.Util;
import accessiblescheduling.manager.AccessibleSecurityManager;
import accessiblescheduling.manager.UpdateInfoManager;
import accessiblescheduling.to.UpdateInfo;

@RestController
@RequestMapping(value = "/updateInfo")
public class UpdateInfoController {
    private static final Logger logger = LoggerFactory.getLogger(UpdateInfoController.class);
    
	@Autowired 
	AccessibleSecurityManager securityManager;
	
	@Autowired
    private CrudRepository<UpdateInfo, String> repository;
    
    @Autowired
    private UpdateInfoManager manager;


    public UpdateInfoController() {
    }

    @RequestMapping(value = "/updateNeeded",method = RequestMethod.GET)
    public String requestRequiresUpdate(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String lastUpdated) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.USER);
    	
    	String updated ="UNMODIFIED";
    	LocalDateTime time = null;
    	//2019-09-22T20:02:26.789Z
    	if(lastUpdated!=null && lastUpdated!="null") {
    		time=Util.getLocalDateTimeFromString(lastUpdated);
    		
    		Iterable<UpdateInfo> infos = repository.findAll();
    		
    		for(UpdateInfo info : infos) {
    			if(info.getTime().isAfter(time)) {
    				updated = "UPDATED";
    			}
    		}
    	}

    	return updated;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public Iterable<UpdateInfo> updateInfos(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.USER);
        return repository.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public UpdateInfo update(@RequestHeader(value="Authorization", required=false) String idToken, @PathVariable String id) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);

        return manager.set(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public UpdateInfo getById(@RequestHeader(value="Authorization", required=false) String idToken, @PathVariable String id) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.USER);
    	logger.info("Getting updateInfo " + id);
    	
    	UpdateInfo updateInfo = repository.findOne(id);
    	if(updateInfo!=null) {
    		logger.info(updateInfo.getTime().toString());
    	}
    	else {
    		manager.set(id);
    		updateInfo = repository.findOne(id);
    	}
        return updateInfo;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteById(@RequestHeader(value="Authorization", required=false) String idToken, @PathVariable String id) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
        logger.info("Deleting updateInfo " + id);
        repository.delete(id);
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public List<UpdateInfo> set(@RequestHeader(value="Authorization", required=false) String idToken, @RequestBody List<UpdateInfo> updateInfos) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	repository.save(updateInfos);
    	
    	return updateInfos;
    }
}