package org.cloudfoundry.samples.music.web;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

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
import accessiblesolutions.accessiblescheduling.domain.Employee;

@RestController
@RequestMapping(value = "/employees")
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private CrudRepository<Employee, String> repository;

    @Autowired
    public EmployeeController(CrudRepository<Employee, String> repository) {
        this.repository = repository;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public Iterable<Employee> employees() {
    	List<Employee> employees = (List<Employee>) repository.findAll();
        Collections.sort(employees);
		return employees;
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Employee add(@RequestBody @Valid Employee employee) {
        logger.info("Adding employee " + employee.getId());
        return repository.save(employee);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Employee update(@RequestBody @Valid Employee employee) {
        logger.info("Updating employee " + employee.getId());
        employee.fixInvalidAvailability();
        return repository.save(employee);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Employee getById(@PathVariable String id) {
        logger.info("Getting employee " + id);
        
        Employee employee = repository.findOne(id);
       
        
        return employee;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteById(@PathVariable String id) {
        logger.info("Deleting employee " + id);
        repository.delete(id);
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public String set(@RequestBody String json) {
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
    	
    	
    	return json;
    }
}