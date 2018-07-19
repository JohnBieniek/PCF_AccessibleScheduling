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
import accessiblesolutions.accessiblescheduling.domain.ShiftRequest;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;

@Component
public class CustomDataManager {
	@Autowired
	private CrudRepository<CustomField,String> customFieldCrud;
	
	@Autowired
	private CrudRepository<Employee,String> employeeCrud;
	
	@Autowired
	private CrudRepository<Client,String> clientCrud;
	
    private MongoCustomFieldDataRepository customFieldDataRepository;
    private CrudRepository<CustomFieldData, String> customFieldDataCrud;
    
    @Autowired
    public CustomDataManager(MongoCustomFieldDataRepository customFieldDataRepository, CrudRepository<CustomFieldData, String> customFieldDataCrud) {
        this.customFieldDataCrud = customFieldDataCrud;
        this.customFieldDataRepository=customFieldDataRepository;
    }
    
    public void removeOrphanedCustomFieldData() {
    	ArrayList<CustomFieldData> orphans = getOrphanedCustomFieldData();
    	for(CustomFieldData request:orphans){
    		customFieldDataCrud.delete(request);
    	}
	}
    public ArrayList<CustomFieldData> getOrphanedCustomFieldData() {
    	ArrayList<CustomFieldData> orphans = new ArrayList<CustomFieldData>();
    	Iterable<CustomFieldData> table = customFieldDataCrud.findAll();
    	Iterable<CustomField> fields =customFieldCrud.findAll();
    	ArrayList<String> fieldIds = new ArrayList<String>();
    	Iterable<Client> clients =clientCrud.findAll();
    	ArrayList<String> clientIds = new ArrayList<String>();
    	Iterable<Employee> employees =employeeCrud.findAll();
    	ArrayList<String> employeeIds = new ArrayList<String>();
    	
    	for(CustomField field : fields){
    		fieldIds.add(field.getId());
    	}
    	
    	for(Employee employee : employees){
    		employeeIds.add(employee.getId());
    	}
    	
    	for(Client client: clients){
    		clientIds.add(client.getId());
    	}

    	for(CustomFieldData data:table){
    		if(!fieldIds.contains(data.getCustomFieldId()) || 
    			(!employeeIds.contains(data.getOwnerId()) && !clientIds.contains(data.getOwnerId()))){
    			orphans.add(data);
    		}
    	}
    	
    	return orphans;
	}
    
    public boolean getCustomFieldData(Object individual, CustomField customField) throws ProccessingException {
    	String id = null;
    	
    	if(null==individual || null ==customField ) {
    		throw new ProccessingException("Null individual pr customField provided to getCustomFieldDataOrCreateIfMissing");
    	}
    	
    	if(individual.getClass()==Employee.class) {
    		Employee employee = (Employee) individual;
    		id = employee.getId();
    	}
    	else if(individual.getClass()==Client.class) {
    		Client client = (Client) individual;
    		id = client.getId();
    	}
    	else {
    		throw new ProccessingException("Invalid individual provided to getCustomFieldDataOrCreateIfMissing");
    	}
    	
		List<CustomFieldData> data= customFieldDataRepository.findByOwnerId(id);
		
		CustomFieldData fieldData=null;
		
		if(null!=data) {
			for(CustomFieldData customFieldData : data){
				if(customFieldData.getCustomFieldId().equals(customField.getId())){
					fieldData=customFieldData;
				}
			}
		}
		
		return fieldData==null?false:fieldData.getBooleanData();
    }
    
