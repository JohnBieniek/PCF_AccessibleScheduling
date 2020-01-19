package accessiblescheduling.manager;
import java.util.ArrayList;
import java.util.HashMap;

import accessiblescheduling.domain.Shift;
import accessiblescheduling.exception.CorruptDataException;
import accessiblescheduling.exception.ProccessingException;
import accessiblescheduling.repositories.mongodb.MongoShiftRepository;
import accessiblescheduling.worker.ShiftWorker;

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

    public HashMap<String, ArrayList<Shift>> getShiftsPerEmployeePerMonthUnconflictingWithAssignedShifts(HashMap<String, ArrayList<Shift>> shiftsPerEmployee, int month, int year) throws CorruptDataException, ProccessingException{
		HashMap<String,ArrayList<Shift>> assignedShiftsPerEmployee = getAssignedShiftsPerEmployeeForMonth(month,year);
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
	
    public HashMap<String, ArrayList<Shift>> getAssignedShiftsPerEmployeeForMonth(int month,int year) throws ProccessingException {
    	Iterable<Shift> shifts = shiftRepository.findByStartMonthAndStartYear(month,year);
    	ArrayList<Shift> assignedShifts = ShiftWorker.getAssignedShifts(shifts);
    	HashMap<String,ArrayList<Shift>> assignedShiftsPerEmployee =ShiftWorker.getPrestaffedEmployeeShiftMap(assignedShifts);
    	
		return assignedShiftsPerEmployee;
	}
}