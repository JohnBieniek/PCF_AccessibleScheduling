package accessiblesolutions.accessiblescheduling.worker;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
@Component
public final class ShiftWorker {
    public static ArrayList<Shift> assignRequestedStaff(HashMap<String, ArrayList<Shift>> prestaffedShiftsPerEmployee) throws ProccessingException {
    	ArrayList<Shift> assignedPrestaffedShifts = new ArrayList<Shift>();
    	
    	if(null == prestaffedShiftsPerEmployee || prestaffedShiftsPerEmployee.isEmpty()){
    		return assignedPrestaffedShifts;
    	}
    	
    	for(String employeeId:prestaffedShiftsPerEmployee.keySet()){
    		ArrayList<Shift> prestaffedShiftsForSelectedEmployee = prestaffedShiftsPerEmployee.get(employeeId);
    		
    		if(null != prestaffedShiftsForSelectedEmployee && !prestaffedShiftsForSelectedEmployee.isEmpty()){
	    		for(Shift prestaffedShift:prestaffedShiftsForSelectedEmployee){
	    			if( null == prestaffedShift.getRequestedStaffId() || 
	    				null == prestaffedShift.getRequestedStaffName() || 
	    				!prestaffedShift.getRequestedStaffId().equalsIgnoreCase(employeeId)){
	    				throw new ProccessingException(Shift.class,prestaffedShift);//This method only for prestaffed, if here, these aren't prestaffed. 
	    			}
	    			//TODO add logic to ensure they have proper qualification to work with this client on this shift, perhaps in a helper method
	    			prestaffedShift.setStaffId(prestaffedShift.getRequestedStaffId());
	    			prestaffedShift.setStaffName(prestaffedShift.getRequestedStaffName());
	    			prestaffedShift.setAssigned(true);
	    			
	    			assignedPrestaffedShifts.add(prestaffedShift);
	    		}
	    		
	    		prestaffedShiftsPerEmployee.put(employeeId, prestaffedShiftsForSelectedEmployee);//TODO can this go?
    		}
    	}
    	
		return assignedPrestaffedShifts;
	}

    
    public static ArrayList<Shift> getAssignedShifts(Iterable<Shift> shifts){
    	ArrayList<Shift> assignedShifts = new ArrayList<Shift>();
    	
    	if(null == shifts){
    		return assignedShifts;
    	}
    	
    	for(Shift shift:shifts){
    		if(null!=shift && (shift.getAssigned()||shift.getStaffName()!=null)){
    			assignedShifts.add(shift);
    		}
    	}
    	
		return assignedShifts;
    }
    
    public static HashMap<String, ArrayList<Shift>> getPrestaffedEmployeeShiftMap(ArrayList<Shift> shifts) throws ProccessingException {
    	HashMap<String,ArrayList<Shift>> prestaffedRecuringShiftsPerEmployee = new HashMap<String,ArrayList<Shift>>();
    	
    	if(null==shifts){
    		return prestaffedRecuringShiftsPerEmployee;
    	}
    	
    	for(Shift shift: shifts){
    		if(shift!=null){
    			if(shift.getRequestedStaffId()==null){
    				throw new ProccessingException(Shift.class,shift);//This method is only for prestaffed Shifts. This shift shoudln't be here.
    			}
    			
	    		ArrayList<Shift> prestaffedRecuringShiftsForSelectedEmployee = new ArrayList<Shift>();
	    		if(prestaffedRecuringShiftsPerEmployee.containsKey(shift.getRequestedStaffId())){
	    			prestaffedRecuringShiftsForSelectedEmployee=prestaffedRecuringShiftsPerEmployee.get(shift.getRequestedStaffId());
	    		}
	    		prestaffedRecuringShiftsForSelectedEmployee.add(shift);
	    		prestaffedRecuringShiftsPerEmployee.put(shift.getRequestedStaffId(),prestaffedRecuringShiftsForSelectedEmployee);
    		}
    	}
    	prestaffedRecuringShiftsPerEmployee.remove(null);
    	
		return prestaffedRecuringShiftsPerEmployee;
	}
    
