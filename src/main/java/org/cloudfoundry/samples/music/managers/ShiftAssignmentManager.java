package org.cloudfoundry.samples.music.managers;
import java.util.ArrayList;
import java.util.HashMap;

import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibilities;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibility;
import accessiblesolutions.accessiblescheduling.domain.Shift;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.worker.ShiftWorker;

@Component
public class ShiftAssignmentManager {
    private CrudRepository<Shift, String> shiftCrud;
    
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
    public ShiftAssignmentManager(CrudRepository<Shift, String> shiftCrud) {
        this.shiftCrud = shiftCrud;
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
    public void saveAssignedUnconflictedPrestaffedRecuringShiftsToTableForMonth(int selectedMonth) throws CorruptDataException, ProccessingException{
    	ArrayList<Shift> prestaffedRecuringShifts = shiftManager.getPrestaffedRecurringShiftsForMonth(selectedMonth);
//    	logger.error(prestaffedRecuringShifts.size() + " prestaffedRecuringShifts");
    	HashMap<String,ArrayList<Shift>> prestaffedRecuringShiftsPerEmployee =ShiftWorker.getPrestaffedEmployeeShiftMap(prestaffedRecuringShifts);

    	HashMap<String,ArrayList<Shift>> unconflictedPrestaffedRecuringShiftsPerEmployee = ShiftWorker.getNonoverlapingShiftsPerEmployee(prestaffedRecuringShiftsPerEmployee);
    	
    	ArrayList<Shift> assignedUnconflictedPrestaffedRecuringShifts = ShiftWorker.assignRequestedStaff(unconflictedPrestaffedRecuringShiftsPerEmployee);
//    	logger.error(assignedUnconflictedPrestaffedRecuringShifts.size() + " assignedUnconflictedPrestaffedRecuringShifts");
    	shiftCrud.save(assignedUnconflictedPrestaffedRecuringShifts);
    }
    

    public void saveAssignedUnconflictedPrestaffedSingleShiftsToTableForMonth(int month) throws CorruptDataException, ProccessingException {
    	ArrayList<Shift> prestaffedSingleShifts = shiftManager.getPrestaffedSingleShiftsForMonth(month);
    	//logger.error("prestaffedSingleShifts " +prestaffedSingleShifts.size());
    	HashMap<String, ArrayList<Shift>> prestaffedSingleShiftsPerEmployee = ShiftWorker.getPrestaffedEmployeeShiftMap(prestaffedSingleShifts);
    	
    	HashMap<String, ArrayList<Shift>> unconflictedPrestaffedSingleShiftsPerEmployee=  ShiftWorker.getNonoverlapingShiftsPerEmployee(prestaffedSingleShiftsPerEmployee);
//    	logger.error("nonoverlappingPRestaffedSingleShifts" +unconflictedPrestaffedSingleShiftsPerEmployee.toString());
    	unconflictedPrestaffedSingleShiftsPerEmployee=employeeShiftMapManager.getShiftsPerEmployeePerMonthUnconflictingWithAssignedShifts(unconflictedPrestaffedSingleShiftsPerEmployee,month);
	    //check to see if any conflicts exist in requested staff	    		
	    //add conflict info somewhere
    	//add conflicted shifts into array for later staff assignment->
    	ArrayList<Shift> assignedUnconflictedPrestaffedSingleShifts = ShiftWorker.assignRequestedStaff(unconflictedPrestaffedSingleShiftsPerEmployee);
    	//logger.error("prestaffedSingleShifts " +assignedUnconflictedPrestaffedSingleShifts.size());
    	shiftCrud.save(assignedUnconflictedPrestaffedSingleShifts);
	}
    
    public void scheduleUnassignedNonEventShiftsFor(int month,int year) throws CorruptDataException, ProccessingException {
		for(int week = 0; week<6;week++){
    		scheduleShiftsStartingWeekOfMonth(week,month,year);
    	}
	}
    public void scheduleShiftsStartingWeekOfMonth(int week, int month, int year) throws CorruptDataException, ProccessingException {
    	System.out.println("scheduling shifts for week " +week + " of month:"+month);
		scheduleWeekendShiftsStartingWeekOfMonth(week, month,year);
		scheduleWeekdayShiftsStartingWeekOfMonth(week, month,year);
	}
      
    private void scheduleWeekendShiftsStartingWeekOfMonth(int week, int month,int year) throws CorruptDataException, ProccessingException {
		ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
		ArrayList<Shift> unassignedShiftsForWeekends= ShiftWorker.getWeekendShifts(unassignedShiftsForWeek);
		for(Shift shift : unassignedShiftsForWeekends){
			System.out.println("Attempitng to assign " + shift.toString());
			EmployeeShiftCompatibilities shiftCompatibilities = employeeShiftCompatibilityManager.getValidUnfixedCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesFor(shift));
			
			if(null==shiftCompatibilities){
				
			}
			Employee employee = employeeShiftCompatibilityManager.getEmployeeWithMostTime(shiftCompatibilities);
			
			if(employee==null){
				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
					if(employeeShiftCompatibilityManager.getHoursScheduledWeekOf(compatibility.getEmployee(),compatibility.getShift())<compatibility.getEmployee().getMinHours()){
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
			else{
				shift.setAssignmentReason("Weekend shift.Employee had the most time until minimn was reached after assignment");
			}
			
			if(employee==null &&shiftCompatibilities!=null&&shiftCompatibilities.compatibilities!=null&&shiftCompatibilities.compatibilities.size()>0){
				float hours = 80;
				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
					if(compatibility.getEmployee().getRequestsExtraShifts()){
						if(employeeShiftCompatibilityManager.getHoursScheduledWeekOf(compatibility.getEmployee(),compatibility.getShift())<hours){
							hours=employeeShiftCompatibilityManager.getHoursScheduledWeekOf(compatibility.getEmployee(),compatibility.getShift());
							employee=compatibility.getEmployee();
							shift.setAssignmentReason("All in overtime, they requested it and have least hours");
						}
					}
				}
				
				if(employee==null){
					for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
						if(compatibility.getEmployee().getRequestsExtraShifts()){
							if(employeeShiftCompatibilityManager.getHoursScheduledWeekOf(compatibility.getEmployee(),compatibility.getShift())<compatibility.getEmployee().getMinHours()){
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
				}
			}
		}
	}
   
    private void scheduleWeekdayShiftsStartingWeekOfMonth(int week, int month,int year) throws CorruptDataException, ProccessingException {
		ArrayList<Shift> shifts = shiftManager.getUnassignedNonEventShiftsForMonth(month);
//		System.out.println(shifts.size()+" unassigned non event shifts for month "+month);
		
		ArrayList<Shift> unassignedShiftsForWeek = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month,year);
	//	System.out.println(unassignedShiftsForWeek.size()+" unassigned non event shifts for week  "+week);

		ArrayList<Shift> unassignedShiftsForWeekdays= ShiftWorker.getWeekdayShifts(unassignedShiftsForWeek);

		//System.out.println("week "+week +" has "+unassignedShiftsForWeekdays.size()+" unassignedShiftsForWeekdays");
		for(Shift shift : unassignedShiftsForWeekdays){
			System.out.println("Attempitng to assign " + shift.toString());
			EmployeeShiftCompatibilities shiftCompatibilities = employeeShiftCompatibilityManager.getValidUnfixedCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesFor(shift));
			
			Employee employee = null;
			employee = employeeShiftCompatibilityManager.getEmployeeWithMostTime(shiftCompatibilities);
			if(employee==null){
				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
					if(employeeShiftCompatibilityManager.getHoursScheduledWeekOf(compatibility.getEmployee(),compatibility.getShift())<compatibility.getEmployee().getMinHours()){
						employee=compatibility.getEmployee();
						shift.setAssignmentReason("Min");
					}
				}
			}
			if(employee==null){
				employee=employeeShiftCompatibilityManager.getEmployeeWithMostTimeBeforeOvertimeAfterAssignment(shiftCompatibilities);
				shift.setAssignmentReason("Employee had the most time before overtime after assignment");
			}
			else{
				shift.setAssignmentReason("Employee had the most time until minimn was reached after assignment");
			}
			
			if(employee==null&&shiftCompatibilities!=null&&shiftCompatibilities.compatibilities!=null&&shiftCompatibilities.compatibilities.size()>0){
				float hours = 80;
				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
					if(compatibility.getEmployee().getRequestsExtraShifts()){
						if(employeeShiftCompatibilityManager.getHoursScheduledWeekOf(compatibility.getEmployee(),compatibility.getShift())<hours){
							hours=employeeShiftCompatibilityManager.getHoursScheduledWeekOf(compatibility.getEmployee(),compatibility.getShift());
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
			}
		}
	}
}