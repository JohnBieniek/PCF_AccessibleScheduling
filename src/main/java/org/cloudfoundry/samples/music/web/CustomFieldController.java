package org.cloudfoundry.samples.music.web;

import java.util.List;

import javax.validation.Valid;

import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.domain.CustomField;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;

@RestController
@RequestMapping(value = "/customFields")
public class CustomFieldController {
    private static final Logger logger = LoggerFactory.getLogger(CustomFieldController.class);
    private CrudRepository<CustomField, String> repository;
    private MongoCustomFieldDataRepository customDataRepository;
    
    @Autowired
    public CustomFieldController(CrudRepository<CustomField, String> repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<CustomField> customFields() {
        return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.PUT)
    public CustomField add(@RequestBody @Valid CustomField customField) {
        logger.info("Adding customField " + customField.getId());
        return repository.save(customField);
    }

    @RequestMapping(method = RequestMethod.POST)
    public CustomField update(@RequestBody @Valid CustomField customField) {
        logger.info("Updating customField " + customField.getId());
        return repository.save(customField);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CustomField getById(@PathVariable String id) {
        logger.info("Getting customField " + id);
        return repository.findOne(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteById(@PathVariable String id) {
        logger.info("Deleting customField " + id);
        repository.delete(id);
        
        List<CustomFieldData> customFieldData = customDataRepository.findByCustomFieldId(id);
        for(CustomFieldData entry:customFieldData) {
        	customDataRepository.delete(entry.getId());
        }
    }
    
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public List<CustomField> set(@RequestBody List<CustomField> customFields) {
    	repository.save(customFields);
    	
    	return customFields;
    }
}