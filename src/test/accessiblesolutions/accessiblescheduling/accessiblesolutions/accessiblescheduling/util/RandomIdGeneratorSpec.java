package accessiblesolutions.accessiblescheduling.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import accessiblescheduling.util.RandomIdGenerator;
public class RandomIdGeneratorSpec {
	@Test
	public void constructorInitializesNoVariables() {
		RandomIdGenerator gen = new RandomIdGenerator();
		
		String id = null;
		id = gen.generateId();
		
		assertNotNull(id);
	}
}
