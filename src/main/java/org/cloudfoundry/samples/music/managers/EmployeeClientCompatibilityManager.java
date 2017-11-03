package org.cloudfoundry.samples.music.managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.CustomField;
import accessiblesolutions.accessiblescheduling.domain.Employee;

@Component
public class EmployeeClientCompatibilityManager {
    private CrudRepository<CustomField, String> customFieldRepository;
    
    @Autowired
    private CustomDataManager customDataManager;
    
    @Autowired
    EmployeeShiftManager employeeShiftManager;
    
    @Autowired
    public EmployeeClientCompatibilityManager(CrudRepository<CustomField, String> customFieldRepository) {
        this.customFieldRepository=customFieldRepository;
    }
    
    public boolean isCompatibleWith(Employee employee,Client client){
    	//if(client!=null)logger.error("checking "+employee.getFirst() +" compatibility with "+client.toString());
    	boolean compatible = true;
    	
    	if(client==null){
    		return false;
    	}
    	
    	if(client.getMedPass() && !employee.getMedPassCertified()){
    		compatible=false;
    	}
    	else if(client.getNoFemaleStaff() && employee.isFemale()){
    		compatible=false;
    	}
    	else if(client.getNoMaleStaff() && employee.isMale()){
    		compatible = false;
    	}
    	else if(client.getNoSmokers() && employee.getSmoker()){
    		compatible = false;
    	}
    	else if(client.getOwnsCats() && employee.getNoCats()){
    		compatible = false;
    	}
    	else if(client.getSigningOnly() && !employee.getSigning()){
    		compatible = false;
    	}
    	
    	Iterable<CustomField> customFields = customFieldRepository.findAll();
    	for(CustomField customField : customFields){
    		boolean clientData = customDataManager.getClientCustomFieldData(client,customField);
    		boolean employeeData = customDataManager.getCustomFieldData(employee,customField);
    		
    		if(customField.getClientRequirement()){
    			if(clientData && !employeeData){
    				compatible=false;
    			}
    		}
    		if(customField.getEmployeeRequirement()){
    			if(employeeData && !clientData){
    				compatible=false;
    			}
    		}
    	}
    	
    	return compatible;
    }
}