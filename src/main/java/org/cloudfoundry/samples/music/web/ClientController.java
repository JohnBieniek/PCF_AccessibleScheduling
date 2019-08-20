package org.cloudfoundry.samples.music.web;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldDataRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRequestRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.domain.ShiftRequest;

@RestController
@RequestMapping(value = "/clients")
public class ClientController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private CrudRepository<Client, String> repository;
    private MongoShiftRepository shiftRepository;
    private MongoShiftRequestRepository requestRepository;
    private MongoCustomFieldDataRepository customDataRepository;

    @Autowired
    public ClientController(CrudRepository<Client, String> repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<Client> clients() {
    	List<Client> clients = (List<Client>) repository.findAll();
        Collections.sort(clients);
		return clients;
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Client add(@RequestBody @Valid Client client) {
        logger.info("Adding client " + client.getId());
        return repository.save(client);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Client update(@RequestBody @Valid Client client) {
        logger.info("Updating client " + client.getId());
        return repository.save(client);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Client getById(@PathVariable String id) {
        logger.info("Getting client " + id);
        return repository.findOne(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public List<Client> deleteById(@PathVariable String id) {
        logger.info("Deleting client " + id);
        repository.delete(id);
    	List<Client> clients = (List<Client>) repository.findAll();
        Collections.sort(clients);
        
        List<Shift> shifts = shiftRepository.findByClientId(id);
        for(Shift shift:shifts) {
        	shiftRepository.delete(shift.getId());
        }
        
        List<ShiftRequest> shiftRequests = requestRepository.findByClientId(id);
        for(ShiftRequest shiftRequest:shiftRequests) {
        	requestRepository.delete(shiftRequest.getId());
        }
        
        List<CustomFieldData> customFieldData = customDataRepository.findByOwnerId(id);
        for(CustomFieldData entry:customFieldData) {
        	customDataRepository.delete(entry.getId());
        }
        
		return clients;
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public String set(@RequestBody String json) {
    	try {
			JSONArray jsonArray = new JSONArray(json);

			for(int i = 0; i < jsonArray.length(); i++){
				JSONObject jsonObject= jsonArray.getJSONObject(i);
				
				ObjectMapper objectMapper = new ObjectMapper();
				Client client = objectMapper.readValue(jsonObject.toString(), Client.class);
				repository.save(client);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
    	
    	
    	return json;
    }
}