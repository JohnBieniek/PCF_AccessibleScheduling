package accessiblesolutions.accessiblescheduling.util;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import accessiblesolutions.accessiblescheduling.domain.Event;
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
}