	public static ArrayList<Shift> getNonEventShifts(Iterable<Shift> shifts) {
    	ArrayList<Shift> nonEventShifts = new ArrayList<Shift>();
    	
    	if(null == shifts){
    		return nonEventShifts;
    	}
    	
    	for(Shift shift:shifts){
    		if(null!=shift && !shift.getEvent()){
    			nonEventShifts.add(shift);
    		}
    	}
    	
		return nonEventShifts;
	}
    
	//No null input, null shifts, or invalid shifts (missing start/end)
	public static HashMap<String, ArrayList<Shift>> getNonoverlapingShiftsPerEmployee(HashMap<String, ArrayList<Shift>> shiftsPerEmployee) throws CorruptDataException, ProccessingException {
    	HashMap<String,ArrayList<Shift>> unconflictedShiftsPerEmployee = new HashMap<String, ArrayList<Shift>>();
    	
    	if(null==shiftsPerEmployee){
    		return unconflictedShiftsPerEmployee;
    	}
    	
    	for(String employeeId:shiftsPerEmployee.keySet()){
    		ArrayList<Shift> shiftsForSelectedEmployee = shiftsPerEmployee.get(employeeId);
	    	ArrayList<Shift> unconflictedShiftsForSelectedEmployee = new ArrayList<Shift>();
	    	
	    	int x=-1;
    		int y;
    		
    		if(shiftsForSelectedEmployee==null){
				throw new ProccessingException("Null shifts");
    		}
    		
    		for(Shift baseShift: shiftsForSelectedEmployee){
    			if(baseShift==null){
    				throw new ProccessingException("Null shift present");
        		}
    			
    			int conflicts = 0;
    			x++;
    			y=-1;
    			for(Shift comparingShift: shiftsForSelectedEmployee){
    				if(comparingShift==null){
        				throw new ProccessingException(Shift.class,baseShift);
            		}
    				y++;
    				
    				if(x!=y && ShiftWorker.isAlmostOverlapping(baseShift,comparingShift)){
    					conflicts++;
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
	
    public static ArrayList<Shift> getOvernightShifts(ArrayList<Shift> shifts) throws CorruptDataException, ProccessingException{
		ArrayList<Shift> overnightShifts = new ArrayList<Shift>();
		
		if(null==shifts || shifts.size()==0){
			return overnightShifts;
		}
		for(Shift shift:shifts){
			if(null==shift){
				throw new ProccessingException("Null shift present");
			}
			if(shift.getOvernight()){
				overnightShifts.add(shift);
			}
		}
		
		return overnightShifts;
	}
	
	public static ArrayList<Shift> getPrestaffedRecurringShifts(Iterable<Shift> shifts) {
		return getPrestaffedShifts(getRecurringShifts(shifts));
	}

	public static ArrayList<Shift> getPrestaffedShifts(Iterable<Shift> shifts){
		ArrayList<Shift> prestaffedShifts = new ArrayList<Shift>();
    	for(Shift shift: shifts){
    		if(null!=shift.getRequestedStaffId() && !shift.getRequestedStaffId().isEmpty()){
    			prestaffedShifts.add(shift);
    		}
    	}
//    	logger.error(prestaffedShifts.size() + " prestaffedShifts found");
    	return prestaffedShifts;
	}
	public static ArrayList<Shift> getPrestaffedSingleShifts(Iterable<Shift> shifts) {
    	ArrayList<Shift> singleShifts = getSingleShifts(shifts);
    	ArrayList<Shift> prestaffedSingleShifts = getPrestaffedShifts(singleShifts);
    	
		return prestaffedSingleShifts;
	}
	public static ArrayList<Shift> getRecurringShifts(Iterable<Shift> shifts){
		ArrayList<Shift> recurringShifts = new ArrayList<Shift>();
    	for(Shift shift:shifts){
    		if(shift.getRecurring()){
    			recurringShifts.add(shift);
    		}
    	}
//    	logger.error(recurringShifts.size() + " recurringShifts found");
    	return recurringShifts;
	}
	//weeks are considered to start at 0 with a maximum possible of 5, year assumed current year
    public static ArrayList<Shift> getShiftsForWeekOfMonth(Iterable<Shift> shifts, int week, int month) throws CorruptDataException{
    	ArrayList<Shift> shiftsForWeek = new ArrayList<Shift>();
		
    	for(Shift shift: shifts){
    		if(shift.getStartWeek()==week){
    			shiftsForWeek.add(shift);
    		}
    	}
    	
    	return shiftsForWeek;
    }


    public static ArrayList<Shift> getShiftsStartingTheLastDayOfMonth(ArrayList<Shift> shifts, int month) throws CorruptDataException{
		ArrayList<Shift> shiftsForTheLastDay = new ArrayList<Shift>();
		
		for(Shift shift:shifts){
			if(shift.getStartMonth()==month){
				if(shift.getStartsLocalDate().plusDays(1).getMonthValue()!=month){
					shiftsForTheLastDay.add(shift);
				}
			}
		}
		
		return shiftsForTheLastDay;
	}

    public static ArrayList<Shift> getSingleShifts(Iterable<Shift> shifts) {
    	ArrayList<Shift> singleShifts = new ArrayList<Shift>();
    	
    	for(Shift shift:shifts){
    		if(!shift.getRecurring() && !shift.getEvent()){
    			singleShifts.add(shift);
    		}
    	}
    	
		return singleShifts;
	}
    
    public static ArrayList<Shift> getUnassignedShifts(Iterable<Shift> shifts) {
    	ArrayList<Shift> unassignedShifts = new ArrayList<Shift>();
    	
    	for(Shift shift:shifts){
    		if(!shift.getAssigned()){
    			unassignedShifts.add(shift);
    		}
    	}
    	
		return unassignedShifts;
	}

	public static ArrayList<Shift> getWeekdayShifts(ArrayList<Shift> shifts) throws CorruptDataException{
		ArrayList<Shift> weekdayShifts = new ArrayList<Shift>();
    	
    	for(Shift shift : shifts){
    		if(!shift.isWeekend()){
    			weekdayShifts.add(shift);
    		}
    	}
    	
    	return weekdayShifts;
	}

	public static ArrayList<Shift> getWeekendShifts(ArrayList<Shift> shifts) throws CorruptDataException{
    	ArrayList<Shift> weekendShifts = new ArrayList<Shift>();
    	
    	for(Shift shift : shifts){
    		if(shift.isWeekend()){
    			weekendShifts.add(shift);
    		}
    	}
    	
    	return weekendShifts;
	}

	public static boolean isAlmostOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
		boolean overlapping = false;
		
		int minutes = start1.getMinute();
		
		if(minutes<30){
			int hour = start1.getHour();
			if(hour==0){
				minutes = minutes+30;
				hour=23;
				start1=start1.withHour(hour).withMinute(minutes).minusDays(1);
			}
		}
		else{
			start1=start1.minusMinutes(30);
		}
		
		if(minutes>30){
			int hour = start1.getHour();
			if(hour==23){
				minutes = minutes-30;
				hour=0;
				start1=start1.withHour(hour).withMinute(minutes).plusDays(1);
			}
		}
		else{
			start1=start1.minusMinutes(30);
		}
		overlapping = start1.isBefore(end2) && end1.isAfter(start2);
		
		return overlapping;
	}

    
    public static boolean isAlmostOverlapping(Shift baseShift, Shift comparingShift) throws CorruptDataException {
		if(null==baseShift||null==comparingShift)return false;
		if(null!=baseShift.getClientId() && (baseShift.getClientId().equals(comparingShift.getClientId()))){
			return isOverlapping(
				baseShift.getStartsLocalDateTime(),
				baseShift.getEndsLocalDateTime(),
				comparingShift.getStartsLocalDateTime(),
				comparingShift.getEndsLocalDateTime()
				);
		}

		return isAlmostOverlapping(
				baseShift.getStartsLocalDateTime(),
				baseShift.getEndsLocalDateTime(),
				comparingShift.getStartsLocalDateTime(),
				comparingShift.getEndsLocalDateTime()
				);
	}
    
	public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
		boolean overlapping = false;
		
		overlapping = start1.isBefore(end2) && end1.isAfter(start2);
		
		return overlapping;
	}
	
	
	public static boolean isOverlapping(Shift baseShift, Shift comparingShift) throws CorruptDataException {
		if(null==baseShift||null==comparingShift)return false;
		
		return isOverlapping(
				baseShift.getStartsLocalDateTime(),
				baseShift.getEndsLocalDateTime(),
				comparingShift.getStartsLocalDateTime(),
				comparingShift.getEndsLocalDateTime()
				);
	}

}