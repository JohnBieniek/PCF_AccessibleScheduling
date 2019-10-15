package org.cloudfoundry.samples.music.managers;
import java.util.ArrayList;
import java.util.HashMap;

import org.cloudfoundry.samples.music.repositories.mongodb.ScheduleStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.constants.Constants;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibilities;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibility;
import accessiblesolutions.accessiblescheduling.domain.ScheduleStatus;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.ScheduleOptions;
import accessiblesolutions.accessiblescheduling.worker.ShiftWorker;

@Component
public class ShiftAssignmentManager {
	@Autowired
	private CrudRepository<Shift, String> shiftCrud;
    
    @Autowired
    public CrudRepository<Employee,String> employeeCrud;
    
    @Autowired
    CustomDataManager customDataManager;
    
    @Autowired
    ShiftManager shiftManager;
    
    @Autowired
    EmployeeShiftMapManager employeeShiftMapManager;
    
    @Autowired
    EmployeeShiftCompatibilityManager employeeShiftCompatibilityManager;
    
    @Autowired
    ShiftGenerationManager shiftGenerationManager;
    @Autowired
    private CrudRepository<ScheduleStatus, String> scheduleStatusCrud;
    
    @Autowired
    private UpdateInfoManager updateInfoManager;
    
    @Autowired
    private ScheduleStatusRepository scheduleStatusRepository;  
    
    public ShiftAssignmentManager() {
    }
   
    //Assigned Unconflicted Recuring
    //Assigned Unconflicted Single
    //Each week 
    	//Each Weekend
    		//Shift with the fewest valid
    		//Each Shift(safely)
    			//Only available
    			//Most needed
    			//Most time
    	//Each Weekday
			//Shift with the fewest valid
			//Each Shift(safely)
				//Only available
				//Most needed
				//Most time
  
