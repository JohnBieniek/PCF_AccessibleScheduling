package accessiblescheduling.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonParseException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import accessiblescheduling.constants.Constants;
import accessiblescheduling.domain.ClientRequest;
import accessiblescheduling.exception.CorruptDataException;
import accessiblescheduling.repositories.mongodb.MongoClientRequestRepository;
import accessiblescheduling.manager.AccessibleSecurityManager;
import accessiblescheduling.manager.UpdateInfoManager;

@RestController
@RequestMapping(value = "/clientRequests")
public class ClientRequestController {
    private static final Logger logger = LoggerFactory.getLogger(ClientRequestController.class);
    private CrudRepository<ClientRequest, String> repository;
    
    @Autowired
    private MongoClientRequestRepository mongoRepository;

    @Autowired
    private UpdateInfoManager updateInfoManager;
    
	@Autowired 
	AccessibleSecurityManager securityManager;

    @Autowired
    public ClientRequestController(CrudRepository<ClientRequest, String> repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<ClientRequest> clientRequests(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);

    	return repository.findAll();
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/validity")
    public @ResponseBody boolean getValidity(HttpServletRequest request) throws CorruptDataException{
    	ClientRequest clientRequest =null;

    	String param= request.getParameter("clientRequest");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			clientRequest = mapper.readValue(param, ClientRequest.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	boolean validity = clientRequest.isValid();

    	return validity;
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ClientRequest add(@RequestHeader(value="Authorization", required=false) String idToken, @RequestBody @Valid ClientRequest clientRequest) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	logger.info("Adding clientRequest " + clientRequest.getId());
    	clientRequest.setLastUpdatedToNow();
        updateInfoManager.set("requests");
        return repository.save(clientRequest);
    }

    //TODO put failed update in different http response
    @RequestMapping(method = RequestMethod.POST)
    public List<ClientRequest> update(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String param) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	ClientRequest clientRequest =null;

    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			clientRequest = mapper.readValue(param, ClientRequest.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	if(clientRequest.isValid()) {
    		clientRequest.setLastUpdatedToNow();
	        repository.save(clientRequest);
	        updateInfoManager.set("requests");
    	}

        return mongoRepository.findByClientId(clientRequest.getClientId());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ClientRequest getById(@RequestHeader(value="Authorization", required=false) String idToken,@PathVariable String id) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
        logger.info("Getting clientRequest " + id);
        return repository.findOne(id);
    }

    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public List<ClientRequest> set(@RequestHeader(value="Authorization", required=false) String idToken, @RequestBody List<ClientRequest> requests) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	for(ClientRequest request:requests) {
    		request.setLastUpdatedToNow();
    	}
    	
    	repository.save(requests);
    	updateInfoManager.set("requests");
    	
    	return requests;
    }
}