package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
public class ShiftRequestSpec {
	@Test
	public void constructorInitializesVariables() {
		ShiftRequest request = new ShiftRequest();
		
		assertNotNull(request);
	}
}
