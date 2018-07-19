package org.cloudfoundry.samples.music.managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.CustomField;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;

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
    
    /**Returns if this employee allowed to work with this client.
     * Clients requiring medpass must have employees that are medpass certified.
     * Clients must have staff of the proper gender.
     * Clients must not be paired with smokers upon request.
     * Clients with cats must not be paired with employees who have cat allergies.
     * Clients must be paired with signing staff when required.
     * Clients and employees must be properly aligned with custom requirements.
     * 
     * @param employee
     * @param client
     * @return boolean Is this employee allowed to work with this client?
     * @throws ProccessingException Null employee or client provided to isCompatibleWith
     */
    public boolean isCompatibleWith(Employee employee,Client client) throws ProccessingException{
    	if(null==employee||null==client) {
			throw new ProccessingException("Null employee or client provided to isCompatibleWith");
		}

    	boolean compatible = true;

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
    	
    	if(null!=customFields) {
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
    	}
    	
    	return compatible;
    }
}