    public boolean getCustomFieldDataOrCreateIfMissing(Object individual, CustomField customField) throws ProccessingException {
    	String id = null;
    	
    	if(null==individual) {
    		throw new ProccessingException("Null individual provided to getCustomFieldDataOrCreateIfMissing");
    	}
    	
    	if(individual.getClass()==Employee.class) {
    		Employee employee = (Employee) individual;
    		id = employee.getId();
    	}
    	else if(individual.getClass()==Client.class) {
    		Client client = (Client) individual;
    		id = client.getId();
    	}
    	else {
    		throw new ProccessingException("Invalid individual provided to getCustomFieldDataOrCreateIfMissing");
    	}
    	
		List<CustomFieldData> data= customFieldDataRepository.findByOwnerId(id);
		
		CustomFieldData fieldData=null;
		if(data.isEmpty()){
			if(individual.getClass()==Employee.class) {
	    		Employee employee = (Employee) individual;
	    		setCustomFieldData(employee,customField,false);
	    	}
	    	else if(individual.getClass()==Client.class) {
	    		Client client = (Client) individual;
	    		setClientCustomFieldData(client,customField,false);
	    	}
			
			data= customFieldDataRepository.findByOwnerId(id);
		}
		
		for(CustomFieldData customFieldData : data){
			if(customFieldData.getCustomFieldId().equals(customField.getId())){
				fieldData=customFieldData;
			}
		}

		if(fieldData==null){
			if(individual.getClass()==Employee.class) {
	    		Employee employee = (Employee) individual;
	    		setCustomFieldData(employee,customField,false);
	    	}
	    	else if(individual.getClass()==Client.class) {
	    		Client client = (Client) individual;
	    		setClientCustomFieldData(client,customField,false);
	    	}
		}
		
		return fieldData==null?false:fieldData.getBooleanData();
	}

	public boolean setClientCustomFieldData(Client client, CustomField customField,boolean value) {
		List<CustomFieldData> data= customFieldDataRepository.findByOwnerId(client.getId());
		boolean dataFound = false;
		if(data.isEmpty()){
			CustomFieldData newData = new CustomFieldData();
			newData.setOwnerId(client.getId());
			newData.setCustomFieldId(customField.getId());
			newData.setBooleanData(value);
			customFieldDataCrud.save(newData);
			data= customFieldDataRepository.findByOwnerId(client.getId());
		}
		
		for(CustomFieldData customFieldData : data){
			System.out.println(client.getFirst()+customField.getClientVariable()+"set for " +value);
			if(customFieldData.getCustomFieldId().equals(customField.getId())){
				customFieldData.setBooleanData(value);;
				customFieldDataCrud.save(customFieldData);
				System.out.println("Saved the thing");
				dataFound=true;
			}
		}
		
		if(!dataFound){
			CustomFieldData newData = new CustomFieldData();
			newData.setOwnerId(client.getId());
			newData.setCustomFieldId(customField.getId());
			newData.setBooleanData(value);
			customFieldDataCrud.save(newData);
			System.out.println(client.getFirst()+customField.getClientVariable()+"set for " +value+"part2");
			System.out.println("Saved the thing");
		}
		
		return value;
	}

	/**
	 * 
	 * @param employee
	 * @param customField
	 * @param value 
	 * @return boolean of the custom field
	 */
	public boolean setCustomFieldData(Employee employee, CustomField customField, boolean value) {
		List<CustomFieldData> data= customFieldDataRepository.findByOwnerId(employee.getId());
		boolean dataFound = false;
		
		if(data.isEmpty()){
			CustomFieldData newData = new CustomFieldData();
			newData.setOwnerId(employee.getId());
			newData.setCustomFieldId(customField.getId());
			newData.setBooleanData(value);
			customFieldDataCrud.save(newData);
			data= customFieldDataRepository.findByOwnerId(employee.getId());
			System.out.println(employee.getFirst()+customField.getEmployeeVariable()+"set for " +value+"part1");
			System.out.println("Saved the thing");
		}
		
		for(CustomFieldData customFieldData : data){
			if(customFieldData.getCustomFieldId().equals(customField.getId())){
				customFieldData.setBooleanData(value);;
				customFieldDataCrud.save(customFieldData);
				
				dataFound=true;
				System.out.println(employee.getFirst()+customField.getEmployeeVariable()+"set for " +value+"part2");
				System.out.println("Saved the thing");
			}
		}
		
		if(!dataFound){
			CustomFieldData newData = new CustomFieldData();
			newData.setOwnerId(employee.getId());
			newData.setCustomFieldId(customField.getId());
			newData.setBooleanData(value);
			customFieldDataCrud.save(newData);
			System.out.println(employee.getFirst()+customField.getEmployeeVariable()+"set for " +value+"part3");
			System.out.println("Saved the thing");
		}
		
		return value;
	}
}