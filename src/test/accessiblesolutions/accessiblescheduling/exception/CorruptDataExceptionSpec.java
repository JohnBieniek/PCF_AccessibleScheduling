package accessiblesolutions.accessiblescheduling.exception;

import static org.junit.Assert.*;

import org.junit.Test;

import accessiblesolutions.accessiblescheduling.domain.Shift;
public class CorruptDataExceptionSpec {
	@Test
	public void constructorInitializesNoVariables() {
		Shift shift = new Shift();
		CorruptDataException e = new CorruptDataException(Shift.class,shift);
		
		assertNotNull(e);
		//This exception should let me find exactly what was corrupt in its current state
		assertEquals(Shift.class,e.getCorruptClass());
		assertEquals(shift, e.getCorruptObject());
	}
}
