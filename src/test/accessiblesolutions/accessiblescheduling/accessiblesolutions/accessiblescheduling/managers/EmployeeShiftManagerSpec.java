package accessiblesolutions.accessiblescheduling.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.repository.CrudRepository;

import accessiblescheduling.domain.Employee;
import accessiblescheduling.domain.Shift;
import accessiblescheduling.exception.CorruptDataException;
import accessiblescheduling.exception.ProccessingException;
import accessiblescheduling.manager.CustomDataManager;
import accessiblescheduling.manager.EmployeeShiftCompatibilityManager;
import accessiblescheduling.manager.EmployeeShiftManager;
import accessiblescheduling.manager.EmployeeShiftMapManager;
import accessiblescheduling.manager.ShiftGenerationManager;
import accessiblescheduling.manager.ShiftManager;
import accessiblescheduling.repositories.mongodb.MongoShiftRepository;

public class EmployeeShiftManagerSpec {
	@Mock
	private Resource resource;

	@Mock
	private CrudRepository<Shift, String> shiftCrud;
    
	@Mock
	MongoShiftRepository shiftRepository;
	
	@Mock
    CrudRepository<Employee,String> employeeCrud;
	
	@Mock
    CrudRepository<Employee,String> employeeRepository;
    
	@Mock
    CustomDataManager customDataManager;
    
	@Mock
    ShiftManager shiftManager;
    
	@Mock
    EmployeeShiftMapManager employeeShiftMapManager;
    
	@Mock
    EmployeeShiftCompatibilityManager employeeShiftCompatibilityManager;
    
	@Mock
    ShiftGenerationManager shiftGenerationManager;
	
	// Testing instance, mocked `resource` should be injected here 
	@InjectMocks
	@Resource
	private EmployeeShiftManager fixture;
	
