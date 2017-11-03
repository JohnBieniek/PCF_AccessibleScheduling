package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
public class ShiftSpec {
	@Test
	public void constructorInitializesVariables() {
		Shift shift = new Shift();
		
		assertNotNull(shift);
		assertFalse(shift.getAssigned());
		assertFalse(shift.getRecurring());
		assertFalse(shift.getEvent());//Shifts by default are not events, few events will exist
	}

	@Test
	public void getDurationFailsWhenNoStartDateIsPresent(){
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("18:00");
		
		shift.setEndDate("2017-08-15");
		
		try {
			assertNull(shift.getDuration());
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getDurationFailsWhenNoEndDateIsPresent(){
		
	}
	
	@Test
	public void getDurationProvidesTimeBetweenStartAndEndForDayShifts(){
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("18:00");
		
		//assertTrue(6.0==shift.getDuration());
	}
}
