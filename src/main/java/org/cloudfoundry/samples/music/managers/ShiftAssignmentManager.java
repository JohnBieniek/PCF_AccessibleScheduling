package org.cloudfoundry.samples.music.managers;
import java.util.ArrayList;
import java.util.HashMap;

import org.cloudfoundry.samples.music.repositories.mongodb.ScheduleStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

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
    public void scheduleShifts(ScheduleOptions options) throws ProccessingException, CorruptDataException {
    	int month = Integer.parseInt(options.getMonth());
    	int year = Integer.parseInt(options.getYear());
    	
    	ScheduleStatus status = scheduleStatusCrud.findOne(options.getMonth());
    	
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(options.getMonth());
    	}
    	
    	scheduleStatusRepository.deleteByMonth(options.getMonth());
    	
    	status.setGenerated(true);
    	status.setAssigning(true);
    	scheduleStatusCrud.save(status);
    	
    	staffPreassignedShifts(options);
    	
    	for(int week = 0; week<6;week++){
    		scheduleWeekendShifts(week,month,year,options);
    		scheduleWeekdayShifts(week,month,year,options);
    	}
    	
    	scheduleStatusRepository.deleteByMonth(options.getMonth());
    	
    	status.setAssigning(false);
    	status.setAssigned(true);
    	scheduleStatusCrud.save(status);
    }
    
    public void scheduleWeekdayShifts(int week, int month,int year, ScheduleOptions options) throws ProccessingException, CorruptDataException {
    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		ArrayList<Shift> unassignedShiftsForWeekdays= ShiftWorker.getWeekdayShifts(unassignedShiftsForWeek);
		
		scheduleShifts(unassignedShiftsForWeekdays,week,month,year,options);
    }
    
    public void scheduleWeekendShifts(int week, int month,int year, ScheduleOptions options) throws ProccessingException, CorruptDataException {
    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		ArrayList<Shift> unassignedShiftsForWeekends= ShiftWorker.getWeekendShifts(unassignedShiftsForWeek);
		
		scheduleShifts(unassignedShiftsForWeekends,week,month,year,options);
    }
    
    public void scheduleShifts(ArrayList<Shift> unassignedShifts, int week, int month, int year, ScheduleOptions options) throws CorruptDataException, ProccessingException {
	    boolean stopped = false;
	    
    	for(int i= 0;i<unassignedShifts.size();i++){
    		if(!stopped) {
	        	if(scheduleStatus(month+"").isStopped()) {
	        		stopped=true;
	        	}
	        	else {
					if(unassignedShifts!=null && unassignedShifts.size()>0) {
						Shift shift = getWeekendShiftStartingWeekOfMonth(week,month,year,options);
						if(null!=shift) {
							unassignedShifts.remove(shift);
						
							scheduleShiftSafely(shift,options);
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
    		
    		if(null != prestaffedShiftsForSelectedEmployee && !prestaffedShiftsForSelectedEmployee.isEmpty()){
	    		for(Shift prestaffedShift:prestaffedShiftsForSelectedEmployee){
	    			if( null == prestaffedShift.getRequestedStaffId() || 
	    				null == prestaffedShift.getRequestedStaffName() || 
	    				!prestaffedShift.getRequestedStaffId().equalsIgnoreCase(employeeId)){
	    				throw new ProccessingException(Shift.class,prestaffedShift);//This method only for prestaffed, if here, these aren't prestaffed. 
	    			}
	    			
	    			//TODO add logic to ensure they have proper qualification to work with this client on this shift, perhaps in a helper method
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
    	
    	result +=saveAssignedUnconflictedPrestaffedRecuringShiftsToTableForMonth(options);
    	
    	result+=saveAssignedUnconflictedPrestaffedSingleShiftsToTableForMonth(options);
    		    	
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
    
    public String saveAssignedUnconflictedPrestaffedRecuringShiftsToTableForMonth(ScheduleOptions options) throws CorruptDataException, ProccessingException{
    	ArrayList<Shift> prestaffedRecuringShifts = shiftManager.getPrestaffedRecurringShiftsForMonth(options.getMonthInt());
    	ArrayList<Shift> onPrestaffedRecuringShifts =getOnPrestaffedShifts(prestaffedRecuringShifts);
    	System.out.println("Staffing " + prestaffedRecuringShifts.size() +" prestaffed shifts that have no conflicts.");
    	HashMap<String,ArrayList<Shift>> onPrestaffedRecuringShiftsPerEmployee =ShiftWorker.getPrestaffedEmployeeShiftMap(onPrestaffedRecuringShifts);

    	HashMap<String,ArrayList<Shift>> unconflictedPrestaffedRecuringShiftsPerEmployee = ShiftWorker.getNonoverlapingShiftsPerEmployee(onPrestaffedRecuringShiftsPerEmployee);
    	
    	ArrayList<Shift> assignedUnconflictedPrestaffedRecuringShifts = assignRequestedStaff(unconflictedPrestaffedRecuringShiftsPerEmployee,options);

    	//    	logger.error(assignedUnconflictedPrestaffedRecuringShifts.size() + " assignedUnconflictedPrestaffedRecuringShifts");
    	shiftCrud.save(assignedUnconflictedPrestaffedRecuringShifts);
    	return "Assigned " + assignedUnconflictedPrestaffedRecuringShifts.size() +" prestaffed recurring shfits.";
    }
    

    public String saveAssignedUnconflictedPrestaffedSingleShiftsToTableForMonth(ScheduleOptions options) throws CorruptDataException, ProccessingException {
    	ArrayList<Shift> prestaffedSingleShifts = shiftManager.getPrestaffedSingleShiftsForMonth(options.getMonthInt());
    	//logger.error("prestaffedSingleShifts " +prestaffedSingleShifts.size());
    	ArrayList<Shift> onPrestaffedSingleShifts =getOnPrestaffedShifts(prestaffedSingleShifts);
    	HashMap<String, ArrayList<Shift>> prestaffedSingleShiftsPerEmployee = ShiftWorker.getPrestaffedEmployeeShiftMap(onPrestaffedSingleShifts);
    	
    	HashMap<String, ArrayList<Shift>> unconflictedPrestaffedSingleShiftsPerEmployee=  ShiftWorker.getNonoverlapingShiftsPerEmployee(prestaffedSingleShiftsPerEmployee);
//    	logger.error("nonoverlappingPRestaffedSingleShifts" +unconflictedPrestaffedSingleShiftsPerEmployee.toString());
    	unconflictedPrestaffedSingleShiftsPerEmployee=employeeShiftMapManager.getShiftsPerEmployeePerMonthUnconflictingWithAssignedShifts(unconflictedPrestaffedSingleShiftsPerEmployee,options.getMonthInt());
	    //check to see if any conflicts exist in requested staff	    		
	    //add conflict info somewhere
    	//add conflicted shifts into array for later staff assignment->
    	ArrayList<Shift> assignedUnconflictedPrestaffedSingleShifts = assignRequestedStaff(unconflictedPrestaffedSingleShiftsPerEmployee,options);
    	//logger.error("prestaffedSingleShifts " +assignedUnconflictedPrestaffedSingleShifts.size());
    	shiftCrud.save(assignedUnconflictedPrestaffedSingleShifts);
    	return "Assigned " + assignedUnconflictedPrestaffedSingleShifts.size() +" prestaffed single shfits.";
	}
    
//    public String scheduleUnassignedNonEventShiftsFor(ScheduleOptions options) throws CorruptDataException, ProccessingException {
//    	String result = "";
//		for(int week = 0; week<6;week++){
//    		result+=scheduleShiftsStartingWeekOfMonth(week,options.getMonthInt(),options.getYearInt());
//    	}
//		return result;
//	}
//    public String scheduleShiftsStartingWeekOfMonth(int week, int month, int year) throws CorruptDataException, ProccessingException {
//    	String result = "scheduling shifts for week " +week + " of month:"+month;
//    	
//    	System.out.println(result);
//		result+=scheduleWeekendShiftsStartingWeekOfMonth(week, month,year);
//		result+=scheduleWeekdayShiftsStartingWeekOfMonth(week, month,year);
//		return result;
//	}
    
    public Shift getWeekendShiftStartingWeekOfMonth(int week,int month, int year, ScheduleOptions options) throws CorruptDataException, ProccessingException{
    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		System.out.println(unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+".");
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
//    public Shift getWeekdayShiftStartingWeekOfMonth(int week,int month, int year) throws CorruptDataException, ProccessingException{
//    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
//		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
//		System.out.println(unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+".");
//		ArrayList<Shift> unassignedShiftsForWeekdays= ShiftWorker.getWeekdayShifts(unassignedShiftsForWeek);
//		int possible = 9001;
//		Shift selected = null;
//		for(Shift shift : unassignedShiftsForWeekdays){
//			EmployeeShiftCompatibilities compatibilities = employeeShiftCompatibilityManager.getValidCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift));
//			if(compatibilities.compatibilities.size()<possible&&compatibilities.compatibilities.size()>0){
//				possible=compatibilities.compatibilities.size();
//				selected=shift;
//			}
//		}
//		return selected;
//    }
//    public String scheduleWeekdayShiftStartingWeekOfMonth(int week, int month, int year) throws ProccessingException, CorruptDataException {
//    	String result = "";
//		Shift shift = getWeekdayShiftStartingWeekOfMonth(week,month,year);
//		if(null!=shift){
//			result+=scheduleWeekdayShiftStartingWeekOfMonth(shift, week, month, year);
//		}
//		else{
//			result+="No weekday shifts remain to schedule for week "+week + " of month " + month;
//		}
//    	return result;
//	}
//    public String scheduleWeekendShiftStartingWeekOfMonth(int week, int month, int year) throws ProccessingException, CorruptDataException {
//    	String result = "";
//		Shift shift = getWeekendShiftStartingWeekOfMonth(week,month,year);
//		if(null!=shift){
//			result+=scheduleWeekendShiftStartingWeekOfMonth(shift);
//		}
//		else{
//			result+="No weekend shifts remain to schedule for week "+week + " of month " + month;
//		}
//    	return result;
//	}
    
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
    		throw new ProccessingException("Null shift provided to scheduleShift");
    	}
    	else if(!shift.isValid()) {
    		throw new CorruptDataException("Null shift provided to scheduleShift");
    	}
    	
    	//Get all those valid to work this shift
    	shiftCompatibilities = employeeShiftCompatibilityManager.getValidCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift),options);

    	//If we have at least one valid employee for this shift
    	if(null!=shiftCompatibilities && null != shiftCompatibilities.compatibilities && 
    	   shiftCompatibilities.compatibilities.size() >0) {
    		//If we have only 1 valid employee give them the shift regardless
    		if(shiftCompatibilities.compatibilities.size()==1 && 
    		   !employeeShiftCompatibilityManager.getAssignmentWouldIncurOvertime(shiftCompatibilities.compatibilities.get(0))) {
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
    			employee=employeeShiftCompatibilityManager.getEmployeeWithMostTimeSafely(shiftCompatibilities);
    			if(null!=employee) {
    				shift.setAssignmentReason(employee.getFirst() +" had the most time before overtime");
    				scheduleShift(shift,employee);
	    			assigned=true;
    			}
			}
    	}
    	
    	return assigned;
    }
    
    public void scheduleShift(Shift shift, Employee employee) throws ProccessingException {
    	if(null == shift || null == employee) {
    		throw new ProccessingException("Null shift or employee provided to scheduleShift");
    	}
    	
    	shift.setStaffId(employee.getId());
		shift.setStaffName(employee.getFirst());
		shift.setAssigned(true);
		shiftCrud.save(shift);
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
    
//    public String scheduleWeekendShiftStartingWeekOfMonth(Shift shift) throws CorruptDataException, ProccessingException{
//    	String result="";
//    	if(null!=shift){
//    		result+="Attempting to assign "+shift.toString();
//    	
//			System.out.println("Attempitng to assign " + shift.toString());
//			EmployeeShiftCompatibilities shiftCompatibilities = employeeShiftCompatibilityManager.getValidCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift));
//			result+=". Shift compatibilities: "+ shiftCompatibilities.toString();
//			Employee employee=null;
//			
//			//If only one person is assignable give them the shift
//			if(null!=shiftCompatibilities && shiftCompatibilities.compatibilities.size()==1){
//				employee=shiftCompatibilities.compatibilities.get(0).getEmployee();
//				shift.setAssignmentReason("Only " + employee.getFirst() +" was compatible and available. ");
//			}
//			
//			if(employee==null){
//				employee = employeeShiftCompatibilityManager.getEmployeeWithMostNeeded(shiftCompatibilities);
//			}
//			
//			
//			//replace with getEmployeeWithMostTime(shiftCompatibilities); or remove entirely?
//			if(employee==null){
//				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
//					if(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift())<compatibility.getEmployee().getMinHours()){
//						employee=compatibility.getEmployee();
//						shift.setAssignmentReason("Min");
//					}
//				}
//			}
//			
//			//repace with most before overtime
//			if(employee==null){
//				employee=employeeShiftCompatibilityManager.getEmployeeWithMostTimeBeforeOvertimeAfterAssignment(shiftCompatibilities);
//				/*System.out.println("likely assigning " + employee.getFirst() + " to shift " +
//						" because they have "+ (hoursNeededWeekOf(employee,shift)-shift.getDuration())
//						+" left after scheduling");*/
//				shift.setAssignmentReason("Weekend shift.Employee had the most time before overtime after assignment");
//			}
//			else if(shift.getAssignmentReason().length()<10){
//				shift.setAssignmentReason("Weekend shift.Employee had the most time until minimn was reached after assignment");
//			}
//			
//			if(employee==null &&shiftCompatibilities!=null&&shiftCompatibilities.compatibilities!=null&&shiftCompatibilities.compatibilities.size()>0){
//				float hours = 80;
//				//Least hours that requested extra shifts
//				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
//					if(compatibility.getEmployee().getRequestsExtraShifts()){
//						if(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift())<hours){
//							hours=employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift());
//							employee=compatibility.getEmployee();
//							shift.setAssignmentReason("All in overtime, they requested it and have least hours");
//						}
//					}
//				}
//				
//				//Shit attempt at min hours, replace with the real min hours funciton
//				if(employee==null){
//					for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
//						if(compatibility.getEmployee().getRequestsExtraShifts()){
//							if(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift())<compatibility.getEmployee().getMinHours()){
//								employee=compatibility.getEmployee();
//								shift.setAssignmentReason("Min");
//							}
//						}
//					}
//				}
//				//random employee
//				if(employee==null){
//					employee=shiftCompatibilities.compatibilities.get(0).getEmployee();
//					shift.setAssignmentReason("One of those in overtime who was available");
//				}
//			}
//			if(employee!=null){
//				if(!employeeShiftCompatibilityManager.getAssignmentWouldViolateAlternateWeekendsOff(new EmployeeShiftCompatibility(employee,shift))){
//					System.out.println("assigning " + employee.getFirst() + " to " + shift.getId() + " " + shift.toString()
//					+ " beacause " + shift.getAssignmentReason());
//					shift.setStaffId(employee.getId());
//					shift.setStaffName(employee.getFirst());
//					shift.setAssigned(true);
//					shiftCrud.save(shift);
//					result+=" assigned to "+employee.getFirst() + " because "+shift.getAssignmentReason();
//				}
//			}
//    	}
//    	else{
//    		result+="Tried to schedule null shift.";
//    	}
//		return result;
//    }
//    public String scheduleWeekendShiftsStartingWeekOfMonth(int week, int month,int year) throws CorruptDataException, ProccessingException {
//		String result = "";
//    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
//    	result+= shifts.size() + " shifts remain unassigned for month "+month+".";
//		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
//		result+= unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+".";
//		ArrayList<Shift> unassignedShiftsForWeekends= ShiftWorker.getWeekendShifts(unassignedShiftsForWeek);
//		result+= unassignedShiftsForWeekends.size() + " weekends shifts will be assigned.";
//		for(int i= 0;i<unassignedShiftsForWeekends.size()+20;i++){
//			Shift shift = getWeekendShiftStartingWeekOfMonth(week,month,year);
//			result+=scheduleWeekendShiftStartingWeekOfMonth(shift);
//		}
////		for(Shift shift : unassignedShiftsForWeekends){
////			result+= scheduleWeekendShiftStartingWeekOfMonth(shift,week,month,year);
////		}
//		shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
//    	result+= shifts.size() + " shifts remain unassigned for month "+month+".";
//		unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
//		result+= unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+".";
//		unassignedShiftsForWeekends= ShiftWorker.getWeekendShifts(unassignedShiftsForWeek);
//		result+= unassignedShiftsForWeekends.size() + " weekends shifts to be assigned.";
//		return result;
//	}
    
//    public String scheduleWeekdayShiftStartingWeekOfMonth(Shift shift, int week, int month, int year) throws CorruptDataException, ProccessingException{
//    	String result="";
//    	if(null!=shift){
//    		result+="Attempting to assign " +shift.toString();
//    		System.out.println("Attempitng to assign " + shift.toString());
//			EmployeeShiftCompatibilities shiftCompatibilities = employeeShiftCompatibilityManager.getValidCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift));
//			result+=shiftCompatibilities.compatibilities.toString();
//			Employee employee=null;
//
//			
//			//If only one person is assignable give them the shift
//			if(null!=shiftCompatibilities && shiftCompatibilities.compatibilities.size()==1){
//				employee=shiftCompatibilities.compatibilities.get(0).getEmployee();
//				shift.setAssignmentReason("Only " + employee.getFirst() +" was compatible and available. ");
//			}
//
//			//replace with getEmployeeWithMostTime(shiftCompatibilities) or have it go first
//			if(employee==null){
//				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
//					if(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift())<compatibility.getEmployee().getMinHours()){
//						employee=compatibility.getEmployee();
//						shift.setAssignmentReason("Min");
//					}
//				}
//			}
//			
//			if(employee==null){
//				employee=employeeShiftCompatibilityManager.getEmployeeWithMostTimeBeforeOvertimeAfterAssignment(shiftCompatibilities);
//				shift.setAssignmentReason("Employee had the most time before overtime after assignment");
//			}
//			else if(shift.getAssignmentReason().length()<10){
//				shift.setAssignmentReason("Employee had the most time until minimn was reached after assignment");
//			}
//			
//			if(employee==null&&shiftCompatibilities!=null&&shiftCompatibilities.compatibilities!=null&&shiftCompatibilities.compatibilities.size()>0){
//				float hours = 80;
//				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
//					if(compatibility.getEmployee().getRequestsExtraShifts()){
//						if(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift())<hours){
//							hours=employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift());
//							employee=compatibility.getEmployee();
//							shift.setAssignmentReason("All in overtime, they requested it and have least hours");
//						}
//					}
//				}
//				
//				if(employee==null){
//					employee=shiftCompatibilities.compatibilities.get(0).getEmployee();
//					shift.setAssignmentReason("One of those in overtime who was available");
//				}
//			}
//			if(employee!=null){
//				System.out.println("assigning " + employee.getFirst() + " to shift " + shift.getId() + " " + shift.toString()
//				+ " beacause " + shift.getAssignmentReason());
//				shift.setStaffId(employee.getId());
//				shift.setStaffName(employee.getFirst());
//				shift.setAssigned(true);
//				shiftCrud.save(shift);
//				result+=" Assigning to "+employee.getFirst() + " " + employee.getInitial() + " because " + shift.getAssignmentReason();
//			}
//    		
//    	}
//   
//    	return result;
//    }
//    public String scheduleWeekdayShiftsStartingWeekOfMonth(int week, int month,int year) throws CorruptDataException, ProccessingException {
//    	String result = "";
//    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
//    	result+= shifts.size() + " shifts remain unassigned for month "+month+". ";
//		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
//		result+= unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+". ";
//		ArrayList<Shift> unassignedShiftsForWeekdays= ShiftWorker.getWeekdayShifts(unassignedShiftsForWeek);
//		result+= unassignedShiftsForWeekdays.size() + " weekday shifts will be assigned. ";
//		for(int i= 0;i<unassignedShiftsForWeekdays.size()+20;i++){
//			Shift shift = getWeekdayShiftStartingWeekOfMonth(week,month,year);
//			result+=scheduleWeekdayShiftStartingWeekOfMonth(shift,week,month,year);
//		}
////		for(Shift shift : unassignedShiftsForWeekends){
////			result+= scheduleWeekendShiftStartingWeekOfMonth(shift,week,month,year);
////		}
//		shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
//    	result+= shifts.size() + " shifts remain unassigned for month "+month+".";
//		unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
//		result+= unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+". ";
//		unassignedShiftsForWeekdays= ShiftWorker.getWeekdayShifts(unassignedShiftsForWeek);
//		result+= unassignedShiftsForWeekdays.size() + " weekday shifts to be assigned.";
//		return result;
//	}
}