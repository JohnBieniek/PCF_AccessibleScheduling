package org.cloudfoundry.samples.music.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.*;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.ShiftRequest;

import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/shiftRequests")
public class ShiftRequestController {
    private static final Logger logger = LoggerFactory.getLogger(ShiftRequestController.class);
    private CrudRepository<ShiftRequest, String> repository;

    @Autowired
    public ShiftRequestController(CrudRepository<ShiftRequest, String> repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<ShiftRequest> shiftRequests() {
        return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ShiftRequest add(@RequestBody @Valid ShiftRequest shiftRequest) {
        logger.info("Adding shiftRequest " + shiftRequest.getId());
        return repository.save(shiftRequest);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ShiftRequest update(@RequestBody @Valid ShiftRequest shiftRequest) {
        logger.info("Updating shiftRequest " + shiftRequest.getId());
        return repository.save(shiftRequest);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ShiftRequest getById(@PathVariable String id) {
        logger.info("Getting shiftRequest " + id);
        return repository.findOne(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteById(@PathVariable String id) {
        logger.info("Deleting shiftRequest " + id);
        repository.delete(id);
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public List<ShiftRequest> set(@RequestBody List<ShiftRequest> requests) {
    	repository.save(requests);
    	
    	return requests;
    }
}