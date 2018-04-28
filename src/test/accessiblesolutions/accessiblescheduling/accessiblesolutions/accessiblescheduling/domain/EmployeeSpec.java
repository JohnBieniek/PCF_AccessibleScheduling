package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;

import org.junit.Test;
public class EmployeeSpec {
	@Test
	public void emptyConstructorInitializesVariables() {
		Employee emptyEmployee = new Employee();
		
		assertTrue(null==emptyEmployee.getId());
		assertTrue(null==emptyEmployee.getFirst());
		assertTrue(null==emptyEmployee.getInitial());
		assertTrue(null==emptyEmployee.getHireDate());
		assertTrue(Gender.FEMALE==emptyEmployee.getGender());
		assertTrue(null==emptyEmployee.compatibile);//Temporarily used for proccessing, needs refactored out
		
		assertFalse(emptyEmployee.getNoCats());
		assertFalse(emptyEmployee.getSmoker());	
		assertFalse(emptyEmployee.getMedPassCertified());
		assertFalse(emptyEmployee.getInactive());
		assertFalse(emptyEmployee.getOffAlternateWeekends());
		assertFalse(emptyEmployee.getRequestsExtraShifts());
		assertFalse(emptyEmployee.getFixedSchedule());
		
		assertTrue(1==emptyEmployee.getMinHours());
		assertTrue(40==emptyEmployee.getMaxHours());   
		
		assertTrue(0==emptyEmployee.getRequestedOff().length);
		    
		assertTrue(7==emptyEmployee.getDaysAvailable().length);
		
		assertTrue(24==emptyEmployee.getSundaysAvailability().length);
		assertTrue(24==emptyEmployee.getMondaysAvailability().length);
		assertTrue(24==emptyEmployee.getTuesdaysAvailability().length);
		assertTrue(24==emptyEmployee.getWednesdaysAvailability().length);
		assertTrue(24==emptyEmployee.getThursdaysAvailability().length);
		assertTrue(24==emptyEmployee.getFridaysAvailability().length);
		assertTrue(24==emptyEmployee.getSaturdaysAvailability().length);
	}
	
	@Test
	public void constructorInitializesVariables() {
		Employee emptyEmployee = new Employee("Test","U");
		
		assertTrue(null==emptyEmployee.getId());
		assertTrue("Test"==emptyEmployee.getFirst());
		assertTrue("U"==emptyEmployee.getInitial());
		assertTrue(null==emptyEmployee.getHireDate());
		assertTrue(Gender.FEMALE==emptyEmployee.getGender());
		assertTrue(null==emptyEmployee.compatibile);//Temporarily used for proccessing, needs refactored out
		
		assertFalse(emptyEmployee.getNoCats());
		assertFalse(emptyEmployee.getSmoker());	
		assertFalse(emptyEmployee.getMedPassCertified());
		assertFalse(emptyEmployee.getInactive());
		assertFalse(emptyEmployee.getOffAlternateWeekends());
		assertFalse(emptyEmployee.getRequestsExtraShifts());
		assertFalse(emptyEmployee.getFixedSchedule());
		
		assertTrue(1==emptyEmployee.getMinHours());
		assertTrue(40==emptyEmployee.getMaxHours());   
		
		assertTrue(0==emptyEmployee.getRequestedOff().length);
		    
		assertTrue(7==emptyEmployee.getDaysAvailable().length);
		
		assertTrue(24==emptyEmployee.getSundaysAvailability().length);
		assertTrue(24==emptyEmployee.getMondaysAvailability().length);
		assertTrue(24==emptyEmployee.getTuesdaysAvailability().length);
		assertTrue(24==emptyEmployee.getWednesdaysAvailability().length);
		assertTrue(24==emptyEmployee.getThursdaysAvailability().length);
		assertTrue(24==emptyEmployee.getFridaysAvailability().length);
		assertTrue(24==emptyEmployee.getSaturdaysAvailability().length);
	}
	
