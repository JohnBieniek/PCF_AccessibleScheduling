package org.cloudfoundry.samples.music.managers;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

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
		ArrayList<Shift> out=new ArrayList<Shift>();
		
		Iterable<Shift> shifts = shiftCrud.findByStartMonthAndClientId(Integer.parseInt(month),clientId);
		
		LocalDate weekStart = LocalDate.of(Integer.parseInt(year), Month.of(Integer.parseInt(month)), Integer.parseInt(day));
		LocalDate weekEnd=null;
		
		while(weekStart.getDayOfWeek().getValue()!=7) {
			weekStart=weekStart.minusDays(1);
		}
		weekEnd=weekStart.plusDays(6);

		for(Shift shift:shifts) {
			try {
				if(shift.getStartsLocalDate().isAfter(weekStart.minusDays(1)) &&
						shift.getStartsLocalDate().isBefore(weekEnd.plusDays(1))){
					out.add(shift);
				}
			} catch (CorruptDataException e) {
				e.printStackTrace();
			}
		}
		
		return out;
	} 
}