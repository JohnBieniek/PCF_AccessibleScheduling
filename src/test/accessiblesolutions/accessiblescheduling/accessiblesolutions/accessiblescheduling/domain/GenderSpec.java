package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import accessiblescheduling.to.Gender;
public class GenderSpec {
	
	@Test
	public void maleAndFemaleConstantsDefined() {
		assertEquals("female",Gender.FEMALE);
		assertEquals("male",Gender.MALE);
	}
}