	@Test
	public void setersUpdateVariables() {
		String sample = "SampleString";
		Employee employee = new Employee();
		
		assertTrue(null==employee.getId());
		employee.setId(sample);
		assertTrue(sample==employee.getId());
		
		assertTrue(null==employee.getFirst());
		employee.setFirst(sample);
		assertTrue(sample==employee.getFirst());
		
		assertTrue(null==employee.getInitial());
		employee.setInitial(sample);
		assertTrue(sample==employee.getInitial());
		
		assertTrue(null==employee.getHireDate());
		employee.setHireDate(sample);
		assertTrue(sample==employee.getHireDate());
		
		assertTrue(Gender.FEMALE==employee.getGender());
		assertTrue(employee.isFemale());
		
		employee.setGender(Gender.MALE);
		assertTrue(Gender.MALE==employee.getGender());
		assertTrue(employee.isMale());
		
		assertTrue(null==employee.compatibile);//Temporarily used for proccessing, needs refactored out
		
		assertFalse(employee.getNoCats());
		employee.setNoCats(true);
		assertTrue(employee.getNoCats());
		
		assertFalse(employee.getSmoker());	
		employee.setSmoker(true);
		assertTrue(employee.getSmoker());
		
		assertFalse(employee.getMedPassCertified());
		employee.setMedPassCertified(true);
		assertTrue(employee.getMedPassCertified());
		
		assertFalse(employee.getInactive());
		employee.setInactive(true);
		assertTrue(employee.getInactive());
		
		assertFalse(employee.getOffAlternateWeekends());
		employee.setOffAlternateWeekends(true);
		assertTrue(employee.getOffAlternateWeekends());
		
		assertFalse(employee.getRequestsExtraShifts());
		employee.setRequestsExtraShifts(true);
		assertTrue(employee.getRequestsExtraShifts());
		
		assertFalse(employee.getFixedSchedule());
		employee.setFixedSchedule(true);
		assertTrue(employee.getFixedSchedule());
		
		assertTrue(1==employee.getMinHours());
		employee.setMinHours(2);
		assertTrue(2==employee.getMinHours());
		
		assertTrue(40==employee.getMaxHours());  
		employee.setMaxHours(39);
		assertTrue(39==employee.getMaxHours());
		
		assertTrue(0==employee.getRequestedOff().length);
		//TODO how do I validate changing the below?
		assertTrue(7==employee.getDaysAvailable().length);
		
		assertTrue(24==employee.getSundaysAvailability().length);
		assertTrue(24==employee.getMondaysAvailability().length);
		assertTrue(24==employee.getTuesdaysAvailability().length);
		assertTrue(24==employee.getWednesdaysAvailability().length);
		assertTrue(24==employee.getThursdaysAvailability().length);
		assertTrue(24==employee.getFridaysAvailability().length);
		assertTrue(24==employee.getSaturdaysAvailability().length);
	}
	
	@Test
	public void getAvailabilityForShowsProperDaySunday() {
		Employee employee = new Employee();
		
		boolean[] availability = new boolean[24];
		availability[0]=true;

		assertFalse(employee.getSundaysAvailability()[0]);
		
		employee.setSundaysAvailability(availability);
		
		assertTrue(employee.getAvailabilityFor(DayOfWeek.SUNDAY.getValue())[0]);
		assertTrue(employee.getAvailabilityFor(0)[0]);
	}
	
	@Test
	public void getAvailabilityForShowsProperDayMonday() {
		Employee employee = new Employee();
		
		boolean[] availability = new boolean[24];
		availability[0]=true;

		assertFalse(employee.getMondaysAvailability()[0]);
		
		employee.setMondaysAvailability(availability);
		
		assertTrue(employee.getAvailabilityFor(DayOfWeek.MONDAY.getValue())[0]);
	}
	
	@Test
	public void getAvailabilityForShowsProperDayTuesday() {
		Employee employee = new Employee();
		
		boolean[] availability = new boolean[24];
		availability[0]=true;

		assertFalse(employee.getTuesdaysAvailability()[0]);
		
		employee.setTuesdaysAvailability(availability);
		
		assertTrue(employee.getAvailabilityFor(DayOfWeek.TUESDAY.getValue())[0]);
	}
	
	@Test
	public void getAvailabilityForShowsProperDayWednesday() {
		Employee employee = new Employee();
		
		boolean[] availability = new boolean[24];
		availability[0]=true;

		assertFalse(employee.getWednesdaysAvailability()[0]);
		
		employee.setWednesdaysAvailability(availability);
		
		assertTrue(employee.getAvailabilityFor(DayOfWeek.WEDNESDAY.getValue())[0]);
	}
	
	@Test
	public void getAvailabilityForShowsProperDayThursday() {
		Employee employee = new Employee();
		
		boolean[] availability = new boolean[24];
		availability[0]=true;

		assertFalse(employee.getThursdaysAvailability()[0]);
		
		employee.setThursdaysAvailability(availability);
		
		assertTrue(employee.getAvailabilityFor(DayOfWeek.THURSDAY.getValue())[0]);
	}
	
	@Test
	public void getAvailabilityForShowsProperDayFriday() {
		Employee employee = new Employee();
		
		boolean[] availability = new boolean[24];
		availability[0]=true;

		assertFalse(employee.getFridaysAvailability()[0]);
		
		employee.setFridaysAvailability(availability);
		
		assertTrue(employee.getAvailabilityFor(DayOfWeek.FRIDAY.getValue())[0]);
	}
	
	@Test
	public void getAvailabilityForShowsProperDaySaturday() {
		Employee employee = new Employee();
		
		boolean[] availability = new boolean[24];
		availability[0]=true;

		assertFalse(employee.getSaturdaysAvailability()[0]);
		
		employee.setSaturdaysAvailability(availability);
		
		assertTrue(employee.getAvailabilityFor(DayOfWeek.SATURDAY.getValue())[0]);
	}
	
