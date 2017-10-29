package org.cloudfoundry.samples.music.managers;
import java.util.List;

import org.cloudfoundry.samples.music.domain.Client;
import org.cloudfoundry.samples.music.domain.CustomField;
import org.cloudfoundry.samples.music.domain.CustomFieldData;
import org.cloudfoundry.samples.music.domain.Employee;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomDataManager {
    private MongoCustomFieldDataRepository customFieldDataRepository;
    private CrudRepository<CustomFieldData, String> customFieldDataCrud;
    
    @Autowired
    public CustomDataManager(MongoCustomFieldDataRepository customFieldDataRepository, CrudRepository<CustomFieldData, String> customFieldDataCrud) {
        this.customFieldDataCrud = customFieldDataCrud;
        this.customFieldDataRepository=customFieldDataRepository;
    }
    
    public boolean getCustomFieldData(Employee employee, CustomField customField) {
		List<CustomFieldData> data= customFieldDataRepository.findByOwnerId(employee.getId());
		CustomFieldData fieldData=null;
		if(data.isEmpty()){
			setCustomFieldData(employee,customField,false);
			data= customFieldDataRepository.findByOwnerId(employee.getId());
		}
		
		for(CustomFieldData customFieldData : data){
			if(customFieldData.getCustomFieldId().equals(customField.getId())){
				fieldData=customFieldData;
			}
		}

		if(fieldData==null){
			setCustomFieldData(employee,customField,false);
		}
		
		return fieldData==null?false:fieldData.getBooleanData();
	}

	public boolean getClientCustomFieldData(Client client, CustomField customField) {
		List<CustomFieldData> data= customFieldDataRepository.findByOwnerId(client.getId());
		CustomFieldData fieldData=null;
		if(data.isEmpty()){
			setClientCustomFieldData(client,customField,false);

			data= customFieldDataRepository.findByOwnerId(client.getId());
		}
		
		for(CustomFieldData customFieldData : data){
			if(customFieldData.getCustomFieldId().equals(customField.getId())){
				fieldData=customFieldData;
			}
		}
		
		if(fieldData==null){
			setClientCustomFieldData(client,customField,false);
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