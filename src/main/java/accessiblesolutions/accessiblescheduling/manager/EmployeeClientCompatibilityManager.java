package accessiblescheduling.manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblescheduling.domain.Client;
import accessiblescheduling.domain.CustomField;
import accessiblescheduling.domain.Employee;
import accessiblescheduling.exception.CorruptDataException;
import accessiblescheduling.exception.ProccessingException;

@Component
public class EmployeeClientCompatibilityManager {
    private CrudRepository<CustomField, String> customFieldRepository;
    
    @Autowired
    public CustomDataManager customDataManager;
    
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
     * @throws CorruptDataException 
     * @Tested
     */
    public boolean isCompatibleWith(Employee employee,Client client) throws ProccessingException, CorruptDataException{
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
	    		boolean clientData = customDataManager.getCustomFieldDatasValueOrCreateIfMissing(client,customField);
	    		boolean employeeData = customDataManager.getCustomFieldDatasValueOrCreateIfMissing(employee,customField);
	    		
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
    	System.out.println("getting compatibleWith for "+employee.toString()+ client.toString()+ "compatible?"+compatible);
    	
    	return compatible;
    }
}