	//1 day is enough to be representative since day choice if offloaded to getAvailabilityFor which fully exercises all day paths
	@Test
	public void getHoursAvailabileForShowsProperHoursSundayWithNoDay() {
		Employee employee = new Employee();
		
		boolean[] availability = new boolean[24];
		for(int i = 0 ;i<8;i++){
			availability[i]=true;
		}
		assertTrue(availability[0]);
		assertTrue(0==employee.getHoursAvailable(DayOfWeek.SUNDAY.getValue()));
		
		employee.setSundaysAvailability(availability);
		assertTrue(employee.getSundaysAvailability()[0]);
		
		assertEquals(0,employee.getHoursAvailable(DayOfWeek.SUNDAY.getValue()));
		assertTrue(0==employee.getHoursAvailable(0));
	}
	
	@Test
	public void getHoursAvailabileForShowsProperHoursSundayWithDay() {
		Employee employee = new Employee();
		
		boolean[] daysAvailable = new boolean[7];
		daysAvailable[0]=true;
		
		employee.setDaysAvailable(daysAvailable);
		
		boolean[] availability = new boolean[24];
		for(int i = 0 ;i<8;i++){
			availability[i]=true;
		}
		assertTrue(availability[0]);
		assertTrue(0==employee.getHoursAvailable(DayOfWeek.SUNDAY.getValue()));
		
		employee.setSundaysAvailability(availability);
		assertTrue(employee.getSundaysAvailability()[0]);
		
		assertEquals(8,employee.getHoursAvailable(DayOfWeek.SUNDAY.getValue()));
		assertTrue(8==employee.getHoursAvailable(0));
	}
	
	@Test
	public void getHoursAvailabileShowsTotalHoursForWeekWithHoursAndDays() {
		Employee employee = new Employee();
		
		boolean[] daysAvailable = new boolean[7];
		daysAvailable[0]=true;
		
		employee.setDaysAvailable(daysAvailable);
		
		boolean[] availability = new boolean[24];
		for(int i = 0 ;i<8;i++){
			availability[i]=true;
		}
		
		employee.setSundaysAvailability(availability);
		
		assertEquals(8,employee.getHoursAvailable());
	}
	
	@Test
	public void getHoursAvailabileShowsTotalHoursForWeekWithHoursAndNoDays() {
		Employee employee = new Employee();
		
		boolean[] availability = new boolean[24];
		for(int i = 0 ;i<8;i++){
			availability[i]=true;
		}
		assertTrue(availability[0]);
		assertTrue(0==employee.getHoursAvailable(DayOfWeek.SUNDAY.getValue()));
		
		employee.setSundaysAvailability(availability);
		assertTrue(employee.getSundaysAvailability()[0]);
		
		
		assertEquals(0,employee.getHoursAvailable());
	}
	
	@Test
	public void isFemaleShowsTrueForFemales(){
		Employee employee = new Employee();
		
		assertTrue(employee.isFemale());
	}
	
	@Test
	public void isFemaleShowsFalseForMales(){
		Employee employee = new Employee();
		
		employee.setGender(Gender.MALE);
		
		assertFalse(employee.isFemale());
	}
	
	@Test
	public void isFemaleShowsFalseForOthers(){
		Employee employee = new Employee();
		
		employee.setGender("Non-binary");
		
		assertFalse(employee.isFemale());
	}
	
	@Test
	public void isMaleShowsTrueForMales(){
		Employee employee = new Employee();
		
		employee.setGender(Gender.MALE);
		
		assertTrue(employee.isMale());
	}
	
	@Test
	public void isMaleShowsFalseForFemales(){
		Employee employee = new Employee();
		
		assertFalse(employee.isMale());
	}
	
	@Test
	public void isMaleShowsFalseForOthers(){
		Employee employee = new Employee();
		
		employee.setGender("Non-binary");
		
		assertFalse(employee.isMale());
	}
	
	@Test
	public void requestedOffShowsOffWhenAShiftStartsTheDayOff() {
		Employee employee = new Employee();
		String[] requestedDays = new String[]{"2017-06-04"};
		employee.setRequestedOff(requestedDays);
		
		Shift shift = new Shift();
		shift.setStartDate("2017-06-04");
		shift.setEndDate("2017-06-04");
		
		assertTrue(employee.requestedOff(shift));
	}
	
	@Test
	public void requestedOffShowsOffWhenAShiftEndsTheDayOff() {
		Employee employee = new Employee();
		String[] requestedDays = new String[]{"2017-06-04"};
		employee.setRequestedOff(requestedDays);
		
		Shift shift = new Shift();
		shift.setStartDate("2017-06-03");
		shift.setEndDate("2017-06-04");
		
		assertTrue(employee.requestedOff(shift));
	}
	
	@Test
	public void requestedOffShowsOnWhenADayIsntVacation() {
		Employee employee = new Employee();
		String[] requestedDays = new String[]{"2017-06-04"};
		employee.setRequestedOff(requestedDays);
		
		Shift shift = new Shift();
		shift.setStartDate("2017-05-04");
		shift.setEndDate("2017-05-04");
		
		assertFalse(employee.requestedOff(shift));
	}
}
