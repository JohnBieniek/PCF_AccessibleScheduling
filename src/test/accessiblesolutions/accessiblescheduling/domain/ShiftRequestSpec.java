package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.*;

import org.junit.Test;
public class ShiftRequestSpec {
	@Test
	public void constructorInitializesVariables() {
		ShiftRequest request = new ShiftRequest();
		
		assertNotNull(request);
	}
	
	@Test
	public void isValidFalseWithStaffRequestedButNotSelected(){
		ShiftRequest request = new ShiftRequest();
		
		request.setRequestEmployee(true);
		request.setStartTime("10:15");
		request.setEndTime("11:15");
		
		request.setStartDate("2017-01-12");
		request.setEndDate("2017-01-12");
		
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidFalseWithNoStartTime(){
		ShiftRequest request = new ShiftRequest();
		
		request.setRequestEmployee(true);
		request.setStaffId("sample");
		request.setEndTime("11:15");
		
		request.setStartDate("2017-01-12");
		request.setEndDate("2017-01-12");
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidFalseWithNoEndTime(){
		ShiftRequest request = new ShiftRequest();
		
		request.setRequestEmployee(true);
		request.setStaffId("sample");
		request.setStartTime("11:15");
		
		request.setStartDate("2017-01-12");
		request.setEndDate("2017-01-12");
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidFalseWithNoStartDate(){
		ShiftRequest request = new ShiftRequest();
		
		request.setRequestEmployee(true);
		request.setStaffId("sample");
		request.setStartTime("10:15");
		request.setEndTime("11:15");
		
		request.setEndDate("2017-01-12");
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidFalseWithNoEndDate(){
		ShiftRequest request = new ShiftRequest();
		
		request.setRequestEmployee(true);
		request.setStaffId("sample");
		request.setStartTime("10:15");
		request.setEndTime("11:15");
		
		request.setStartDate("2017-01-12");
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidFalseWithNegativeDuration(){
		ShiftRequest request = new ShiftRequest();
		
		request.setRequestEmployee(true);
		request.setStaffId("sample");
		request.setStartTime("11:15");
		request.setEndTime("10:15");
		
		request.setStartDate("2017-01-12");
		request.setEndDate("2017-01-12");
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidFalseFor25Hours(){
		ShiftRequest request = new ShiftRequest();
		
		request.setRequestEmployee(true);
		request.setStaffId("sample");
		request.setStartTime("11:15");
		request.setEndTime("12:15");
		
		request.setStartDate("2017-01-12");
		request.setEndDate("2017-02-12");
		assertFalse(request.isValid());
	}
	
	@Test
	public void isValidTrueForProper(){
		ShiftRequest request = new ShiftRequest();
		
		request.setRequestEmployee(true);
		request.setStaffId("sampleB");
		request.setStartTime("11:25");
		request.setEndTime("12:15");
		
		request.setStartDate("2017-01-12");
		request.setEndDate("2017-01-12");
		assertTrue(request.isValid());
	}
}
