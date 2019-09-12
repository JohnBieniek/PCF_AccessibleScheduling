package org.cloudfoundry.samples.music.web;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.security.sasl.AuthenticationException;
import javax.validation.Valid;

import org.cloudfoundry.samples.music.managers.AccessibleSecurityManager;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldDataRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRepository;
import org.codehaus.jettison.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonParseException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.constants.Constants;
import accessiblesolutions.accessiblescheduling.domain.CallAuth;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.Shift;

@RestController
@RequestMapping(value = "/employees")
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    
	@Autowired 
	AccessibleSecurityManager securityManager;
	
    private CrudRepository<Employee, String> repository;
    private MongoCustomFieldDataRepository customDataRepository;
    private MongoShiftRepository shiftRepository;
    
    @Autowired
    public EmployeeController(CrudRepository<Employee, String> repository) {
        this.repository = repository;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public Iterable<Employee> employees(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	List<Employee> employees = (List<Employee>) repository.findAll();
        Collections.sort(employees);
		return employees;
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Employee add(@RequestHeader(value="Authorization", required=false) String idToken,@RequestBody @Valid Employee employee) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	logger.info("Adding employee " + employee.getId());
        return repository.save(employee);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Employee update(@RequestHeader(value="Authorization", required=false) String idToken, @RequestBody @Valid Employee employee) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
        logger.info("Updating employee " + employee.getId());
        employee.fixInvalidAvailability();
        return repository.save(employee);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody String getById(@RequestHeader(value="Authorization", required=false) String idToken, @PathVariable String id) throws AuthenticationException, org.codehaus.jettison.json.JSONException, JsonProcessingException {
    	String response = null;
    	CallAuth auth = securityManager.authorize(idToken, Constants.USER);
    	System.out.println("auth.getEmployeeId():"+auth.getEmployeeId());
    	System.out.println("id"+id);
    	if(!auth.getEmployeeId().equalsIgnoreCase(id)) {
    		if(!auth.isAdmin() && !auth.isManager()) {
    			throw new AuthenticationException();
    		}
    	}
        logger.info("Getting employee " + id);
        
        Employee employee = repository.findOne(id);
       
        if(!auth.isAdmin() && !auth.isManager()) {
        	response = employee.getUserSafeEmployeeData().toString();
		}
        else {
        	ObjectMapper mapper = new ObjectMapper();

        	response = mapper.writeValueAsString(employee);

        }
        
        return response;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteById(@RequestHeader(value="Authorization", required=false) String idToken, @PathVariable String id) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
        logger.info("Deleting employee " + id);
        repository.delete(id);
        
        List<CustomFieldData> customFieldData = customDataRepository.findByOwnerId(id);
        for(CustomFieldData entry:customFieldData) {
        	customDataRepository.delete(entry.getId());
        }
        
        List<Shift> shifts = shiftRepository.findByStaffId(id);
        for(Shift shift:shifts) {
        	shift.setAssigned(false);
        	shift.setStaffId(null);
        	shift.setStaffName(null);
        	shiftRepository.save(shift);
        }
        
        shifts = shiftRepository.findByRequestedStaffId(id);
        for(Shift shift:shifts) {
        	shift.setRequestedStaffId(null);
        	shift.setRequestedStaffName(null);
        	shiftRepository.save(shift);
        }
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public String set(@RequestHeader(value="Authorization", required=false) String idToken, @RequestBody String json) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	try {
			JSONArray jsonArray = new JSONArray(json);

			for(int i = 0; i < jsonArray.length(); i++){
				org.json.JSONObject jsonObject= jsonArray.getJSONObject(i);
				
				ObjectMapper objectMapper = new ObjectMapper();
				Employee employee = objectMapper.readValue(jsonObject.toString(), Employee.class);
				repository.save(employee);
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