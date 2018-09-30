package org.cloudfoundry.samples.music.managers;
import java.util.ArrayList;
import java.util.HashMap;

import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibilities;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibility;
import accessiblesolutions.accessiblescheduling.domain.Shift;

import org.junit.experimental.theories.suppliers.TestedOn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
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
    
    public ShiftAssignmentManager() {
    }
   
    public String staffPreassignedShifts(String month, String year) throws CorruptDataException, ProccessingException{
    	String result = "";
    	
    	if(null!=month){
    		int monthInt = Integer.parseInt(month);
	    	result +=saveAssignedUnconflictedPrestaffedRecuringShiftsToTableForMonth(monthInt);
	    	
	    	result+=saveAssignedUnconflictedPrestaffedSingleShiftsToTableForMonth(monthInt);
    	}
    		    	
		return result;
    }
//    public void saveAssignedUnconflictedPrestaffedRecuringShiftsToTableForMonth(int selectedMonth){
//    	ArrayList<Shift> prestaffedRecuringShifts = shiftManager.getPrestaffedRecurringShiftsForMonth(selectedMonth);
//
//    	HashMap<String,ArrayList<Shift>> prestaffedRecuringShiftsPerEmployee =ShiftWorker.getEmployeeShiftMap(prestaffedRecuringShifts);
//
//    	HashMap<String,ArrayList<Shift>> unconflictedPrestaffedRecuringShiftsPerEmployee = ShiftWorker.getNonoverlapingShiftsPerEmployee(prestaffedRecuringShiftsPerEmployee);
//    	
//    	ArrayList<Shift> assignedUnconflictedPrestaffedRecuringShifts = assignRequestedStaff(unconflictedPrestaffedRecuringShiftsPerEmployee);
//
//    	shiftCrud.save(assignedUnconflictedPrestaffedRecuringShifts);
//    }

    //@Tested
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
    
    public String saveAssignedUnconflictedPrestaffedRecuringShiftsToTableForMonth(int selectedMonth) throws CorruptDataException, ProccessingException{
    	ArrayList<Shift> prestaffedRecuringShifts = shiftManager.getPrestaffedRecurringShiftsForMonth(selectedMonth);
    	ArrayList<Shift> onPrestaffedRecuringShifts =getOnPrestaffedShifts(prestaffedRecuringShifts);
    	System.out.println("Staffing " + prestaffedRecuringShifts.size() +" prestaffed shifts that have no conflicts.");
    	HashMap<String,ArrayList<Shift>> onPrestaffedRecuringShiftsPerEmployee =ShiftWorker.getPrestaffedEmployeeShiftMap(onPrestaffedRecuringShifts);

    	HashMap<String,ArrayList<Shift>> unconflictedPrestaffedRecuringShiftsPerEmployee = ShiftWorker.getNonoverlapingShiftsPerEmployee(onPrestaffedRecuringShiftsPerEmployee);
    	
    	ArrayList<Shift> assignedUnconflictedPrestaffedRecuringShifts = ShiftWorker.assignRequestedStaff(unconflictedPrestaffedRecuringShiftsPerEmployee);

    	//    	logger.error(assignedUnconflictedPrestaffedRecuringShifts.size() + " assignedUnconflictedPrestaffedRecuringShifts");
    	shiftCrud.save(assignedUnconflictedPrestaffedRecuringShifts);
    	return "Assigned " + assignedUnconflictedPrestaffedRecuringShifts.size() +" prestaffed recurring shfits.";
    }
    

    public String saveAssignedUnconflictedPrestaffedSingleShiftsToTableForMonth(int month) throws CorruptDataException, ProccessingException {
    	ArrayList<Shift> prestaffedSingleShifts = shiftManager.getPrestaffedSingleShiftsForMonth(month);
    	//logger.error("prestaffedSingleShifts " +prestaffedSingleShifts.size());
    	ArrayList<Shift> onPrestaffedSingleShifts =getOnPrestaffedShifts(prestaffedSingleShifts);
    	HashMap<String, ArrayList<Shift>> prestaffedSingleShiftsPerEmployee = ShiftWorker.getPrestaffedEmployeeShiftMap(onPrestaffedSingleShifts);
    	
    	HashMap<String, ArrayList<Shift>> unconflictedPrestaffedSingleShiftsPerEmployee=  ShiftWorker.getNonoverlapingShiftsPerEmployee(prestaffedSingleShiftsPerEmployee);
//    	logger.error("nonoverlappingPRestaffedSingleShifts" +unconflictedPrestaffedSingleShiftsPerEmployee.toString());
    	unconflictedPrestaffedSingleShiftsPerEmployee=employeeShiftMapManager.getShiftsPerEmployeePerMonthUnconflictingWithAssignedShifts(unconflictedPrestaffedSingleShiftsPerEmployee,month);
	    //check to see if any conflicts exist in requested staff	    		
	    //add conflict info somewhere
    	//add conflicted shifts into array for later staff assignment->
    	ArrayList<Shift> assignedUnconflictedPrestaffedSingleShifts = ShiftWorker.assignRequestedStaff(unconflictedPrestaffedSingleShiftsPerEmployee);
    	//logger.error("prestaffedSingleShifts " +assignedUnconflictedPrestaffedSingleShifts.size());
    	shiftCrud.save(assignedUnconflictedPrestaffedSingleShifts);
    	return "Assigned " + assignedUnconflictedPrestaffedSingleShifts.size() +" prestaffed single shfits.";
	}
    
    public String scheduleUnassignedNonEventShiftsFor(int month,int year) throws CorruptDataException, ProccessingException {
    	String result = "";
		for(int week = 0; week<6;week++){
    		result+=scheduleShiftsStartingWeekOfMonth(week,month,year);
    	}
		return result;
	}
    public String scheduleShiftsStartingWeekOfMonth(int week, int month, int year) throws CorruptDataException, ProccessingException {
    	String result = "scheduling shifts for week " +week + " of month:"+month;
    	
    	System.out.println(result);
		result+=scheduleWeekendShiftsStartingWeekOfMonth(week, month,year);
		result+=scheduleWeekdayShiftsStartingWeekOfMonth(week, month,year);
		return result;
	}
    
    public Shift getWeekendShiftStartingWeekOfMonth(int week,int month, int year) throws CorruptDataException, ProccessingException{
    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		System.out.println(unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+".");
		ArrayList<Shift> unassignedShiftsForWeekends= ShiftWorker.getWeekendShifts(unassignedShiftsForWeek);
		int possible = 9001;
		Shift selected = null;
		for(Shift shift : unassignedShiftsForWeekends){
			EmployeeShiftCompatibilities compatibilities = employeeShiftCompatibilityManager.getValidUnfixedCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift));
			if(compatibilities.compatibilities.size()<possible&&compatibilities.compatibilities.size()>0){
				possible=compatibilities.compatibilities.size();
				selected=shift;
			}
		}
		return selected;
    }
    public Shift getWeekdayShiftStartingWeekOfMonth(int week,int month, int year) throws CorruptDataException, ProccessingException{
    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		System.out.println(unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+".");
		ArrayList<Shift> unassignedShiftsForWeekdays= ShiftWorker.getWeekdayShifts(unassignedShiftsForWeek);
		int possible = 9001;
		Shift selected = null;
		for(Shift shift : unassignedShiftsForWeekdays){
			EmployeeShiftCompatibilities compatibilities = employeeShiftCompatibilityManager.getValidUnfixedCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift));
			if(compatibilities.compatibilities.size()<possible&&compatibilities.compatibilities.size()>0){
				possible=compatibilities.compatibilities.size();
				selected=shift;
			}
		}
		return selected;
    }
    public String scheduleWeekdayShiftStartingWeekOfMonth(int week, int month, int year) throws ProccessingException, CorruptDataException {
    	String result = "";
		Shift shift = getWeekdayShiftStartingWeekOfMonth(week,month,year);
		if(null!=shift){
			result+=scheduleWeekdayShiftStartingWeekOfMonth(shift, week, month, year);
		}
		else{
			result+="No weekday shifts remain to schedule for week "+week + " of month " + month;
		}
    	return result;
	}
    public String scheduleWeekendShiftStartingWeekOfMonth(int week, int month, int year) throws ProccessingException, CorruptDataException {
    	String result = "";
		Shift shift = getWeekendShiftStartingWeekOfMonth(week,month,year);
		if(null!=shift){
			result+=scheduleWeekendShiftStartingWeekOfMonth(shift, month, year);
		}
		else{
			result+="No weekend shifts remain to schedule for week "+week + " of month " + month;
		}
    	return result;
	}
    public String scheduleWeekendShiftStartingWeekOfMonth(Shift shift,  int month, int year) throws CorruptDataException, ProccessingException{
    	String result="";
    	if(null!=shift){
    		result+="Attempting to assign "+shift.toString();
    	
			System.out.println("Attempitng to assign " + shift.toString());
			EmployeeShiftCompatibilities shiftCompatibilities = employeeShiftCompatibilityManager.getValidUnfixedCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift));
			result+=". Shift compatibilities: "+ shiftCompatibilities.toString();
			Employee employee=null;
			//attemptAssigningOnlyCompatibility
			if(null!=shiftCompatibilities && shiftCompatibilities.compatibilities.size()==1){
				employee=shiftCompatibilities.compatibilities.get(0).getEmployee();
				shift.setAssignmentReason("Only " + employee.getFirst() +" was compatible and available. ");
			}
			//end attemptAssigningOnlyCompatibility
			if(employee==null){
				employee = employeeShiftCompatibilityManager.getEmployeeWithMostTime(shiftCompatibilities);
			}
			
			
			if(employee==null){
				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
					if(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift())<compatibility.getEmployee().getMinHours()){
						employee=compatibility.getEmployee();
						shift.setAssignmentReason("Min");
					}
				}
			}
			
			if(employee==null){
				employee=employeeShiftCompatibilityManager.getEmployeeWithMostTimeBeforeOvertimeAfterAssignment(shiftCompatibilities);
				/*System.out.println("likely assigning " + employee.getFirst() + " to shift " +
						" because they have "+ (hoursNeededWeekOf(employee,shift)-shift.getDuration())
						+" left after scheduling");*/
				shift.setAssignmentReason("Weekend shift.Employee had the most time before overtime after assignment");
			}
			else if(shift.getAssignmentReason().length()<10){
				shift.setAssignmentReason("Weekend shift.Employee had the most time until minimn was reached after assignment");
			}
			
			if(employee==null &&shiftCompatibilities!=null&&shiftCompatibilities.compatibilities!=null&&shiftCompatibilities.compatibilities.size()>0){
				float hours = 80;
				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
					if(compatibility.getEmployee().getRequestsExtraShifts()){
						if(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift())<hours){
							hours=employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift());
							employee=compatibility.getEmployee();
							shift.setAssignmentReason("All in overtime, they requested it and have least hours");
						}
					}
				}
				
				if(employee==null){
					for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
						if(compatibility.getEmployee().getRequestsExtraShifts()){
							if(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift())<compatibility.getEmployee().getMinHours()){
								employee=compatibility.getEmployee();
								shift.setAssignmentReason("Min");
							}
						}
					}
				}
				if(employee==null){
					employee=shiftCompatibilities.compatibilities.get(0).getEmployee();
					shift.setAssignmentReason("One of those in overtime who was available");
				}
			}
			if(employee!=null){
				if(!employeeShiftCompatibilityManager.getAssignmentWouldViolateAlternateWeekendsOff(new EmployeeShiftCompatibility(employee,shift))){
					System.out.println("assigning " + employee.getFirst() + " to " + shift.getId() + " " + shift.toString()
					+ " beacause " + shift.getAssignmentReason());
					shift.setStaffId(employee.getId());
					shift.setStaffName(employee.getFirst());
					shift.setAssigned(true);
					shiftCrud.save(shift);
					result+=" assigned to "+employee.getFirst() + " because "+shift.getAssignmentReason();
				}
			}
    	}
    	else{
    		result+="Tried to schedule null shift.";
    	}
		return result;
    }
    public String scheduleWeekendShiftsStartingWeekOfMonth(int week, int month,int year) throws CorruptDataException, ProccessingException {
		String result = "";
    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
    	result+= shifts.size() + " shifts remain unassigned for month "+month+".";
		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		result+= unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+".";
		ArrayList<Shift> unassignedShiftsForWeekends= ShiftWorker.getWeekendShifts(unassignedShiftsForWeek);
		result+= unassignedShiftsForWeekends.size() + " weekends shifts will be assigned.";
		for(int i= 0;i<unassignedShiftsForWeekends.size()+20;i++){
			Shift shift = getWeekendShiftStartingWeekOfMonth(week,month,year);
			result+=scheduleWeekendShiftStartingWeekOfMonth(shift,month,year);
		}
//		for(Shift shift : unassignedShiftsForWeekends){
//			result+= scheduleWeekendShiftStartingWeekOfMonth(shift,week,month,year);
//		}
		shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
    	result+= shifts.size() + " shifts remain unassigned for month "+month+".";
		unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		result+= unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+".";
		unassignedShiftsForWeekends= ShiftWorker.getWeekendShifts(unassignedShiftsForWeek);
		result+= unassignedShiftsForWeekends.size() + " weekends shifts to be assigned.";
		return result;
	}
    
    public String scheduleWeekdayShiftStartingWeekOfMonth(Shift shift, int week, int month, int year) throws CorruptDataException, ProccessingException{
    	String result="";
    	if(null!=shift){
    		result+="Attempting to assign " +shift.toString();
    		System.out.println("Attempitng to assign " + shift.toString());
			EmployeeShiftCompatibilities shiftCompatibilities = employeeShiftCompatibilityManager.getValidUnfixedCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift));
			result+=shiftCompatibilities.compatibilities.toString();
			Employee employee=null;
			//attemptAssigningOnlyCompatibility
			if(null!=shiftCompatibilities && shiftCompatibilities.compatibilities.size()==1){
				employee=shiftCompatibilities.compatibilities.get(0).getEmployee();
				shift.setAssignmentReason("Only " + employee.getFirst() +" was compatible and available. ");
			}
			//end attemptAssigningOnlyCompatibility
			if(employee==null){
				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
					if(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift())<compatibility.getEmployee().getMinHours()){
						employee=compatibility.getEmployee();
						shift.setAssignmentReason("Min");
					}
				}
			}
			if(employee==null){
				employee=employeeShiftCompatibilityManager.getEmployeeWithMostTimeBeforeOvertimeAfterAssignment(shiftCompatibilities);
				shift.setAssignmentReason("Employee had the most time before overtime after assignment");
			}
			else if(shift.getAssignmentReason().length()<10){
				shift.setAssignmentReason("Employee had the most time until minimn was reached after assignment");
			}
			
			if(employee==null&&shiftCompatibilities!=null&&shiftCompatibilities.compatibilities!=null&&shiftCompatibilities.compatibilities.size()>0){
				float hours = 80;
				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
					if(compatibility.getEmployee().getRequestsExtraShifts()){
						if(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift())<hours){
							hours=employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift());
							employee=compatibility.getEmployee();
							shift.setAssignmentReason("All in overtime, they requested it and have least hours");
						}
					}
				}
				
				if(employee==null){
					employee=shiftCompatibilities.compatibilities.get(0).getEmployee();
					shift.setAssignmentReason("One of those in overtime who was available");
				}
			}
			if(employee!=null){
				System.out.println("assigning " + employee.getFirst() + " to shift " + shift.getId() + " " + shift.toString()
				+ " beacause " + shift.getAssignmentReason());
				shift.setStaffId(employee.getId());
				shift.setStaffName(employee.getFirst());
				shift.setAssigned(true);
				shiftCrud.save(shift);
				result+=" Assigning to "+employee.getFirst() + " " + employee.getInitial() + " because " + shift.getAssignmentReason();
			}
    		
    	}
   
    	return result;
    }
    public String scheduleWeekdayShiftsStartingWeekOfMonth(int week, int month,int year) throws CorruptDataException, ProccessingException {
    	String result = "";
    	ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
    	result+= shifts.size() + " shifts remain unassigned for month "+month+". ";
		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		result+= unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+". ";
		ArrayList<Shift> unassignedShiftsForWeekdays= ShiftWorker.getWeekdayShifts(unassignedShiftsForWeek);
		result+= unassignedShiftsForWeekdays.size() + " weekday shifts will be assigned. ";
		for(int i= 0;i<unassignedShiftsForWeekdays.size()+20;i++){
			Shift shift = getWeekdayShiftStartingWeekOfMonth(week,month,year);
			result+=scheduleWeekdayShiftStartingWeekOfMonth(shift,week,month,year);
		}
//		for(Shift shift : unassignedShiftsForWeekends){
//			result+= scheduleWeekendShiftStartingWeekOfMonth(shift,week,month,year);
//		}
		shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
    	result+= shifts.size() + " shifts remain unassigned for month "+month+".";
		unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		result+= unassignedShiftsForWeek.size() + " shifts remain unassigned for week "+week+". ";
		unassignedShiftsForWeekdays= ShiftWorker.getWeekdayShifts(unassignedShiftsForWeek);
		result+= unassignedShiftsForWeekdays.size() + " weekday shifts to be assigned.";
		return result;
	}
}