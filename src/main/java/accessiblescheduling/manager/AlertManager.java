package accessiblescheduling.manager;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import accessiblescheduling.domain.AccessRequest;
import accessiblescheduling.domain.CustomField;
import accessiblescheduling.domain.CustomFieldData;
import accessiblescheduling.repositories.mongodb.MongoAccessRequestRepository;
import accessiblescheduling.repositories.mongodb.MongoCustomFieldDataRepository;
import accessiblescheduling.repositories.mongodb.MongoEmployeeRepository;

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