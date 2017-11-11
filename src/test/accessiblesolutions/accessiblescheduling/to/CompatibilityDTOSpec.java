package accessiblesolutions.accessiblescheduling.to;

import static org.junit.Assert.*;

import org.junit.Test;
public class CompatibilityDTOSpec {
	
	@Test
	public void constructorSetsVariables() {
		CompatibilityDTO to = new CompatibilityDTO();
		
		assertNotNull(to);
		assertFalse(to.getCompatible());//Any boolean value to transmit, the receiver ignores it if not needed
		assertEquals(to.getShiftId(),"none");
		assertEquals(to.getEmployeeId(),"default");//The id of the object being updated in the UI
	}
}
