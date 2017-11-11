package accessiblesolutions.accessiblescheduling.util;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

import org.json.JSONArray;
import org.junit.Test;

import accessiblesolutions.accessiblescheduling.domain.Event;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;

public class UtilSpec {
	@Test
	public void eventArrayListContainsEventReturnsTrueIfPresent() {
		String targetId = "Something";
		Event event1 = new Event();
		event1.setId("Nothing");
		
		Event targetEvent = new Event();
		targetEvent.setId(targetId);
		
		ArrayList<Event> events = new ArrayList<Event>();
		events.add(event1);
		events.add(targetEvent);
		
		assertTrue(Util.eventArrayListContainsEvent(events, targetId));
	}
	
	@Test
	public void eventArrayListContainsEventReturnsFalseIfAbsent() {
		String targetId = "Something";
		Event event1 = new Event();
		event1.setId("Nothing");
		
		ArrayList<Event> events = new ArrayList<Event>();
		events.add(event1);
		
		assertFalse(Util.eventArrayListContainsEvent(events, targetId));
	}
	
	@Test
	public void eventArrayListContainsEventReturnsFalseWithNullList() {
		String targetId = "Something";
		
		assertFalse(Util.eventArrayListContainsEvent(null, targetId));
	}
	
	@Test
	public void eventArrayListContainsEventReturnsFalseIfTargetIdAbsent() {
		Event event1 = new Event();
		event1.setId("Nothing");
		
		ArrayList<Event> events = new ArrayList<Event>();
		events.add(event1);
		
		assertFalse(Util.eventArrayListContainsEvent(events, null));
	}
	
	@Test
	public void eventArrayListContainsEventReturnsFalseIfEventListIsEmpty() {
		String targetId = "Something";
		
		ArrayList<Event> events = new ArrayList<Event>();
		
		assertFalse(Util.eventArrayListContainsEvent(events, targetId));
	}
	
