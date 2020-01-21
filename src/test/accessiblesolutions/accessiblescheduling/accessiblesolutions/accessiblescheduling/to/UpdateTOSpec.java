package accessiblesolutions.accessiblescheduling.to;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import accessiblescheduling.to.UpdateTO;
public class UpdateTOSpec {
	
	@Test
	public void constructorSetsVariables() {
		UpdateTO to = new UpdateTO();
		
		assertNotNull(to);
		assertFalse(to.getBooleanResponse());//Any boolean value to transmit, the receiver ignores it if not needed
		assertTrue(to.getNumericResponse()==0);//Any numeric value to transmit, the receiver ignores it if not needed
		assertEquals(to.getId(),"default");//The id of the object being updated in the UI
	}
}
