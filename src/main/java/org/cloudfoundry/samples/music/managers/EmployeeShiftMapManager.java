package org.cloudfoundry.samples.music.managers;
import java.util.ArrayList;
import java.util.HashMap;

import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.worker.ShiftWorker;

import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmployeeShiftMapManager {
    private MongoShiftRepository shiftRepository;//TODO change to autowired
    
    @Autowired
    EmployeeShiftManager employeeShiftManager;
    
    @Autowired
    EmployeeClientCompatibilityManager employeeClientCompatibilityManager;
    
    @Autowired
    public EmployeeShiftMapManager(MongoShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public HashMap<String, ArrayList<Shift>> getShiftsPerEmployeePerMonthUnconflictingWithAssignedShifts(HashMap<String, ArrayList<Shift>> shiftsPerEmployee, int month) throws CorruptDataException, ProccessingException{
		HashMap<String,ArrayList<Shift>> assignedShiftsPerEmployee = getAssignedShiftsPerEmployeeForMonth(month);
		HashMap<String,ArrayList<Shift>> unconflictedShiftsPerEmployee = new HashMap<String, ArrayList<Shift>>();
    	for(String employeeId:shiftsPerEmployee.keySet()){
    		ArrayList<Shift> shiftsForSelectedEmployee = shiftsPerEmployee.get(employeeId);
    		ArrayList<Shift> assignedShiftsForSelectedEmployee = assignedShiftsPerEmployee.get(employeeId);
	    	ArrayList<Shift> unconflictedShiftsForSelectedEmployee = new ArrayList<Shift>();
	    	
	    	int x=-1;
    		int y;
    		
    		for(Shift baseShift: shiftsForSelectedEmployee){
    			int conflicts = 0;
    			x++;
    			y=-1;
    			if(null!=assignedShiftsForSelectedEmployee){
	    			for(Shift comparingShift: assignedShiftsForSelectedEmployee){
	    				y++;
	    				
	    				if(null!=baseShift && null!=comparingShift && ShiftWorker.isAlmostOverlapping(baseShift,comparingShift)){
	    					conflicts++;
	    				}
		    		}
    			}
    			if(conflicts==0){
    				unconflictedShiftsForSelectedEmployee.add(baseShift);
    			}
    		}
    		unconflictedShiftsPerEmployee.put(employeeId, unconflictedShiftsForSelectedEmployee);
    	}
    	
		return unconflictedShiftsPerEmployee;
	}
	
    public HashMap<String, ArrayList<Shift>> getAssignedShiftsPerEmployeeForMonth(int month) throws ProccessingException {
    	Iterable<Shift> shifts = shiftRepository.findByStartMonth(month);
    	ArrayList<Shift> assignedShifts = ShiftWorker.getAssignedShifts(shifts);
    	HashMap<String,ArrayList<Shift>> assignedShiftsPerEmployee =ShiftWorker.getPrestaffedEmployeeShiftMap(assignedShifts);
    	
		return assignedShiftsPerEmployee;
	}
}