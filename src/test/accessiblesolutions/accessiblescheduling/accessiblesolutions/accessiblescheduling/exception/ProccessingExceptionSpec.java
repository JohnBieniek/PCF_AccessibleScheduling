package accessiblesolutions.accessiblescheduling.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import accessiblescheduling.domain.Shift;
import accessiblescheduling.exception.ProccessingException;
public class ProccessingExceptionSpec {
	@Test
	public void constructorInitializesNoVariables() {
		Shift shift = new Shift();
		ProccessingException e = new ProccessingException(Shift.class,shift);
		
		assertNotNull(e);
		//This exception should let me find exactly what was corrupt in its current state
		assertEquals(Shift.class,e.getCorruptClass());
		assertEquals(shift, e.getCorruptObject());
	}
}
