package org.cloudfoundry.samples.music.managers;
import java.util.ArrayList;

import org.cloudfoundry.samples.music.repositories.mongodb.MongoAccessRequestRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldDataRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoEmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import accessiblesolutions.accessiblescheduling.domain.AccessRequest;
import accessiblesolutions.accessiblescheduling.domain.CustomField;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;

@Component
public class AlertManager {
	@Autowired
	private CrudRepository<CustomField,String> customFieldCrud;
	
	@Autowired
	private MongoEmployeeRepository employeeCrud;
	
	
	@Autowired
	private MongoAccessRequestRepository accessCrud;
	
	@Autowired
    private MongoCustomFieldDataRepository customFieldDataRepository;

	@Autowired
    private CrudRepository<CustomFieldData, String> customFieldDataCrud;
    
	RestTemplate restTemplate = new RestTemplate();
    
    public AlertManager() {}
    
    public ArrayList<AccessRequest> getAlerts() {
    	return (ArrayList<AccessRequest>) accessCrud.findAll();
    }
}