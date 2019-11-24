package accessiblesolutions.accessiblescheduling.to;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import accessiblescheduling.to.CompatibilityDTO;
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
