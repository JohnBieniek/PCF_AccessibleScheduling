package org.cloudfoundry.samples.music.web;

import java.util.List;

import javax.security.sasl.AuthenticationException;
import javax.validation.Valid;

import org.cloudfoundry.samples.music.managers.AccessibleSecurityManager;
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
import accessiblesolutions.accessiblescheduling.domain.CallAuth;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;

@RestController
@RequestMapping(value = "/customFieldData")
public class CustomFieldDataController {
    private static final Logger logger = LoggerFactory.getLogger(CustomFieldDataController.class);
    
	@Autowired 
	AccessibleSecurityManager securityManager;
	
    private CrudRepository<CustomFieldData, String> repository;

    @Autowired
    public CustomFieldDataController(CrudRepository<CustomFieldData, String> repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<CustomFieldData> customFieldDatas(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
        return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.PUT)
    public CustomFieldData add(@RequestHeader(value="Authorization", required=false) String idToken,@RequestBody @Valid CustomFieldData customFieldData) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	logger.info("Adding customFieldData " + customFieldData.getId());
    	customFieldData.update();
        return repository.save(customFieldData);
    }

    @RequestMapping(method = RequestMethod.POST)
    public CustomFieldData update(@RequestHeader(value="Authorization", required=false) String idToken, @RequestBody @Valid CustomFieldData customFieldData) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	logger.info("Updating customFieldData " + customFieldData.getId());
    	customFieldData.update();
        return repository.save(customFieldData);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CustomFieldData getById(@RequestHeader(value="Authorization", required=false) String idToken, @PathVariable String id) throws AuthenticationException {
    	CallAuth auth = securityManager.authorize(idToken, Constants.USER);
    	logger.info("Getting customFieldData " + id);
    	
    	CustomFieldData customFieldData = repository.findOne(id);
    	
    	if(!auth.getEmployeeId().equalsIgnoreCase(customFieldData.getOwnerId())) {
    		if(!auth.isAdmin() && !auth.isManager()) {
    			throw new AuthenticationException();
    		}
    	}
    	
        return customFieldData;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteById(@RequestHeader(value="Authorization", required=false) String idToken, @PathVariable String id) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
        logger.info("Deleting customFieldData " + id);
        repository.delete(id);
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public List<CustomFieldData> set(@RequestHeader(value="Authorization", required=false) String idToken, @RequestBody List<CustomFieldData> customFieldDatas) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	for(CustomFieldData data: customFieldDatas) {
        	data.update();
    	}
    	repository.save(customFieldDatas);
    	
    	return customFieldDatas;
    }
}