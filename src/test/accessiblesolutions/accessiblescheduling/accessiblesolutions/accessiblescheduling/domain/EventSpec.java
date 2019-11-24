package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import accessiblescheduling.domain.Event;
public class EventSpec {
	@Test
	public void constructorInitializesNoVariables() {
		Event event = new Event();
		
		assertNotNull(event);
	}
}
