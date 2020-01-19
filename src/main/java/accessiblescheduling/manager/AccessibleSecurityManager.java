package accessiblescheduling.manager;
import java.time.LocalDateTime;

import javax.security.sasl.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import accessiblescheduling.constants.Constants;
import accessiblescheduling.domain.AccessRequest;
import accessiblescheduling.domain.Employee;
import accessiblescheduling.domain.Session;
import accessiblescheduling.repositories.mongodb.MongoAccessRequestRepository;
import accessiblescheduling.repositories.mongodb.MongoEmployeeRepository;
import accessiblescheduling.repositories.mongodb.MongoSessionRepository;
import accessiblescheduling.to.CallAuth;
import accessiblescheduling.to.User;

@Component
public class AccessibleSecurityManager {
	@Autowired
	private MongoEmployeeRepository employeeCrud;

	@Autowired
	private MongoSessionRepository sessionRepository;

	
	@Autowired
	private MongoAccessRequestRepository accessCrud;
	
	@Autowired
    private UpdateInfoManager updateInfoManager;
	
	RestTemplate restTemplate = new RestTemplate();
    
    public AccessibleSecurityManager() {}
    
    
    @Scheduled(fixedRate = Constants.ONE_HOUR_IN_MILISECONDS)
    public void clearSessions() {
    	sessionRepository.deleteAll();
    }
    
    public CallAuth authorize(String idToken, String requiredRole) throws AuthenticationException {
    	CallAuth auth = null;
    	
    	if(null==idToken || idToken.isEmpty()) {
			System.out.println("invalid id token");
			throw new AuthenticationException();
    	}
    	
    	boolean getUpdatedUser=false;
    	
    	User user = null;
    	
    	Session session = sessionRepository.findByToken(idToken);
    	
    	if(null!=session) {
    		if(!session.hasExpired()) {
    			user = new User();
    			user.setUserId(session.getUserId());
        		System.out.println("Pulled auth data from the session repository");
    		}
    		else {
    			getUpdatedUser=true;
    		}
    	}
    	else {
    		getUpdatedUser=true;
    	}
    	
    	if(getUpdatedUser) {
    		user = getUser(idToken);

//    		if(!Constants.CLIENT_ID.equalsIgnoreCase(user.getIssuedTo())) {
	//			System.out.println("invalid client id");
	//			throw new AuthenticationException();
	//		}
	//		else 
			if(!Constants.TOKEN_ISSUER.equalsIgnoreCase(user.getIssuer())) {
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
			
    		System.out.println("Pulled auth data from google apis");
    		session= new Session();
    		session.setToken(idToken);
    		session.setUserId(user.getUserId());
    		session.setExpires(LocalDateTime.now().plusSeconds(user.getExpiresIn()));
    		sessionRepository.insert(session);
    	}
    	
    	
    	if(null!=user) {
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
    			updateInfoManager.set("alerts");
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

    public String deny(String userId) {
    	if(null!=userId) {
    	   	AccessRequest request = accessCrud.findOne(userId);
    	   	
    	   	if(request!=null) {
    			updateInfoManager.set("alerts");
        		accessCrud.delete(request);    	   		
    	   	}
    	   	else {
    	   		return "DELETED";
    	   	}
    	};
    	
    	return "DENIED";
	}
    
	public String approve(String userId, String employeeId) {
    	if(null!=userId) {
    	   	AccessRequest request = accessCrud.findOne(userId);
    	   	
    	   	if(request!=null) {
        	   	Employee employee = employeeCrud.findOne(employeeId);
        	   	
        	   	if(employee!=null) {
	        	   	employee.setUserId(userId);
	    			updateInfoManager.set("employees");
	        	   	employeeCrud.save(employee);
	        	   	
	    			updateInfoManager.set("alerts");
	        		accessCrud.delete(request);    	   		
        	   	}
        	   	else {
        	   		return "INVALID EMPLOYEE";
        	   	}
    	   	}
    	   	else {
    	   		return "DELETED";
    	   	}
    	};
    	
    	return "APPROVED";
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
		
    	return user;
    }
}