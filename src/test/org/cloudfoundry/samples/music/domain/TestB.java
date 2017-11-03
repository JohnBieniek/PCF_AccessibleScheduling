package org.cloudfoundry.samples.music.domain;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import accessiblesolutions.accessiblescheduling.domain.Event;
import accessiblesolutions.accessiblescheduling.domain.Shift;

public class TestB {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void test() {
		assertTrue(true);
	}
//	@Test
//	public void test2() {
//		assertTrue(false);
//	}
	@Test
	public void eventTestSample(){
		Shift shift = new Shift();
		assertTrue(shift!=null);
		assertFalse(shift.getAssigned());
		assertTrue(shift.getStartDate()==null);
	}
}
