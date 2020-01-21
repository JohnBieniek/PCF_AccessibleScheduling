package accessiblescheduling.manager;
import java.util.ArrayList;

import accessiblescheduling.domain.Shift;
import accessiblescheduling.exception.CorruptDataException;
import accessiblescheduling.exception.ProccessingException;
import accessiblescheduling.repositories.mongodb.MongoShiftRepository;
import accessiblescheduling.worker.ShiftWorker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShiftManager {
    private MongoShiftRepository shiftRepository;
    
    @Autowired
    public ShiftManager(MongoShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }
    
    public ArrayList<Shift> getRecurringShiftsForMonth(int selectedMonth,int selectedYear) throws ProccessingException{
    	return  ShiftWorker.getRecurringShifts(shiftRepository.findByStartMonthAndStartYear(selectedMonth,selectedYear));
	}
    
    public ArrayList<Shift> getPrestaffedShiftsForMonth(int selectedMonth,int selectedYear) throws ProccessingException{
    	return  ShiftWorker.getPrestaffedShifts(shiftRepository.findByStartMonthAndStartYear(selectedMonth,selectedYear));
	}
    
    public ArrayList<Shift> getPrestaffedSingleShiftsForMonth(int selectedMonth,int selectedYear) throws ProccessingException {
    	ArrayList<Shift> singleShifts = ShiftWorker.getSingleShifts(shiftRepository.findByStartMonthAndStartYear(selectedMonth,selectedYear));
    	ArrayList<Shift> prestaffedSingleShifts = ShiftWorker.getPrestaffedShifts(singleShifts);

    	return prestaffedSingleShifts;
	}
    
    public ArrayList<Shift> getShiftsForMonth(int month,int year){
		return (ArrayList<Shift>) shiftRepository.findByStartMonthAndStartYear(month,year);
	}
    
    public ArrayList<Shift> deleteShiftsForMonth(int month,int year){
		ArrayList<Shift> shifts = (ArrayList<Shift>) shiftRepository.findByStartMonthAndStartYear(month,year);
		
		for(Shift shift:shifts) {
			shiftRepository.delete(shift);
		}
		return shifts;
	}
    
  	public ArrayList<Shift> getPrestaffedRecurringShiftsForMonth(int selectedMonth,int selectedYear) throws ProccessingException {
    	Iterable<Shift> shiftsForMonth = shiftRepository.findByStartMonthAndStartYear(selectedMonth,selectedYear);
    	ArrayList<Shift> recurringShiftsForMonth = ShiftWorker.getRecurringShifts(shiftsForMonth);
//    	logger.error(recurringShiftsForMonth.size() + " recurringShiftsPerMonth");
    	ArrayList<Shift> prestaffedRecurringShifts = ShiftWorker.getPrestaffedRecurringShifts(recurringShiftsForMonth);
//    	logger.error(prestaffedRecurringShifts.size() + " prestaffedRecurringShiftsPerMonth");
		return prestaffedRecurringShifts;
	}
    
  	public ArrayList<Shift> getAssignedShiftsForMonth(int selectedMonth,int selectedYear) throws ProccessingException {
    	ArrayList<Shift> assignedShifts = ShiftWorker.getAssignedShifts(shiftRepository.findByStartMonthAndStartYear(selectedMonth,selectedYear));

    	return assignedShifts;
	}
  	
    public ArrayList<Shift> getUnassignedShiftsForMonth(int selectedMonth,int selectedYear) throws ProccessingException {
    	ArrayList<Shift> unassignedShifts = ShiftWorker.getUnassignedShifts(shiftRepository.findByStartMonthAndStartYear(selectedMonth,selectedYear));

    	return unassignedShifts;
	}
    
    public ArrayList<Shift> getUnassignedNonEventShiftsForMonth(int month,int year) throws ProccessingException {
		ArrayList<Shift> unassignedShifts = getUnassignedShiftsForMonth(month,year);
		ArrayList<Shift> unassignedNonEventShifts = ShiftWorker.getNonEventShifts(unassignedShifts);
		
		return unassignedNonEventShifts;
	}
    
  //weeks are considered to start at 0 with a maximum possible of 5, year assumed current year
    public ArrayList<Shift> getShiftsStartingWeekOfMonth(int week, int month,int year) throws CorruptDataException, ProccessingException{
    	return ShiftWorker.getShiftsStartingWeekOfMonth(shiftRepository.findByStartMonthAndStartYear(month,year),week,month,year);
    }
    
    public float getDurationOfShiftsStartingWeekOfMonth(int week, int month,int year) throws CorruptDataException, ProccessingException{
    	return ShiftWorker.getTotalShiftHours(ShiftWorker.getShiftsStartingWeekOfMonth(shiftRepository.findByStartMonthAndStartYear(month,year),week,month,year));
    }
}