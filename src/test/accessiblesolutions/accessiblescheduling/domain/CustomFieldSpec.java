package accessiblesolutions.accessiblescheduling.domain;

import org.junit.Test;

import static org.junit.Assert.*;
public class CustomFieldSpec {
	@Test
	public void emptyConstructorInitializesVariables() {
		CustomField field = new CustomField();
		
		assertEquals("",field.getClientVariable());
		assertEquals("",field.getEmployeeVariable());
		assertFalse("",field.getClientRequirement());
		assertFalse("",field.getEmployeeRequirement());
		assertTrue(field.getOverrideable());
	}
}
