package org.cloudfoundry.samples.music.managers;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.ScheduleStatus;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
@Component
public class ScheduleManager {
    @Autowired
    ShiftGenerationManager shiftGenerationManager;
    
    @Autowired
    ShiftAssignmentManager shiftAssignmentManager;
    
    @Autowired
    private MongoShiftRepository shiftCrud;
    
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

	public Iterable<Shift> getClientShiftsForWeek(String clientId, String month, String day, String year) {
		// TODO Auto-generate
		LocalDate date = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
		System.out.println("Found date for request of :"+date.toString());
		
		Iterable<Shift> shifts = shiftCrud.findByStartMonthAndClientId(Integer.parseInt(month),clientId);
		
		for(Shift shift:shifts) {
			//if(datesShareWeeks)
		}
		
		return shifts;
	} 
}