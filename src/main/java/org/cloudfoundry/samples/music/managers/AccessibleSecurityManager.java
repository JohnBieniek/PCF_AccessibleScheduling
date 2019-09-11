package org.cloudfoundry.samples.music.managers;
import java.util.ArrayList;

import javax.security.sasl.AuthenticationException;

import org.cloudfoundry.samples.music.repositories.mongodb.MongoAccessRequestRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoEmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import accessiblesolutions.accessiblescheduling.constants.Constants;
import accessiblesolutions.accessiblescheduling.domain.AccessRequest;
import accessiblesolutions.accessiblescheduling.domain.CallAuth;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.User;
import accessiblesolutions.accessiblescheduling.proxy.SecurityProxy;

@Component
public class AccessibleSecurityManager {
	@Autowired
	private MongoEmployeeRepository employeeCrud;
	
	@Autowired
	private MongoAccessRequestRepository accessCrud;
	
	RestTemplate restTemplate = new RestTemplate();
    
    public AccessibleSecurityManager() {}
    
    public CallAuth authorize(String idToken, String requiredRole) throws AuthenticationException {
    	CallAuth auth = null;
    	
    	System.out.println("for role:"+requiredRole+"authorizing:"+idToken);
    	if(null==idToken || idToken.isEmpty()) {
			System.out.println("invalid id token");
			throw new AuthenticationException();
    	}
    	
    	User user = getUser(idToken);
    	
    	if(null!=user) {
    		if(!Constants.CLIENT_ID.equalsIgnoreCase(user.getIssuedTo())) {
    			System.out.println("invalid client id");
    			throw new AuthenticationException();
    		}
    		else if(!Constants.TOKEN_ISSUER.equalsIgnoreCase(user.getIssuer())) {
    			System.out.println("invalid id issuer");
    			throw new AuthenticationException();
    		}
    		else if(user.getExpiresIn()<=0) {
    			System.out.println("expired token");
    			throw new AuthenticationException();
    		}
    		else if(!user.isVerifiedEmail()) {
    			System.out.println("e-mail not verified");
    			throw new AuthenticationException();
    		}
    		
    		user= getUserDetails(user);
    		
    		if(!requiredRole.equalsIgnoreCase("guest") && !user.isUser()) {
    			System.out.println("not signed in");
       			throw new AuthenticationException();
    		}
    		else if(requiredRole.equalsIgnoreCase("user") && !user.isUser()) {
    			System.out.println("not a user");
       			throw new AuthenticationException();
    		}
    		else if(requiredRole.equalsIgnoreCase("manager") && (!user.isManager() && !user.isAdmin())) {
    			System.out.println("not a manager");
       			throw new AuthenticationException();
    		}
    		else if(requiredRole.equalsIgnoreCase("admin") && !user.isAdmin()) {
    			System.out.println("not a manager");
    			throw new AuthenticationException();
    		}
    		
    		auth= new CallAuth(user.isManager(),user.isAdmin());
    		auth.setEmployeeId(user.getEmployeeId());
    	}
    	
    	System.out.println("Call Auth found:"+auth.toString());
    	return auth;
    }
    public boolean signedUp(String idToken) throws AuthenticationException {
    	User user = getUser(idToken);
    	
    	if(null!=user) {
        	AccessRequest request = accessCrud.findOne(user.getUserId());
        	if(null!=request) {
        		return true;
        	}
    	}
    	
    	return false;
    }
    
    public  AccessRequest signUp(String idToken, String name) throws AuthenticationException {
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
    
    public boolean isAdmin(String idToken) throws AuthenticationException {
    	return getUserDetails(idToken).isAdmin();
    }
    
    public boolean isManager(String idToken) throws AuthenticationException {
    	return getUserDetails(idToken).isManager();
    }
    
    public boolean isUser(String idToken) throws AuthenticationException {
    	return getUserDetails(idToken).isUser();
    }
    
    public User getUserDetails(String idToken) throws AuthenticationException {
    	return getUserDetails(getUser(idToken));
    }
    
    public User getUserDetails(User user){
    	Employee employee = employeeCrud.findByUserId(user.getUserId());
    	
    	if(null!=employee && employee.getId()!=null) {
    		user.setUser(true);
    		user.setManager(employee.isManager());
    		user.setAdmin(employee.isAdmin());
    		user.setEmployeeId(employee.getId());
    	}
    	
    	return user;
    }

    public ArrayList<AccessRequest> deny(String userId) {
    	if(null!=userId) {
    	   	AccessRequest request = accessCrud.findOne(userId);
    	   	
    	   	if(request!=null) {
        	   	System.out.println("deleteing request for "+request.getName());
        		accessCrud.delete(request);    	   		
    	   	}
    	};
    	
    	return (ArrayList<AccessRequest>) accessCrud.findAll();
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
	
	public User getUser(String idToken) throws AuthenticationException{
    	String tokenInfo = restTemplate.getForObject("https://www.googleapis.com/oauth2/v2/tokeninfo?id_token="+idToken, String.class);
    	
    	ObjectMapper mapper = new ObjectMapper();
    	User user = null;
    	
		try {
			user = mapper.readValue(tokenInfo, User.class);
		} catch (Exception e) {	
			throw new AuthenticationException();
		}
    	System.out.println("user found:"+user.toString());
    	return user;
    }
}