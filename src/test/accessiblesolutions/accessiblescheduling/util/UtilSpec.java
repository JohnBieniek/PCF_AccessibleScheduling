package accessiblesolutions.accessiblescheduling.util;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.json.JSONArray;
import org.junit.Test;

import accessiblesolutions.accessiblescheduling.domain.Event;
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
			Util.getDatesForMonth(0);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getDatesForMonthFailsForInvalidMonths2(){
		boolean exception = false;
		try {
			Util.getDatesForMonth(13);
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
			array = Util.getDatesForMonth(1);//For 2017
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertNotNull(array);
		assertEquals(array.toString(),expected);
		assertFalse(exception);
	}
}
