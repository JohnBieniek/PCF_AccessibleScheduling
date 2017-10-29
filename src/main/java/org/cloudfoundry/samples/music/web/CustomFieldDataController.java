package org.cloudfoundry.samples.music.web;

import org.cloudfoundry.samples.music.domain.CustomFieldData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/customFieldData")
public class CustomFieldDataController {
    private static final Logger logger = LoggerFactory.getLogger(CustomFieldDataController.class);
    private CrudRepository<CustomFieldData, String> repository;

    @Autowired
    public CustomFieldDataController(CrudRepository<CustomFieldData, String> repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<CustomFieldData> customFieldDatas() {
        return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.PUT)
    public CustomFieldData add(@RequestBody @Valid CustomFieldData customFieldData) {
        logger.info("Adding customFieldData " + customFieldData.getId());
        return repository.save(customFieldData);
    }

    @RequestMapping(method = RequestMethod.POST)
    public CustomFieldData update(@RequestBody @Valid CustomFieldData customFieldData) {
        logger.info("Updating customFieldData " + customFieldData.getId());
        return repository.save(customFieldData);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CustomFieldData getById(@PathVariable String id) {
        logger.info("Getting customFieldData " + id);
        return repository.findOne(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteById(@PathVariable String id) {
        logger.info("Deleting customFieldData " + id);
        repository.delete(id);
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public List<CustomFieldData> set(@RequestBody List<CustomFieldData> customFieldDatas) {
    	repository.save(customFieldDatas);
    	
    	return customFieldDatas;
    }
}