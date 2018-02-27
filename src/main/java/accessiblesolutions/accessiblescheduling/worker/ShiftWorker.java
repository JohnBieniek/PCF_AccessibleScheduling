package accessiblesolutions.accessiblescheduling.worker;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.util.Util;
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
	    			prestaffedShift.setAssignmentReason("Prestaffed shift with an available employee");
	    			prestaffedShift.setAssigned(true);
	    			
	    			assignedPrestaffedShifts.add(prestaffedShift);
	    		}
	    		
	    		prestaffedShiftsPerEmployee.put(employeeId, prestaffedShiftsForSelectedEmployee);//TODO can this go?
    		}
    	}
    	
		return assignedPrestaffedShifts;
	}

    
    //TODO test
    public static ArrayList<Shift> getAssignedShiftsFor(Iterable<Shift> shifts, String employeeId){
    	ArrayList<Shift> assignedShifts = new ArrayList<Shift>();

    	if(null == shifts){
    		return assignedShifts;
    	}
    	
    	for(Shift shift:shifts){
    		if(null!=shift && null !=shift.getStaffId() && shift.getStaffId().equalsIgnoreCase(employeeId)){
    			assignedShifts.add(shift);
    		}
    	}
    	
		return assignedShifts;
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
    				//throw new ProccessingException(Shift.class,"shift");//This method is only for prestaffed Shifts. This shift shoudln't be here.
    			}
    			else{
    			
		    		ArrayList<Shift> prestaffedRecuringShiftsForSelectedEmployee = new ArrayList<Shift>();
		    		if(prestaffedRecuringShiftsPerEmployee.containsKey(shift.getRequestedStaffId())){
		    			prestaffedRecuringShiftsForSelectedEmployee=prestaffedRecuringShiftsPerEmployee.get(shift.getRequestedStaffId());
		    		}
		    		prestaffedRecuringShiftsForSelectedEmployee.add(shift);
		    		prestaffedRecuringShiftsPerEmployee.put(shift.getRequestedStaffId(),prestaffedRecuringShiftsForSelectedEmployee);
    			}
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
    		if(null!=shift && !shift.getEvent()){//TODO consider changing to throw exception on null shifts like the others
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
	
	public static ArrayList<Shift> getSameDayShifts(ArrayList<Shift> shifts) throws CorruptDataException, ProccessingException{
		ArrayList<Shift> sameDayShifts = new ArrayList<Shift>();
		
		if(null==shifts || shifts.size()==0){
			return sameDayShifts;
		}
		for(Shift shift:shifts){
			if(null==shift){
				throw new ProccessingException("Null shift present");
			}
			if(!shift.getOvernight()){
				sameDayShifts.add(shift);
			}
		}
		
		return sameDayShifts;
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
	
	public static ArrayList<Shift> getPrestaffedRecurringShifts(Iterable<Shift> shifts) throws ProccessingException {
		return getPrestaffedShifts(getRecurringShifts(shifts));
	}

	public static ArrayList<Shift> getPrestaffedShifts(Iterable<Shift> shifts) throws ProccessingException{
		ArrayList<Shift> prestaffedShifts = new ArrayList<Shift>();
		
		if(null==shifts || ((ArrayList<Shift>)shifts).size()==0){
			return prestaffedShifts;
		}
		
    	for(Shift shift: shifts){
    		if(null==shift){
    			throw new ProccessingException("Null shift present");
    		}
    		
    		if(null!=shift.getRequestedStaffId() && !shift.getRequestedStaffId().isEmpty()){
    			prestaffedShifts.add(shift);
    		}
    	}

    	return prestaffedShifts;
	}
	public static ArrayList<Shift> getPrestaffedSingleShifts(Iterable<Shift> shifts) throws ProccessingException {
    	ArrayList<Shift> singleShifts = getSingleShifts(shifts);
    	ArrayList<Shift> prestaffedSingleShifts = getPrestaffedShifts(singleShifts);
    	
		return prestaffedSingleShifts;
	}
	public static ArrayList<Shift> getRecurringShifts(Iterable<Shift> shifts) throws ProccessingException{
		ArrayList<Shift> recurringShifts = new ArrayList<Shift>();
		
		if(null==shifts || ((ArrayList<Shift>)shifts).size()==0){
			return recurringShifts;
		}
		
    	for(Shift shift:shifts){
    		if(null==shift){
    			throw new ProccessingException("Null shift present");
    		}
    		
    		if(shift.getRecurring()){
    			recurringShifts.add(shift);
    		}
    	}

    	return recurringShifts;
	}
	
    public static ArrayList<Shift> getShiftsStartingWeekOfMonth(Iterable<Shift> shifts, int week, int month, int year) throws CorruptDataException, ProccessingException{
    	ArrayList<Shift> shiftsForWeek = new ArrayList<Shift>();
		
    	if(month<1||month>12){
			throw new ProccessingException(month+ " isn't a valid month");
		}
    	
    	if(week<0||week>5){
			throw new ProccessingException(week+ " isn't a valid week");
		}
    	
    	for(Shift shift: shifts){
    		if(null==shift){
				throw new ProccessingException("Null shift present");
			}
    		
    		if(shift.getStartWeek()==week && shift.getStartsLocalDate().getMonthValue()==month && shift.getStartsLocalDate().getYear()==year){
    			shiftsForWeek.add(shift);
    		}
    	}
    	
    	return shiftsForWeek;
    }


    public static ArrayList<Shift> getShiftsStartingTheLastDayOfMonth(ArrayList<Shift> shifts, int month) throws CorruptDataException, ProccessingException{
		ArrayList<Shift> shiftsForTheLastDay = new ArrayList<Shift>();
		
		if(null==shifts|| shifts.size()==0){
			return shiftsForTheLastDay;
		}
		
		if(month<1||month>12){
			throw new ProccessingException(month+ " isn't a valid month");
		}
		
		for(Shift shift:shifts){
			if(null==shift){
				throw new ProccessingException("Null shift present");
			}
			
			if(shift.getStartsLocalDate().getMonth().getValue()==month){
				if(shift.getStartsLocalDate().plusDays(1).getMonthValue()!=month){
					shiftsForTheLastDay.add(shift);
				}
			}
		}
		
		return shiftsForTheLastDay;
	}

    public static ArrayList<Shift> getSingleShifts(Iterable<Shift> shifts) throws ProccessingException {
    	ArrayList<Shift> singleShifts = new ArrayList<Shift>();
    	
    	if(null==shifts || ((ArrayList<Shift>)shifts).size()==0){
			return singleShifts;
		}
    	
    	for(Shift shift:shifts){
    		if(null==shift){
    			throw new ProccessingException("Null shift present");
    		}
    		
    		if(!shift.getRecurring() && !shift.getEvent()){
    			singleShifts.add(shift);
    		}
    	}
    	
		return singleShifts;
	}
    
    public static ArrayList<Shift> getUpcomingShifts(Iterable<Shift> shifts) throws ProccessingException, CorruptDataException {
    	ArrayList<Shift> upcomingShifts = new ArrayList<Shift>();
    	
    	if(null==shifts || ((ArrayList<Shift>)shifts).size()==0){
			return upcomingShifts;
		}
    	LocalDateTime now = LocalDateTime.now();
    	for(Shift shift:shifts){
    		if(null==shift){
    			throw new ProccessingException("Null shift present");
    		}
    		
    		if(shift.getStartsLocalDateTime().isAfter(now)){
    			upcomingShifts.add(shift);
    		}
    	}
    	
		return upcomingShifts;
	}
    
    public static ArrayList<Shift> getShiftsAssignedWeekendBefore(Shift shift, ArrayList<Shift> shifts) throws CorruptDataException, ProccessingException{
    	if(null==shift){
    		throw new ProccessingException("No shift provided to getShiftsAssignedWeekendBefore");
    	}
    	if(null==shifts){
    		throw new ProccessingException("No shifts provided to getShiftsAssignedWeekendBefore");
    	}
    	
    	ArrayList<Shift> result = new ArrayList<Shift>();
    	
    	if(shifts.size()==0){
    		return result;
    	}
    	
    	ArrayList<Shift> assignedShifts=getAssignedShiftsFor(shifts,shift.getStaffId());
    	
    	ArrayList<Shift> assignedWeekendShifts = getWeekendShifts(assignedShifts);
    	
    	LocalDateTime wednesdayPrior = null;
    	if(shift.getEndsLocalDateTime().getDayOfWeek().getValue()==1){
    		wednesdayPrior = shift.getEndsLocalDateTime().minusDays(5);
    	}
    	else if(shift.getEndsLocalDateTime().getDayOfWeek().getValue()==7){
    		wednesdayPrior = shift.getEndsLocalDateTime().minusDays(4);
    	}
    	else if(shift.getEndsLocalDateTime().getDayOfWeek().getValue()==6){
    		wednesdayPrior = shift.getEndsLocalDateTime().minusDays(3);
    	}
    	else if(shift.getEndsLocalDateTime().getDayOfWeek().getValue()==5){
    		wednesdayPrior = shift.getEndsLocalDateTime().minusDays(2);
    	}
    	else return new ArrayList<Shift>();
    	
    	ArrayList<Shift> assignedPriorWeekendShifts = getShiftsBefore(assignedWeekendShifts,wednesdayPrior);
    	
    	ArrayList<Shift> assignedShiftsLastWeekend = getShiftsAfter(assignedPriorWeekendShifts,wednesdayPrior.minusWeeks(1));
    	
    	return assignedShiftsLastWeekend;
    }
    
    public static float getTotalShiftHours(ArrayList<Shift> shifts) throws CorruptDataException{
    	float duration = 0;
    	
    	for(Shift shift:shifts){
    		duration+=shift.getDuration();
    	}
    	return duration;
    }
    
    public static ArrayList<Shift> getShiftsAfter(ArrayList<Shift> shifts, LocalDateTime time) throws CorruptDataException, ProccessingException{
    	ArrayList<Shift> laterShifts = new ArrayList<Shift>();
    	
    	if(null==time){
    		throw new ProccessingException("No time supplied to getShiftsAfter()");
    	}
    	
    	if(null==shifts||shifts.size()<1){
    		return laterShifts;
    	}
    	
    	for(Shift shift:shifts){
    		if(shift.getStartsLocalDateTime().isAfter(time)){
    			laterShifts.add(shift);
    		}
    	}
    	
    	return laterShifts;
    }
    
    public static ArrayList<Shift> getShiftsBefore(ArrayList<Shift> shifts, LocalDateTime time) throws CorruptDataException, ProccessingException{
    	ArrayList<Shift> priorShifts = new ArrayList<Shift>();

    	if(null==time){
    		throw new ProccessingException("No time supplied to getShiftsBefore()");
    	}
    	
    	if(null==shifts||shifts.size()<1){
    		return priorShifts;
    	}
    	
    	for(Shift shift:shifts){
    		if(shift.getEndsLocalDateTime().isBefore(time)){
    			priorShifts.add(shift);
    		}
    	}
    	
    	return priorShifts;
    }
    
    public static ArrayList<Shift> getUnassignedShifts(Iterable<Shift> shifts) throws ProccessingException {
    	ArrayList<Shift> unassignedShifts = new ArrayList<Shift>();
    	
    	if(null==shifts || ((ArrayList<Shift>)shifts).size()==0){
			return unassignedShifts;
		}
    	
    	for(Shift shift:shifts){
    		if(null==shift){
    			throw new ProccessingException("Null shift present");
    		}
    		
    		if(!shift.getAssigned()){
    			unassignedShifts.add(shift);
    		}
    	}
    	
		return unassignedShifts;
	}

	public static ArrayList<Shift> getWeekdayShifts(ArrayList<Shift> shifts) throws CorruptDataException, ProccessingException{
		ArrayList<Shift> weekdayShifts = new ArrayList<Shift>();
    	
		if(null==shifts || shifts.size()==0){
			return weekdayShifts;
		}
		
    	for(Shift shift : shifts){
    		if(null==shift){
    			throw new ProccessingException("Null shift present");
    		}
    		
    		if(!shift.isWeekend()){
    			weekdayShifts.add(shift);
    		}
    	}
    	
    	return weekdayShifts;
	}

	public static ArrayList<Shift> getWeekendShifts(ArrayList<Shift> shifts) throws CorruptDataException, ProccessingException{
    	ArrayList<Shift> weekendShifts = new ArrayList<Shift>();
    	
    	if(null==shifts || shifts.size()==0){
			return weekendShifts;
		}
    	
    	for(Shift shift : shifts){
    		if(null==shift){
    			throw new ProccessingException("Null shift present");
    		}
    		
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

    
    public static boolean isAlmostOverlapping(Shift baseShift, Shift comparingShift) throws CorruptDataException, ProccessingException {
		if(null==baseShift||null==comparingShift)return false;
		if(null!=baseShift.getClientId() && (baseShift.getClientId().equals(comparingShift.getClientId()))){
			return Util.isOverlapping(
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
	
	public static boolean isOverlapping(Shift baseShift, Shift comparingShift) throws CorruptDataException, ProccessingException {
		if(null==baseShift||null==comparingShift){
			throw new ProccessingException("Null shift present");
		}
		
		return Util.isOverlapping(
				baseShift.getStartsLocalDateTime(),
				baseShift.getEndsLocalDateTime(),
				comparingShift.getStartsLocalDateTime(),
				comparingShift.getEndsLocalDateTime()
				);
	}

}