package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.*;

import java.time.DayOfWeek;

import org.junit.Test;
public class RecurringShiftNeedSpec {
	//The Day variable is used to load the UI with a sortable day. It is computed at each request from start day
	@Test
	public void getDayReturnsTheStartDayProperlyForSunday() {
		RecurringShiftNeed request = new RecurringShiftNeed();
		request.setStartDay(DayOfWeek.SUNDAY.toString());
		assertEquals(0,request.getDay());
	}
	
	@Test
	public void getDayReturnsTheStartDayProperlyForSaturday() {
		RecurringShiftNeed request = new RecurringShiftNeed();
		request.setStartDay(DayOfWeek.SATURDAY.toString());
		assertEquals(DayOfWeek.SATURDAY.getValue(),request.getDay());
	}
	
	@Test
	public void isValidFalseForMissingStartTime() {
		RecurringShiftNeed request = new RecurringShiftNeed();
		request.setClientName("test");
		request.setFixedStaff(true);
		request.setStaffId("Sample");
		request.setEndTime("11:11");
		
		request.setStartDay(DayOfWeek.SATURDAY.toString());
		request.setEndDay(DayOfWeek.SATURDAY.toString());
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidFalseForMissingEndTime() {
		RecurringShiftNeed request = new RecurringShiftNeed();
		request.setClientName("test");
		request.setFixedStaff(true);
		request.setStaffId("Sample");
		request.setStartTime("11:11");
		
		request.setStartDay(DayOfWeek.SATURDAY.toString());
		request.setEndDay(DayOfWeek.SATURDAY.toString());
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidFalseForMissingStartDay() {
		RecurringShiftNeed request = new RecurringShiftNeed();
		request.setClientName("test");
		request.setFixedStaff(true);
		request.setStaffId("Sample");
		request.setStartTime("11:11");
		request.setEndTime("12:11");
		request.setEndDay(DayOfWeek.SATURDAY.toString());
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidFalseForMissingEndDay() {
		RecurringShiftNeed request = new RecurringShiftNeed();
		request.setClientName("test");
		request.setFixedStaff(true);
		request.setStaffId("Sample");
		request.setStartTime("11:11");
		request.setEndTime("12:11");
		request.setStartDay(DayOfWeek.SATURDAY.toString());
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidWorksCrossingWeeks() {
		RecurringShiftNeed request = new RecurringShiftNeed();
		request.setClientName("test");
		request.setFixedStaff(true);
		request.setStaffId("Sample");
		request.setStartTime("11:11");
		request.setEndTime("11:10");
		request.setStartDay(DayOfWeek.SATURDAY.toString());

		request.setEndDay(DayOfWeek.SUNDAY.toString());
		assertTrue(request.isValid());
	}
	
	@Test
	public void isValidFalseNegativeDuration() {
		RecurringShiftNeed request = new RecurringShiftNeed();
		request.setClientName("test");
		request.setFixedStaff(true);
		request.setStaffId("Sample");
		request.setStartTime("11:11");
		request.setEndTime("11:10");
		request.setStartDay(DayOfWeek.SATURDAY.toString());

		request.setEndDay(DayOfWeek.SATURDAY.toString());
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidFalseOver24Hours() {
		RecurringShiftNeed request = new RecurringShiftNeed();
		request.setClientName("test");
		request.setFixedStaff(true);
		request.setStaffId("Sample");
		request.setStartTime("11:11");
		request.setEndTime("11:12");
		request.setStartDay(DayOfWeek.SATURDAY.toString());

		request.setEndDay(DayOfWeek.SUNDAY.toString());
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidFalseForNoClient() {
		RecurringShiftNeed request = new RecurringShiftNeed();
		request.setFixedStaff(true);
		request.setStaffId("Sample");
		request.setStartTime("11:11");
		request.setEndTime("11:10");
		request.setStartDay(DayOfWeek.SATURDAY.toString());

		request.setEndDay(DayOfWeek.SUNDAY.toString());
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidFalseForRequiredStaffWithNoneSelected() {
		RecurringShiftNeed request = new RecurringShiftNeed();
		request.setClientName("bitchTits");
		request.setFixedStaff(true);
		request.setStartTime("11:11");
		request.setEndTime("01:10");
		request.setStartDay(DayOfWeek.SATURDAY.toString());

		request.setEndDay(DayOfWeek.SUNDAY.toString());
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidTrueCase() {
		RecurringShiftNeed request = new RecurringShiftNeed();
		request.setClientName("test");
		request.setFixedStaff(true);
		request.setStaffId("Sample");
		request.setStartTime("11:11");
		request.setEndTime("12:11");
		request.setStartDay(DayOfWeek.SATURDAY.toString());

		request.setEndDay(DayOfWeek.SATURDAY.toString());
		assertTrue(request.isValid());
	}
}
