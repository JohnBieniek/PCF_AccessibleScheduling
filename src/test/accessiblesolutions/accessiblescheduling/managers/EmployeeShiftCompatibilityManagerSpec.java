package managers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import javax.annotation.Resource;

import org.cloudfoundry.samples.music.managers.CustomDataManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftCompatibilityManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftMapManager;
import org.cloudfoundry.samples.music.managers.ShiftGenerationManager;
import org.cloudfoundry.samples.music.managers.ShiftManager;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoClientRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;

import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibility;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;

//@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeShiftCompatibilityManagerSpec {
	@Mock
	private Resource resource;

	@Mock
	private CrudRepository<Shift, String> shiftCrud;
    
	@Mock
	MongoShiftRepository shiftRepository;
	
	@Mock
    CrudRepository<Client,String> clientRepository;
	
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
	EmployeeShiftManager employeeShiftManager;
	
	@Mock
    EmployeeShiftCompatibilityManager employeeShiftCompatibilityManager;
    
	@Mock
    ShiftGenerationManager shiftGenerationManager;
	
	// Testing instance, mocked `resource` should be injected here 
	@InjectMocks
	@Resource
	private EmployeeShiftCompatibilityManager fixture;
	
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
	    when(shiftRepository.findByStartMonth(4)).thenReturn(assignedOneDayShifts);
	    when(employeeCrud.findOne("assignedOneDay")).thenReturn(assignedOneDay);
	    when(employeeRepository.findOne("assignedOneDay")).thenReturn(assignedOneDay);
	    
	    Employee crossMonthEmployee = new Employee("Cross","Month");
	    ArrayList<Shift> crossMonthShifts = new ArrayList<Shift>();
	    Shift  crossMonthShift1 = new Shift();
	    crossMonthShift1.setStaffId("crossMonth");
	    crossMonthShift1.setClientId("crossMonthClient");
	    crossMonthShift1.setStartDate("2018-05-31");
	    crossMonthShift1.setEndDate("2018-06-01");
	    crossMonthShift1.setStartTime("20:00");
	    crossMonthShift1.setEndTime("10:00");
	    crossMonthShifts.add(crossMonthShift1);
	    when(shiftRepository.findByStartMonth(5)).thenReturn(crossMonthShifts);
	    when(employeeCrud.findOne("crossMonth")).thenReturn(crossMonthEmployee);
	    when(employeeRepository.findOne("crossMonth")).thenReturn(crossMonthEmployee);
	    employeeShiftManager.shiftRepository=shiftRepository;
	    when(shiftRepository.findByStartMonth(6)).thenReturn(crossMonthShifts);
	   
	    Employee inOvertime = new Employee("In","Overtime");
	    inOvertime.setMaxHours(1);
	    when(employeeCrud.findOne("inOvertime")).thenReturn(inOvertime);
	    Shift inOvertimeShift1 = new Shift();
	    inOvertimeShift1.setStaffId("inOvertime");
	    inOvertimeShift1.setClientId("inOvertimeClient");
	    inOvertimeShift1.setStartDate("2018-07-01");
	    inOvertimeShift1.setEndDate("2018-07-01");
	    inOvertimeShift1.setStartTime("10:00");
	    inOvertimeShift1.setEndTime("20:00");
	    inOvertimeShift1.setStartMonth(7);
	    ArrayList<Shift> inOvertimeShifts = new ArrayList<Shift>();
		inOvertimeShifts.add(inOvertimeShift1);
		
	    when(shiftRepository.findByStartMonth(7)).thenReturn(inOvertimeShifts);
	    
	    EmployeeShiftManager customEmployeeShiftManager = new EmployeeShiftManager(employeeCrud, shiftRepository);
	    customEmployeeShiftManager.shiftRepository=shiftRepository;
	    
	     fixture= new EmployeeShiftCompatibilityManager(clientRepository, employeeCrud);
	     fixture.employeeShiftManager=customEmployeeShiftManager;
	}
	
	@After
    public final void tearDown() {
		
	}

	@Test
	public void getHoursScheduledWeekOfShiftThrowsProccessingExceptionForNullShift() {
		boolean errored = false;
		
		 Shift assignedOneDayShift2 = new Shift();
	    assignedOneDayShift2.setStaffId("assignedOneDay");
	    assignedOneDayShift2.setStartDate("2018-04-02");
	    assignedOneDayShift2.setEndDate("2018-04-02");
	    assignedOneDayShift2.setStartTime("10:00");
	    assignedOneDayShift2.setEndTime("18:00");
	    Employee crossMonthEmployee = new Employee("Cross","Month");
		try {
			float hours = fixture.getHoursScheduledWeekOfShift(crossMonthEmployee,null);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getHoursScheduledWeekOfShiftThrowsProccessingExceptionForNullEmployee() {
		boolean errored = false;
		
		 Shift assignedOneDayShift2 = new Shift();
	    assignedOneDayShift2.setStaffId("assignedOneDay");
	    assignedOneDayShift2.setStartDate("2018-04-02");
	    assignedOneDayShift2.setEndDate("2018-04-02");
	    assignedOneDayShift2.setStartTime("10:00");
	    assignedOneDayShift2.setEndTime("18:00");
		    
		try {
			float hours = fixture.getHoursScheduledWeekOfShift(null,assignedOneDayShift2);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getHoursScheduledWeekOfShiftReturns0ForUnassignedEmployee() {
		boolean errored = false;
		float hours = -1;
		float zero = 0;

	    Employee offOnce = new Employee("Off","Once");
	    String[] requestedOff = {"2018-04-01"};
	    offOnce.setRequestedOff(requestedOff);
	    
	    Shift assignedOneDayShift2 = new Shift();
	    assignedOneDayShift2.setStaffId("assignedOneDay");
	    assignedOneDayShift2.setClientId("fake");
	    assignedOneDayShift2.setStartDate("2018-04-02");
	    assignedOneDayShift2.setEndDate("2018-04-02");
	    assignedOneDayShift2.setStartTime("10:00");
	    assignedOneDayShift2.setEndTime("18:00");
	    assignedOneDayShift2.setStartMonth(4);
	    
		try {
			hours = fixture.getHoursScheduledWeekOfShift(offOnce,assignedOneDayShift2);
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
	public void getHoursScheduledWeekOfShiftThrowsCorruptDataExceptionWithInvalidShift() throws CorruptDataException, ProccessingException {
		boolean errored = false;
		float hours = -1;
		Employee crossMonth = new Employee("Cross","Month");
		crossMonth.setId("crossMonth");
	    
		Shift crossMonthShift1 = new Shift();
	    crossMonthShift1.setStaffId("crossMonth");
	    crossMonthShift1.setStartMonth(6);
	    crossMonthShift1.setStartDate("2018-06-01");
	    crossMonthShift1.setEndDate("2018-06-01");
	    crossMonthShift1.setStartTime("20:00");
	    crossMonthShift1.setEndTime("22:00");
	    
	    ArrayList<Shift> crossMonthShifts = new ArrayList<Shift>();
	    crossMonthShifts.add(crossMonthShift1);
	
		try {
			hours = fixture.getHoursScheduledWeekOfShift(crossMonth,crossMonthShift1);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getHoursScheduledWeekOfShiftReturnsSumOfShiftsCrossWeekOrMonth() throws CorruptDataException, ProccessingException {
		boolean errored = false;
		float hours = -1;
		Employee crossMonth = new Employee("Cross","Month");
		crossMonth.setId("crossMonth");
	    
		Shift crossMonthShift1 = new Shift();
	    crossMonthShift1.setStaffId("crossMonth");
	    crossMonthShift1.setClientId("crossMonthClient");
	    crossMonthShift1.setStartMonth(6);
	    crossMonthShift1.setStartDate("2018-06-01");
	    crossMonthShift1.setEndDate("2018-06-01");
	    crossMonthShift1.setStartTime("20:00");
	    crossMonthShift1.setEndTime("22:00");
	    
	    ArrayList<Shift> crossMonthShifts = new ArrayList<Shift>();
	    crossMonthShifts.add(crossMonthShift1);
	
		try {
			hours = fixture.getHoursScheduledWeekOfShift(crossMonth,crossMonthShift1);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(10==hours);
	}
	
	@Test
	public void getHoursScheduledWeekOfShiftReturnsSumOfShiftsInWeek() {
		boolean errored = false;
		float result = 0;

		Employee assignedOneDay = new Employee("Assigned","OneDay");
		assignedOneDay.setId("assignedOneDay");
	    
		Shift assignedOneDayShift2 = new Shift();
	    assignedOneDayShift2.setStaffId("assignedOneDay");
	    assignedOneDayShift2.setClientId("assignedOneDayClient");
	    assignedOneDayShift2.setStartDate("2018-04-02");
	    assignedOneDayShift2.setEndDate("2018-04-02");
	    assignedOneDayShift2.setStartTime("10:00");
	    assignedOneDayShift2.setEndTime("18:00");
	    assignedOneDayShift2.setStartMonth(4);
		   
		try {
			result = fixture.getHoursScheduledWeekOfShift(assignedOneDay,assignedOneDayShift2);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(36==result);
	}

	@Test
	public void getAssignmentWouldIncurOvertimeThrowsProccessingExceptionWithNullCompatibility() {
		boolean exception = false;
		Employee crossMonth = new Employee("Cross","Month");
		crossMonth.setId("crossMonth");
	    
		Shift crossMonthShift1 = new Shift();
	    crossMonthShift1.setStaffId("crossMonth");
	    crossMonthShift1.setClientId("crossMonthClient");
	    crossMonthShift1.setStartMonth(6);
	    crossMonthShift1.setStartDate("2018-06-01");
	    crossMonthShift1.setEndDate("2018-06-01");
	    crossMonthShift1.setStartTime("20:00");
	    crossMonthShift1.setEndTime("22:00");
		try {
			fixture.getAssignmentWouldIncurOvertime(null);
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		assertTrue(exception);
	}

	@Test
	public void getAssignmentWouldIncurOvertimeThrowsProccessingExceptionWithNullShift() {
		boolean exception = false;
		Employee crossMonth = new Employee("Cross","Month");
		crossMonth.setId("crossMonth");
	    
		Shift crossMonthShift1 = new Shift();
	    crossMonthShift1.setStaffId("crossMonth");
	    crossMonthShift1.setClientId("crossMonthClient");
	    crossMonthShift1.setStartMonth(6);
	    crossMonthShift1.setStartDate("2018-06-01");
	    crossMonthShift1.setEndDate("2018-06-01");
	    crossMonthShift1.setStartTime("20:00");
	    crossMonthShift1.setEndTime("22:00");
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(crossMonth,null);
		try {
			fixture.getAssignmentWouldIncurOvertime(compatibility);
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		assertTrue(exception);
	}
	
	@Test
	public void getAssignmentWouldIncurOvertimeThrowsProccessingExceptionWithNullEmployee() {
		boolean exception = false;
		Employee crossMonth = new Employee("Cross","Month");
		crossMonth.setId("crossMonth");
	    
		Shift crossMonthShift1 = new Shift();
	    crossMonthShift1.setStaffId("crossMonth");
	    crossMonthShift1.setClientId("crossMonthClient");
	    crossMonthShift1.setStartMonth(6);
	    crossMonthShift1.setStartDate("2018-06-01");
	    crossMonthShift1.setEndDate("2018-06-01");
	    crossMonthShift1.setStartTime("20:00");
	    crossMonthShift1.setEndTime("22:00");
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(null,crossMonthShift1);
		try {
			fixture.getAssignmentWouldIncurOvertime(compatibility);
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		assertTrue(exception);
	}
	
	@Test
	public void getAssignmentWouldIncurOvertimeThrowsCorruptDataExceptionWithInvalidShift() {
		boolean exception = false;
		Employee crossMonth = new Employee("Cross","Month");
		crossMonth.setId("crossMonth");
	    
		Shift crossMonthShift1 = new Shift();
	    crossMonthShift1.setStaffId("crossMonth");
	    crossMonthShift1.setClientId("crossMonthClient");
	    crossMonthShift1.setStartMonth(6);
	    crossMonthShift1.setEndDate("2018-06-01");
	    crossMonthShift1.setStartTime("20:00");
	    crossMonthShift1.setEndTime("22:00");
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(crossMonth,crossMonthShift1);
		try {
			fixture.getAssignmentWouldIncurOvertime(compatibility);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		assertTrue(exception);
	}
	
	@Test
	public void getAssignmentWouldIncurOvertimeReturnsFalseWhenBelowMaxAfterAssignment() {
		boolean exception = false;
		boolean overtime=true;
		Employee crossMonth = new Employee("Cross","Month");
		crossMonth.setId("crossMonth");
	    
		Shift crossMonthShift1 = new Shift();
	    crossMonthShift1.setStaffId("crossMonth");
	    crossMonthShift1.setClientId("crossMonthClient");
	    crossMonthShift1.setStartMonth(6);
	    crossMonthShift1.setStartDate("2018-06-01");
	    crossMonthShift1.setEndDate("2018-06-01");
	    crossMonthShift1.setStartTime("20:00");
	    crossMonthShift1.setEndTime("22:00");
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(crossMonth,crossMonthShift1);
		try {
			overtime = fixture.getAssignmentWouldIncurOvertime(compatibility);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		assertFalse(exception);
		assertFalse(overtime);
	}
	
	@Test
	public void getAssignmentWouldIncurOvertimeReturnsTrueWhenAboveMaxAfterAssignment() {
		boolean exception = false;
		boolean overtime=true;
		Employee inOvertime = new Employee("In","Overtime");
		inOvertime.setMaxHours(1);
		inOvertime.setId("inOvertime");
	    
		Shift inOvertimeShift1 = new Shift();
		inOvertimeShift1.setStaffId("inOvertime");
		inOvertimeShift1.setClientId("inOvertimeClient");
		inOvertimeShift1.setStartMonth(7);
		inOvertimeShift1.setStartDate("2018-07-02");
		inOvertimeShift1.setEndDate("2018-07-02");
		inOvertimeShift1.setStartTime("02:00");
	    inOvertimeShift1.setEndTime("22:00");
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(inOvertime,inOvertimeShift1);
		try {
			overtime = fixture.getAssignmentWouldIncurOvertime(compatibility);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		assertFalse(exception);
		assertTrue(overtime);
	}
	
	@Test
	public void hoursNeededWeekOfThrowsProccessingExceptionWithNullEmployee() {
		Shift shift = new Shift();
		boolean exception=false;
		
		try {
			fixture.hoursNeededWeekOf(null,shift);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void hoursNeededWeekOfThrowsProccessingExceptionWithNullShift() {
		Employee employee = new Employee();
		boolean exception=false;
		
		try {
			fixture.hoursNeededWeekOf(employee,null);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void hoursNeededWeekOfThrowsCorruptDataExceptionWithInvalidShift() {
		Employee employee = new Employee();
		Shift shift = new Shift();
		boolean exception=false;
		
		try {
			fixture.hoursNeededWeekOf(employee,shift);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void hoursNeededWeekOfReturnsFloatWhenUnderHours() {
	    Employee assignedOneDay = new Employee("Assigned","OneDay");
	    assignedOneDay.setId("assignedOneDay");
	    assignedOneDay.setMinHours(40);
	    Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setClientId("assignedOneDayClient");
	    assignedOneDayShift1.setStartDate("2018-04-01");
	    assignedOneDayShift1.setEndDate("2018-04-01");
	    assignedOneDayShift1.setStartMonth(4);
	    assignedOneDayShift1.setStartTime("02:00");
	    assignedOneDayShift1.setEndTime("10:00");
		boolean exception=false;
		float hours=0;
		try {
			hours = fixture.hoursNeededWeekOf(assignedOneDay,assignedOneDayShift1);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertFalse(exception);
		assertTrue(hours==4.0);
	}
	
	@Test
	public void hoursNeededWeekOfReturns0WhenMinMet() {
	    Employee assignedOneDay = new Employee("Assigned","OneDay");
	    assignedOneDay.setId("assignedOneDay");
	    Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setClientId("assignedOneDayClient");
	    assignedOneDayShift1.setStartDate("2018-04-01");
	    assignedOneDayShift1.setEndDate("2018-04-01");
	    assignedOneDayShift1.setStartMonth(4);
	    assignedOneDayShift1.setStartTime("02:00");
	    assignedOneDayShift1.setEndTime("10:00");
		boolean exception=false;
		float hours=0;
		try {
			hours = fixture.hoursNeededWeekOf(assignedOneDay,assignedOneDayShift1);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertFalse(exception);
		assertTrue(hours==0.0);
	}
}
