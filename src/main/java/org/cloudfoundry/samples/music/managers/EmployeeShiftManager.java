package org.cloudfoundry.samples.music.managers;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import accessiblesolutions.accessiblescheduling.domain.Shift;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.util.Util;
import accessiblesolutions.accessiblescheduling.worker.ShiftWorker;

@Component
public class EmployeeShiftManager {
    private CrudRepository<Employee, String> employeeRepository;
    private MongoShiftRepository shiftRepository;
    
    @Autowired
    public EmployeeShiftManager(CrudRepository<Employee, String> employeeRepository, MongoShiftRepository shiftRepository) {
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
    }
    
    /**A week after the start date of the assigned shift we get getAssignedShiftsForEmployeeForWeekOfMonth.
     * If the shift in question starts saturday night and ends sunday info for the week of saturday is returned.
     * Does not return shifts for previous weeks that run into this week.
  	 * Weeks are 0 indexed starting with week 0.
  	 */
    public ArrayList<Shift> getAssignedShiftsForEmployeeForWeekAfterShift(String employeeId, Shift shift) throws CorruptDataException, ProccessingException {
    	ArrayList<Shift> shifts = new ArrayList<Shift>();
    	
    	if(null!=shift&& null!=employeeId) {
	    	LocalDate weekAftersDate = shift.getStartsLocalDate().plusWeeks(1);
			String month = weekAftersDate.getMonth().getValue()>9?weekAftersDate.getMonth().getValue()+"":"0"+weekAftersDate.getMonth().getValue();
			String day = weekAftersDate.getDayOfMonth()>9?weekAftersDate.getDayOfMonth()+"":"0"+weekAftersDate.getDayOfMonth();
			int weekAfter = Util.getWeekOfDate(weekAftersDate.getYear()+"-"+month+"-"+day);
			
			shifts = getAssignedShiftsForEmployeeForWeekOfMonth(employeeId,weekAfter,weekAftersDate.getMonthValue());
    	}
    	
		return shifts;
	}
  	
    /**If the shift in question starts saturday night and ends sunday info for the week of saturday is returned.
     * Does not return shifts for previous weeks that run into this week.
  	 * Weeks are 0 indexed starting with week 0.
  	 * 
  	 * @Tested
  	 */
  	public ArrayList<Shift> getAssignedShiftsForEmployeeForWeekOfShift(String employeeId, Shift shift) throws CorruptDataException{
  		ArrayList<Shift> assignedShiftsForEmployee = new ArrayList<Shift>();
  		
  		if(null!=shift) {
  			assignedShiftsForEmployee = getAssignedShiftsForEmployeeForWeekOfMonth(employeeId,shift.getStartWeek(), shift.getStartMonth());
  		}
  		
  		return assignedShiftsForEmployee;
  	}
  	
  	/**Does not return shifts for previous weeks that run into this week.
  	 * Weeks are 0 indexed starting with week 0
  	 * 
  	 * @Tested
  	 */
  	public ArrayList<Shift> getAssignedShiftsForEmployeeForWeekOfMonth(String employeeId, int week, int month) throws CorruptDataException{
		ArrayList<Shift> assignedShiftsForEmployeeForMonth =getAssignedShiftsForEmployeeForMonth(employeeId,month);
		
		ArrayList<Shift> assignedShiftsForEmployeeForWeekOfMonth = new ArrayList<Shift>();
		if(assignedShiftsForEmployeeForMonth!=null){
			for(Shift shift: assignedShiftsForEmployeeForMonth){
				if(shift.getStartWeek()==week){
					assignedShiftsForEmployeeForWeekOfMonth.add(shift);
				}
			}
		}
		return assignedShiftsForEmployeeForWeekOfMonth;
	}
  	
  	/**Returns and empty list if nothing is found
  	 * 
  	 * @Tested
  	 */
  	public ArrayList<Shift> getAssignedShiftsForEmployeeForDayOfMonth(String employeeId, int day, int month){
		ArrayList<Shift> assignedShiftsForEmployeeForDayOfMonth = new ArrayList<Shift>();
		
		if(null!=employeeId && day>0) {
			assignedShiftsForEmployeeForDayOfMonth = getAssignedShiftsForEmployeeStartingDayOfMonth(employeeId,day,month);
			LocalDate previousDay = LocalDate.of(2018,month,day).minusDays(1);
			
			ArrayList<Shift> assignedShiftsForEmployeeForPreviousDayOfMonth = getAssignedShiftsForEmployeeStartingDayOfMonth(employeeId,previousDay.getDayOfMonth(),previousDay.getMonthValue());
			if(assignedShiftsForEmployeeForPreviousDayOfMonth!=null){
				for(Shift shift: assignedShiftsForEmployeeForPreviousDayOfMonth){
					int endDay=-1;
					try {
						endDay = shift.getEndsLocalDate().getDayOfMonth();
						
						if(endDay==day){
							assignedShiftsForEmployeeForDayOfMonth.add(shift);
						}
					} catch (CorruptDataException e) {
						// TODO LOG
						e.printStackTrace();
					}
				}
			}
		}
		return assignedShiftsForEmployeeForDayOfMonth;
	}
  	
