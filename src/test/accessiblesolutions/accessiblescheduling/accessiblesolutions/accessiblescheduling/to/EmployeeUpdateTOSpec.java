package accessiblesolutions.accessiblescheduling.to;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import accessiblescheduling.to.EmployeeUpdateTO;
public class EmployeeUpdateTOSpec {
	
	@Test
	public void constructorSetsVariables() {
		EmployeeUpdateTO to = new EmployeeUpdateTO();
		
		assertNotNull(to);
		assertFalse(to.getBooleanResponse());//Any boolean value to transmit, the receiver ignores it if not needed
		assertTrue(to.getNumericResponse()==0);//Any numeric value to transmit, the receiver ignores it if not needed
		assertEquals(to.getEmployeeId(),"default");//The id of the object being updated in the UI
	}
}
