package accessiblesolutions.accessiblescheduling.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import accessiblescheduling.domain.Shift;
import accessiblescheduling.exception.BadRequestException;
public class BadRequestExceptionSpec {
	@Test
	public void constructorInitializesNoVariables() {
		Shift shift = new Shift();
		BadRequestException e = new BadRequestException(Shift.class,shift);
		
		assertNotNull(e);
		//This exception should let me find exactly what was corrupt in its current state
		assertEquals(Shift.class,e.getCorruptClass());
		assertEquals(shift, e.getCorruptObject());
	}
}
