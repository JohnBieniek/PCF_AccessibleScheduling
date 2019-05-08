package org.cloudfoundry.samples.music.web;

import java.io.IOException;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.domain.ClientRequest;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;

@RestController
@RequestMapping(value = "/clientRequests")
public class ClientRequestController {
    private static final Logger logger = LoggerFactory.getLogger(ClientRequestController.class);
    private CrudRepository<ClientRequest, String> repository;

    @Autowired
    public ClientRequestController(CrudRepository<ClientRequest, String> repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<ClientRequest> clientRequests() {
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
    public ClientRequest add(@RequestBody @Valid ClientRequest clientRequest) {
        logger.info("Adding clientRequest " + clientRequest.getId());
        return repository.save(clientRequest);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ClientRequest update(@RequestParam String param) {
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
        return repository.save(clientRequest);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ClientRequest getById(@PathVariable String id) {
        logger.info("Getting clientRequest " + id);
        return repository.findOne(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteById(@PathVariable String id) {
        logger.info("Deleting clientRequest " + id);
        repository.delete(id);
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public List<ClientRequest> set(@RequestBody List<ClientRequest> requests) {
    	repository.save(requests);
    	
    	return requests;
    }
}