package org.cloudfoundry.samples.music.managers;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;

import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibilities;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibility;
import accessiblesolutions.accessiblescheduling.domain.Shift;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.util.Util;
import accessiblesolutions.accessiblescheduling.worker.ShiftWorker;

@Component
public class EmployeeShiftCompatibilityManager {
	private static int MAX_SHIFTS_PER_DAY = 3;
	private static int MAX_WEEKLY_WORK_DAYS = 5;
	
    private CrudRepository<Client, String> clientRepository;
    private CrudRepository<Employee, String> employeeRepository;
    
    @Autowired
    public EmployeeShiftManager employeeShiftManager;
    
    @Autowired
    EmployeeClientCompatibilityManager employeeClientCompatibilityManager;
    
    @Autowired
    public EmployeeShiftCompatibilityManager(CrudRepository<Client, String> clientRepository, CrudRepository<Employee, String> employeeRepository) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
    }
    
    public EmployeeShiftCompatibilities getValidUnfixedCompatibilities(EmployeeShiftCompatibilities compatibilities) throws CorruptDataException, ProccessingException{
		ArrayList<EmployeeShiftCompatibility> validCompatibilities = new ArrayList<EmployeeShiftCompatibility>();
    	compatibilities= getValidCompatibilities(compatibilities);
    	for(EmployeeShiftCompatibility compatibility :compatibilities.compatibilities){
    		Employee employee = null;
			employee=compatibility.getEmployee();
			if(!employee.getFixedSchedule()){
				validCompatibilities.add(compatibility);
			}
    	}
    	return new EmployeeShiftCompatibilities(validCompatibilities);
    }

    public EmployeeShiftCompatibilities getEmployeeShiftCompatibilitiesFor(Shift shift) {
		ArrayList<EmployeeShiftCompatibility> compatibility = new ArrayList<EmployeeShiftCompatibility>();
		
		ArrayList<Employee> employees = (ArrayList<Employee>) employeeRepository.findAll();
		for(Employee employee : employees){
			compatibility.add(new EmployeeShiftCompatibility(employee,shift,clientRepository.findOne(shift.getClientId())));
		}
		
		return new EmployeeShiftCompatibilities(compatibility);
	}

    public boolean getAssignable(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException {
    	Employee employee = compatibility.getEmployee();
		Shift shift = compatibility.getShift();
		
		return isAssignableFor(employee,shift);
	}
    
    public Employee getEmployeeWithMostTimeBeforeOvertimeAfterAssignment(EmployeeShiftCompatibilities compatibilities) throws CorruptDataException, ProccessingException {
    	//System.out.println("Getting the employee with the most time for " + compatibilities.compatibilities.get(0).getShift().toString());
		Employee employee = null;
		float time = 0;
		
		for(EmployeeShiftCompatibility compatibility :compatibilities.compatibilities){
			float timeUntilOvertimeForShift = compatibility.getEmployee().getMaxHoursAvailable();
			System.out.println(compatibility.getEmployee().getFirst() + " has "+ timeUntilOvertimeForShift + " hours available per week");
			System.out.println(compatibility.getShift().getDuration() + " is the length of "+compatibility.getShift().toString());
			timeUntilOvertimeForShift-=compatibility.getShift().getDuration();
			System.out.println(compatibility.getEmployee().getFirst() + " is scheduled" +getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift()) + " the week of shift " + compatibility.getShift().toString());
			timeUntilOvertimeForShift-=getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift());
			System.out.println(compatibility.getEmployee().getFirst() + " would have "+ timeUntilOvertimeForShift + " time till overtime");
			if(timeUntilOvertimeForShift>time && !getAssignmentWouldViolateAlternateWeekendsOff(compatibility)){
				System.out.println(compatibility.getEmployee().getFirst() + " at "+timeUntilOvertimeForShift+" has more time till overtime than anyone "+employee + " at " + time);
				time=timeUntilOvertimeForShift;
				employee=compatibility.getEmployee();
			}
		}
		//System.out.println(employee.getFirst() + " has more time till overtime than anyone for "+compatibilities.compatibilities.get(0).getShift().toString());
		return employee;
	}
    
	public boolean getCompatible(EmployeeShiftCompatibility compatibility) {
		Employee employee = compatibility.getEmployee();
		Shift shift = compatibility.getShift();
		
		return isCompatibleWith(employee,shift);
	}
	
