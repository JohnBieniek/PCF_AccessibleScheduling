package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.*;

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
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("18:00");
		
		shift.setEndDate("2017-08-15");
		
		try {
			assertNull(shift.getDuration());
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getDurationFailsWhenNoEndDateIsPresent(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("18:00");
		
		shift.setStartDate("2017-08-15");
		
		try {
			assertNull(shift.getDuration());
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getDurationFailsWhenEndTimePreceedsStartTime(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("18:00");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-08-15");
		shift.setEndDate("2017-08-15");
		
		try {
			assertNull(shift.getDuration());
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getDurationFailsWhenEndDatePreceedsStartDate(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("18:00");
		
		shift.setStartDate("2017-08-15");
		shift.setEndDate("2017-08-14");
		
		try {
			assertNull(shift.getDuration());
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	//Trouble?
	@Test
	public void getDurationFailsWhenDurationExceeds24(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("13:00");
		
		shift.setStartDate("2017-08-15");
		shift.setEndDate("2017-08-16");
		
		try {
			float duration = shift.getDuration();
			System.out.println(duration);
			assertNull(duration);
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getDurationFailsWhenDurationExceeds24Two(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("22:00");
		
		shift.setStartDate("2017-08-15");
		shift.setEndDate("2017-08-16");
		
		try {
			float duration = shift.getDuration();
			System.out.println(duration);
			assertNull(duration);
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	@Test
	public void getDurationFailsWhenDurationExceeds24Three(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("01:00");
		shift.setEndTime("22:00");
		
		shift.setStartDate("2017-08-15");
		shift.setEndDate("2017-08-16");
		
		try {
			float duration = shift.getDuration();
			System.out.println(duration);
			assertNull(duration);
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getDurationProvidesTimeBetweenStartAndEndForDayShifts(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("18:00");
		
		shift.setStartDate("2017-08-15");
		shift.setEndDate("2017-08-15");
		
		try {
			assertTrue(shift.getDuration()==6);
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertFalse(exception);
	}
	
	//Trouble
	@Test
	public void getDurationProvidesTimeBetweenStartAndEndForDayOvernightShifts(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-08-15");
		shift.setEndDate("2017-08-16");
		
		try {
			assertTrue(shift.getDuration()==24);
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertFalse(exception);
	}
	
	@Test
	public void getDurationProvidesTimeBetweenStartAndEndForDayOvernightShiftsTwo(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("10:00");
		
		shift.setStartDate("2017-08-15");
		shift.setEndDate("2017-08-16");
		
		try {
			assertTrue(shift.getDuration()==22);
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertFalse(exception);
	}
	
	@Test
	public void getOvernightIsTrueForShiftsEndingTheNextDay(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("10:00");
		
		shift.setStartDate("2017-08-15");
		shift.setEndDate("2017-08-16");
		
		try {
			assertTrue(shift.getOvernight());
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertFalse(exception);
	}
	
	@Test
	public void getOvernightIsFalseForShiftsEndingTheSameDay(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("14:00");
		
		shift.setStartDate("2017-08-15");
		shift.setEndDate("2017-08-15");
		
		try {
			assertFalse(shift.getOvernight());
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertFalse(exception);
	}
	
	@Test
	public void isWeekendIsTrueWhenStartDayIsSaturday(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-11-04");
		shift.setEndDate("2017-11-04");
		
		try {
			assertTrue(shift.isWeekend());
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertFalse(exception);
	}
	
	@Test
	public void isWeekendIsTrueWhenStartDayIsSunday(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-11-05");
		shift.setEndDate("2017-11-06");
		
		try {
			assertTrue(shift.isWeekend());
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertFalse(exception);
	}
	
	@Test
	public void isWeekendIsTrueWhenEndDayIsSunday(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-11-03");
		shift.setEndDate("2017-11-04");
		
		try {
			assertTrue(shift.isWeekend());
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertFalse(exception);
	}
	
	@Test
	public void isWeekendIsFalseWeekdays(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-11-01");
		shift.setEndDate("2017-11-02");
		
		try {
			assertFalse(shift.isWeekend());
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertFalse(exception);
	}
	
	@Test
	public void isWeekendFailsWithNoStartDay(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.isWeekend();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void isWeekendFailsWithNoEndDay(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-01-11");
		
		try {
			shift.isWeekend();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	//IsWeekend
	//GetSTart and ends local time and date 
}
