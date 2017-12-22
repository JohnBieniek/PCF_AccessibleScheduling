package org.cloudfoundry.samples.music.managers;
import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.CustomField;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.RecurringShiftNeed;
import accessiblesolutions.accessiblescheduling.domain.ShiftRequest;

@Component
public class CleaningManager {
	@Autowired
	private CrudRepository<CustomField,String> customFieldCrud;
	
	@Autowired
	private CrudRepository<Employee,String> employeeCrud;
	@Autowired
	private CrudRepository<RecurringShiftNeed,String> recurringShiftNeedCrud;
	@Autowired
	private CrudRepository<ShiftRequest,String> shiftRequestCrud;
	@Autowired
	private CrudRepository<Client,String> clientCrud;
	
    private MongoCustomFieldDataRepository customFieldDataRepository;
    private CrudRepository<CustomFieldData, String> customFieldDataCrud;
    
    @Autowired
    public CleaningManager(MongoCustomFieldDataRepository customFieldDataRepository, CrudRepository<CustomFieldData, String> customFieldDataCrud) {
        this.customFieldDataCrud = customFieldDataCrud;
        this.customFieldDataRepository=customFieldDataRepository;
    }
    

    public ArrayList<ShiftRequest> getOrphanedShiftRequests() {
    	ArrayList<ShiftRequest> orphans = new ArrayList<ShiftRequest>();
    	Iterable<ShiftRequest> table = shiftRequestCrud.findAll();
    	Iterable<Client> clients =clientCrud.findAll();
    	ArrayList<String> clientIds = new ArrayList<String>();
    	
    	
    	for(Client client: clients){
    		clientIds.add(client.getId());
    	}

    	for(ShiftRequest request:table){
    			if(!clientIds.contains(request.getClientId())){
    				orphans.add(request);
    			}
    	}
    	
    	return orphans;
	}
    
    public ArrayList<RecurringShiftNeed> getOrphanedRecurringShiftRequests() {
    	ArrayList<RecurringShiftNeed> orphans = new ArrayList<RecurringShiftNeed>();
    	Iterable<RecurringShiftNeed> table = recurringShiftNeedCrud.findAll();
    	Iterable<Client> clients =clientCrud.findAll();
    	ArrayList<String> clientIds = new ArrayList<String>();
    	
    	
    	for(Client client: clients){
    		clientIds.add(client.getId());
    	}

    	for(RecurringShiftNeed request:table){
    			if(!clientIds.contains(request.getClientId())){
    				orphans.add(request);
    			}
    	}
    	
    	return orphans;
	}
    
    public void removeOrphanedRecurringShiftRequests() {
    	ArrayList<RecurringShiftNeed> orphanedShiftRequests = getOrphanedRecurringShiftRequests();
    	for(RecurringShiftNeed request:orphanedShiftRequests){
    		recurringShiftNeedCrud.delete(request);
    	}
	}
    public void removeOrphanedSingleShiftRequests() {
    	ArrayList<ShiftRequest> orphanedShiftRequests = getOrphanedShiftRequests();
    	for(ShiftRequest request:orphanedShiftRequests){
    		shiftRequestCrud.delete(request);
    	}
	}
}