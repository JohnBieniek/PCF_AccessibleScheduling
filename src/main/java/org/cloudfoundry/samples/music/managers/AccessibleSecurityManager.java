package org.cloudfoundry.samples.music.managers;
import java.io.IOException;
import java.util.ArrayList;

import org.cloudfoundry.samples.music.repositories.mongodb.MongoAccessRequestRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldDataRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoEmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import accessiblesolutions.accessiblescheduling.domain.AccessRequest;
import accessiblesolutions.accessiblescheduling.domain.CustomField;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.User;

@Component
public class AccessibleSecurityManager {
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
    
    public AccessibleSecurityManager() {}
    
    public boolean signedUp(String idToken) {
    	User user = getUser(idToken);
    	
    	if(null!=user) {
        	AccessRequest request = accessCrud.findOne(user.getUserId());
        	if(null!=request) {
        		return true;
        	}
    	}
    	
    	return false;
    }
    
    public  AccessRequest signUp(String idToken, String name) {
    	User user = getUser(idToken);
    	AccessRequest request = null;
    	
    	if(null!=user) {
    		request = accessCrud.findOne(user.getUserId());
    		if(null==request) {
    			return accessCrud.insert(new AccessRequest(user.getUserId(),name));
    		}
    	};
    	
    	return null;
    }
    
    public boolean isAdmin(String idToken) {
    	return getUserDetails(idToken).isAdmin();
    }
    
    public boolean isManager(String idToken) {
    	return getUserDetails(idToken).isManager();
    }
    
    public boolean isUser(String idToken) {
    	return getUserDetails(idToken).isUser();
    }
    
    public User getUserDetails(String idToken) {
    	return getUserDetails(getUser(idToken));
    }
    
    public User getUser(String idToken){
    	String tokenInfo = restTemplate.getForObject("https://www.googleapis.com/oauth2/v2/tokeninfo?id_token="+idToken, String.class);
    	
    	ObjectMapper mapper = new ObjectMapper();
    	User user = null;
    	
		try {
			user = mapper.readValue(tokenInfo, User.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return user;
    }
    
    public User getUserDetails(User user){
    	Employee employee = employeeCrud.findByUserId(user.getUserId());
    	
    	//if(null!=employee && employee.getId()!=null) {
    		user.setUser(true);
    		user.setManager(true);//employee.isManager());
    		user.setAdmin(true);//employee.isAdmin());
    	//}
    	
    	return user;
    }

	public ArrayList<AccessRequest> approve(String userId, String employeeId) {
    	if(null!=userId) {
    	   	Employee employee = employeeCrud.findOne(employeeId);
    	   	
    	   	employee.setUserId(userId);
    	   	employeeCrud.save(employee);
    	   	AccessRequest request = accessCrud.findOne(userId);
    	   	if(request!=null) {
        	   	System.out.println("deleteing request for "+request.getName());
        		accessCrud.delete(request);    	   		
    	   	}
    	};
    	
    	return (ArrayList<AccessRequest>) accessCrud.findAll();
	}
}