package org.cloudfoundry.samples.music.managers;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibilities;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibility;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.Availability;
import accessiblesolutions.accessiblescheduling.to.ScheduleOptions;
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
	public
    EmployeeClientCompatibilityManager employeeClientCompatibilityManager;
    
    @Autowired
    public EmployeeShiftCompatibilityManager(CrudRepository<Client, String> clientRepository, CrudRepository<Employee, String> employeeRepository) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
    }
    

    /** Would assigning this employee this shift would put them over their maximum requested hours for the week?
	 * 
	 * @param compatibility An employee and shift for consideration in assignment
	 * @return boolean Assigning this employee this shift would put them over their maximum requested hours for the week
	 * @throws ProccessingException Coding failure, null employee,shift or compatibility
	 * @throws CorruptDataException The Shift provided is invalid
	 * @Tested
	 */
	public boolean getAssignmentWouldIncurOvertime(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException {
		boolean incursOvertime;
		
		if(null!=compatibility) {
			Employee employee = compatibility.getEmployee();
			Shift shift = compatibility.getShift();
			
			if(null!=shift && null!=employee) {
				if(shift.isValid()) {
					incursOvertime=getHoursScheduledWeekOfShift(employee,shift)+shift.getDuration()>employee.getMaxHours();
				}
				else{
					throw new CorruptDataException("Invalid shift provided in getAssignmentWouldIncurOvertime");
				}
			}
			else {
				throw new ProccessingException("Null Shift or Employee provided for assesment to getAssignmentWouldIncurOvertim");
			}
		}
		else {
			throw new ProccessingException("Null EmployeeShiftCompatibility provided for assesment to getAssignmentWouldIncurOvertim");
		}
		
		return incursOvertime;
	}

  /**Return if the employee would reach their minimum requested
	 * hours if they were to be assigned the provided shift
	 * 
	 * @param EmployeeShiftCompatibility An employee, shift, and client for consideration
	 * @return boolean the employee would satisfy their minimum hour request if assigned this shift
	 * @throws ProccessingException Coding failure, null employee,shift or compatibility
	 * @throws CorruptDataException The Shift provided is invalid
	 * @Tested
	 */
	public boolean getAssignmentWouldReachMinimum(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException {
		if(null==compatibility || null == compatibility.getShift() || null == compatibility.getEmployee()) {
			throw new ProccessingException("Null EmployeeShiftCompatibility , Shift, or Employee provided for assesment to getAssignmentWouldReachMinimum");
		}
		else if(!compatibility.getShift().isValid()) {
			throw new CorruptDataException("Shift is invalid when trying to getAssignmentWouldReachMinimum");
		}
		
		return getHoursNeededAfterAssignment(compatibility)==0;
	}
    
    /**Returns true if the employee requests alternate weekends off and is currently scheduled
	 * to work a shift either the weekend before or after this shift (if this is a weekend shift).
	 * 
	 * @param compatibility an employee and valid shift
	 * @return boolean true when employee and shift are incompatible
	 * @throws CorruptDataException invalid shift
	 * @throws ProccessingException null compatibility, employee, or shift
	 * @Tested
	 */
	public boolean getAssignmentWouldViolateAlternateWeekendsOff(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException {
		Employee employee = null;
		Shift shift = null; 
		boolean violatesAlternateWeekendsOff = false;
		
		if(null==compatibility ){
			throw new ProccessingException("Null compatibility provided to getAssignmentWouldViolateAlternateWeekendsOff");
		}
		
		employee = compatibility.getEmployee();
		shift = compatibility.getShift();
		
		if(null==employee || null==shift) {
			throw new ProccessingException("Null employee or shift provided to getAssignmentWouldViolateAlternateWeekendsOff");
		}
		else if(!shift.isValid()) {
			throw new CorruptDataException("Invalid shift provided to getAssignmentWouldViolateAlternateWeekendsOff");
		}
		
		if(employee.getOffAlternateWeekends()){
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
				int thisMonth=shift.getStartMonth();
				int lastMonth =thisMonth-1;
				if(lastMonth<1) {
					lastMonth=12;
				}
				
				int nextMonth=thisMonth+1;
				if(nextMonth>12) {
					nextMonth=1;
				}
				
				ArrayList<Shift> lastMonthsShifts = employeeShiftManager.getAssignedShiftsForEmployeeForMonth(employee.getId(),lastMonth);
				ArrayList<Shift> thisMonthsShifts = employeeShiftManager.getAssignedShiftsForEmployeeForMonth(employee.getId(),thisMonth);
				ArrayList<Shift> nextMonthsShifts = employeeShiftManager.getAssignedShiftsForEmployeeForMonth(employee.getId(),nextMonth);
				
				for(Shift selectedShift :lastMonthsShifts){
					if(selectedShift.getStartDate().equals(lastSaturday.toString())
					   ||selectedShift.getStartDate().equals(lastSunday.toString())
					   ||selectedShift.getStartDate().equals(nextSaturday.toString())
					   ||selectedShift.getStartDate().equals(nextSunday.toString())
					   ||selectedShift.getEndDate().equals(nextSunday.toString())
					   ||selectedShift.getEndDate().equals(nextSaturday.toString())
					   ||selectedShift.getEndDate().equals(lastSunday.toString())
					   ||selectedShift.getEndDate().equals(lastSaturday.toString())){
						violatesAlternateWeekendsOff = true;
					}
				}
				
				for(Shift selectedShift :thisMonthsShifts){
					if(selectedShift.getStartDate().equals(lastSaturday.toString())
					   ||selectedShift.getStartDate().equals(lastSunday.toString())
					   ||selectedShift.getStartDate().equals(nextSaturday.toString())
					   ||selectedShift.getStartDate().equals(nextSunday.toString())
					   ||selectedShift.getEndDate().equals(nextSunday.toString())
					   ||selectedShift.getEndDate().equals(nextSaturday.toString())
					   ||selectedShift.getEndDate().equals(lastSunday.toString())
					   ||selectedShift.getEndDate().equals(lastSaturday.toString())){
						violatesAlternateWeekendsOff = true;
					}
				}
				
				for(Shift selectedShift :nextMonthsShifts){
					if(selectedShift.getStartDate().equals(lastSaturday.toString())
					   ||selectedShift.getStartDate().equals(lastSunday.toString())
					   ||selectedShift.getStartDate().equals(nextSaturday.toString())
					   ||selectedShift.getStartDate().equals(nextSunday.toString())
					   ||selectedShift.getEndDate().equals(nextSunday.toString())
					   ||selectedShift.getEndDate().equals(nextSaturday.toString())
					   ||selectedShift.getEndDate().equals(lastSunday.toString())
					   ||selectedShift.getEndDate().equals(lastSaturday.toString())){
						violatesAlternateWeekendsOff = true;
					}
				}
			}
		}
		
		return violatesAlternateWeekendsOff;
	}

	/**Return if assignment would violate max shifts per day
	 * 
	 * @param EmployeeShiftCompatibility An employee, shift, and client for consideration
	 * @return boolean if assignment would violate max shifts per day
	 * @throws ProccessingException Coding failure, null employee,shift or compatibility
	 * @throws CorruptDataException The Shift provided is invalid
	 * @Tested
	 */
	public boolean getAssignmentWouldViolateMaxShiftsPerDay(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException{
		boolean violatesMaxShiftsPerDay = true;
		
		if(null!=compatibility) {
			Employee employee = compatibility.getEmployee();
			Shift shift = compatibility.getShift();
			
			if(null!=shift && null!=employee) {
				if(shift.isValid()) {
					ArrayList<Shift> shiftsForDay =employeeShiftManager.getAssignedShiftsForEmployeeForDayOfMonth(employee.getId(), shift.getStartDay(), shift.getStartMonth());
					
					if(!(shiftsForDay.size()>=MAX_SHIFTS_PER_DAY)){
						violatesMaxShiftsPerDay=false;
					}
				}
				else{
					throw new CorruptDataException("Invalid shift provided in getAssignmentWouldViolateMaxShiftsPerDay");
				}
			}
			else {
				throw new ProccessingException("Null Shift or Employee provided for assesment to getAssignmentWouldViolateMaxShiftsPerDay");
			}
		}
		else {
			throw new ProccessingException("Null EmployeeShiftCompatibility provided for assesment to getAssignmentWouldViolateMaxShiftsPerDay");
		}
		
		return violatesMaxShiftsPerDay;
	}
	
	/** Returns if assignment would violate max weekly work days rules.
	 * 
	 * @param EmployeeShiftCompatibility An employee, shift, and client for consideration
	 * @return boolean if assignment would violate max work days per week
	 * @throws CorruptDataException The Shift provided is invalid
	 * @throws ProccessingException Coding failure, null employee,shift or compatibility
	 * @Tested
	 */
	public boolean getAssignmentWouldViolateMaxWeeklyWorkDays(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException{
		boolean violatesMaxWeeklyWorkDays = false;
		
		if(null!=compatibility) {
			Employee employee = compatibility.getEmployee();
			Shift shift = compatibility.getShift();
			
			if(null!=shift && null!=employee) {
				if(shift.isValid()) {
					int daysWorked = 0;
					
					ArrayList<Shift> shiftsForWeek =employeeShiftManager.getAssignedShiftsForEmployeeForWeekOfMonth(employee.getId(), shift.getStartWeek(), shift.getStartMonth());

					ArrayList<Boolean> workedDays = new ArrayList<Boolean>();
					for(int i =0;i<7;i++){
						workedDays.add(false);
					}
					
					int startDay = -1;
					int endDay = -1;
					
					startDay = shift.getStartsLocalDate().getDayOfWeek().getValue() ==7?0:shift.getStartsLocalDate().getDayOfWeek().getValue();
					
					workedDays.set(startDay,true);
					
					if(shift.getOvernight()){
						endDay = shift.getEndsLocalDate().getDayOfWeek().getValue() ==7?0:shift.getEndsLocalDate().getDayOfWeek().getValue();
						workedDays.set(endDay,true);
					}
					
					for(Shift selectedShift:shiftsForWeek){
						startDay = -1;
						endDay = -1;
						
						startDay = selectedShift.getStartsLocalDate().getDayOfWeek().getValue() ==7?0:selectedShift.getStartsLocalDate().getDayOfWeek().getValue();
						
						if(!workedDays.contains(startDay)) {
							workedDays.set(startDay,true);
						}
						
						
						if(selectedShift.getOvernight()){
							endDay = selectedShift.getEndsLocalDate().getDayOfWeek().getValue() ==7?0:selectedShift.getEndsLocalDate().getDayOfWeek().getValue();
							
							if(!workedDays.contains(endDay)) {
								workedDays.set(endDay,true);
							}
						}
					}
					
					for(Boolean day: workedDays){
						if(day){
							daysWorked++;
						}
					}
					
					if(daysWorked>MAX_WEEKLY_WORK_DAYS){
						violatesMaxWeeklyWorkDays=true;
					}
				}
				else{
					throw new CorruptDataException("Invalid shift provided in getAssignmentWouldViolateMaxWeeklyWorkDays");
				}
			}
			else {
				throw new ProccessingException("Null Shift or Employee provided for assesment to getAssignmentWouldViolateMaxWeeklyWorkDays");
			}
		}
		else {
			throw new ProccessingException("Null EmployeeShiftCompatibility provided for assesment to getAssignmentWouldViolateMaxWeeklyWorkDays");
		}
		
		return violatesMaxWeeklyWorkDays;
	}
	
	/**Returns if this employee allowed to work this shift and with any client covered by it.
     * Clients requiring medpass must have employees that are medpass certified.
     * Clients must have staff of the proper gender.
     * Clients must not be paired with smokers upon request.
     * Clients with cats must not be paired with employees who have cat allergies.
     * Clients must be paired with signing staff when required.
     * Clients and employees must be properly aligned with custom requirements.
     * 
	 * @param EmployeeShiftCompatibility
	 * @return boolean true for valid events. If the employee is allowed to work with the client this shift is scheduled for
	 * @throws ProccessingException Coding failure, null compatibility, employee, shift or client
	 * @throws CorruptDataException The Shift provided is invalid
	 * @Tested
	 */
	public boolean getCompatible(EmployeeShiftCompatibility compatibility) throws ProccessingException, CorruptDataException {
		Employee employee = null;
		Shift shift = null;
		
		if(null==compatibility) {
			throw new ProccessingException("Null compatibility provided to isCompatibleWith");
		}
		
		employee= compatibility.getEmployee();
		shift = compatibility.getShift();
		
		return isCompatibleWith(employee,shift);
	}
	
	/**Returns an EmployeeShiftCompatibility for every employee and the provided shift and the shifts client.
     * A null client is provided for all event shifts.
     * Returns Empty when no employees exist
     * 
     * @param shift a valid non-event shift 
     * @return EmployeeShiftCompatibilities An EmployeeShiftCompatibility for every employee and the provided shift
     * @throws ProccessingException null shift
     * @throws CorruptDataException invalid shift
     * @Tested
     */
    public EmployeeShiftCompatibilities getEmployeeShiftCompatibilitiesForShift(Shift shift) throws ProccessingException, CorruptDataException {
    	if(null==shift) {
    		throw new ProccessingException("Null Shift provided for assesment to getEmployeeShiftCompatibilitiesForShift");
    	}
    	else if(!shift.isValid()) {
    		throw new CorruptDataException("Invalid Shift provided for assesment to getEmployeeShiftCompatibilitiesForShift");
    	}
    	
		ArrayList<EmployeeShiftCompatibility> compatibility = new ArrayList<EmployeeShiftCompatibility>();
		Client client = null;
		ArrayList<Employee> employees = (ArrayList<Employee>) employeeRepository.findAll();

		if(!shift.getEvent()) {
			client = clientRepository.findOne(shift.getClientId());
		}

		if(null!=employees) {
			for(Employee employee : employees){
				compatibility.add(new EmployeeShiftCompatibility(employee,shift,client));
			}
		}
		
		return new EmployeeShiftCompatibilities(compatibility);
	}
    
     /** 
     * @param compatibilities Employees coupled with shifts they are being considered for
     * @return employee has the most time before their min hours is met
     * @throws CorruptDataException invalid shift
     * @throws ProccessingException null compatibility, employee, or shift
     */
	public Employee getEmployeeWithFewestHours(EmployeeShiftCompatibilities compatibilities) throws CorruptDataException, ProccessingException {
    	Employee employee = null;
		float time = 9001;
		
		if(null==compatibilities || compatibilities.compatibilities==null) {
			throw new ProccessingException("Null EmployeeShiftCompatibilities provided for assesment to getEmployeeWithFewestHours");
		}
		
		for(EmployeeShiftCompatibility compatibility :compatibilities.compatibilities){
			if(getHoursNeeded(compatibility)<=time){
				time=getHoursNeeded(compatibility);
				employee=compatibility.getEmployee();
			}
		}
		
		return employee;
	}
	
    /**Returns the employee of the group that has the most time until min hours is met
     * for the week of their shift
     * 
     * @param compatibilities Employees coupled with shifts they are being considered for
     * @return employee has the most time before their min hours is met
     * @throws CorruptDataException invalid shift
     * @throws ProccessingException null compatibility, employee, or shift
     * @Tested
     */
	public Employee getEmployeeWithMostNeeded(EmployeeShiftCompatibilities compatibilities) throws CorruptDataException, ProccessingException {
    	Employee employee = null;
		float time = 0;
		
		if(null==compatibilities || compatibilities.compatibilities==null) {
			throw new ProccessingException("Null EmployeeShiftCompatibilities provided for assesment to getEmployeeWithMostTime");
		}
		
		for(EmployeeShiftCompatibility compatibility :compatibilities.compatibilities){
			if(getHoursNeeded(compatibility)>=time){
				time=getHoursNeeded(compatibility);
				employee=compatibility.getEmployee();
			}
		}
		
		return employee;
	}
	
	 /**Returns the employee of the group that would have the most time until min hours is met
     * for the week of their shift if they were assigned the shift they are being considered for
     * 
     * @param compatibilities Employees coupled with shifts they are being considered for
     * @return employee has the most time before their min hours is met
     * @throws CorruptDataException invalid shift
     * @throws ProccessingException null compatibility, employee, or shift
     * @Tested
     */
	public Employee getEmployeeWithMostTimeAfterAssignment(EmployeeShiftCompatibilities compatibilities) throws CorruptDataException, ProccessingException {
		Employee employee = null;
		float time = 0;
		
		if(null==compatibilities || compatibilities.compatibilities==null) {
			throw new ProccessingException("Null EmployeeShiftCompatibilities provided for assesment to getEmployeeWithMostTime");
		}
		
		for(EmployeeShiftCompatibility compatibility :compatibilities.compatibilities){
			if(getHoursNeededAfterAssignment(compatibility)>=time){
				time=getHoursNeededAfterAssignment(compatibility);
				employee=compatibility.getEmployee();
			}
		}
		
		return employee;
	}
	
	/**Returns the employee of the group that has the most time until max hours are met
     * for the week of their shift if they were assigned the shift they are being considered for
     * 
     * @param compatibilities Employees coupled with shifts they are being considered for
     * @return employee has the most time before their max hours is met or null if none is outside overtime
     * @throws CorruptDataException invalid shift
     * @throws ProccessingException null compatibility, employee, or shift
     */
    public Employee getEmployeeWithMostTimeSafely(EmployeeShiftCompatibilities compatibilities) throws CorruptDataException, ProccessingException {
		Employee employee = null;
		float time = 0;
		float timeUntilOvertimeForShift = 0;
		
		if(null==compatibilities || compatibilities.compatibilities==null) {
			throw new ProccessingException("Null EmployeeShiftCompatibilities provided for assesment to getEmployeeWithMostTime");
		}
		
		for(EmployeeShiftCompatibility compatibility :compatibilities.compatibilities){
			if(null == compatibility || null == compatibility.getShift() || null==compatibility.getEmployee()) {
				throw new ProccessingException("Null EmployeeShiftCompatibility provided for assesment to getEmployeeWithMostTime");
			}
			
			timeUntilOvertimeForShift = compatibility.getEmployee().getMaxHoursAvailable();
			timeUntilOvertimeForShift-= getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift());
			
			if(timeUntilOvertimeForShift>time){
				time=timeUntilOvertimeForShift;
				employee=compatibility.getEmployee();
			}
		}
		
		return employee;
	}
    
	 /**Returns the employee of the group that would have the most time until max hours are met
     * for the week of their shift if they were assigned the shift they are being considered for
     * 
     * @param compatibilities Employees coupled with shifts they are being considered for
     * @return employee has the most time before their max hours is met
     * @throws CorruptDataException invalid shift
     * @throws ProccessingException null compatibility, employee, or shift
     * @Tested
     */
    public Employee getEmployeeWithMostTimeBeforeOvertimeAfterAssignment(EmployeeShiftCompatibilities compatibilities) throws CorruptDataException, ProccessingException {
		Employee employee = null;
		float time = 0;
		float timeUntilOvertimeForShift = 0;
		
		if(null==compatibilities || compatibilities.compatibilities==null) {
			throw new ProccessingException("Null EmployeeShiftCompatibilities provided for assesment to getEmployeeWithMostTime");
		}
		
		for(EmployeeShiftCompatibility compatibility :compatibilities.compatibilities){
			if(null == compatibility || null == compatibility.getShift() || null==compatibility.getEmployee()) {
				throw new ProccessingException("Null EmployeeShiftCompatibility provided for assesment to getEmployeeWithMostTime");
			}
			
			timeUntilOvertimeForShift = compatibility.getEmployee().getMaxHoursAvailable();
			timeUntilOvertimeForShift-= compatibility.getShift().getDuration();
			timeUntilOvertimeForShift-= getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift());
			
			if(timeUntilOvertimeForShift>=time){
				time=timeUntilOvertimeForShift;
				employee=compatibility.getEmployee();
			}
		}
		
		return employee;
	}

	/**Return the number of hours below minimum the provided employee is for the week of this shift
	 * 
	 * @param EmployeeShiftCompatibility An employee, shift, and client for consideration
	 * @return float number of hours below minimum
	 * @throws ProccessingException Coding failure, null employee,shift or compatibility
	 * @throws CorruptDataException The Shift provided is invalid
	 * @Tested
	 */
	public float getHoursNeeded(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException {
		if(null==compatibility) {
			throw new ProccessingException("Null EmployeeShiftCompatibility provided for assesment to getHoursNeeded");
		}
		
		return hoursNeededWeekOfShift(compatibility.getEmployee(),compatibility.getShift());
	}
	
	/**Return the number of hours below minimum the provided employee is for the week of this shift
	 * minus the duration of the provided shift. This is how many hours the employee would still need
	 * to reach their minimum requested hours if they were to be assigned the provided shift.
	 * 
	 * @param EmployeeShiftCompatibility An employee, shift, and client for consideration
	 * @return number of hours below minimum the employee would be the week 
	 * 		   of the shift if they were to be assigned it
	 * @throws ProccessingException Coding failure, null employee,shift or compatibility
	 * @throws CorruptDataException The Shift provided is invalid
	 * @Tested
	 */
	public float getHoursNeededAfterAssignment(EmployeeShiftCompatibility compatibility) throws CorruptDataException, ProccessingException {
		if(null==compatibility || null == compatibility.getShift() || null == compatibility.getEmployee()) {
			throw new ProccessingException("Null EmployeeShiftCompatibility , Shift, or Employee provided for assesment to getHoursNeededAfterAssignment");
		}
		
		return hoursNeededWeekOfShift(compatibility.getEmployee(),compatibility.getShift())-compatibility.getShift().getDuration()<0?0:hoursNeededWeekOfShift(compatibility.getEmployee(),compatibility.getShift())-compatibility.getShift().getDuration();
	}
	
	/** Returns the number of hours scheduled the week of the start date for the provided Shift.
	 * 
	 * @param employee
	 * @param shift
	 * @return float hours
	 * @throws ProccessingException Coding failure, null employee or shift
	 * @throws CorruptDataException The Shift provided is invalid
	 * @Tested
	 */
	public float getHoursScheduledWeekOfShift(Employee employee,Shift shift) throws ProccessingException, CorruptDataException{
		float hours = 200;
		
		if(null!=employee && null !=shift){
			if(shift.isValid()) {
				int week = Util.getWeekOfDate(shift.getStartDate());
				
				try {
					//System.out.println("getting hours");
					hours = employeeShiftManager.getHoursScheduledWeekOfMonth(employee,week,shift.getStartMonth());
					//System.out.println("got "+hours+" hours");
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
	
	
	/**Returns true if assignment would violate max shifts per day, week, or alternate weekends off
	 * 
	 * @param compatibility
	 * @param options 
	 * @return
	 * @throws CorruptDataException invalid shift
	 * @throws ProccessingException null compatibility, employee, or shift
	 * @Tested
	 */
	public boolean getResting(EmployeeShiftCompatibility compatibility, ScheduleOptions options) throws CorruptDataException, ProccessingException{
		boolean resting = false;
		Employee employee = null;
		Shift shift = null; 
		
		if(null==compatibility || null == options){
			throw new ProccessingException("Null options or compatibility provided to getResting");
		}
		
		employee = compatibility.getEmployee();
		shift = compatibility.getShift();
		
		if(null==employee || null==shift) {
			throw new ProccessingException("Null employee or shift provided to getResting");
		}
		else if(!shift.isValid()) {
			throw new CorruptDataException("Invalid shift provided to getResting");
		}
		//System.out.println("options.isWeeklyMax():"+options.isWeeklyMax());
		if(options.isDailyMax() && getAssignmentWouldViolateMaxShiftsPerDay(compatibility)){
			//System.out.println("violates max daily");
			resting= true;
		}
		else if(options.isWeeklyMax() &&getAssignmentWouldViolateMaxWeeklyWorkDays(compatibility)){
			//System.out.println("violates max weekly");
			resting=true;
		}
		else if(getAssignmentWouldViolateAlternateWeekendsOff(compatibility)){
			//System.out.println("violates alternate weekends");
			resting=true;
		}
		//System.out.println("resting:"+resting);
		return resting;
	}
	    
	/**Returns All Valid pairings of employees and shifts
	 * 
	 * @param compatibilities A group of EmployeeShiftCompatibility objects
	 * @return ArrayList<EmployeeShiftCompatibility> EmployeeShiftCompatibility All Valid pairings
	 * @throws CorruptDataException invalid shift
	 * @throws ProccessingException null compatibilities, compatibility, employee, client, or shift
	 * @Tested
	 */
	public EmployeeShiftCompatibilities getValidCompatibilities(EmployeeShiftCompatibilities compatibilities,ScheduleOptions options) throws CorruptDataException, ProccessingException{
		Employee employee = null;
		Shift shift = null;
		ArrayList<EmployeeShiftCompatibility> validCompatibilities = new ArrayList<EmployeeShiftCompatibility>();
		
		if(null==compatibilities||null == compatibilities.compatibilities) {
			throw new ProccessingException("Null compatibilities provided to getValidCompatibilities");
		}
		
		for(EmployeeShiftCompatibility compatibility :compatibilities.compatibilities){
			if(null==compatibility) {
				throw new ProccessingException("Null compatibility provided to getValidCompatibilities");
			}
			
			employee=null;
			employee=compatibility.getEmployee();
			
			shift = null;
			shift = compatibility.getShift();

			if(null == employee || null == shift) {
				throw new ProccessingException("Null shift or employee provided to getValidCompatibilities");
			}
			//System.out.println("shift is checking validity of options:"+shift.toString()+"  employee:"+employee.toString());
			if(isValidFor(employee,shift,options)){
				validCompatibilities.add(compatibility);
			}
		}
		
		return new EmployeeShiftCompatibilities(validCompatibilities);
	}

	/**Return the number of hours below maximum for the week the provided employee 
	 * will be after being assigned this shift
	 * 
	 * @param employee 
	 * @param shift A fully formed shift that returns true with shift.isValid
	 * @return float number of hours below minimum
	 * @throws ProccessingException Coding failure, null employee or shift
	 * @throws CorruptDataException The Shift provided is invalid
	 * @Tested
	 */
	public float hoursAvailableAfterAssignment(Employee employee,Shift shift) throws CorruptDataException, ProccessingException{
		float hoursScheduled = getHoursScheduledWeekOfShift(employee,shift);
		float hoursAfterAssignment = (hoursScheduled+shift.getDuration()>employee.getMaxHours()) ? 0 : (employee.getMaxHours()-hoursScheduled-shift.getDuration());
		
		return hoursAfterAssignment;
	}
    
	/**Return the number of hours below maximum the provided employee is for the week of this shift
	 * 
	 * @param employee 
	 * @param shift A fully formed shift that returns true with shift.isValid
	 * @return float number of hours below minimum
	 * @throws ProccessingException Coding failure, null employee or shift
	 * @throws CorruptDataException The Shift provided is invalid
	 * @Tested
	 */
	public float hoursAvailableWeekOfShift(Employee employee,Shift shift) throws CorruptDataException, ProccessingException{
		float hoursScheduled = getHoursScheduledWeekOfShift(employee,shift);
		float hoursAvailable = (hoursScheduled>employee.getMaxHours()) ? 0 : (employee.getMaxHours()-hoursScheduled);

		return hoursAvailable;
	}
	
	/**Return the number of hours below minimum the provided employee is for the week of this shift
	 * 
	 * @param employee 
	 * @param shift A fully formed shift that returns true with shift.isValid
	 * @return float number of hours below minimum
	 * @throws ProccessingException Coding failure, null employee or shift
	 * @throws CorruptDataException The Shift provided is invalid
	 * @Tested
	 */
	public float hoursNeededWeekOfShift(Employee employee,Shift shift) throws CorruptDataException, ProccessingException{
		float hoursScheduled = getHoursScheduledWeekOfShift(employee,shift);
		float hoursNeeded = (hoursScheduled>employee.getMinHours()) ? 0 : (employee.getMinHours()-hoursScheduled);
		
		return hoursNeeded;
	}
		
	/** Available, Unassigned, not requested off. 
	 * For this shift the employee hasn't requested off, 
	 * isn't scheduled to be working elsewhere, 
	 * and is generally willing to work this time of day for the shifts day of the week.
	 * 
	 * @param employee
	 * @param shift a valid shift
	 * @param options 
	 * @return boolean Available, Unassigned, not requested off
	 * @throws CorruptDataException invalid shift
	 * @throws ProccessingException null employee or shift
	 * @Tested
	 */
	public boolean isAssignableFor(Employee employee,Shift shift, ScheduleOptions options) throws CorruptDataException, ProccessingException{
		boolean assignable=false;
		if(null==employee||null==shift || null ==options) {
			throw new ProccessingException("Null options,employee or shift provided to isUnassignedFor");
		}
		else if(!shift.isValid()) {
			throw new CorruptDataException("Invalid shift provided to isUnassignedFor");
		}
		
		if(!employee.requestedOff(shift)){
			//System.out.println("not requested off");
    		if(isUnassignedFor(employee,shift)){
    			//System.out.println("unassinged");
    			if(options.isAllowUnavailable() || isAvailableFor(employee,shift)){
    				assignable=true;
    			}
    		}
		}
		
		return assignable;
    }
	
	/**Returns if the employee has themselves listed as willing to work during the days and times covered by the shift
	 * 
	 * @param employee
	 * @param shift a valid shift
	 * @return boolean if the employee has themselves listed as willing to work during the days and times covered by the shift
	 * @throws CorruptDataException invalid shift
	 * @throws ProccessingException null employee or shift
	 * @TestsInvalid
	 */
	public boolean isAvailableFor(Employee employee, Shift shift) throws CorruptDataException, ProccessingException{
		boolean available = true;
		
		if(null==employee||null==shift) {
			throw new ProccessingException("Null employee or shift provided to isAvailableFor");
		}
		else if(!shift.isValid()) {
			throw new CorruptDataException("Invalid shift provided to isAvailableFor");
		}

		LocalDate date = LocalDate.of(shift.getStartYear(), shift.getStartMonth(), shift.getStartDay());
		DayOfWeek day = date.getDayOfWeek();
		int dayInt = day.getValue();
		if(dayInt==7){
			dayInt=0;
		}
		
		LocalDateTime weeksEnd =  Util.getEndOfWeekFromLocalDateTime(shift.getStartsLocalDateTime());
		
		if(!shift.getOvernight()){
			LocalDateTime availableUntil = shift.getStartsLocalDateTime();
			
			for(int run = 0; run<employee.getDays().length;run++) {
				for(int index = 0; index<employee.getDays().length;index++) {
					Availability availability = employee.getAvailability(index);

					LocalDate availabilityDay = weeksEnd.toLocalDate();
					if(availability.day.getValue()<6) {
						availabilityDay=availabilityDay.minusDays(6-availability.day.getValue());
					}
					else if(availability.day.getValue()==7) {
						availabilityDay=availabilityDay.minusDays(6);
					}
					LocalDateTime availabilityStart= LocalDateTime.of(availabilityDay, availability.startTime);
					LocalDateTime availabilityEnd= LocalDateTime.of(availabilityDay, availability.endTime);
					
					if(Util.isOverlapping(shift.getStartsLocalDateTime(), shift.getEndsLocalDateTime(), availabilityStart,availabilityEnd)) {
						if(availableUntil.isAfter(availabilityStart) || availableUntil.isEqual(availabilityStart)){
							availableUntil=availabilityEnd;
						}
					}
					
					if(availableUntil.isAfter(shift.getEndsLocalDateTime())||availableUntil.isEqual(shift.getEndsLocalDateTime())) {
						break;
					}
				}
				
				if(availableUntil.isAfter(shift.getEndsLocalDateTime())||availableUntil.isEqual(shift.getEndsLocalDateTime())) {
					break;
				}
			}
		}
		else{
			
		}
		
		return available;
	}
	
	/**Returns if this employee allowed to work this shift and with any client covered by it.
     * Clients requiring medpass must have employees that are medpass certified.
     * Clients must have staff of the proper gender.
     * Clients must not be paired with smokers upon request.
     * Clients with cats must not be paired with employees who have cat allergies.
     * Clients must be paired with signing staff when required.
     * Clients and employees must be properly aligned with custom requirements.
     * 
	 * @param employee
	 * @param shift A fully formed shift that returns true with shift.isValid
	 * @return boolean true for valid events. If the employee is allowed to work with the client this shift is scheduled for
	 * @throws ProccessingException Coding failure, null employee, shift or client
	 * @throws CorruptDataException The Shift provided is invalid
	 * @Tested
	 */
	public boolean isCompatibleWith(Employee employee,Shift shift) throws ProccessingException, CorruptDataException{
		String clientID=null;
		Client client = null;
		boolean valid = false; 
		
		if(null==employee||null==shift) {
			throw new ProccessingException("Null employee or shift provided to isCompatibleWith");
		}
		else if(!shift.isValid()) {
			throw new CorruptDataException("Invalid shift provided to isCompatibleWith");
		}
		else if(null==shift.getClientId()) {
			throw new ProccessingException("Null clientId provided to isCompatibleWith");
		}
		
		
    	if(shift.getEvent()){
    		valid =  true;
    	}
    	else{
    		clientID = shift.getClientId();
    		//System.out.println("getting client "+clientID);
    		client=clientRepository.findOne(clientID);

    		//System.out.println("got client "+client.toString());
	    	valid = employeeClientCompatibilityManager.isCompatibleWith(employee,client);
    	}
    	
    	return valid;
    }
	
	/** Returns if the employee has no shift currently scheduled at the same time as the selected shift and
	 * if the employee has no shift scheduled with different clients within 29 minutes of each other.
	 * 
	 * @param employee
	 * @param shift a valid shift
	 * @return boolean  if the employee has no shift currently scheduled at the same time
	 * @throws CorruptDataException invalid shift
	 * @throws ProccessingException null employee or shift
	 * @Tested
	 */
	public boolean isUnassignedFor(Employee employee, Shift shift) throws ProccessingException, CorruptDataException{
		boolean unassigned= true;
		
		if(null==employee||null==shift) {
			throw new ProccessingException("Null employee or shift provided to isUnassignedFor");
		}
		else if(!shift.isValid()) {
			throw new CorruptDataException("Invalid shift provided to isUnassignedFor");
		}
		
		ArrayList<Shift> shiftsForWeek = employeeShiftManager.getAssignedShiftsForEmployeeForWeekOfShift(employee.getId(), shift);
    	
		for(Shift scheduledShift : shiftsForWeek){
			if(ShiftWorker.isAlmostOverlapping(shift, scheduledShift)){
				unassigned=false;
			}
		}

    	return unassigned;
	}
	
	 
	/** Returns if the employee/shift are compatible,
	 *  assignable (available, unassigned,not called off),
	 *  active, without fixed scheduling, 
	 *  and not resting (exceeds max shifts per days, days per week, or alternate weekends)
	 * 
	 * @param employee
	 * @param shift a valid shift
	 * @return boolean compatible,assignable, active, unfixed, and not resting
	 * @throws ProccessingException Coding failure, null employee, shift or client
	 * @throws CorruptDataException The Shift provided is invalid
	 * @Tested
	 */
	public boolean isValidFor(Employee employee, Shift shift,ScheduleOptions options) throws CorruptDataException, ProccessingException{
		boolean validity=false;
		//System.out.println("checking validity of:"+shift.toString());
		if(null==employee||null==shift ||null==options) {
			throw new ProccessingException("Null options, employee or shift provided to isCompatibleWith");
		}
		else if(!shift.isValid()) {
			throw new CorruptDataException("Invalid shift provided to isCompatibleWith");
		}
		
		if(isCompatibleWith(employee,shift)){
			if(isAssignableFor(employee,shift,options)){
				if(options.isAllowInactive() || !employee.getInactive()){
					if(employee.getId().equalsIgnoreCase(shift.getRequestedStaffId()) || !employee.getFixedSchedule()){
						if(!getResting(new EmployeeShiftCompatibility(employee,shift),options)){
							validity=true;
						}
					}
				}
			}
		}
		//System.out.println("valid?"+validity);
		return validity;
	}
}