	@Before
	public void setUp() throws Exception {
	    // Initialize mocks created above
	    MockitoAnnotations.initMocks(this);
	    
	    Employee onAlways = new Employee("On","Always");
	    when(employeeCrud.findOne("onAlways")).thenReturn(onAlways);// Change behaviour of `resource`
	    
	    Employee offOnce = new Employee("Off","Once");
	    String[] requestedOff = {"2018-04-01"};
	    offOnce.setRequestedOff(requestedOff);
	    when(employeeCrud.findOne("offOnce")).thenReturn(offOnce);

	    Employee assignedOneDay = new Employee("Assigned","OneDay");
	    ArrayList<Shift> assignedOneDayShifts = new ArrayList<Shift>();
	    Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-04-01");
	    assignedOneDayShift1.setEndDate("2018-04-02");
	    assignedOneDayShift1.setStartTime("20:00");
	    assignedOneDayShift1.setEndTime("10:00");
	    
	    Shift assignedOneDayShift2 = new Shift();
	    assignedOneDayShift2.setStaffId("assignedOneDay");
	    assignedOneDayShift2.setStartDate("2018-04-02");
	    assignedOneDayShift2.setEndDate("2018-04-02");
	    assignedOneDayShift2.setStartTime("10:00");
	    assignedOneDayShift2.setEndTime("18:00");
	    
	    Shift assignedOneDayShift3 = new Shift();
	    assignedOneDayShift3.setStaffId("assignedOneDay");
	    assignedOneDayShift3.setStartDate("2018-04-02");
	    assignedOneDayShift3.setEndDate("2018-04-03");
	    assignedOneDayShift3.setStartTime("20:00");
	    assignedOneDayShift3.setEndTime("10:00");
	    assignedOneDayShifts.add(assignedOneDayShift1);
	    assignedOneDayShifts.add(assignedOneDayShift2);
	    assignedOneDayShifts.add(assignedOneDayShift3);
	    when(shiftRepository.findByStartMonthAndStartYear(4,2018)).thenReturn(assignedOneDayShifts);
	    when(employeeCrud.findOne("assignedOneDay")).thenReturn(assignedOneDay);
	    when(employeeRepository.findOne("assignedOneDay")).thenReturn(assignedOneDay);
	    
	    Employee crossMonthEmployee = new Employee("Cross","Month");
	    ArrayList<Shift> crossMonthShifts = new ArrayList<Shift>();
	    Shift crossMonthShift1 = new Shift();
	    crossMonthShift1.setStaffId("crossMonth");
	    crossMonthShift1.setClientId("crossMonthClient");
	    crossMonthShift1.setStartDate("2018-05-31");
	    crossMonthShift1.setEndDate("2018-06-01");
	    crossMonthShift1.setStartTime("20:00");
	    crossMonthShift1.setEndTime("10:00");
	    crossMonthShifts.add(crossMonthShift1);
	    when(shiftRepository.findByStartMonthAndStartYear(5,2018)).thenReturn(crossMonthShifts);
	    when(shiftRepository.findByStartMonthAndStartYear(6,2018)).thenReturn(crossMonthShifts);
	    when(employeeCrud.findOne("crossMonth")).thenReturn(crossMonthEmployee);
	    when(employeeRepository.findOne("crossMonth")).thenReturn(crossMonthEmployee);
	    
	    Employee crossWeekEmployee = new Employee("Cross","Week");
	    ArrayList<Shift> crossWeekShifts = new ArrayList<Shift>();
	    Shift crossWeekShift1 = new Shift();
	    crossWeekShift1.setStaffId("crossWeek");
	    crossWeekShift1.setClientId("crossWeekClient");
	    crossWeekShift1.setStartDate("2018-09-22");
	    crossWeekShift1.setEndDate("2018-09-23");
	    crossWeekShift1.setStartTime("20:00");
	    crossWeekShift1.setEndTime("10:00");
	    crossWeekShifts.add(crossWeekShift1);
	    when(shiftRepository.findByStartMonthAndStartYear(9,2018)).thenReturn(crossWeekShifts);
	    when(employeeCrud.findOne("crossWeek")).thenReturn(crossWeekEmployee);
	    when(employeeRepository.findOne("crossWeek")).thenReturn(crossWeekEmployee);
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForMonthReturnsShiftsWhenAssigned() {
		ArrayList<Shift> assignedShifts = fixture.getAssignedShiftsForEmployeeForMonth("assignedOneDay", 4,2018);
		assertEquals(assignedShifts.size(),3);
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForMonthReturnsEmptyWhenUnassigned() {
		ArrayList<Shift> assignedShifts = fixture.getAssignedShiftsForEmployeeForMonth("offOnce", 4,2018);

		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForMonthReturnsEmptyWithNullId() {
		ArrayList<Shift> assignedShifts = fixture.getAssignedShiftsForEmployeeForMonth(null, 4,2018);

		assertEquals(0,assignedShifts.size());
	}
	
	
	@Test
	public void getAssignedShiftsForEmployeeForMonthReturnsShiftsCrossingMonths() {
		ArrayList<Shift> assignedShifts = fixture.getAssignedShiftsForEmployeeForMonth("crossMonth", 6,2018);
		
		assertEquals(1,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForWeekOfMonthReturnsAssignedShifts() {
		ArrayList<Shift> assignedShifts=null;
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekOfMonth("assignedOneDay", 0, 4,2018);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		}
		
		assertEquals(3,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForWeekOfMonthReturnsEmptyForNullEmployee() {
		ArrayList<Shift> assignedShifts=null;
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekOfMonth(null, 0, 4,2018);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		}
		
		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForWeekOfMonthReturnsEmptyForUnassignedEmployee() {
		ArrayList<Shift> assignedShifts=null;
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekOfMonth("offOnce", 0, 4,2018);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		}
		
		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForWeekOfShiftReturnsAssignedShifts() {
		ArrayList<Shift> assignedShifts=null;
		
		Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-04-02");
	    assignedOneDayShift1.setEndDate("2018-04-02");
	    assignedOneDayShift1.setStartMonth(4);
	    
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekOfShift("assignedOneDay", assignedOneDayShift1);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		}
		
		assertEquals(3,assignedShifts.size());
	}
	
	
	@Test
	public void getAssignedShiftsForEmployeeForWeekOfShiftReturnsEmptyForNullEmployee() {
		ArrayList<Shift> assignedShifts=null;
		
		Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-04-01");
	    assignedOneDayShift1.setEndDate("2018-04-02");
	    
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekOfShift(null, assignedOneDayShift1);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		}
		
		assertEquals(0,assignedShifts.size());
	}
	@Test
	public void getAssignedShiftsForEmployeeForWeekOfShiftReturnsEmptyForNullShift() {
		ArrayList<Shift> assignedShifts=null;
		
		Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-04-01");
	    assignedOneDayShift1.setEndDate("2018-04-02");
	    
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekOfShift("assignedOneDay",null);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		}
		
		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForWeekOfShiftReturnsEmptyForUnassignedEmployee() {
		ArrayList<Shift> assignedShifts=null;
		
		Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-04-01");
	    assignedOneDayShift1.setEndDate("2018-04-02");
	    
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekOfShift("offOnce",assignedOneDayShift1);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		}
		
		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForDayOfMonthReturnsEmptyForNullEmployee() {
		ArrayList<Shift> assignedShifts=null;
		
		assignedShifts = fixture.getAssignedShiftsForEmployeeForDayOfMonth(null, 0, 4,2018);
		
		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForDayOfMonthReturnsEmptyForUnassignedEmployee() {
		ArrayList<Shift> assignedShifts=null;
		
		assignedShifts = fixture.getAssignedShiftsForEmployeeForDayOfMonth("offOnce", 0, 4,2018);
		
		assertEquals(0,assignedShifts.size());
	}

	@Test
	public void getAssignedShiftsForEmployeeForDayOfMonthReturnsAssignedShifts() {
		ArrayList<Shift> assignedShifts=null;
		
		assignedShifts = fixture.getAssignedShiftsForEmployeeForDayOfMonth("assignedOneDay", 2, 4,2018);
		
		assertEquals(3,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeStartingDayOfMonthReturnsEmptyForNullEmployee() {
		ArrayList<Shift> assignedShifts=null;
		
		assignedShifts = fixture.getAssignedShiftsForEmployeeStartingDayOfMonth(null, 0, 4,2018);
		
		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeStartingDayOfMonthReturnsEmptyForUnassignedEmployee() {
		ArrayList<Shift> assignedShifts=null;
		
		assignedShifts = fixture.getAssignedShiftsForEmployeeStartingDayOfMonth("offOnce", 0, 4,2018);
		
		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeStartingDayOfMonthReturnsAssignedShifts() {
		ArrayList<Shift> assignedShifts=null;
		
		assignedShifts = fixture.getAssignedShiftsForEmployeeStartingDayOfMonth("assignedOneDay", 2, 4,2018);
		
		assertEquals(2,assignedShifts.size());
	}
	
	
	@Test
	public void getAssignedShiftsForEmployeeForWeekAfterReturnsAssignedShifts() {
		ArrayList<Shift> assignedShifts=null;
		boolean errored = false;
		
		Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-03-27");
	    assignedOneDayShift1.setEndDate("2018-03-27");
	    assignedOneDayShift1.setStartMonth(4);
	    
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekAfterShift("assignedOneDay", assignedOneDayShift1);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertEquals(3,assignedShifts.size());
	}
	
	
	@Test
	public void getAssignedShiftsForEmployeeForWeekAfterReturnsEmptyForNullEmployee() {
		ArrayList<Shift> assignedShifts=null;
		boolean errored = false;
		
		Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-04-01");
	    assignedOneDayShift1.setEndDate("2018-04-02");
	    
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekAfterShift(null, assignedOneDayShift1);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
			
		assertFalse(errored);
		assertEquals(0,assignedShifts.size());
	}
	@Test
	public void getAssignedShiftsForEmployeeForWeekAfterReturnsEmptyForNullShift() {
		ArrayList<Shift> assignedShifts=null;
		boolean errored = false;
		
		Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-04-01");
	    assignedOneDayShift1.setEndDate("2018-04-02");
	    
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekAfterShift("assignedOneDay",null);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForWeekAfterReturnsEmptyForUnassignedEmployee() {
		ArrayList<Shift> assignedShifts=null;
		boolean errored = false;
		
		Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-04-01");
	    assignedOneDayShift1.setEndDate("2018-04-02");
	    
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekAfterShift("offOnce",assignedOneDayShift1);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getHoursScheduledWeekOfMonthThrowsProccessingExceptionForNullEmployee() {
		boolean errored = false;
		try {
			float hours = fixture.getHoursScheduledWeekOfMonth(null,1,4,2018);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getHoursScheduledWeekOfMonthReturns0ForUnassignedEmployee() {
		boolean errored = false;
		float hours = -1;
		float zero = 0;

	    Employee offOnce = new Employee("Off","Once");
	    String[] requestedOff = {"2018-04-01"};
	    offOnce.setRequestedOff(requestedOff);
	    
		try {
			hours = fixture.getHoursScheduledWeekOfMonth(offOnce,1,6,2018);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(zero==hours);
	}
	
	@Test
	public void getHoursScheduledWeekOfMonthReturnsSumOfShiftsInWeek() {
		boolean errored = false;
		float hours = -1;

		Employee assignedOneDay = new Employee("Assigned","OneDay");
		assignedOneDay.setId("assignedOneDay");
	    
		try {
			hours = fixture.getHoursScheduledWeekOfMonth(assignedOneDay,0,4,2018);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}

		assertFalse(errored);
		assertTrue(36==hours);
	}
	
	
	@Test
	public void getHoursScheduledWeekOfMonthReturnsSumOfShiftsCrossMonth() {
		boolean errored = false;
		float hours = -1;

		Employee crossMonth = new Employee("Cross","Month");
		crossMonth.setId("crossMonth");
	    
		try {
			hours = fixture.getHoursScheduledWeekOfMonth(crossMonth,0,6,2018);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(14==hours);
	}
	
	@Test
	public void getHoursScheduledWeekOfMonthReturnsSumOfShiftsCrossWeek() {
		boolean errored = false;
		float hours = -1;

		Employee crossWeek = new Employee("Cross","Week");
		crossWeek.setId("crossWeek");
	    
		try {
			hours = fixture.getHoursScheduledWeekOfMonth(crossWeek,4,9,2018);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		System.out.println("hours:"+hours);
		assertTrue(10==hours);
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForWeekBeforeShiftReturnsAssignedShifts() {
		ArrayList<Shift> assignedShifts=null;
		
		Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-04-09");
	    assignedOneDayShift1.setEndDate("2018-04-09");
	    assignedOneDayShift1.setStartMonth(4);
	    
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekBeforeShift("assignedOneDay", assignedOneDayShift1);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(3,assignedShifts.size());
	}
	
	
	@Test
	public void getAssignedShiftsForEmployeeForWeekBeforeShiftReturnsEmptyForNullEmployee() {
		ArrayList<Shift> assignedShifts=null;
		
		Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-04-09");
	    assignedOneDayShift1.setEndDate("2018-04-09");
	    
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekBeforeShift(null, assignedOneDayShift1);
		} catch (CorruptDataException | ProccessingException e) {
			e.printStackTrace();
		}
		
		assertEquals(0,assignedShifts.size());
	}
	@Test
	public void getAssignedShiftsForEmployeeForWeekBeforeShiftReturnsEmptyForNullShift() {
		ArrayList<Shift> assignedShifts=null;
		
		Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-04-01");
	    assignedOneDayShift1.setEndDate("2018-04-02");
	    
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekBeforeShift("assignedOneDay",null);
		} catch (CorruptDataException | ProccessingException e) {
			e.printStackTrace();
		}
		
		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForWeekBeforeShiftReturnsEmptyForUnassignedEmployee() {
		ArrayList<Shift> assignedShifts=null;
		
		Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setStartDate("2018-04-09");
	    assignedOneDayShift1.setEndDate("2018-04-09");
	    
		try {
			assignedShifts = fixture.getAssignedShiftsForEmployeeForWeekBeforeShift("offOnce",assignedOneDayShift1);
		} catch (CorruptDataException | ProccessingException e) {
			e.printStackTrace();
		}
		
		assertEquals(0,assignedShifts.size());
	}
	

	@Test
	public void getAssignedOvernightShiftsForEmployeeForMonthReturnsShiftsWhenAssigned() {
		ArrayList<Shift> assignedShifts = null;
		try {
			assignedShifts = fixture.getAssignedOvernightShiftsForEmployeeForMonth("assignedOneDay", 4,2018);
		} catch (CorruptDataException | ProccessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(2,assignedShifts.size());
	}
	
	@Test
	public void getAssignedOvernightShiftsForEmployeeForMonthReturnsEmptyWhenUnassigned() {
		ArrayList<Shift> assignedShifts = null;
		try {
			assignedShifts = fixture.getAssignedOvernightShiftsForEmployeeForMonth("offOnce", 4,2018);
		} catch (CorruptDataException | ProccessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedOvernightShiftsForEmployeeForMonthReturnsEmptyWithNullId() {
		ArrayList<Shift> assignedShifts = null;
		try {
			assignedShifts = fixture.getAssignedOvernightShiftsForEmployeeForMonth(null, 4,2018);
		} catch (CorruptDataException | ProccessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals(0,assignedShifts.size());
	}
	
	
	@Test
	public void getAssignedOvernightShiftsForEmployeeForMonthReturnsShiftsCrossingMonths() {
		ArrayList<Shift> assignedShifts = null;
		try {
			assignedShifts = fixture.getAssignedOvernightShiftsForEmployeeForMonth("crossMonth", 6,2018);
		} catch (CorruptDataException | ProccessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(assignedShifts);
		assertEquals(1,assignedShifts.size());
	}
	
}
