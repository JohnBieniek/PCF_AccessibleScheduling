package accessiblesolutions.accessiblescheduling.domain;

import org.junit.Test;

import static org.junit.Assert.*;
public class CustomFieldDataSpec {
	@Test
	public void emptyConstructorInitializesVariables() {
		CustomFieldData fieldData = new CustomFieldData();
		
		assertEquals("boolean",fieldData.getVariableType());//Custom fields currently ONLY carry boolean data, thats the default too
	}
}
