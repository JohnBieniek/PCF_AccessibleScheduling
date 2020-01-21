package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import accessiblescheduling.domain.CustomField;
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