	@Test
	public void getDatesForMonthFailsForInvalidMonths1(){
		boolean exception = false;
		try {
			Util.getDatesForMonth(2017,0);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getDatesForMonthFailsForInvalidMonths2(){
		boolean exception = false;
		try {
			Util.getDatesForMonth(2017,13);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getDatesForMonthReturnsCalendar(){
		boolean exception = false;
		JSONArray array = null;
		
		String expected = "[{\"period\":[\"1/1-1/7/17\"],\"days\":[[{\"date\":[\"2017-01-01\"],\"day\":[\"Sunday\"]},{\"date\":[\"2017-01-02\"],\"day\":[\"Monday\"]},{\"date\":[\"2017-01-03\"],\"day\":[\"Tuesday\"]},{\"date\":[\"2017-01-04\"],\"day\":[\"Wednesday\"]},{\"date\":[\"2017-01-05\"],\"day\":[\"Thursday\"]},{\"date\":[\"2017-01-06\"],\"day\":[\"Friday\"]},{\"date\":[\"2017-01-07\"],\"day\":[\"Saturday\"]}]]},{\"period\":[\"1/8-1/14/17\"],\"days\":[[{\"date\":[\"2017-01-08\"],\"day\":[\"Sunday\"]},{\"date\":[\"2017-01-09\"],\"day\":[\"Monday\"]},{\"date\":[\"2017-01-10\"],\"day\":[\"Tuesday\"]},{\"date\":[\"2017-01-11\"],\"day\":[\"Wednesday\"]},{\"date\":[\"2017-01-12\"],\"day\":[\"Thursday\"]},{\"date\":[\"2017-01-13\"],\"day\":[\"Friday\"]},{\"date\":[\"2017-01-14\"],\"day\":[\"Saturday\"]}]]},{\"period\":[\"1/15-1/21/17\"],\"days\":[[{\"date\":[\"2017-01-15\"],\"day\":[\"Sunday\"]},{\"date\":[\"2017-01-16\"],\"day\":[\"Monday\"]},{\"date\":[\"2017-01-17\"],\"day\":[\"Tuesday\"]},{\"date\":[\"2017-01-18\"],\"day\":[\"Wednesday\"]},{\"date\":[\"2017-01-19\"],\"day\":[\"Thursday\"]},{\"date\":[\"2017-01-20\"],\"day\":[\"Friday\"]},{\"date\":[\"2017-01-21\"],\"day\":[\"Saturday\"]}]]},{\"period\":[\"1/22-1/28/17\"],\"days\":[[{\"date\":[\"2017-01-22\"],\"day\":[\"Sunday\"]},{\"date\":[\"2017-01-23\"],\"day\":[\"Monday\"]},{\"date\":[\"2017-01-24\"],\"day\":[\"Tuesday\"]},{\"date\":[\"2017-01-25\"],\"day\":[\"Wednesday\"]},{\"date\":[\"2017-01-26\"],\"day\":[\"Thursday\"]},{\"date\":[\"2017-01-27\"],\"day\":[\"Friday\"]},{\"date\":[\"2017-01-28\"],\"day\":[\"Saturday\"]}]]},{\"period\":[\"1/29-2/4/17\"],\"days\":[[{\"date\":[\"2017-01-29\"],\"day\":[\"Sunday\"]},{\"date\":[\"2017-01-30\"],\"day\":[\"Monday\"]},{\"date\":[\"2017-01-31\"],\"day\":[\"Tuesday\"]},{\"date\":[\"2017-02-01\"],\"day\":[\"Wednesday\"]},{\"date\":[\"2017-02-02\"],\"day\":[\"Thursday\"]},{\"date\":[\"2017-02-03\"],\"day\":[\"Friday\"]},{\"date\":[\"2017-02-04\"],\"day\":[\"Saturday\"]}]]}]";
		
		try {
			array = Util.getDatesForMonth(2017,1);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertNotNull(array);
		assertEquals(array.toString(),expected);
		assertFalse(exception);
	}
	
	@Test
	public void getWeekAfterDateFailsWithPoorlyFormatedDate1(){
		boolean exception = false;
		
		try {
			Util.getWeekAfterDate("2017-02/11");	
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getWeekAfterDateFailsWithPoorlyFormatedDate2(){
		boolean exception = false;

		try {
			Util.getWeekAfterDate("2017/02/11");	
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getWeekAfterDateFailsWithPoorlyFormatedDate3(){
		boolean exception = false;

		try {
			Util.getWeekAfterDate("2017-22-11");	
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getWeekAfterDateFailsWithPoorlyFormatedDate4(){
		boolean exception = false;

		try {
			Util.getWeekAfterDate("2017-XY-11");	
		} catch (ProccessingException e) {
			exception=true;
		}

		assertTrue(exception);
	}
	
	@Test
	public void getWeekAfterDateConvertsAGoodDate(){
		boolean exception = false;
		int week =0;
		try {
			week =Util.getWeekAfterDate("2017-03-11");	
		} catch (ProccessingException e) {
			exception=true;
		}
		assertEquals(week,2);
		assertFalse(exception);
	}
	
	@Test
	public void getWeekBeforeDateFailsWithPoorlyFormatedDate1(){
		boolean exception = false;
		
		try {
			Util.getWeekBeforeDate("2017-02/11");	
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getWeekBeforeDateFailsWithPoorlyFormatedDate2(){
		boolean exception = false;

		try {
			Util.getWeekBeforeDate("2017/02/11");	
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getWeekBeforeDateFailsWithPoorlyFormatedDate3(){
		boolean exception = false;

		try {
			Util.getWeekBeforeDate("2017-22-11");	
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getWeekBeforeDateFailsWithPoorlyFormatedDate4(){
		boolean exception = false;

		try {
			Util.getWeekBeforeDate("2017-XY-11");	
		} catch (ProccessingException e) {
			exception=true;
		}

		assertTrue(exception);
	}
	
	@Test
	public void getWeekBeforeDateConvertsAGoodDate(){
		boolean exception = false;
		int week =0;
		try {
			week =Util.getWeekBeforeDate("2017-12-14");	
		} catch (ProccessingException e) {
			exception=true;
		}
		assertEquals(week,1);
		assertFalse(exception);
	}
	
	@Test
	public void getWeekOfDateFailsWithPoorlyFormatedDate1(){
		boolean exception = false;
		
		try {
			Util.getWeekOfDate("2017-02/11");	
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getWeekOfDateFailsWithPoorlyFormatedDate2(){
		boolean exception = false;

		try {
			Util.getWeekOfDate("2017/02/11");	
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getWeekOfDateFailsWithPoorlyFormatedDate3(){
		boolean exception = false;

		try {
			Util.getWeekOfDate("2017-22-11");	
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getWeekOfDateFailsWithPoorlyFormatedDate4(){
		boolean exception = false;

		try {
			Util.getWeekOfDate("2017-XY-11");	
		} catch (ProccessingException e) {
			exception=true;
		}

		assertTrue(exception);
	}
	
	@Test
	public void getWeekOfDateConvertsAGoodDate(){
		boolean exception = false;
		int week =0;
		try {
			week =Util.getWeekOfDate("2017-12-14");	
		} catch (ProccessingException e) {
			exception=true;
		}
		assertEquals(week,2);
		assertFalse(exception);
	}
	
	@Test
	public void isOverlappingFailsWithMissingStart1(){
		boolean exception = false;
		
		LocalDateTime end1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 10));
		LocalDateTime start2 = LocalDate.of(2017,11,16).atTime(LocalTime.of(10, 10));
		LocalDateTime end2 = LocalDate.of(2017,11,16).atTime(LocalTime.of(11, 10));
		try {
			Util.isOverlapping(null, end1, start2, end2);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void isOverlappingFailsWithMissingEnd1(){
		boolean exception = false;
		
		LocalDateTime start1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(10, 10));
		LocalDateTime start2 = LocalDate.of(2017,11,16).atTime(LocalTime.of(10, 10));
		LocalDateTime end2 = LocalDate.of(2017,11,16).atTime(LocalTime.of(11, 10));
		try {
			Util.isOverlapping(start1,null, start2, end2);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void isOverlappingFailsWithMissingStart2(){
		boolean exception = false;
		
		LocalDateTime start1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(10, 10));
		LocalDateTime end1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 10));
		LocalDateTime end2 = LocalDate.of(2017,11,16).atTime(LocalTime.of(11, 10));
		try {
			Util.isOverlapping(start1, end1, null, end2);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void isOverlappingFailsWithMissingEnd2(){
		boolean exception = false;
		
		LocalDateTime start1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(10, 10));
		LocalDateTime end1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 10));
		LocalDateTime start2 = LocalDate.of(2017,11,16).atTime(LocalTime.of(10, 10));
		try {
			Util.isOverlapping(start1, end1, start2, null);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void isOverlappingFailsWithEnd1PreceedingStart(){
		boolean exception = false;
		
		LocalDateTime start1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 10));
		LocalDateTime end1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(10, 10));
		LocalDateTime start2 = LocalDate.of(2017,11,16).atTime(LocalTime.of(10, 10));
		LocalDateTime end2 = LocalDate.of(2017,11,16).atTime(LocalTime.of(11, 10));
		try {
			Util.isOverlapping(start1, end1, start2, end2);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void isOverlappingFailsWithEnd2PreceedingStart(){
		boolean exception = false;
		
		LocalDateTime start1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(10, 10));
		LocalDateTime end1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 10));
		LocalDateTime start2 = LocalDate.of(2017,11,16).atTime(LocalTime.of(11, 10));
		LocalDateTime end2 = LocalDate.of(2017,11,16).atTime(LocalTime.of(10, 10));
		try {
			Util.isOverlapping(start1, end1, start2, end2);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void isOverlappingFalseWith1Before2(){
		boolean exception = false;
		
		LocalDateTime start1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(10, 10));
		LocalDateTime end1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 10));
		LocalDateTime start2 = LocalDate.of(2017,11,16).atTime(LocalTime.of(10, 10));
		LocalDateTime end2 = LocalDate.of(2017,11,16).atTime(LocalTime.of(11, 10));
		try {
			assertFalse(Util.isOverlapping(start1, end1, start2, end2));
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertFalse(exception);
	}
	
	@Test
	public void isOverlappingFalseWith2Before1(){
		boolean exception = false;
		
		LocalDateTime start1 = LocalDate.of(2017,11,16).atTime(LocalTime.of(10, 10));
		LocalDateTime end1 = LocalDate.of(2017,11,16).atTime(LocalTime.of(11, 10));
		LocalDateTime start2 = LocalDate.of(2017,11,15).atTime(LocalTime.of(10, 10));
		LocalDateTime end2 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 10));
		try {
			assertFalse(Util.isOverlapping(start1, end1, start2, end2));
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertFalse(exception);
	}
	
	@Test
	public void isOverlappingTrueWith1EndingAfter2Starts(){
		boolean exception = false;
		
		LocalDateTime start1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(10, 10));
		LocalDateTime end1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 10));
		LocalDateTime start2 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 0));
		LocalDateTime end2 = LocalDate.of(2017,11,15).atTime(LocalTime.of(12, 10));
		try {
			assertTrue(Util.isOverlapping(start1, end1, start2, end2));
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertFalse(exception);
	}
	
	@Test
	public void isOverlappingTrueWith2EndingAfter1Starts(){
		boolean exception = false;
		
		LocalDateTime start1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 0));
		LocalDateTime end1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(12, 10));
		LocalDateTime start2 = LocalDate.of(2017,11,15).atTime(LocalTime.of(10, 10));
		LocalDateTime end2 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 10));
		try {
			assertTrue(Util.isOverlapping(start1, end1, start2, end2));
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertFalse(exception);
	}
	
	@Test
	public void isOverlappingTrueWith1Containing2(){
		boolean exception = false;
		
		LocalDateTime start1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(6, 0));
		LocalDateTime end1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 10));
		LocalDateTime start2 = LocalDate.of(2017,11,15).atTime(LocalTime.of(7, 10));
		LocalDateTime end2 = LocalDate.of(2017,11,15).atTime(LocalTime.of(8, 10));
		try {
			assertTrue(Util.isOverlapping(start1, end1, start2, end2));
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertFalse(exception);
	}
	
	@Test
	public void isOverlappingTrueWith2Containing1(){
		boolean exception = false;
		
		LocalDateTime start1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(7, 0));
		LocalDateTime end1 = LocalDate.of(2017,11,15).atTime(LocalTime.of(8, 10));
		LocalDateTime start2 = LocalDate.of(2017,11,15).atTime(LocalTime.of(6, 10));
		LocalDateTime end2 = LocalDate.of(2017,11,15).atTime(LocalTime.of(11, 10));
		try {
			assertTrue(Util.isOverlapping(start1, end1, start2, end2));
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertFalse(exception);
	}
}
