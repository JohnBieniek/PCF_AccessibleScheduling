package org.cloudfoundry.samples.music.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import accessiblesolutions.accessiblescheduling.domain.RecurringShiftNeed;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/recurringShiftNeeds")
public class RecurringShiftNeedController {
    private static final Logger logger = LoggerFactory.getLogger(RecurringShiftNeedController.class);
    private CrudRepository<RecurringShiftNeed, String> repository;

    @Autowired
    public RecurringShiftNeedController(CrudRepository<RecurringShiftNeed, String> repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<RecurringShiftNeed> recurringShiftNeeds() {
    	Iterable<RecurringShiftNeed> requests = repository.findAll();
    	ArrayList<RecurringShiftNeed> rVal = new ArrayList<RecurringShiftNeed>();
    	for(RecurringShiftNeed request: requests){
    		request.setDay(request.getDay());
    		rVal.add(request);
    	}
        return (Iterable<RecurringShiftNeed>) rVal;
    }

    @RequestMapping(method = RequestMethod.PUT)
    public RecurringShiftNeed add(@RequestBody @Valid RecurringShiftNeed recurringShiftNeed) {
        logger.info("Adding recurringShiftNeed " + recurringShiftNeed.getId());
        return repository.save(recurringShiftNeed);
    }

    @RequestMapping(method = RequestMethod.POST)
    public RecurringShiftNeed update(@RequestBody @Valid RecurringShiftNeed recurringShiftNeed) {
        logger.info("Updating recurringShiftNeed " + recurringShiftNeed.getId());
        return repository.save(recurringShiftNeed);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public RecurringShiftNeed getById(@PathVariable String id) {
        logger.info("Getting recurringShiftNeed " + id);
        return repository.findOne(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteById(@PathVariable String id) {
        logger.info("Deleting recurringShiftNeed " + id);
        repository.delete(id);
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public List<RecurringShiftNeed> set(@RequestBody List<RecurringShiftNeed> requests) {
    	repository.save(requests);
    	
    	return requests;
    }
}