    //The real new one
    public void scheduleShifts(ScheduleOptions options){
    	int month = Integer.parseInt(options.getMonth());
    	int year = Integer.parseInt(options.getYear());
    	
    	ScheduleStatus status = scheduleStatus(options.getMonth());
    	
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(options.getMonth());
    	}
    	
    	
    	status.setStopped(false);
    	status.setGenerated(true);
    	status.setAssigning(true);
    	status.setAssigned(false);
    	status.setErrored(false);
    	status.setLastUpdatedToNow();
    	scheduleStatusRepository.deleteByMonth(options.getMonth());
    	scheduleStatusCrud.save(status);
    	updateInfoManager.set(Constants.STATUS);
    	try {
    		System.out.println("staffing preassigned shifts:"+options.toString());

			staffPreassignedShifts(options);
			
			for(int week = 0; week<6;week++){
				System.out.println("scheduling shifts for week :"+week);
	    		scheduleWeekendShifts(week,month,year,options);
	    		scheduleWeekdayShifts(week,month,year,options);
	    	}
			
	    	status.setAssigning(false);
	    	status.setAssigned(true);
	    	
	    	status.setLastUpdatedToNow();
	    	scheduleStatusRepository.deleteByMonth(options.getMonth());
	    	scheduleStatusCrud.save(status);
	    	updateInfoManager.set(Constants.STATUS);
		} catch (Exception e) {
	    	status.setAssigning(false);
	    	status.setAssigned(false);
	    	status.setStopped(false);
	    	status.setStopping(false);
	    	status.setErrored(true);
	    	status.setLastUpdatedToNow();
	    	scheduleStatusRepository.deleteByMonth(options.getMonth());
	    	scheduleStatusCrud.save(status);
	    	updateInfoManager.set(Constants.STATUS);
	    	System.out.println("Failed to assign everything. "+e.getMessage());
	    	System.out.println("Failed to assign everything. "+e.getLocalizedMessage());
	    	System.out.println("Failed to assign everything. "+e.getStackTrace().toString());
	    	e.printStackTrace();
		}
    }
    
    public void scheduleWeekdayShifts(int week, int month,int year, ScheduleOptions options) throws ProccessingException, CorruptDataException {
    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
		System.out.println("scheudling :"+ shifts.size()+" shifts formonth");
		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		ArrayList<Shift> unassignedShiftsForWeekdays= ShiftWorker.getWeekdayShifts(unassignedShiftsForWeek);
		System.out.println("scheduling weekday shifts:"+unassignedShiftsForWeekdays.size());
		scheduleShifts(unassignedShiftsForWeekdays,week,month,year,options);
    }
    
    public void scheduleWeekendShifts(int week, int month,int year, ScheduleOptions options) throws ProccessingException, CorruptDataException {
    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
		System.out.println("scheudling :"+ shifts.size()+" shifts formonth");
		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		ArrayList<Shift> unassignedShiftsForWeekends= ShiftWorker.getWeekendShifts(unassignedShiftsForWeek);
		System.out.println("scheduling weekend shifts:"+unassignedShiftsForWeekends.size());//TODO last known print
		scheduleShifts(unassignedShiftsForWeekends,week,month,year,options);
    }
    
    public void scheduleShifts(ArrayList<Shift> unassignedShifts, int week, int month, int year, ScheduleOptions options) throws CorruptDataException, ProccessingException {
	    boolean stopped = false;
	    int maxItterations = unassignedShifts.size();
	    
    	for(int i= 0;i< maxItterations;i++){
    		if(!stopped) {
    			ScheduleStatus status = scheduleStatus(month+"");
	        	if(status.isStopping() || status.isStopped() || !status.isAssigning()) {
	        		stopped=true;
	        		i=maxItterations;
	            	
	            	if(null==status) {
	            		status= new ScheduleStatus();
	            		status.setMonth(month+"");
	            	}
	            	
	            	status.setGenerated(true);
	            	status.setAssigning(false);
	            	status.setAssigned(true);
	            	status.setStopped(true);
	        		status.setStopping(false);
	        		status.setLastUpdatedToNow();
	    	    	scheduleStatusRepository.deleteByMonth(options.getMonth());
	    	    	scheduleStatusCrud.save(status);
	    	    	updateInfoManager.set(Constants.STATUS);
	        	}
	        	else {
					if(unassignedShifts!=null && unassignedShifts.size()>0) {
						Shift shift = getShift(week,options,unassignedShifts);
						
						if(null!=shift) {
							System.out.println("attmepting to scheduleShiftSafely:"+shift.toString());
							unassignedShifts.remove(shift);
						
							boolean assigned = scheduleShiftSafely(shift,options);
							
							if(!assigned) System.out.println("Failed to assign shift:"+ shift.toString());
						}
						else {
							System.out.println("null shift was attempted to be assigned");
						}
					}
	        	}
    		}
		}
    }
    
    public ArrayList<Shift> assignRequestedStaff(HashMap<String, ArrayList<Shift>> prestaffedShiftsPerEmployee,ScheduleOptions options) throws ProccessingException, CorruptDataException {
    	ArrayList<Shift> assignedPrestaffedShifts = new ArrayList<Shift>();
 
    	if(null == prestaffedShiftsPerEmployee || prestaffedShiftsPerEmployee.isEmpty()){
    		return assignedPrestaffedShifts;
    	}
    	
    	for(String employeeId:prestaffedShiftsPerEmployee.keySet()){
    		ArrayList<Shift> prestaffedShiftsForSelectedEmployee = prestaffedShiftsPerEmployee.get(employeeId);
     		Employee employee = employeeCrud.findOne(employeeId);
       		System.out.println("assigning "+ prestaffedShiftsForSelectedEmployee + " shifts to "+employee.getFirst()+" "+employee.getInitial());
    		
    		if(null != prestaffedShiftsForSelectedEmployee && !prestaffedShiftsForSelectedEmployee.isEmpty()){
	    		for(Shift prestaffedShift:prestaffedShiftsForSelectedEmployee){
	    			if( null == prestaffedShift.getRequestedStaffId() || 
	    				null == prestaffedShift.getRequestedStaffName() || 
	    				!prestaffedShift.getRequestedStaffId().equalsIgnoreCase(employeeId)){
	    				throw new ProccessingException(Shift.class,prestaffedShift);//This method only for prestaffed, if here, these aren't prestaffed. 
	    			}
	    			if((employeeShiftCompatibilityManager.isValidFor(employee, prestaffedShift,options) &&
	    				( options.isAllowOvertime() || 
    					  !(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(employee,prestaffedShift)+prestaffedShift.getDuration()>employee.getMaxHours())
						)
	    			)){
		    			prestaffedShift.setStaffId(prestaffedShift.getRequestedStaffId());
		    			prestaffedShift.setStaffName(prestaffedShift.getRequestedStaffName());
		    			prestaffedShift.setAssignmentReason("Prestaffed shift with an available employee");
		    			prestaffedShift.setAssigned(true);
		    			
		    			assignedPrestaffedShifts.add(prestaffedShift);
	    			}
	    		}
	    		
	    		prestaffedShiftsPerEmployee.put(employeeId, prestaffedShiftsForSelectedEmployee);//TODO can this go?
    		}
    	}
    	
		return assignedPrestaffedShifts;
	}

    public String staffPreassignedShifts(ScheduleOptions options) throws CorruptDataException, ProccessingException{
    	String result = "";
    	
    	result +=saveAssignedUnconflictedShiftsToTableForMonth(options);
    	result +=saveAssignedShiftsToTableForMonth(options);
    	System.out.println(result);
		return result;
    }

    /**
     * 
     * @param shifts
     * @return
     * @throws ProccessingException
     * @throws CorruptDataException
     * @Tested
     */
    public ArrayList<Shift> getOnPrestaffedShifts(ArrayList<Shift> shifts) throws ProccessingException, CorruptDataException{
    	ArrayList<Shift> onShifts = new ArrayList<Shift>();
    	
    	if(null!=shifts) {
	    	for(Shift shift: shifts){
	    		Employee employee = employeeCrud.findOne(shift.getRequestedStaffId());
	    		
	    		if(null!=employee && !employee.requestedOff(shift)){
	    			onShifts.add(shift);
	    		}
	    	}
    	}
    	
    	return onShifts;
    }
  
    public String saveAssignedUnconflictedShiftsToTableForMonth(ScheduleOptions options) throws CorruptDataException, ProccessingException {
    	ArrayList<Shift> prestaffedShifts = shiftManager.getPrestaffedShiftsForMonth(options.getMonthInt());
    	System.out.println("attempting to schedule "+prestaffedShifts.size() + " shifts for month:"+options.getMonth());
    	ArrayList<Shift> onPrestaffedShifts =getOnPrestaffedShifts(prestaffedShifts);
    	System.out.println("attempting to schedule "+prestaffedShifts.size() + " OnPrestaffed shifts for month:"+options.getMonth());
    	HashMap<String, ArrayList<Shift>> prestaffedShiftsPerEmployee = ShiftWorker.getPrestaffedEmployeeShiftMap(onPrestaffedShifts);
    	HashMap<String, ArrayList<Shift>> unconflictedPrestaffedShiftsPerEmployee=  ShiftWorker.getNonoverlapingShiftsPerEmployee(prestaffedShiftsPerEmployee);
//    	logger.error("nonoverlappingPRestaffedSingleShifts" +unconflictedPrestaffedSingleShiftsPerEmployee.toString());
    	unconflictedPrestaffedShiftsPerEmployee=employeeShiftMapManager.getShiftsPerEmployeePerMonthUnconflictingWithAssignedShifts(unconflictedPrestaffedShiftsPerEmployee,options.getMonthInt());
	    //check to see if any conflicts exist in requested staff	    		
    	ArrayList<Shift> assignedUnconflictedPrestaffedShifts = assignRequestedStaff(unconflictedPrestaffedShiftsPerEmployee,options);
    	shiftCrud.save(assignedUnconflictedPrestaffedShifts);
    	return "Assigned " + assignedUnconflictedPrestaffedShifts.size() +" prestaffed single shfits.";
	}
    
    public String saveAssignedShiftsToTableForMonth(ScheduleOptions options) throws CorruptDataException, ProccessingException {
    	ArrayList<Shift> prestaffedShifts = shiftManager.getPrestaffedShiftsForMonth(options.getMonthInt());
    	System.out.println("attempting to schedule "+prestaffedShifts.size() + " shifts for month:"+options.getMonth());
    	ArrayList<Shift> onPrestaffedShifts =getOnPrestaffedShifts(prestaffedShifts);
    	System.out.println("attempting to schedule "+prestaffedShifts.size() + " OnPrestaffed shifts for month:"+options.getMonth());
    	HashMap<String, ArrayList<Shift>> prestaffedShiftsPerEmployee = ShiftWorker.getPrestaffedEmployeeShiftMap(onPrestaffedShifts);
    	ArrayList<Shift> assignedUnconflictedPrestaffedShifts = assignRequestedStaff(prestaffedShiftsPerEmployee,options);
    	shiftCrud.save(assignedUnconflictedPrestaffedShifts);
    	return "Assigned " + assignedUnconflictedPrestaffedShifts.size() +" prestaffed single shfits.";
	}
    
    public Shift getWeekendShiftStartingWeekOfMonth(int week,int month, int year, ScheduleOptions options) throws CorruptDataException, ProccessingException{
    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		//System.out.println(unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+".");
		ArrayList<Shift> unassignedShiftsForWeekends= ShiftWorker.getWeekendShifts(unassignedShiftsForWeek);
		int possible = 9001;
		Shift selected = null;
		for(Shift shift : unassignedShiftsForWeekends){
			EmployeeShiftCompatibilities compatibilities = employeeShiftCompatibilityManager.getValidCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift),options);
			if(compatibilities.compatibilities.size()<possible&&compatibilities.compatibilities.size()>0){
				possible=compatibilities.compatibilities.size();
				selected=shift;
			}
		}
		return selected;
    }
    
    public Shift getShift(int year, ScheduleOptions options, ArrayList<Shift> shifts) throws CorruptDataException, ProccessingException{
		int possible = 9001;
		Shift selected = null;
		System.out.println("getting a shift to assign");
		if(null!=shifts) {
			selected=shifts.get(0);
			
			for(Shift shift : shifts){
				System.out.println("seeing if the next shift we should assign is :"+shift.toString());
				EmployeeShiftCompatibilities compatibilities = employeeShiftCompatibilityManager.getValidCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift),options);
				if(compatibilities.compatibilities.size()<possible&&compatibilities.compatibilities.size()>0){
					possible=compatibilities.compatibilities.size();
					selected=shift;
				}
			}
		}
		return selected;
    }
    
    /**Schedules the selected shift without incurring overtime
     * Priority:
     * Only available
     * Most needed
     * Most time
     * 
     * @param shift
     * @return boolean assigned
     * @throws CorruptDataException Invalid shift
     * @throws ProccessingException Null Shift
     */
    public boolean scheduleShiftSafely(Shift shift,ScheduleOptions options) throws CorruptDataException,ProccessingException{
    	boolean assigned = false;
    	Employee employee=null;
    	EmployeeShiftCompatibilities shiftCompatibilities;
    	
    	if(null == shift) {
    		System.out.println("Null shift provided to scheduleShiftSafely");
    		throw new ProccessingException("Null shift provided to scheduleShiftSafely");
    	}
    	else if(!shift.isValid()) {
    		System.out.println("Invalid shift provided to scheduleShiftSafely");
    		throw new CorruptDataException("Invalid shift provided to scheduleShiftSafely");
    	}//TODO look here
    	System.out.println("Scheduling the following shift:"+shift.toString());
    	//Get all those valid to work this shift
    	shiftCompatibilities = employeeShiftCompatibilityManager.getValidCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift),options);
	
    	//If we have at least one valid employee for this shift
    	if(null!=shiftCompatibilities && null != shiftCompatibilities.compatibilities && 
    	   shiftCompatibilities.compatibilities.size() >0) {
    		//System.out.println("options.isAllowOvertime() when at least one available:"+options.isAllowOvertime() );
    		//If we have only 1 valid employee give them the shift regardless
    		if(shiftCompatibilities.compatibilities.size()==1 && 
    				( options.isAllowOvertime() || 
					!employeeShiftCompatibilityManager.getAssignmentWouldIncurOvertime(shiftCompatibilities.compatibilities.get(0)))) {
    			employee=shiftCompatibilities.compatibilities.get(0).getEmployee();
    			if(employee!=null) {
	    			shift.setAssignmentReason("Only " + employee.getFirst() +" was compatible and available. ");
	    			scheduleShift(shift,employee);
	    			assigned=true;
    			}
    		}
    		
    		//Assign to the person with the most time needed to meet their minimum
    		if(!assigned) {
    			employee = employeeShiftCompatibilityManager.getEmployeeWithMostNeeded(shiftCompatibilities);
    			if(employeeShiftCompatibilityManager.hoursNeededWeekOfShift(employee, shift)>0) {
    				shift.setAssignmentReason(employee.getFirst() +" needed the most hours.");
	    			scheduleShift(shift,employee);
	    			assigned=true;
    			}
    		}
    		
    		//Assign to the person with the most time
    		if(!assigned) {
    			//System.out.println("trying to assign to most time");
    			employee=employeeShiftCompatibilityManager.getEmployeeWithMostTimeSafely(shiftCompatibilities);
    			if(null!=employee) {
    				shift.setAssignmentReason(employee.getFirst() +" had the most time before overtime");
    				scheduleShift(shift,employee);
	    			assigned=true;
    			}
			}
    		
    		//If scheduling when already in overtime is allowed, give to the person with the fewest hours
    		if(!assigned && options.isAllowOvertime()){
				employee=employeeShiftCompatibilityManager.getEmployeeWithFewestHours(shiftCompatibilities);
				
				if(null!=employee) {
					shift.setAssignmentReason(employee.getFirst() +" had the fewest hours when overtime was allowed");
    				scheduleShift(shift,employee);
	    			assigned=true;
				}
    		}
    	}
    	
    	System.out.println("assigned:"+assigned);
    	
    	return assigned;
    }
    
    public Employee suggestScheduleShiftSafely(Shift shift,ScheduleOptions options) throws CorruptDataException,ProccessingException{
    	boolean assigned = false;
    	Employee employee=null;
    	EmployeeShiftCompatibilities shiftCompatibilities;
    	
    	if(null == shift) {
    		System.out.println("Null shift provided to scheduleShiftSafely");
    		throw new ProccessingException("Null shift provided to scheduleShiftSafely");
    	}
    	else if(!shift.isValid()) {
    		System.out.println("Invalid shift provided to scheduleShiftSafely");
    		throw new CorruptDataException("Invalid shift provided to scheduleShiftSafely");
    	}//TODO look here
    	System.out.println("Scheduling the following shift:"+shift.toString());
    	//Get all those valid to work this shift
    	shiftCompatibilities = employeeShiftCompatibilityManager.getValidCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift),options);
	
    	//If we have at least one valid employee for this shift
    	if(null!=shiftCompatibilities && null != shiftCompatibilities.compatibilities && 
    	   shiftCompatibilities.compatibilities.size() >0) {
    		//System.out.println("options.isAllowOvertime() when at least one available:"+options.isAllowOvertime() );
    		//If we have only 1 valid employee give them the shift regardless
    		if(shiftCompatibilities.compatibilities.size()==1 && 
    				( options.isAllowOvertime() || 
					!employeeShiftCompatibilityManager.getAssignmentWouldIncurOvertime(shiftCompatibilities.compatibilities.get(0)))) {
    			employee=shiftCompatibilities.compatibilities.get(0).getEmployee();
    			if(employee!=null) {
	    			shift.setAssignmentReason("Only " + employee.getFirst() +" was compatible and available. ");
	    			assigned=true;
    			}
    		}
    		
    		//Assign to the person with the most time needed to meet their minimum
    		if(!assigned) {
    			employee = employeeShiftCompatibilityManager.getEmployeeWithMostNeeded(shiftCompatibilities);
    			if(employeeShiftCompatibilityManager.hoursNeededWeekOfShift(employee, shift)>0) {
    				shift.setAssignmentReason(employee.getFirst() +" needed the most hours.");
	    			assigned=true;
    			}
    		}
    		
    		//Assign to the person with the most time
    		if(!assigned) {
    			//System.out.println("trying to assign to most time");
    			employee=employeeShiftCompatibilityManager.getEmployeeWithMostTimeSafely(shiftCompatibilities);
    			if(null!=employee) {
    				shift.setAssignmentReason(employee.getFirst() +" had the most time before overtime");
	    			assigned=true;
    			}
			}
    		
    		//If scheduling when already in overtime is allowed, give to the person with the fewest hours
    		if(!assigned && options.isAllowOvertime()){
				employee=employeeShiftCompatibilityManager.getEmployeeWithFewestHours(shiftCompatibilities);
				
				if(null!=employee) {
					shift.setAssignmentReason(employee.getFirst() +" had the fewest hours when overtime was allowed");
	    			assigned=true;
				}
    		}
    	}
    	
    	System.out.println("assigned:"+assigned);
    	
    	return employee;
    }
    public void scheduleShift(Shift shift, Employee employee) throws ProccessingException {
    	if(null == shift || null == employee) {
    		throw new ProccessingException("Null shift or employee provided to scheduleShift");
    	}
    	
    	shift.setStaffId(employee.getId());
		shift.setStaffName(employee.getFirst());
		shift.setAssigned(true);
		shiftCrud.save(shift);
		//System.out.println("assigned:"+shift.toString());
    }
    
    public ScheduleStatus scheduleStatus(String month) {
	    Iterable<ScheduleStatus> statusList = scheduleStatusCrud.findAll();
		ScheduleStatus status = null;
		for(ScheduleStatus selectedStatus:statusList) {
			if(selectedStatus.getMonth().equalsIgnoreCase(month)) {
				status=selectedStatus;
			}
		}
	    return status;
    }
}