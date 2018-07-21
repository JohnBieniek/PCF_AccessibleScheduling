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
import accessiblesolutions.accessiblescheduling.exception.BadRequestException;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.util.Util;

@Component
public class CustomDataManager {
	@Autowired
	private CrudRepository<CustomField,String> customFieldCrud;
	
	@Autowired
	private CrudRepository<Employee,String> employeeCrud;
	
	@Autowired
	private CrudRepository<Client,String> clientCrud;
	
	@Autowired
	public MongoCustomFieldDataRepository customFieldDataRepository;

	@Autowired
	public CrudRepository<CustomFieldData, String> customFieldDataCrud;
    
    @Autowired
    public CustomDataManager() {
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
    	
    	id = Util.getIdFromEmployeeOrClient(individual);
    	
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
    
    public boolean getCustomFieldDataOrCreateIfMissing(Object individual, CustomField customField) throws ProccessingException, CorruptDataException {
    	String id = null;
    	
    	if(null==individual) {
    		throw new ProccessingException("Null individual provided to getCustomFieldDataOrCreateIfMissing");
    	}
    	
    	id = Util.getIdFromEmployeeOrClient(individual);
    	
		List<CustomFieldData> data= customFieldDataRepository.findByOwnerId(id);
		
		CustomFieldData fieldData=null;
		if(data.isEmpty()){
			if(individual.getClass()==Employee.class) {
	    		Employee employee = (Employee) individual;
	    		setCustomFieldData(employee,customField,false);
	    	}
	    	else if(individual.getClass()==Client.class) {
	    		Client client = (Client) individual;
	    		setCustomFieldData(client,customField,false);
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
	    		setCustomFieldData(client,customField,false);
	    	}
		}
		
		return fieldData==null?false:fieldData.getBooleanData();
	}

	/**Finds the CustomFieldData for the provided Client/Employee and field
	 * then sets the content to the passed value. 
	 * If CustomFieldData doesn't already exist for this individual create it and set the value.
	 * 
	 * @param object an Employee or Client containing a valid ID, and preferably a first name for output
	 * @param customField containing a valid ID
	 * @param value the data to set for the CustomFieldData for this person and field
	 * @throws ProccessingException Null input provided
	 * @throws CorruptDataException No Id provided for individual in setCustomFieldData
	 */
	public void setCustomFieldData(Object individual, CustomField customField, boolean value) throws ProccessingException, CorruptDataException {
		String id =null;
		List<CustomFieldData> data = null;
		boolean dataFound = false;
		
		if(null==individual || null==customField) {
			throw new ProccessingException("Client,Employee,or CustomField null in setCustomFieldData");
		}
		
		id = Util.getIdFromEmployeeOrClient(individual);
		
		if(null==id) {
			throw new CorruptDataException("No Id provided for individual in setCustomFieldData");
		}
		
		data = customFieldDataRepository.findByOwnerId(id);
		
		//Find the data for this individual/field and set it's data to the passed value
		if(null!=data && data.isEmpty()){
			for(CustomFieldData customFieldData : data){
				if(customFieldData.getCustomFieldId().equals(customField.getId())){
					customFieldData.setBooleanData(value);;
					customFieldDataCrud.save(customFieldData);
					
					dataFound=true;
				}
			}
		}
		
		//Create the CustomFieldData and save it if it was not found
		if(!dataFound){
			CustomFieldData newData = new CustomFieldData();
			newData.setOwnerId(id);
			newData.setCustomFieldId(customField.getId());
			newData.setBooleanData(value);
			
			customFieldDataCrud.save(newData);
		}
	}
}