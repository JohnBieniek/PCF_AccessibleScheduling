package org.cloudfoundry.samples.music.web;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.security.sasl.AuthenticationException;
import javax.validation.Valid;

import org.cloudfoundry.samples.music.managers.AccessibleSecurityManager;
import org.cloudfoundry.samples.music.managers.UpdateInfoManager;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldDataRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRepository;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	
	@Autowired
    private CrudRepository<Employee, String> repository;
	@Autowired
    private MongoCustomFieldDataRepository customDataRepository;
	@Autowired
    private MongoShiftRepository shiftRepository;
	@Autowired
    private UpdateInfoManager updateInfoManager;

    
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
        updateInfoManager.set("employees");
        employee.setLastUpdatedToNow();
        return repository.save(employee);
    }

    @RequestMapping(value = "/removeAbsence",method = RequestMethod.POST)
    public Employee removeAbsence(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String id,@RequestParam String date) throws AuthenticationException, ParseException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	Employee employee=repository.findOne(id);
    	ArrayList<String> requestedOff = new ArrayList<String>(Arrays.asList(employee.getRequestedOff()));
    	ArrayList<String> updatedRequestedOff = new ArrayList<String>();
    	if(null!=requestedOff && requestedOff.size()>0) {
    		for(String day :requestedOff) {
    			if(!day.equalsIgnoreCase(date)) {
    				updatedRequestedOff.add(day);
    			}
    		}
    	}

    	employee.setRequestedOff(Arrays.asList(updatedRequestedOff.toArray()).toArray(new String[updatedRequestedOff.toArray().length]));
        employee.setLastUpdatedToNow();
    	updateInfoManager.set("employees");
        return repository.save(employee);
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public Employee update(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String param) throws AuthenticationException, ParseException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	Employee employee=null;

    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee= mapper.readValue(param, Employee.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	logger.info(employee.toString());
    	employee.sortCallOffs();
        employee.setLastUpdatedToNow();
    	updateInfoManager.set("employees");
        return repository.save(employee);
    }
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Employee getById(@RequestHeader(value="Authorization", required=false) String idToken, @PathVariable String id) throws AuthenticationException {
    	CallAuth auth = securityManager.authorize(idToken, Constants.USER);
    	
    	if(!auth.getEmployeeId().equalsIgnoreCase(id)) {
    		if(!auth.isAdmin() && !auth.isManager()) {
    			throw new AuthenticationException();
    		}
    	}
        logger.info("Getting employee " + id);
        
        Employee employee = repository.findOne(id);
       
        
        return employee;
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
        
        updateInfoManager.set("employees");
        updateInfoManager.set("customFieldData");
        updateInfoManager.set("shifts");
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public String set(@RequestHeader(value="Authorization", required=false) String idToken, @RequestBody String json) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	try {
			JSONArray jsonArray = new JSONArray(json);

			for(int i = 0; i < jsonArray.length(); i++){
				JSONObject jsonObject= jsonArray.getJSONObject(i);
				
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
    	
    	updateInfoManager.set("employees");
    	
    	return json;
    }
}