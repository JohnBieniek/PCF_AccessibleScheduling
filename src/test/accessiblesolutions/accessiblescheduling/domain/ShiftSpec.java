package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.*;

import java.time.LocalDate;

import org.junit.Test;

import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;

//Up to date as of 11/4
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

	@Test
	public void getStartsLocalDateFailsWithNoStartDay(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getStartsLocalDate();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getStartsLocalDateFailsWithPoorlyFormatedStartDay1(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		shift.setStartDate("2017-02/11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getStartsLocalDate();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	@Test
	public void getStartsLocalDateFailsWithPoorlyFormatedStartDay2(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		shift.setStartDate("2017/02/11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getStartsLocalDate();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	@Test
	public void getStartsLocalDateFailsWithPoorlyFormatedStartDay3(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		shift.setStartDate("2017-22-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getStartsLocalDate();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getStartsLocalDateFailsWithPoorlyFormatedStartDay4(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		shift.setStartDate("2017-XY-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getStartsLocalDate();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getStartsLocalDateConvertsAGoodStartDay(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		shift.setStartDate("2017-03-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			LocalDate date = shift.getStartsLocalDate();
			assertTrue(date.getYear()==2017);
			assertTrue(date.getMonthValue()==3);
			assertTrue(date.getDayOfMonth()==11);
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertFalse(exception);
	}
	
	@Test
	public void getStartsLocalDateTimeFailsWithNoStartTime(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-02-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getStartsLocalDateTime();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getStartsLocalDateTimeFailsWithPoorlyFormatedStartTime(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12-00");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-02-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getStartsLocalDateTime();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getStartsLocalDateTimeFailsWithPoorlyFormatedStartTime2(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("11:9");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-02-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getStartsLocalDateTime();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getStartsLocalDateTimeFailsWithPoorlyFormatedStartTime3(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("1:59");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-02-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getStartsLocalDateTime();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getStartsLocalDateTimeFailsWithPoorlyFormatedStartTime4(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime(":59");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-02-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getStartsLocalDateTime();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getStartsLocalDateTimeSucceedsWithValidStartTimeAndDate(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-02-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getStartsLocalDateTime();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertFalse(exception);
	}
	
	//End
	@Test
	public void getEndsLocalDateFailsWithNoEndDay(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-02-11");
		
		try {
			shift.getEndsLocalDate();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getEndsLocalDateFailsWithPoorlyFormatedEndDay1(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		shift.setEndDate("2017-02/11");		
		shift.setStartDate("2017-02-11");
		
		try {
			shift.getEndsLocalDate();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	@Test
	public void getEndsLocalDateFailsWithPoorlyFormatedEndDay2(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		shift.setEndDate("2017/02/11");		
		shift.setStartDate("2017-02-11");
		
		try {
			shift.getEndsLocalDate();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	@Test
	public void getEndsLocalDateFailsWithPoorlyFormatedEndDay3(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		shift.setEndDate("2017-22-11");		
		shift.setStartDate("2017-02-11");
		
		try {
			shift.getEndsLocalDate();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getEndsLocalDateFailsWithPoorlyFormatedEndDay4(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		shift.setEndDate("2017-XY-11");		
		shift.setStartDate("2017-02-11");
		
		try {
			shift.getEndsLocalDate();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getEndsLocalDateConvertsAGoodEndDay(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		shift.setEndDate("2017-03-11");		
		shift.setStartDate("2017-02-11");
		
		try {
			LocalDate date = shift.getEndsLocalDate();
			assertTrue(date.getYear()==2017);
			assertTrue(date.getMonthValue()==3);
			assertTrue(date.getDayOfMonth()==11);
		} catch (CorruptDataException e) {
			exception=true;
		}
		
		assertFalse(exception);
	}
	
	@Test
	public void getEndsLocalDateTimeFailsWithNoEndTime(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		
		shift.setStartDate("2017-02-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getEndsLocalDateTime();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getEndsLocalDateTimeFailsWithPoorlyFormatedEndTime(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setEndTime("12-00");
		shift.setStartTime("12:00");
		
		shift.setStartDate("2017-02-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getEndsLocalDateTime();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getEndsLocalDateTimeFailsWithPoorlyFormatedEndTime2(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setEndTime("11:9");
		shift.setStartTime("12:00");
		
		shift.setStartDate("2017-02-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getEndsLocalDateTime();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getEndsLocalDateTimeFailsWithPoorlyFormatedEndTime3(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setEndTime("1:59");
		shift.setStartTime("12:00");
		
		shift.setStartDate("2017-02-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getEndsLocalDateTime();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getEndsLocalDateTimeFailsWithPoorlyFormatedEndTime4(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setEndTime(":59");
		shift.setStartTime("12:00");
		
		shift.setStartDate("2017-02-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getEndsLocalDateTime();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void getEndsLocalDateTimeSucceedsWithValidEndTimeAndDate(){
		boolean exception = false;
		Shift shift = new Shift();
		
		shift.setStartTime("12:00");
		shift.setEndTime("12:00");
		
		shift.setStartDate("2017-02-11");		
		shift.setEndDate("2017-02-11");
		
		try {
			shift.getEndsLocalDateTime();
		} catch (CorruptDataException e) {
			exception=true;
		}
		assertFalse(exception);
	}
}