//	public boolean getAssignmentWouldViolateAlternateWeekendsOff(EmployeeShiftCompatibility compatibility) {
//		boolean violatesAlternateWeekendsOff = false;
//		Shift shift = compatibility.getShift();
//		ArrayList<Shift> earlierShifts = employeeShiftManager.getShiftsForEmployeeForWeekBefore(compatibility.getEmployee().getId(),shift);
//		ArrayList<Shift> laterShifts = employeeShiftManager.getShiftsForEmployeeForWeekAfter(compatibility.getEmployee().getId(),shift);
//		
//		for(Shift selectedShift :earlierShifts){
//			if(selectedShift.isWeekend()){
//				violatesAlternateWeekendsOff = true;
//			}
//		}
//		
//		for(Shift selectedShift :laterShifts){
//			if(selectedShift.isWeekend()){
//				violatesAlternateWeekendsOff = true;
//			}
//		}
//		
//		return violatesAlternateWeekendsOff;
//	}
	
	public boolean getAssignmentWouldViolateAlternateWeekendsOff(EmployeeShiftCompatibility compatibility) throws CorruptDataException {
		Employee employee =compatibility.getEmployee();

		boolean violatesAlternateWeekendsOff = false;
		//System.out.println("checking alternate week violation for"+compatibility.getEmployee().getFirst());
		if(employee.getOffAlternateWeekends()){
			System.out.println(compatibility.getEmployee().getFirst()+" requires alternate weekends off");
			Shift shift = compatibility.getShift();
			System.out.println("Can " + compatibility.getEmployee().getFirst()+" work on "+shift.getStartDate() + " and " + shift.getEndDate());
			if(shift.isWeekend()|| shift.getStartsLocalDate().getDayOfWeek().getValue()==6
					||shift.getStartsLocalDate().getDayOfWeek().getValue()==7
					||shift.getEndsLocalDate().getDayOfWeek().getValue()==6
					||shift.getEndsLocalDate().getDayOfWeek().getValue()==7){
				LocalDate lastSaturday = null;
				LocalDate lastSunday = null;
				
				LocalDate nextSaturday = null;
				LocalDate nextSunday = null;
				
				if(shift.getStartsLocalDate().getDayOfWeek().getValue()==6){
					lastSaturday=shift.getStartsLocalDate().minusWeeks(1);
					lastSunday=shift.getStartsLocalDate().minusDays(6);
					
					nextSaturday=shift.getStartsLocalDate().plusWeeks(1);
					nextSunday=shift.getStartsLocalDate().plusDays(8);
				}
				else if(shift.getStartsLocalDate().getDayOfWeek().getValue()==7){
					lastSaturday=shift.getStartsLocalDate().minusDays(8);
					lastSunday=shift.getStartsLocalDate().minusDays(7);
					
					nextSaturday=shift.getStartsLocalDate().plusDays(6);
					nextSunday=shift.getStartsLocalDate().plusDays(7);
				}
				else if(shift.getStartsLocalDate().getDayOfWeek().getValue()==5){//test
					lastSaturday=shift.getStartsLocalDate().minusDays(6);
					lastSunday=shift.getStartsLocalDate().minusDays(5);
					
					nextSaturday=shift.getStartsLocalDate().plusDays(8);
					nextSunday=shift.getStartsLocalDate().plusDays(9);
				}
				
				ArrayList<Shift> lastMonthsShifts = employeeShiftManager.getAssignedShiftsForEmployeeForMonth(employee.getId(),shift.getStartMonth()-1);
				ArrayList<Shift> thisMonthsShifts = employeeShiftManager.getAssignedShiftsForEmployeeForMonth(employee.getId(),shift.getStartMonth());
				
				for(Shift selectedShift :lastMonthsShifts){
					if(selectedShift.getStartDate().equals(lastSaturday)
					   ||selectedShift.getStartDate().equals(lastSunday)
					   ||selectedShift.getStartDate().equals(nextSaturday)
					   ||selectedShift.getStartDate().equals(nextSunday)
					   ||selectedShift.getEndDate().equals(nextSunday)
					   ||selectedShift.getEndDate().equals(nextSaturday)
					   ||selectedShift.getEndDate().equals(lastSunday)
					   ||selectedShift.getEndDate().equals(lastSaturday)){
						violatesAlternateWeekendsOff = true;
					}
				}
				
				for(Shift selectedShift :thisMonthsShifts){
					if(selectedShift.getStartDate().equals(lastSaturday)
					   ||selectedShift.getStartDate().equals(lastSunday)
					   ||selectedShift.getStartDate().equals(nextSaturday)
					   ||selectedShift.getStartDate().equals(nextSunday)
					   ||selectedShift.getEndDate().equals(nextSunday)
					   ||selectedShift.getEndDate().equals(nextSaturday)
					   ||selectedShift.getEndDate().equals(lastSunday)
					   ||selectedShift.getEndDate().equals(lastSaturday)){
						violatesAlternateWeekendsOff = true;
					}
				}
			}
		}
		System.out.println(violatesAlternateWeekendsOff);
		return violatesAlternateWeekendsOff;
	}
	
	public boolean getResting(EmployeeShiftCompatibility compatibility) throws CorruptDataException{
		boolean resting = false;
		
		if(getAssignmentWouldViolateMaxShiftsPerDay(compatibility)){
			resting= true;
		}
		else if(getAssignmentWouldViolateMaxWeeklyWorkDays(compatibility)){
			resting=true;
		}
		else if(getAssignmentWouldViolateAlternateWeekendsOff(compatibility)){
			resting=true;
		}
		
		return resting;
	}
	public Employee getEmployeeWithMostTime(EmployeeShiftCompatibilities compatibilities) throws CorruptDataException, ProccessingException {
    	Employee employee = null;
		float time = 0;
		
		for(EmployeeShiftCompatibility compatibility :compatibilities.compatibilities){
			if(getHoursNeeded(compatibility)>time && !getAssignmentWouldViolateAlternateWeekendsOff(compatibility)){
				time=getHoursNeeded(compatibility);
				employee=compatibility.getEmployee();
			}
		}
		
		if(employee!=null)System.out.println("employee with the most time is "+employee.toString());
		return employee;
	}
	
	public boolean getAssignmentWouldViolateMaxShiftsPerDay(EmployeeShiftCompatibility compatibility){
		Employee employee = compatibility.getEmployee();
		Shift shift = compatibility.getShift();
		
		ArrayList<Shift> shiftsForDay =employeeShiftManager.getAssignedShiftsForEmployeeForDayOfMonth(employee.getId(), shift.getStartDay(), shift.getStartMonth());
		
		if(shiftsForDay.size()>=MAX_SHIFTS_PER_DAY){
			return true;
		}
		
		return false;
	}
	
	public boolean getAssignmentWouldViolateMaxWeeklyWorkDays(EmployeeShiftCompatibility compatibility) throws CorruptDataException{
		boolean violatesMaxWeeklyWorkDays = false;
		int daysWorked = 0;
		
		Employee employee = compatibility.getEmployee();
		Shift shift = compatibility.getShift();
		
		ArrayList<Shift> shiftsForWeek =employeeShiftManager.getAssignedShiftsForEmployeeForWeekOfMonth(employee.getId(), shift.getStartDay(), shift.getStartMonth());
		
		ArrayList<Boolean> workedDays = new ArrayList<Boolean>();
		for(int i =0;i<7;i++){
			workedDays.add(false);
		}

		for(Shift selectedShift:shiftsForWeek){
			int startDay = -1;
			int endDay = -1;
			
			startDay = selectedShift.getStartsLocalDate().getDayOfWeek().getValue() ==7?0:selectedShift.getStartsLocalDate().getDayOfWeek().getValue();
			workedDays.set(startDay,true);
			
			if(selectedShift.getOvernight()){
				endDay = selectedShift.getEndsLocalDate().getDayOfWeek().getValue() ==7?0:selectedShift.getEndsLocalDate().getDayOfWeek().getValue();
				workedDays.set(startDay,true);
			}
		}
		
		for(Boolean day: workedDays){
			if(day){
				daysWorked++;
			}
		}
		
		if(daysWorked>=MAX_WEEKLY_WORK_DAYS){
			violatesMaxWeeklyWorkDays=true;
		}
		
		return violatesMaxWeeklyWorkDays;
	}
	
	public boolean getAssignmentWouldIncurOvertime(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException {
		Employee employee = compatibility.getEmployee();
		Shift shift = compatibility.getShift();
		
		return getHoursScheduledWeekOfShift(employee,shift)+shift.getDuration()>employee.getMaxHours();
	}
	
	public float getHoursAfterAssignment(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException {
		return hoursNeededWeekOf(compatibility.getEmployee(),compatibility.getShift())-compatibility.getShift().getDuration()<0?0:hoursNeededWeekOf(compatibility.getEmployee(),compatibility.getShift())-compatibility.getShift().getDuration();
	}
	
	public float getHoursNeededAfterAssignment(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException {
		return hoursNeededWeekOf(compatibility.getEmployee(),compatibility.getShift())-compatibility.getShift().getDuration()<0?0:hoursNeededWeekOf(compatibility.getEmployee(),compatibility.getShift())-compatibility.getShift().getDuration();
	}

	public boolean getAssignmentWouldReachMinimum(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException {
		return compatibility.getShift().getDuration()>=getHoursNeeded(compatibility);
	}
	
	public float getHoursNeeded(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException {
		return hoursNeededWeekOf(compatibility.getEmployee(),compatibility.getShift());
	}
	
	public Employee getEmployeeWithMostTimeAfterAssignment(EmployeeShiftCompatibilities compatibilties) throws CorruptDataException, ProccessingException {
		Employee employee = null;
		float time = 0;
		
		for(EmployeeShiftCompatibility compatibility :compatibilties.compatibilities){
			if(getHoursNeededAfterAssignment(compatibility)>time){
				time=getHoursNeededAfterAssignment(compatibility);
				employee=compatibility.getEmployee();
			}
		}
		
		
		return employee;
	}
	public float getEmployeeWithMostTimeAfterAssignmentsTimeAfterAssignment(EmployeeShiftCompatibilities compatibilities) throws CorruptDataException, ProccessingException{
		float time = 0;
		
		for(EmployeeShiftCompatibility compatibility :compatibilities.compatibilities){
			if(getHoursNeededAfterAssignment(compatibility)>time){
				time=getHoursNeededAfterAssignment(compatibility);
			}
		}
		
		
		return time;
	}
	
//    public EmployeeShiftCompatibilities getValidCompatibilities(EmployeeShiftCompatibilities compatibilities){
//		ArrayList<EmployeeShiftCompatibility> validCompatibilities = new ArrayList<EmployeeShiftCompatibility>();
//
//		for(EmployeeShiftCompatibility compatibility :compatibilities.compatibilities){
//			Employee employee = null;
//			employee=compatibility.getEmployee();
//			boolean compatible = false;
//			Client client = null;
//			client = compatibility.client;
//			if(employee!=null && client!=null){
//				compatible = employeeClientCompatibilityManager.isCompatibleWith(employee,compatibility.client);
//			}
//			
//			if(compatible){
//				//System.out.println(employee.getFirst() + " is compatible with "+client.getFirst());
//				validCompatibilities.add(compatibility);
//			}
//			else{
//				//System.out.println(employee.getFirst() + " is incompatible with "+client.getFirst());
//			}
//		}
//		if(validCompatibilities.isEmpty()){
//			System.out.println("No valid compatibilites found for " + compatibilities.compatibilities.get(0).getShift());
//		}
//		else{
////				System.out.println(validCompatibilities.size()+" valid compatibilites found for " + compatibilities.compatibilities.get(0).getShift());
//		}
//		return new EmployeeShiftCompatibilities(validCompatibilities);
//	}
	
	public EmployeeShiftCompatibilities getValidCompatibilities(EmployeeShiftCompatibilities compatibilities) throws CorruptDataException, ProccessingException{
		ArrayList<EmployeeShiftCompatibility> validCompatibilities = new ArrayList<EmployeeShiftCompatibility>();
		//System.out.println("getting valid compatibilities for "+compatibilities.compatibilities.get(0).getShift().toString());
		for(EmployeeShiftCompatibility compatibility :compatibilities.compatibilities){
			Employee employee = null;
			employee=compatibility.getEmployee();
			boolean compatible = false;
			Client client = null;
			client = compatibility.client;
			if(employee!=null && client!=null){
				compatible = isValidFor(employee,compatibility.getShift());
			}
			
			if(compatible){
				//System.out.println(employee.getFirst() + " is compatible with "+client.getFirst());
				validCompatibilities.add(compatibility);
			}
			else{
				//System.out.println(employee.getFirst() + " is incompatible with "+client.getFirst());
			}
		}
		if(validCompatibilities.isEmpty()){
			System.out.println("No valid compatibilites found for " + compatibilities.compatibilities.get(0).getShift());
		}
		else{
//			System.out.println(validCompatibilities.size()+" valid compatibilites found for " + compatibilities.compatibilities.get(0).getShift());
		}
		return new EmployeeShiftCompatibilities(validCompatibilities);
	}
	    
	public boolean isValidFor(Employee employee, Shift shift) throws CorruptDataException, ProccessingException{
		boolean validity=false;
		
		if(isCompatibleWith(employee,shift)){
			if(isAssignableFor(employee,shift)){
				if(!employee.requestedOff(shift)){
					if(!employee.getInactive()){
						if(!employee.getFixedSchedule()){
							EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,shift);
							if(!getAssignmentWouldViolateAlternateWeekendsOff(compatibility)){
								if(!getResting(compatibility)){
									validity=true;
								}
							}
						}
						//else if(employee.getRequestsOvertime()){validity=true;}
					}
				}
			}
		}
		
		return validity;
	}

	public boolean isCompatibleWith(Employee employee,Shift shift){
    	if(!shift.getEvent()){
    		String clientID = shift.getClientId();
	    	Client client = null;
    		client=clientRepository.findOne(clientID);
	    	
	    	return employeeClientCompatibilityManager.isCompatibleWith(employee,client);
    	}
    	else{
    		return true;
    		
    	}
    	
    }
    
	public float hoursAvailable(Employee employee,Shift shift) throws CorruptDataException, ProccessingException{
		float hoursScheduled = getHoursScheduledWeekOfShift(employee,shift);
		
		return (hoursScheduled>employee.getMaxHours()) ? 0 : (employee.getMaxHours()-hoursScheduled);
	}
	public float hoursAvailableAfterAssignment(Employee employee,Shift shift) throws CorruptDataException, ProccessingException{
		float hoursScheduled = getHoursScheduledWeekOfShift(employee,shift);
		
		return (hoursScheduled+shift.getDuration()>employee.getMaxHours()) ? 0 : (employee.getMaxHours()-hoursScheduled-shift.getDuration());
	}
		
	public float hoursNeededWeekOf(Employee employee,Shift shift) throws CorruptDataException, ProccessingException{
		float hoursScheduled = getHoursScheduledWeekOfShift(employee,shift);
		
		return (hoursScheduled>employee.getMinHours()) ? 0 : (employee.getMinHours()-hoursScheduled);
	}
	
	public boolean isAssignableFor(Employee employee,Shift shift) throws CorruptDataException, ProccessingException{
    	if(!employee.requestedOff(shift)){
    		if(isUnassignedFor(employee,shift)){
    			if(!isAvailableFor(employee,shift)){
    				return false;
    			}
    		}
    		else{
    			return false;
    		}
    	}
    	else{
    		return false;
    	}
    	return true;//Ya ran the gauntlet
    }
	
	public boolean isUnassignedFor(Employee employee, Shift shift) throws CorruptDataException, ProccessingException{
		boolean unassigned= true;

		ArrayList<Shift> shiftsForWeek = employeeShiftManager.getAssignedShiftsForEmployeeForWeekOfShift(employee.getId(), shift);
    	
		for(Shift scheduledShift : shiftsForWeek){
			if(ShiftWorker.isAlmostOverlapping(shift, scheduledShift)){
				unassigned=false;
			}
		}

    	return unassigned;
	}
	
	public boolean isAvailableFor(Employee employee, Shift shift) throws CorruptDataException{
		LocalDate date = LocalDate.of(shift.getStartYear(), shift.getStartMonth(), shift.getStartDay());
		DayOfWeek day = date.getDayOfWeek();
		int dayInt = day.getValue();
		if(dayInt==7){
			dayInt=0;
		}
		if(null!=employee && null!=shift){
			System.out.println("is "+employee.getFirst()+" available for " +shift.toString());
		}
		if(!shift.getOvernight()){
			System.out.println("available on the day");
			if(employee.getDaysAvailable()[dayInt]){
				boolean[] availability = employee.getAvailabilityFor(dayInt);
				
				String start = shift.getStartTime();
				int startHour = (int) Integer.parseInt(start.split(":")[0]);
				
				String end = shift.getEndTime();
				int endHour = (int) Integer.parseInt(end.split(":")[0]);
				int endMinute = (int) Integer.parseInt(end.split(":")[1]);
				
				for(int hourCursor = startHour;hourCursor<=endHour;hourCursor++){
					if(hourCursor!=endHour){
    					if(!availability[hourCursor]){
    						return false;
    					}
					}
					else if(endMinute!=0){
						if(!availability[hourCursor]){
    						return false;
    					}
					}
				}
			}
			else{
				return false;
			}
		}
		else{
			System.out.println("checking availability for an overnight shift");
			if(employee.getDaysAvailable()[dayInt]){
				boolean[] availability = employee.getAvailabilityFor(dayInt);
				
				String start = shift.getStartTime();
				int startHour = (int) Integer.parseInt(start.split(":")[0]);
				
				for(int hourCursor = startHour;hourCursor<24;hourCursor++){
					if(!availability[hourCursor]){
						return false;
					}
				}
				
				dayInt++;
				if(dayInt==7){
    				dayInt=0;
    			}
				
				String end = shift.getEndTime();
				int endHour = (int) Integer.parseInt(end.split(":")[0]);
				int endMinute = (int) Integer.parseInt(end.split(":")[1]);
				
				if(employee.getDaysAvailable()[dayInt]){
					for(int hourCursor =0;hourCursor<endHour;hourCursor++){
    					if(hourCursor!=endHour){
	    					if(!availability[hourCursor]){
	    						return false;
	    					}
    					}
    					else if(endMinute!=0){
							if(!availability[hourCursor]){
	    						return false;
	    					}
    					}
    				}
				}
				else{
    				return false;
    			}
			}
			else{
				return false;
			}
		}
		return true;
	}
	
	 
	/** Returns the number of hours scheduled the week of the start date for the provided Shift.
	 * 
	 * @param employee
	 * @param shift
	 * @return float hours
	 * @throws ProccessingException Coding failure
	 * @throws CorruptDataException The Shift provided is invalid
	 */
	public float getHoursScheduledWeekOfShift(Employee employee,Shift shift) throws ProccessingException, CorruptDataException{
		float hours = 200;
		
		if(null!=employee && null !=shift){
			if(shift.isValid()) {
				int week = Util.getWeekOfDate(shift.getStartDate());
				
				try {
					System.out.println("Calling");
					hours = employeeShiftManager.getHoursScheduledWeekOfMonth(employee,week,shift.getStartMonth());
				} catch (CorruptDataException e) {
					throw new ProccessingException(e);
				}
			}
			else {
				throw new CorruptDataException("Shift is invalid when trying to get hours schedule for week of shift");
			}
		}
		else {
			throw new ProccessingException("Cannot get hours scheduled for a null employee or shift");
		}
		
		return hours;
	}
}