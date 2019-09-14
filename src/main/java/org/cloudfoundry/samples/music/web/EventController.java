package org.cloudfoundry.samples.music.web;

import accessiblesolutions.accessiblescheduling.domain.Event;

import org.cloudfoundry.samples.music.managers.AccessibleSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.*;

import accessiblesolutions.accessiblescheduling.domain.RecurringShiftNeed;

import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/events")
public class EventController {
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    
//	@Autowired 
//	AccessibleSecurityManager securityManager;
//	
//    private CrudRepository<Event, String> repository;
//
//    @Autowired
//    public EventController(CrudRepository<Event, String> repository) {
//        this.repository = repository;
//    }
//
//    @RequestMapping(method = RequestMethod.GET)
//    public Iterable<Event> events(@RequestHeader(value="Authorization", required=false) String idToken) {
//        return repository.findAll();
//    }
//
//    @RequestMapping(method = RequestMethod.PUT)
//    public Event add(@RequestHeader(value="Authorization", required=false) String idToken, @RequestBody @Valid Event event) {
//        logger.info("Adding event " + event.getId());
//        return repository.save(event);
//    }
//
//    @RequestMapping(method = RequestMethod.POST)
//    public Event update(@RequestHeader(value="Authorization", required=false) String idToken, @RequestBody @Valid Event event) {
//        logger.info("Updating event " + event.getId());
//        return repository.save(event);
//    }
//
//    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
//    public Event getById(@RequestHeader(value="Authorization", required=false) String idToken,@PathVariable String id) {
//        logger.info("Getting event " + id);
//        return repository.findOne(id);
//    }
//
//    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
//    public void deleteById(@RequestHeader(value="Authorization", required=false) String idToken,@PathVariable String id) {
//        logger.info("Deleting event " + id);
//        repository.delete(id);
//    }
//    
//    @RequestMapping(value = "/set", method = RequestMethod.POST)
//    public List<Event> set(@RequestHeader(value="Authorization", required=false) String idToken, @RequestBody List<Event> events) {
//    	repository.save(events);
//    	
//    	return events;
//    }
}