package org.cloudfoundry.samples.music.web;

import java.util.List;

import javax.security.sasl.AuthenticationException;

import org.cloudfoundry.samples.music.managers.AccessibleSecurityManager;
import org.cloudfoundry.samples.music.managers.UpdateInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.constants.Constants;
import accessiblesolutions.accessiblescheduling.domain.UpdateInfo;

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