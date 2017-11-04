package org.cloudfoundry.samples.music.managers;
import java.time.LocalDate;
import java.util.ArrayList;

import accessiblesolutions.accessiblescheduling.domain.Shift;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRepository;
import org.cloudfoundry.samples.music.worker.ShiftWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.util.Util;

@Component
public class EmployeeShiftManager {
    private CrudRepository<Employee, String> employeeRepository;
    private MongoShiftRepository shiftRepository;
    
    @Autowired
    public EmployeeShiftManager(CrudRepository<Employee, String> employeeRepository, MongoShiftRepository shiftRepository) {
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
    }
    
    public ArrayList<Shift> getShiftsForEmployeeForWeekAfter(String employeeId, Shift shift) throws CorruptDataException {
		LocalDate weekAftersDate = shift.getStartsLocalDate().plusWeeks(1);
		int weekAfter = Util.getWeekOfDate(weekAftersDate.getYear()+"-"+weekAftersDate.getMonth().getValue()+"-"+weekAftersDate.getDayOfMonth());
		
		ArrayList<Shift> shifts = getAssignedShiftsForEmployeeForWeekOfMonth(employeeId,weekAfter,weekAftersDate.getMonthValue());
		
		return shifts;
	}
  	
    //If the shift in question starts saturday night and ends sunday info for the week of saturday is returned
  	public ArrayList<Shift> getAssignedShiftsForEmployeeForWeekOfShift(String employeeId, Shift shift){
  		return getAssignedShiftsForEmployeeForWeekOfMonth(employeeId,shift.getStartWeek(), shift.getStartMonth());
  	}
  	
  	
  	public ArrayList<Shift> getAssignedShiftsForEmployeeForWeekOfMonth(String employeeId, int week, int month){
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
  	
  	public ArrayList<Shift> getAssignedShiftsForEmployeeForDayOfMonth(String employeeId, int day, int month){
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
  	
  	public ArrayList<Shift> getAssignedShiftsForEmployeeForMonth(String employeeId, int month){
		ArrayList<Shift> assignedShiftsForEmployeeForMonth = new ArrayList<Shift>();
		ArrayList<Shift> assignedOvernightShiftsForEmployeForTheLastDayOfMonthBefore = new ArrayList<Shift>();
		
		for(Shift shift: shiftRepository.findByStartMonth(month)){
			if(shift!=null&& shift.getStaffId()!=null&&shift.getStaffId().equals(employeeId)){
				assignedShiftsForEmployeeForMonth.add(shift);
			}
		}
		
//		assignedOvernightShiftsForEmployeForTheLastDayOfMonthBefore= getAssignedOvernightShiftsForEmployeForTheLastDayOfMonthBefore(employeeId,month);
//		
//		if(!assignedOvernightShiftsForEmployeForTheLastDayOfMonthBefore.isEmpty()){
//			assignedShiftsForEmployeeForMonth.addAll(assignedOvernightShiftsForEmployeForTheLastDayOfMonthBefore);
//		}
		
		return assignedShiftsForEmployeeForMonth;
	}
  

  	public ArrayList<Shift> getAssignedOvernightShiftsForEmployeForTheLastDayOfMonthBefore(String employeeId, int month) throws CorruptDataException{
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
  	
	public ArrayList<Shift> getAssignedOvernightShiftsForEmployeForTheLastDayOfMonth(String employeeId, int month) throws CorruptDataException{
		ArrayList<Shift> assignedOvernightShiftsForEmployeForTheLastDayOfMonth = new ArrayList<Shift>();
		
		ArrayList<Shift> shifts = getAssignedOvernightShiftsForEmployeForMonth(employeeId,month);
		
		assignedOvernightShiftsForEmployeForTheLastDayOfMonth = ShiftWorker.getShiftsStartingTheLastDayOfMonth(shifts, month);
		
		return assignedOvernightShiftsForEmployeForTheLastDayOfMonth;
	}
	

	public ArrayList<Shift> getAssignedOvernightShiftsForEmployeForMonth(String employeeId, int month) throws CorruptDataException{
		ArrayList<Shift> assignedOvernightShiftsForEmployeeForMonth = new ArrayList<Shift>();
		
		ArrayList<Shift> shifts = getAssignedShiftsForEmployeeForMonth(employeeId,month);
		
		assignedOvernightShiftsForEmployeeForMonth = ShiftWorker.getOvernightShifts(shifts);
		
		return assignedOvernightShiftsForEmployeeForMonth;
	}
  	
  	public ArrayList<Shift> getShiftsForEmployeeForWeekBefore(String employeeId,Shift shift) throws CorruptDataException {
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