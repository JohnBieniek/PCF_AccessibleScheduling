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
  	 * 
  	 * @Tested
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
  	 * @param employeeId
  	 * @param week
  	 * @param month
  	 * @return ArrayList<Shift> Shift
  	 * @throws CorruptDataException when a shift has a malformed or missing start week
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
  	 * @param employeeId
  	 * @param month
  	 * @return ArrayList<Shift> Shift
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
		
		ArrayList<Shift> shifts = getAssignedOvernightShiftsForEmployeeForMonth(employeeId,month);
		
		assignedOvernightShiftsForEmployeForTheLastDayOfMonth = ShiftWorker.getShiftsStartingTheLastDayOfMonth(shifts, month);
		
		return assignedOvernightShiftsForEmployeForTheLastDayOfMonth;
	}
	

	/**Returns all shifts that start and end on a different day assigned to the requested
	 * employee for the provided month.
	 * If the shift in question starts saturday night and ends sunday info for the week of saturday is returned.
     * Does not return shifts for previous weeks that run into this week.
  	 * Weeks are 0 indexed starting with week 0.
	 * 
	 * @param employeeId
	 * @param month 1-12
	 * @return ArrayList<Shift> Shift
	 * @throws CorruptDataException A Shift has a null start or end date
	 * @throws ProccessingException Shifts are null internally
	 * @Tested
	 */
	public ArrayList<Shift> getAssignedOvernightShiftsForEmployeeForMonth(String employeeId, int month) throws CorruptDataException, ProccessingException{
		ArrayList<Shift> assignedOvernightShiftsForEmployeeForMonth = new ArrayList<Shift>();
		
		ArrayList<Shift> shifts = getAssignedShiftsForEmployeeForMonth(employeeId,month);
		
		assignedOvernightShiftsForEmployeeForMonth = ShiftWorker.getOvernightShifts(shifts);
		
		return assignedOvernightShiftsForEmployeeForMonth;
	}
  	
	/**Returns an ArrayList<Shift> of everything the employee is scheduled for the
	 * week prior to the provided shift.
	 * 
	 * @param employeeId
	 * @param shift
	 * @return
	 * @throws CorruptDataException when a shift has a malformed or missing start week
	 * @throws ProccessingException when the shifts start date is malformed
	 * @Tested
	 * @TODO consider a refactor to throw proccessingExceptions with null input
	 */
  	public ArrayList<Shift> getAssignedShiftsForEmployeeForWeekBeforeShift(String employeeId,Shift shift) throws CorruptDataException, ProccessingException {
  		ArrayList<Shift> shifts = new ArrayList<Shift>();
  		
  		if(null!=employeeId && null!=shift) {
	  		LocalDate weekBeforesDate = shift.getStartsLocalDate().minusWeeks(1);
			String month = weekBeforesDate.getMonth().getValue()>9?weekBeforesDate.getMonth().getValue()+"":"0"+weekBeforesDate.getMonth().getValue();
			String day = weekBeforesDate.getDayOfMonth()>9?weekBeforesDate.getDayOfMonth()+"":"0"+weekBeforesDate.getDayOfMonth();
			int weekBefore = Util.getWeekOfDate(weekBeforesDate.getYear()+"-"+month+"-"+day);
	  		
			shifts = getAssignedShiftsForEmployeeForWeekOfMonth(employeeId,weekBefore,weekBeforesDate.getMonthValue());
  		}
  		
		return shifts;
	}
  	
  	/** Return the hours the selected employee is currently scheduled for the week of the month provided.
  	 * Weeks are 0 indexed.
  	 * 
  	 * @param employee
  	 * @param week
  	 * @param month
  	 * @return float hours scheduled for the employee
  	 * @throws CorruptDataException when a shift has a malformed or missing start week
  	 * @throws ProccessingException when no employee is provided
  	 * @Tested
  	 */
  	public float getHoursScheduledWeekOfMonth(Employee employee,int week, int month) throws CorruptDataException, ProccessingException{
		float hours = 0;
		ArrayList<Shift> shiftsForWeek = null;
		int weekBefore = -1;//The week before the one provided
		ArrayList<Shift> shiftsForPreviousWeek;
		String[] endTime = null;//For  a shift on the last day of the week going overnight
		int hoursThisWeek = 0;//For  a shift on the last day of the week going overnight
		int minutesThisWeek = 0;//For  a shift on the last day of the week going overnight
		
		if(null!=employee){
			shiftsForWeek = getAssignedShiftsForEmployeeForWeekOfMonth(employee.getId(), week, month);
			
			if(null!=shiftsForWeek){
				for(Shift scheduledShift : shiftsForWeek){
					hours+= scheduledShift.getDuration();
				}
			}
			
			//Handle the possibility of a shift on the last day of the week going overnight
			weekBefore = Util.getWeekBeforeDate(Util.getLocalDateOfDayInWeek(2018, month,  week).toString());
			shiftsForPreviousWeek = getAssignedShiftsForEmployeeForWeekOfMonth(employee.getId(), weekBefore, month);
			
			if(null!=shiftsForPreviousWeek){
				for(Shift scheduledShift : shiftsForPreviousWeek){
					if(scheduledShift.isValid() &&
							Util.getWeekOfDate(scheduledShift.getEndsLocalDate().toString())==week) {
						endTime = scheduledShift.getEndTime().split(":");
						
						hoursThisWeek = Integer.parseInt(endTime[0]);
						hours+= hoursThisWeek;
						
						minutesThisWeek = Integer.parseInt(endTime[1]);
						hours+= minutesThisWeek/60;
					}
				}
			}
		}
		else {
			throw new ProccessingException("Cannot getHoursScheduledWeekOfMonth for a null employee");
		}
		
		return hours;
	}
}