package org.cloudfoundry.samples.music.managers;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

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
    
    public Iterable<Shift> getEmployeeShiftsForWeek(String employeeId, String month, String day, String year) {
		ArrayList<Shift> out=new ArrayList<Shift>();
		
		LocalDate weekStart = LocalDate.of(Integer.parseInt(year), Month.of(Integer.parseInt(month)), Integer.parseInt(day));
		LocalDate weekEnd=null;
		
		while(weekStart.getDayOfWeek().getValue()!=7) {
			weekStart=weekStart.minusDays(1);
		}
		weekEnd=weekStart.plusDays(6);
		
		Iterable<Shift> firstMonthsShifts = shiftCrud.findByStartMonthAndStaffId(weekStart.getMonthValue(),employeeId);
		Iterable<Shift> secondMonthsShifts = null;
		System.out.println("weekStart"+weekStart.getMonthValue());
		System.out.println("weekEnd:"+weekEnd.getMonthValue());
		if(weekStart.getMonthValue()!=weekEnd.getMonthValue()) {
			System.out.println("getting second months shifts");
			
			secondMonthsShifts = shiftCrud.findByStartMonthAndStaffId(weekEnd.getMonthValue(),employeeId);
			if(null!=secondMonthsShifts) {
				System.out.println("shifts found for second month:"+secondMonthsShifts.toString());
			}
		}
		
		System.out.println("getting shifts starting:"+weekStart.toString()+" and ending:"+weekEnd.toString());
		for(Shift shift:firstMonthsShifts) {
			try {
				System.out.println("shifts start:"+shift.getStartsLocalDate().toString());
				if(shift.getStartsLocalDate().isAfter(weekStart.minusDays(1)) &&
						shift.getStartsLocalDate().isBefore(weekEnd.plusDays(1))){
					boolean alreadyAdded=false;
					
					for(Shift existingShift:out) {
						if(existingShift.getId().equalsIgnoreCase(shift.getId())) {
							alreadyAdded=true;
						}
					}
					if(!alreadyAdded) {
						out.add(shift);
					}
				}
			} catch (CorruptDataException e) {
				e.printStackTrace();
			}
		}
		if(null!=secondMonthsShifts) {
			for(Shift shift:secondMonthsShifts) {
				try {
					System.out.println("shifts start:"+shift.getStartsLocalDate().toString());
					if(shift.getStartsLocalDate().isAfter(weekStart.minusDays(1)) &&
							shift.getStartsLocalDate().isBefore(weekEnd.plusDays(1))){
						boolean alreadyAdded=false;
						
						for(Shift existingShift:out) {
							if(existingShift.getId().equalsIgnoreCase(shift.getId())) {
								alreadyAdded=true;
							}
						}
						if(!alreadyAdded) {
							out.add(shift);
						}
					}
				} catch (CorruptDataException e) {
					e.printStackTrace();
				}
			}
		}
		Shift[] outArray = new Shift[out.size()];
		
		for(int index = 0 ; index<out.size();index++){
			outArray[index]=out.get(index);
		}
		
		Arrays.sort(outArray, new Comparator<Shift>() {
	        @Override
	        public int compare(Shift o1, Shift o2) {
	            return o1.compareTo(o2);
	        }
	    });
		out=new ArrayList<Shift>();
		for(Shift returnedShift: outArray){
			out.add(returnedShift);
		}
		 
		return out;
	} 

	public Iterable<Shift> getClientShiftsForWeek(String clientId, String month, String day, String year) {
		ArrayList<Shift> out=new ArrayList<Shift>();
		
		LocalDate weekStart = LocalDate.of(Integer.parseInt(year), Month.of(Integer.parseInt(month)), Integer.parseInt(day));
		LocalDate weekEnd=null;
		
		while(weekStart.getDayOfWeek().getValue()!=7) {
			weekStart=weekStart.minusDays(1);
		}
		weekEnd=weekStart.plusDays(6);
		
		Iterable<Shift> firstMonthsShifts = shiftCrud.findByStartMonthAndClientId(weekStart.getMonthValue(),clientId);
		Iterable<Shift> secondMonthsShifts = null;
		if(weekStart.getMonthValue()!=weekEnd.getMonthValue()) {
			secondMonthsShifts = shiftCrud.findByStartMonthAndClientId(weekEnd.getMonthValue(),clientId);
		}
		
		System.out.println("getting shifts starting:"+weekStart.toString()+" and ending:"+weekEnd.toString());
		for(Shift shift:firstMonthsShifts) {
			try {
				System.out.println("shifts start:"+shift.getStartsLocalDate().toString());
				if(shift.getStartsLocalDate().isAfter(weekStart.minusDays(1)) &&
						shift.getStartsLocalDate().isBefore(weekEnd.plusDays(1))){
					boolean alreadyAdded=false;
					
					for(Shift existingShift:out) {
						if(existingShift.getId().equalsIgnoreCase(shift.getId())) {
							alreadyAdded=true;
						}
					}
					if(!alreadyAdded) {
						out.add(shift);
					}
				}
			} catch (CorruptDataException e) {
				e.printStackTrace();
			}
		}
		if(null!=secondMonthsShifts) {
			for(Shift shift:secondMonthsShifts) {
				try {
					System.out.println("shifts start:"+shift.getStartsLocalDate().toString());
					if(shift.getStartsLocalDate().isAfter(weekStart.minusDays(1)) &&
							shift.getStartsLocalDate().isBefore(weekEnd.plusDays(1))){
						boolean alreadyAdded=false;
						
						for(Shift existingShift:out) {
							if(existingShift.getId().equalsIgnoreCase(shift.getId())) {
								alreadyAdded=true;
							}
						}
						if(!alreadyAdded) {
							out.add(shift);
						}
					}
				} catch (CorruptDataException e) {
					e.printStackTrace();
				}
			}
		}
		Shift[] outArray = new Shift[out.size()];
		
		for(int index = 0 ; index<out.size();index++){
			outArray[index]=out.get(index);
		}
		
		Arrays.sort(outArray, new Comparator<Shift>() {
	        @Override
	        public int compare(Shift o1, Shift o2) {
	            return o1.compareTo(o2);
	        }
	    });
		out=new ArrayList<Shift>();
		for(Shift returnedShift: outArray){
			out.add(returnedShift);
		}
		 
		return out;
	} 
}