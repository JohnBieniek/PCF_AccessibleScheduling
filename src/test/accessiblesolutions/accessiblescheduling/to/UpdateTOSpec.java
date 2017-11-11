package accessiblesolutions.accessiblescheduling.to;

import static org.junit.Assert.*;

import org.junit.Test;
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
