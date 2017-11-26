package org.cloudfoundry.samples.music.managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
@Component
public class ScheduleManager {
    @Autowired
    ShiftGenerationManager shiftGenerationManager;
    
    @Autowired
    ShiftAssignmentManager shiftAssignmentManager;
    
    public ScheduleManager() {}

    public String generateShifts(String selectedMonth) throws CorruptDataException {
    	String eventResponse = shiftGenerationManager.generateEventShifts(selectedMonth);
    	String requestResponse = shiftGenerationManager.generateRequestedShifts(selectedMonth);

    	return eventResponse + requestResponse;
    }
    
    public String staffShifts(String selectedMonth, String selectedYear) throws CorruptDataException, ProccessingException {
    	if(null!=selectedMonth){
    		int month = Integer.parseInt(selectedMonth);
    		int year = Integer.parseInt(selectedYear);
	    	shiftAssignmentManager.saveAssignedUnconflictedPrestaffedRecuringShiftsToTableForMonth(month);
	    	
	    	shiftAssignmentManager.saveAssignedUnconflictedPrestaffedSingleShiftsToTableForMonth(month);
	    	
	    	shiftAssignmentManager.scheduleUnassignedNonEventShiftsFor(month,year);
    	}
    	
        return "Nothing too terrible happened";
    }
}