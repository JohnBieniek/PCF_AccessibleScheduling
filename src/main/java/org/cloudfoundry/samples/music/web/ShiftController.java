package org.cloudfoundry.samples.music.web;

import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.domain.ShiftRequest;

import org.cloudfoundry.samples.music.managers.ScheduleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonParseException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.*;

import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "/shifts")
public class ShiftController {
    private static final Logger logger = LoggerFactory.getLogger(ShiftController.class);
    
    @Autowired
	ScheduleManager manager;
    
    private CrudRepository<Shift, String> repository;

    @Autowired
    public ShiftController(CrudRepository<Shift, String> repository) {
        this.repository = repository;
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public List<Shift> set(@RequestBody List<Shift> shifts) {
    	repository.save(shifts);
    	
    	return shifts;
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/validity")
    public @ResponseBody boolean getValidity(HttpServletRequest request) throws CorruptDataException{
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
    public @ResponseBody float getDuration(HttpServletRequest request) throws CorruptDataException{
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
    public Iterable<Shift> shifts() {
        return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Shift add(@RequestBody @Valid Shift shift) {
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
    public Shift update(@RequestBody @Valid Shift shift) {
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
    public Shift getById(@PathVariable String id) {
        logger.info("Getting shift " + id);
        return repository.findOne(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteById(@PathVariable String id) {
        logger.info("Deleting shift " + id);
        repository.delete(id);
    }
}