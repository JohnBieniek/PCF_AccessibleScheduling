package org.cloudfoundry.samples.music.managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.ScheduleStatus;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
@Component
public class ScheduleManager {
    @Autowired
    ShiftGenerationManager shiftGenerationManager;
    
    @Autowired
    ShiftAssignmentManager shiftAssignmentManager;
    
    @Autowired
    private CrudRepository<ScheduleStatus, String> scheduleStatusCrud;
    
    public ScheduleManager() {}

    public void generateStatusList() {
    	for(int month=1;month<13;month++) {
    		ScheduleStatus status = new ScheduleStatus();
    		status.setMonth(month+"");
    		scheduleStatusCrud.save(status);
    	}
    }
    
    public String generateShifts(String selectedMonth) throws CorruptDataException {
    	String eventResponse = shiftGenerationManager.generateEventShifts(selectedMonth);
    	String requestResponse = shiftGenerationManager.generateRequestedShifts(selectedMonth);
    	
    	return eventResponse + requestResponse;
    }
    
    public String generateSingleShifts(String selectedMonth) throws CorruptDataException {
    	String requestResponse = shiftGenerationManager.generateRequestedSingleShifts(selectedMonth);

    	return requestResponse;
    }
    
    public String staffShifts(String selectedMonth, String selectedYear) throws CorruptDataException, ProccessingException {
    	String result = "";
    	if(null!=selectedMonth){
    		int month = Integer.parseInt(selectedMonth);
    		int year = Integer.parseInt(selectedYear);
	    	shiftAssignmentManager.saveAssignedUnconflictedPrestaffedRecuringShiftsToTableForMonth(month,false);
	    	
	    	shiftAssignmentManager.saveAssignedUnconflictedPrestaffedSingleShiftsToTableForMonth(month,false);
	    	
	    	result =shiftAssignmentManager.scheduleUnassignedNonEventShiftsFor(month,year);
    	}
    	
        return result;
    }
}