  	/**Returns and empty list if nothing is found
  	 * 
  	 * @Tested
  	 */
  	public ArrayList<Shift> getAssignedShiftsForEmployeeStartingDayOfMonth(String employeeId, int day, int month){
		ArrayList<Shift> assignedShiftsForEmployeeForMonth =getAssignedShiftsForEmployeeForMonth(employeeId,month);
		
		ArrayList<Shift> assignedShiftsForEmployeeForDayOfMonth = new ArrayList<Shift>();
		
		if(assignedShiftsForEmployeeForMonth!=null){
			for(Shift shift: assignedShiftsForEmployeeForMonth){
				if(shift.getStartDay()==day){
					assignedShiftsForEmployeeForDayOfMonth.add(shift);
				}
			}
		}
		
		return assignedShiftsForEmployeeForDayOfMonth;
	}
  	/**Includes shifts starting in previous months that roll into the first day
  	 * 
  	 * @Tested
  	 */
  	public ArrayList<Shift> getAssignedShiftsForEmployeeForMonth(String employeeId, int month){
		ArrayList<Shift> assignedShiftsForEmployeeForMonth = new ArrayList<Shift>();
		
		if(null!=employeeId) {
			for(Shift shift: shiftRepository.findByStartMonth(month)){
				if(shift!=null&& shift.getStaffId()!=null&&shift.getStaffId().equals(employeeId)){
					assignedShiftsForEmployeeForMonth.add(shift);
				}
			}
			
			for(Shift shift: shiftRepository.findByStartMonth(month-1>0?month-1:12)){
				try {
					if(shift!=null&& shift.getStaffId()!=null&&shift.getStaffId().equals(employeeId)&& shift.getEndsLocalDate().getMonthValue()==month){
						assignedShiftsForEmployeeForMonth.add(shift);
					}
				} catch (CorruptDataException e) {
					// TODO LOG, we want to know its an issue but it doesn't stop this
					e.printStackTrace();
				}
			}
		}
		
		return assignedShiftsForEmployeeForMonth;
	}
  

  	public ArrayList<Shift> getAssignedOvernightShiftsForEmployeForTheLastDayOfMonthBefore(String employeeId, int month) throws CorruptDataException, ProccessingException{
  		ArrayList<Shift> assignedOvernightShiftsForEmployeForTheLastDayOfMonthBefore = new ArrayList<Shift>();
  		
  		if(month==1){
  			month=12;
  		}
  		else{
  			month--;
  		}
  		
  		assignedOvernightShiftsForEmployeForTheLastDayOfMonthBefore = getAssignedOvernightShiftsForEmployeForTheLastDayOfMonth(employeeId,month);
  		
  		return assignedOvernightShiftsForEmployeForTheLastDayOfMonthBefore;
  	}
  	
	public ArrayList<Shift> getAssignedOvernightShiftsForEmployeForTheLastDayOfMonth(String employeeId, int month) throws CorruptDataException, ProccessingException{
		ArrayList<Shift> assignedOvernightShiftsForEmployeForTheLastDayOfMonth = new ArrayList<Shift>();
		
		ArrayList<Shift> shifts = getAssignedOvernightShiftsForEmployeForMonth(employeeId,month);
		
		assignedOvernightShiftsForEmployeForTheLastDayOfMonth = ShiftWorker.getShiftsStartingTheLastDayOfMonth(shifts, month);
		
		return assignedOvernightShiftsForEmployeForTheLastDayOfMonth;
	}
	

	public ArrayList<Shift> getAssignedOvernightShiftsForEmployeForMonth(String employeeId, int month) throws CorruptDataException, ProccessingException{
		ArrayList<Shift> assignedOvernightShiftsForEmployeeForMonth = new ArrayList<Shift>();
		
		ArrayList<Shift> shifts = getAssignedShiftsForEmployeeForMonth(employeeId,month);
		
		assignedOvernightShiftsForEmployeeForMonth = ShiftWorker.getOvernightShifts(shifts);
		
		return assignedOvernightShiftsForEmployeeForMonth;
	}
  	
  	public ArrayList<Shift> getShiftsForEmployeeForWeekBefore(String employeeId,Shift shift) throws CorruptDataException, ProccessingException {
		LocalDate weekBeforesDate = shift.getStartsLocalDate().minusWeeks(1);
		int weekBefore = Util.getWeekOfDate(weekBeforesDate.getYear()+"-"+weekBeforesDate.getMonth().getValue()+"-"+weekBeforesDate.getDayOfMonth());
		
		ArrayList<Shift> shifts = getAssignedShiftsForEmployeeForWeekOfMonth(employeeId,weekBefore,weekBeforesDate.getMonthValue());
		
		return shifts;
	}

  	public float getHoursScheduledWeek(String employeeId,int week, int month) throws CorruptDataException{
		return getHoursScheduledWeek(employeeRepository.findOne(employeeId), week, month);
	}
  	
  	public float getHoursScheduledWeek(Employee employee,int week, int month) throws CorruptDataException{
		float hours = 80;
		if(null!=employee){
			hours= 0;
			ArrayList<Shift> shiftsForWeek = null;
			shiftsForWeek = getAssignedShiftsForEmployeeForWeekOfMonth(employee.getId(), week, month);
			
			if(null!=shiftsForWeek){
				for(Shift scheduledShift : shiftsForWeek){
					hours+= scheduledShift.getDuration();
				}
			}
		}
		return hours;
	}
}