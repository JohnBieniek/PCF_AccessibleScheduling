package org.cloudfoundry.samples.music.managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class ScheduleManager {
    @Autowired
    ShiftGenerationManager shiftGenerationManager;
    
    @Autowired
    ShiftAssignmentManager shiftAssignmentManager;
    
    public ScheduleManager() {}

    public String generateShifts(String selectedMonth) {
    	String eventResponse = shiftGenerationManager.generateEventShifts(selectedMonth);
    	String requestResponse = shiftGenerationManager.generateRequestedShifts(selectedMonth);

    	return eventResponse + requestResponse;
    }
    
    public String staffShifts(String selectedMonth) {
    	if(null!=selectedMonth){
    		int month = Integer.parseInt(selectedMonth);
	    	
	    	shiftAssignmentManager.saveAssignedUnconflictedPrestaffedRecuringShiftsToTableForMonth(month);
	    	
	    	shiftAssignmentManager.saveAssignedUnconflictedPrestaffedSingleShiftsToTableForMonth(month);
	    	
	    	shiftAssignmentManager.scheduleUnassignedNonEventShiftsFor(month);
    	}
    	
        return "Nothing too terrible happened";
    }
}