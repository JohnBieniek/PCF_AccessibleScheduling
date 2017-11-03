package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.assertEquals;

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
}
