package org.cloudfoundry.samples.music.managers;
import java.util.ArrayList;

import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.worker.ShiftWorker;

import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShiftManager {
    private MongoShiftRepository shiftRepository;
    
    @Autowired
    public ShiftManager(MongoShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }
    
    public ArrayList<Shift> getRecurringShiftsForMonth(int selectedMonth){
    	return  ShiftWorker.getRecurringShifts(shiftRepository.findByStartMonth(selectedMonth));
	}
    
    public ArrayList<Shift> getPrestaffedSingleShiftsForMonth(int selectedMonth) throws ProccessingException {
    	ArrayList<Shift> singleShifts = ShiftWorker.getSingleShifts(shiftRepository.findByStartMonth(selectedMonth));
    	ArrayList<Shift> prestaffedSingleShifts = ShiftWorker.getPrestaffedShifts(singleShifts);

    	return prestaffedSingleShifts;
	}
    
    public ArrayList<Shift> getShiftsForMonth(int month){
		return (ArrayList<Shift>) shiftRepository.findByStartMonth(month);
	}
    
  	public ArrayList<Shift> getPrestaffedRecurringShiftsForMonth(int selectedMonth) throws ProccessingException {
    	Iterable<Shift> shiftsForMonth = shiftRepository.findByStartMonth(selectedMonth);
    	ArrayList<Shift> recurringShiftsForMonth = ShiftWorker.getRecurringShifts(shiftsForMonth);
//    	logger.error(recurringShiftsForMonth.size() + " recurringShiftsPerMonth");
    	ArrayList<Shift> prestaffedRecurringShifts = ShiftWorker.getPrestaffedRecurringShifts(recurringShiftsForMonth);
//    	logger.error(prestaffedRecurringShifts.size() + " prestaffedRecurringShiftsPerMonth");
		return prestaffedRecurringShifts;
	}
    
    public ArrayList<Shift> getUnassignedShiftsForMonth(int selectedMonth) {
    	ArrayList<Shift> unassignedShifts = ShiftWorker.getUnassignedShifts(shiftRepository.findByStartMonth(selectedMonth));

    	return unassignedShifts;
	}
    
    public ArrayList<Shift> getUnassignedNonEventShiftsForMonth(int month) {
		ArrayList<Shift> unassignedShifts = getUnassignedShiftsForMonth(month);
		ArrayList<Shift> unassignedNonEventShifts = ShiftWorker.getNonEventShifts(unassignedShifts);
		
		return unassignedNonEventShifts;
	}
    
  //weeks are considered to start at 0 with a maximum possible of 5, year assumed current year
    public ArrayList<Shift> getShiftsForWeekOfMonth(int week, int month) throws CorruptDataException{
    	return ShiftWorker.getShiftsForWeekOfMonth(shiftRepository.findByStartMonth(month),week,month);
    }
}