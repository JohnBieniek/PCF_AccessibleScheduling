package org.cloudfoundry.samples.music.web;

import java.io.IOException;
import java.util.List;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.cloudfoundry.samples.music.managers.AccessibleSecurityManager;
import org.cloudfoundry.samples.music.managers.ScheduleManager;
import org.cloudfoundry.samples.music.managers.ShiftManager;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.constants.Constants;
import accessiblesolutions.accessiblescheduling.domain.CallAuth;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;

@RestController
@RequestMapping(value = "/shifts")
public class ShiftController {
    private static final Logger logger = LoggerFactory.getLogger(ShiftController.class);
    
	@Autowired 
	AccessibleSecurityManager securityManager;
	
    @Autowired
	ScheduleManager manager;
    
    private CrudRepository<Shift, String> repository;

    @Autowired
    ShiftManager shiftManager;
    
    @Autowired
    public ShiftController(CrudRepository<Shift, String> repository) {
        this.repository = repository;
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public List<Shift> set(@RequestHeader(value="Authorization", required=false) String idToken,@RequestBody List<Shift> shifts) {
    	repository.save(shifts);
    	
    	return shifts;
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/validity")
    public @ResponseBody boolean getValidity(@RequestHeader(value="Authorization", required=false) String idToken, HttpServletRequest request) throws CorruptDataException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	Shift shift =null;

    	String param= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			shift = mapper.readValue(param, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	boolean validity = shift.isValid();

    	return validity;
    }

    @RequestMapping(method = RequestMethod.POST, value= "/duration")
    public @ResponseBody float getDuration(@RequestHeader(value="Authorization", required=false) String idToken,HttpServletRequest request) throws CorruptDataException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	Shift shift =null;

    	String param= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			shift = mapper.readValue(param, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	float duration = shift.getDuration();

    	return duration;
    }
    
    
    @RequestMapping(method = RequestMethod.GET)
    public Iterable<Shift> shifts(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
        return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Shift add(@RequestHeader(value="Authorization", required=false) String idToken,@RequestBody @Valid Shift shift) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	logger.info("Adding shift " + shift.getId());
        
        if(shift.getStartMonth()==0){
            shift.setStartMonth((int) Integer.parseInt(shift.getStartDate().split("-")[1]));
        }
        
        if(shift.getStartYear()==0){
        	shift.setStartYear((int) Integer.parseInt(shift.getStartDate().split("-")[0]));
        }
        
        return repository.save(shift);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Shift update(@RequestHeader(value="Authorization", required=false) String idToken, @RequestBody @Valid Shift shift) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
        logger.info("Updating shift " + shift.getId());
        
        if(shift.getStartMonth()==0){
            shift.setStartMonth((int) Integer.parseInt(shift.getStartDate().split("-")[1]));
        }
        
        if(shift.getStartYear()==0){
        	shift.setStartYear((int) Integer.parseInt(shift.getStartDate().split("-")[0]));
        }
        
        return repository.save(shift);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Shift getById(@RequestHeader(value="Authorization", required=false) String idToken,@PathVariable String id) throws AuthenticationException {
    	CallAuth auth = securityManager.authorize(idToken, Constants.USER);
    	
    	Shift shift = repository.findOne(id);
    	if(auth.getEmployeeId()!=shift.getStaffId()) {
    		if(!auth.isAdmin() && !auth.isManager()) {
    			throw new AuthenticationException();
    		}
    	}
        logger.info("Getting shift " + id);
        
        return shift;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteById(@RequestHeader(value="Authorization", required=false) String idToken, @PathVariable String id) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
        logger.info("Deleting shift " + id);
        repository.delete(id);
    }
}