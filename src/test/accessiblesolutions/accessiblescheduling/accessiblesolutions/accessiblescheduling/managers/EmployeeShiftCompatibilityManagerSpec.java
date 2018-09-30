package accessiblesolutions.accessiblescheduling.managers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import javax.annotation.Resource;

import org.cloudfoundry.samples.music.managers.CustomDataManager;
import org.cloudfoundry.samples.music.managers.EmployeeClientCompatibilityManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftCompatibilityManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftMapManager;
import org.cloudfoundry.samples.music.managers.ShiftGenerationManager;
import org.cloudfoundry.samples.music.managers.ShiftManager;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoClientRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldRepository;
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
import accessiblesolutions.accessiblescheduling.domain.CustomField;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibilities;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibility;
import accessiblesolutions.accessiblescheduling.domain.Gender;
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
	MongoCustomFieldRepository customFieldRepository;
	
	@Mock
    CrudRepository<Employee,String> employeeRepository;
    
	@Mock
	CrudRepository<CustomFieldData, String> customFieldDataRepository;
	
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
	    assignedOneDay.setId("assignedOneDay");
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
	    
	    ArrayList<Employee> employees = new ArrayList<Employee>();
		employees.add(inOvertime);
		Iterable<Employee> itterable = employees;
		when(employeeCrud.findAll()).thenReturn(itterable);
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(new Client());
		
		Employee assignedFiveDay = new Employee("Assigned","FiveDay");
	    assignedFiveDay.setId("assignedFiveDay");
	    ArrayList<Shift> assignedFiveDayShifts = new ArrayList<Shift>();
	    Shift assignedFiveDayShift1 = new Shift();
	    assignedFiveDayShift1.setStaffId("assignedFiveDay");
	    assignedFiveDayShift1.setStartDate("2018-02-05");
	    assignedFiveDayShift1.setEndDate("2018-02-05");
	    assignedFiveDayShift1.setStartTime("02:00");
	    assignedFiveDayShift1.setEndTime("10:00");
	    assignedFiveDayShift1.setStartMonth(2);
	    
	    Shift assignedFiveDayShift2 = new Shift();
	    assignedFiveDayShift2.setStaffId("assignedFiveDay");
	    assignedFiveDayShift2.setStartDate("2018-02-06");
	    assignedFiveDayShift2.setEndDate("2018-02-06");
	    assignedFiveDayShift2.setStartTime("02:00");
	    assignedFiveDayShift2.setEndTime("18:00");
	    assignedFiveDayShift2.setStartMonth(2);
	    
	    Shift assignedFiveDayShift3 = new Shift();
	    assignedFiveDayShift3.setStaffId("assignedFiveDay");
	    assignedFiveDayShift3.setStartDate("2018-02-07");
	    assignedFiveDayShift3.setEndDate("2018-02-07");
	    assignedFiveDayShift3.setStartTime("02:00");
	    assignedFiveDayShift3.setEndTime("10:00");
	    assignedFiveDayShift3.setStartMonth(2);
	    
	    Shift assignedFiveDayShift4 = new Shift();
	    assignedFiveDayShift4.setStaffId("assignedFiveDay");
	    assignedFiveDayShift4.setStartDate("2018-02-08");
	    assignedFiveDayShift4.setEndDate("2018-02-08");
	    assignedFiveDayShift4.setStartTime("02:00");
	    assignedFiveDayShift4.setEndTime("10:00");
	    assignedFiveDayShift4.setStartMonth(2);
	    
	    Shift assignedFiveDayShift5 = new Shift();
	    assignedFiveDayShift5.setStaffId("assignedFiveDay");
	    assignedFiveDayShift5.setStartDate("2018-02-09");
	    assignedFiveDayShift5.setEndDate("2018-02-09");
	    assignedFiveDayShift5.setStartTime("02:00");
	    assignedFiveDayShift5.setEndTime("10:00");
	    assignedFiveDayShift5.setStartMonth(2);
	    
	    assignedFiveDayShifts.add(assignedFiveDayShift1);
	    assignedFiveDayShifts.add(assignedFiveDayShift2);
	    assignedFiveDayShifts.add(assignedFiveDayShift3);
	    assignedFiveDayShifts.add(assignedFiveDayShift4);
	    assignedFiveDayShifts.add(assignedFiveDayShift5);
	    
	    when(shiftRepository.findByStartMonth(2)).thenReturn(assignedFiveDayShifts);
	    when(employeeCrud.findOne("assignedFiveDay")).thenReturn(assignedFiveDay);
	    when(employeeRepository.findOne("assignedFiveDay")).thenReturn(assignedFiveDay);
	    
	    CustomField woodField = new CustomField();
		woodField.setClientRequirement(true);
		woodField.setId("woodId");
		Client onAlwaysClient = new Client("On","AlwaysClient");
		onAlwaysClient.setId("onAlwaysClient");
		CustomFieldData onAlwaysClientData = new CustomFieldData();
		onAlwaysClientData.setOwnerId("onAlwaysClient");
		onAlwaysClientData.setCustomFieldId("woodId");
		onAlwaysClientData.setBooleanData(true);
		
	    EmployeeShiftManager customEmployeeShiftManager = new EmployeeShiftManager(employeeCrud, shiftRepository);
	    customEmployeeShiftManager.shiftRepository=shiftRepository;
	    when(employeeCrud.findAll()).thenReturn(itterable);
	     fixture= new EmployeeShiftCompatibilityManager(clientRepository, employeeCrud);
	     fixture.employeeShiftManager=customEmployeeShiftManager;
	     fixture.employeeClientCompatibilityManager = new EmployeeClientCompatibilityManager(customFieldRepository);
	     when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlways,woodField)).thenReturn(false);
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlwaysClient,woodField)).thenReturn(true);
		    
		fixture.employeeClientCompatibilityManager.customDataManager = customDataManager;
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
	public void hoursNeededWeekOfShiftThrowsProccessingExceptionWithNullEmployee() {
		Shift shift = new Shift();
		boolean exception=false;
		
		try {
			fixture.hoursNeededWeekOfShift(null,shift);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void hoursNeededWeekOfShiftThrowsProccessingExceptionWithNullShift() {
		Employee employee = new Employee();
		boolean exception=false;
		
		try {
			fixture.hoursNeededWeekOfShift(employee,null);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void hoursNeededWeekOfShiftThrowsCorruptDataExceptionWithInvalidShift() {
		Employee employee = new Employee();
		Shift shift = new Shift();
		boolean exception=false;
		
		try {
			fixture.hoursNeededWeekOfShift(employee,shift);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void hoursNeededWeekOfShiftReturnsFloatWhenUnderHours() {
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
			hours = fixture.hoursNeededWeekOfShift(assignedOneDay,assignedOneDayShift1);
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
	public void hoursNeededWeekOfShiftReturns0WhenMinMet() {
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
			hours = fixture.hoursNeededWeekOfShift(assignedOneDay,assignedOneDayShift1);
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
	
	@Test
	public void hoursAvailableWeekOfShiftThrowsProccessingExceptionWithNullEmployee() {
		Shift shift = new Shift();
		boolean exception=false;
		
		try {
			fixture.hoursAvailableWeekOfShift(null,shift);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void hoursAvailableWeekOfShiftThrowsProccessingExceptionWithNullShift() {
		Employee employee = new Employee();
		boolean exception=false;
		
		try {
			fixture.hoursAvailableWeekOfShift(employee,null);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void hoursAvailableWeekOfShiftThrowsCorruptDataExceptionWithInvalidShift() {
		Employee employee = new Employee();
		Shift shift = new Shift();
		boolean exception=false;
		
		try {
			fixture.hoursAvailableWeekOfShift(employee,shift);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void hoursAvailableWeekOfShiftReturnsFloatWhenUnderMaxHours() {
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
			hours = fixture.hoursAvailableWeekOfShift(assignedOneDay,assignedOneDayShift1);
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
	public void hoursAvailableWeekOfShiftReturns0WhenMaxMet() {
	    Employee assignedOneDay = new Employee("Assigned","OneDay");
	    assignedOneDay.setMaxHours(1);
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
			hours = fixture.hoursAvailableWeekOfShift(assignedOneDay,assignedOneDayShift1);
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
	
	@Test
	public void hoursAvailableAfterAssignmentThrowsProccessingExceptionWithNullEmployee() {
		Shift shift = new Shift();
		boolean exception=false;
		
		try {
			fixture.hoursAvailableAfterAssignment(null,shift);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void hoursAvailableAfterAssignmentThrowsProccessingExceptionWithNullShift() {
		Employee employee = new Employee();
		boolean exception=false;
		
		try {
			fixture.hoursAvailableAfterAssignment(employee,null);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void hoursAvailableAfterAssignmentThrowsCorruptDataExceptionWithInvalidShift() {
		Employee employee = new Employee();
		Shift shift = new Shift();
		boolean exception=false;
		
		try {
			fixture.hoursAvailableAfterAssignment(employee,shift);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void hoursAvailableAfterAssignmentReturnsFloatWhenUnderHours() {
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
	    assignedOneDayShift1.setEndTime("04:00");
		boolean exception=false;
		float hours=0;
		try {
			hours = fixture.hoursAvailableAfterAssignment(assignedOneDay,assignedOneDayShift1);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertFalse(exception);
		assertTrue(hours==2.0);
	}
	
	@Test
	public void hoursAvailableAfterAssignmentReturns0WhenMaxMet() {
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
			hours = fixture.hoursAvailableAfterAssignment(assignedOneDay,assignedOneDayShift1);
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
	
	@Test
	public void isUnassignedForThrowsProccessingExceptionForNullEmployee() {
		Shift shift = new Shift();
		boolean errored=false;
		
		try {
			fixture.isUnassignedFor(null, shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void isUnassignedForThrowsProccessingExceptionForNullShift() {
		Employee employee = new Employee();
		boolean errored=false;
		
		try {
			fixture.isUnassignedFor(employee,null);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void isUnassignedForThrowsCorruptDataExceptionForInvalidShift() {
		Employee employee = new Employee();
		Shift shift = new Shift();
		boolean errored=false;
		
		try {
			fixture.isUnassignedFor(employee,shift);
		} catch (ProccessingException e) {
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void isUnassignedForReturnsTrueForShiftlessEmployee() {
		Employee assignedOneDay = new Employee("Assigned","OneDay");
		assignedOneDay.setId("assignedOneDay");
		 Shift inOvertimeShift1 = new Shift();
		    inOvertimeShift1.setStaffId("inOvertime");
		    inOvertimeShift1.setClientId("inOvertimeClient");
		    inOvertimeShift1.setStartDate("2018-07-01");
		    inOvertimeShift1.setEndDate("2018-07-01");
		    inOvertimeShift1.setStartTime("10:00");
		    inOvertimeShift1.setEndTime("20:00");
		    inOvertimeShift1.setStartMonth(7);
		boolean errored=false;
		boolean unassigned = false;
		try {
			unassigned = fixture.isUnassignedFor(assignedOneDay,inOvertimeShift1);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(unassigned);
	}
	
	@Test
	public void isUnassignedForReturnsTrueForAlternateClientAfter30Minutes() {
		Employee assignedOneDay = new Employee("In","Overtime");
		assignedOneDay.setId("inOvertime");
		 Shift inOvertimeShift1 = new Shift();
		    inOvertimeShift1.setStaffId("inOvertime");
		    inOvertimeShift1.setClientId("inOvertimeClient2");
		    inOvertimeShift1.setStartDate("2018-07-01");
		    inOvertimeShift1.setEndDate("2018-07-01");
		    inOvertimeShift1.setStartTime("20:30");
		    inOvertimeShift1.setEndTime("22:00");
		    inOvertimeShift1.setStartMonth(7);
		boolean errored=false;
		boolean unassigned = false;
		try {
			unassigned = fixture.isUnassignedFor(assignedOneDay,inOvertimeShift1);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(unassigned);
	}
	
	@Test
	public void isUnassignedForReturnsFalseForOverlappingShift() {
		Employee assignedOneDay = new Employee("In","Overtime");
		assignedOneDay.setId("inOvertime");
		 Shift inOvertimeShift1 = new Shift();
		    inOvertimeShift1.setStaffId("inOvertime");
		    inOvertimeShift1.setClientId("inOvertimeClient");
		    inOvertimeShift1.setStartDate("2018-07-01");
		    inOvertimeShift1.setEndDate("2018-07-01");
		    inOvertimeShift1.setStartTime("10:00");
		    inOvertimeShift1.setEndTime("20:00");
		    inOvertimeShift1.setStartMonth(7);
		boolean errored=false;
		boolean unassigned = false;
		try {
			unassigned = fixture.isUnassignedFor(assignedOneDay,inOvertimeShift1);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(unassigned);
	}
	
	@Test
	public void isUnassignedForReturnsFalseForAlmostOverlappingShift() {
		Employee assignedOneDay = new Employee("In","Overtime");
		assignedOneDay.setId("inOvertime");
		 Shift inOvertimeShift1 = new Shift();
		    inOvertimeShift1.setStaffId("inOvertime");
		    inOvertimeShift1.setClientId("inOvertimeClient2");
		    inOvertimeShift1.setStartDate("2018-07-01");
		    inOvertimeShift1.setEndDate("2018-07-01");
		    inOvertimeShift1.setStartTime("20:29");
		    inOvertimeShift1.setEndTime("22:00");
		    inOvertimeShift1.setStartMonth(7);
		boolean errored=false;
		boolean unassigned = false;
		try {
			unassigned = fixture.isUnassignedFor(assignedOneDay,inOvertimeShift1);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(unassigned);
	}
	
	@Test
	public void isUnassignedForReturnsTrueForAlmostOverlappingShiftOfSameClient() {
		Employee assignedOneDay = new Employee("In","Overtime");
		assignedOneDay.setId("inOvertime");
		 Shift inOvertimeShift1 = new Shift();
		    inOvertimeShift1.setStaffId("inOvertime");
		    inOvertimeShift1.setClientId("inOvertimeClient");
		    inOvertimeShift1.setStartDate("2018-07-01");
		    inOvertimeShift1.setEndDate("2018-07-01");
		    inOvertimeShift1.setStartTime("20:29");
		    inOvertimeShift1.setEndTime("22:00");
		    inOvertimeShift1.setStartMonth(7);
		boolean errored=false;
		boolean unassigned = false;
		try {
			unassigned = fixture.isUnassignedFor(assignedOneDay,inOvertimeShift1);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(unassigned);
	}
	
	@Test
	public void getEmployeeShiftCompatibilitiesForShiftNullShiftThrowsProccessingException() {
		boolean exception = false;
		
		try {
			fixture.getEmployeeShiftCompatibilitiesForShift(null);
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getEmployeeShiftCompatibilitiesForShiftInvalidShiftThrowsCorruptDataException() {
		boolean exception = false;
		
		try {
			fixture.getEmployeeShiftCompatibilitiesForShift(new Shift());
		} catch (ProccessingException e) {
			e.printStackTrace();
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getEmployeeShiftCompatibilitiesForShiftReturnsContentForAllEmployees() {
		boolean exception = false;
		
		Shift inOvertimeShift1 = new Shift();
	    inOvertimeShift1.setStaffId("inOvertime");
	    inOvertimeShift1.setClientId("inOvertimeClient");
	    inOvertimeShift1.setStartDate("2018-07-01");
	    inOvertimeShift1.setEndDate("2018-07-01");
	    inOvertimeShift1.setStartTime("10:00");
	    inOvertimeShift1.setEndTime("20:00");
	    inOvertimeShift1.setStartMonth(7);
	    EmployeeShiftCompatibilities compatibilities=null;
		try {
			compatibilities = fixture.getEmployeeShiftCompatibilitiesForShift(inOvertimeShift1);
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertFalse(exception);
		assertNotNull(compatibilities);
		assertTrue(compatibilities.compatibilities.size()==1);
	}
	
	@Test
	public void getEmployeeShiftCompatibilitiesForShiftReturnsClientInfo() {
		boolean exception = false;
		
		Shift inOvertimeShift1 = new Shift();
	    inOvertimeShift1.setStaffId("inOvertime");
	    inOvertimeShift1.setClientId("inOvertimeClient");
	    inOvertimeShift1.setStartDate("2018-07-01");
	    inOvertimeShift1.setEndDate("2018-07-01");
	    inOvertimeShift1.setStartTime("10:00");
	    inOvertimeShift1.setEndTime("20:00");
	    inOvertimeShift1.setStartMonth(7);
	    EmployeeShiftCompatibilities compatibilities=null;
		try {
			compatibilities = fixture.getEmployeeShiftCompatibilitiesForShift(inOvertimeShift1);
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertFalse(exception);
		assertNotNull(compatibilities);
		assertNotNull(compatibilities.compatibilities.get(0));
		assertNotNull(compatibilities.compatibilities.get(0).client);
	}
	
	@Test
	public void getEmployeeShiftCompatibilitiesForShiftReturnsNoClientInfoForEvents() {
		boolean exception = false;
		
		Shift inOvertimeShift1 = new Shift();
		inOvertimeShift1.setEvent(true);
	    inOvertimeShift1.setStaffId("inOvertime");
	    inOvertimeShift1.setClientId("inOvertimeClient");
	    inOvertimeShift1.setStartDate("2018-07-01");
	    inOvertimeShift1.setEndDate("2018-07-01");
	    inOvertimeShift1.setStartTime("10:00");
	    inOvertimeShift1.setEndTime("20:00");
	    inOvertimeShift1.setStartMonth(7);
	    EmployeeShiftCompatibilities compatibilities=null;
		try {
			compatibilities = fixture.getEmployeeShiftCompatibilitiesForShift(inOvertimeShift1);
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertFalse(exception);
		assertNotNull(compatibilities);
		assertNotNull(compatibilities.compatibilities.get(0));
		assertNull(compatibilities.compatibilities.get(0).client);
	}
	
	@Test
	public void getEmployeeShiftCompatibilitiesForShiftReturnsEmptyForNoEmployees() {
		EmployeeShiftManager customEmployeeShiftManager = new EmployeeShiftManager(employeeCrud, shiftRepository);
	    customEmployeeShiftManager.shiftRepository=shiftRepository;
	    when(employeeCrud.findAll()).thenReturn(null);
	     fixture= new EmployeeShiftCompatibilityManager(clientRepository, employeeCrud);
	     fixture.employeeShiftManager=customEmployeeShiftManager;
	     
		boolean exception = false;
		
		Shift inOvertimeShift1 = new Shift();
	    inOvertimeShift1.setStaffId("inOvertime");
	    inOvertimeShift1.setClientId("inOvertimeClient");
	    inOvertimeShift1.setStartDate("2018-07-01");
	    inOvertimeShift1.setEndDate("2018-07-01");
	    inOvertimeShift1.setStartTime("10:00");
	    inOvertimeShift1.setEndTime("20:00");
	    inOvertimeShift1.setStartMonth(7);
	    EmployeeShiftCompatibilities compatibilities=null;
		try {
			compatibilities = fixture.getEmployeeShiftCompatibilitiesForShift(inOvertimeShift1);
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertFalse(exception);
		assertNotNull(compatibilities);
		assertTrue(compatibilities.compatibilities.size()==0);
	}
	
	@Test
	public void getHoursNeededAfterAssignmentThrowsProccessingExceptionWithNullCompatibility() {
		boolean exception=false;
		
		try {
			fixture.getHoursNeededAfterAssignment(null);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getHoursNeededAfterAssignmentThrowsProccessingExceptionWithNullEmployee() {
		Shift shift = new Shift();
		
		boolean exception=false;
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(null,shift);
		try {
			fixture.getHoursNeededAfterAssignment(compatibility);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getHoursNeededAfterAssignmentThrowsProccessingExceptionWithNullShift() {
		Employee employee = new Employee();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,null);
		boolean exception=false;
		
		try {
			fixture.getHoursNeededAfterAssignment(compatibility);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getHoursNeededAfterAssignmentThrowsCorruptDataExceptionWithInvalidShift() {
		Employee employee = new Employee();
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,shift);
		boolean exception=false;
		
		try {
			fixture.getHoursNeededAfterAssignment(compatibility);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getHoursNeededAfterAssignmentReturnsFloatWhenUnderHours() {
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
	    assignedOneDayShift1.setEndTime("04:00");
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(assignedOneDay,assignedOneDayShift1);
		boolean exception=false;
		float hours=0;
		try {
			hours = fixture.getHoursNeededAfterAssignment(compatibility);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertFalse(exception);
		assertTrue(hours==2.0);
	}
	
	@Test
	public void getHoursNeededAfterAssignmentReturns0WhenMinMet() {
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
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(assignedOneDay,assignedOneDayShift1);

		boolean exception=false;
		float hours=0;
		try {
			hours = fixture.getHoursNeededAfterAssignment(compatibility);
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
	
	@Test
	public void getHoursNeededThrowsProccessingExceptionWithNullCompatibility() {
		boolean exception=false;
		
		try {
			fixture.getHoursNeeded(null);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getHoursNeededThrowsProccessingExceptionWithNullEmployee() {
		Shift shift = new Shift();
		
		boolean exception=false;
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(null,shift);
		try {
			fixture.getHoursNeeded(compatibility);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getHoursNeededThrowsProccessingExceptionWithNullShift() {
		Employee employee = new Employee();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,null);
		boolean exception=false;
		
		try {
			fixture.getHoursNeeded(compatibility);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getHoursNeededThrowsCorruptDataExceptionWithInvalidShift() {
		Employee employee = new Employee();
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,shift);
		boolean exception=false;
		
		try {
			fixture.getHoursNeeded(compatibility);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getHoursNeededReturnsFloatWhenUnderHours() {
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
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(assignedOneDay,assignedOneDayShift1);
		boolean exception=false;
		float hours=0;
		try {
			hours = fixture.getHoursNeeded(compatibility);
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
	public void getAssignmentWouldReachMinimumThrowsProccessingExceptionWithNullCompatibility() {
		boolean exception=false;
		
		try {
			fixture.getAssignmentWouldReachMinimum(null);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getAssignmentWouldReachMinimumThrowsProccessingExceptionWithNullEmployee() {
		Shift shift = new Shift();
		
		boolean exception=false;
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(null,shift);
		try {
			fixture.getAssignmentWouldReachMinimum(compatibility);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getAssignmentWouldReachMinimumThrowsProccessingExceptionWithNullShift() {
		Employee employee = new Employee();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,null);
		boolean exception=false;
		
		try {
			fixture.getAssignmentWouldReachMinimum(compatibility);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getAssignmentWouldReachMinimumThrowsCorruptDataExceptionWithInvalidShift() {
		Employee employee = new Employee();
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,shift);
		boolean exception=false;
		
		try {
			fixture.getAssignmentWouldReachMinimum(compatibility);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getAssignmentWouldReachMinimumReturnsFalseWhenUnderHours() {
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
	    assignedOneDayShift1.setEndTime("04:00");
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(assignedOneDay,assignedOneDayShift1);
		boolean exception=false;
		boolean minReached=true;
		try {
			minReached = fixture.getAssignmentWouldReachMinimum(compatibility);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertFalse(exception);
		assertFalse(minReached);
	}
	
	@Test
	public void getAssignmentWouldReachMinimumReturnsTrueWhenMinMet() {
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
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(assignedOneDay,assignedOneDayShift1);

		boolean exception=false;
		boolean minReached = false;
		try {
			minReached = fixture.getAssignmentWouldReachMinimum(compatibility);
		} catch (CorruptDataException e) {
			exception=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
			e.printStackTrace();
		}
		
		assertFalse(exception);
		assertTrue(minReached);
	}
	
	@Test
	public void getAssignmentWouldViolateMaxShiftsPerDayReturnsProccessingExceptionWithNullCompatibility() {
		boolean errored=false;
		
		try {
			fixture.getAssignmentWouldViolateMaxShiftsPerDay(null);
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getAssignmentWouldViolateMaxShiftsPerDayReturnsProccessingExceptionWithNullEmployee() {
		boolean errored=false;
		Employee employee = new Employee();
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(null,shift);
		try {
			fixture.getAssignmentWouldViolateMaxShiftsPerDay(compatibility);
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getAssignmentWouldViolateMaxShiftsPerDayReturnsProccessingExceptionWithNullShift() {
		boolean errored=false;
		Employee employee = new Employee();
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,null);
		try {
			fixture.getAssignmentWouldViolateMaxShiftsPerDay(compatibility);
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getAssignmentWouldViolateMaxShiftsPerDayReturnsCorruptDataExceptionWithInvalidShift() {
		boolean errored=false;
		Employee employee = new Employee();
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,shift);
		try {
			fixture.getAssignmentWouldViolateMaxShiftsPerDay(compatibility);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getAssignmentWouldViolateMaxShiftsPerDayReturnsFalseUnderMax() {
		boolean errored=false;
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
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(assignedOneDay,assignedOneDayShift1);
	    boolean violates =true;
		try {
			violates =fixture.getAssignmentWouldViolateMaxShiftsPerDay(compatibility);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(violates);
	}
	
	@Test
	public void getAssignmentWouldViolateMaxShiftsPerDayReturnsTrueAtMax() {
		boolean errored=false;
	    Employee assignedOneDay = new Employee("Assigned","OneDay");
	    assignedOneDay.setId("assignedOneDay");
	    Shift assignedOneDayShift1 = new Shift();
	    assignedOneDayShift1.setStaffId("assignedOneDay");
	    assignedOneDayShift1.setClientId("assignedOneDayClient");
	    assignedOneDayShift1.setStartDate("2018-04-02");
	    assignedOneDayShift1.setEndDate("2018-04-02");
	    assignedOneDayShift1.setStartMonth(4);
	    assignedOneDayShift1.setStartTime("02:00");
	    assignedOneDayShift1.setEndTime("10:00");
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(assignedOneDay,assignedOneDayShift1);
	    boolean violates =true;
		try {
			violates =fixture.getAssignmentWouldViolateMaxShiftsPerDay(compatibility);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(violates);
	}
	

	@Test
	public void getAssignmentWouldViolateMaxWeeklyWorkDaysReturnsProccessingExceptionWithNullCompatibility() {
		boolean errored=false;
		
		try {
			fixture.getAssignmentWouldViolateMaxWeeklyWorkDays(null);
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getAssignmentWouldViolateMaxWeeklyWorkDaysReturnsProccessingExceptionWithNullEmployee() {
		boolean errored=false;
		Employee employee = new Employee();
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(null,shift);
		try {
			fixture.getAssignmentWouldViolateMaxWeeklyWorkDays(compatibility);
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getAssignmentWouldViolateMaxWeeklyWorkDaysReturnsProccessingExceptionWithNullShift() {
		boolean errored=false;
		Employee employee = new Employee();
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,null);
		try {
			fixture.getAssignmentWouldViolateMaxWeeklyWorkDays(compatibility);
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getAssignmentWouldViolateMaxWeeklyWorkDaysReturnsCorruptDataExceptionWithInvalidShift() {
		boolean errored=false;
		Employee employee = new Employee();
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,shift);
		try {
			fixture.getAssignmentWouldViolateMaxWeeklyWorkDays(compatibility);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getAssignmentWouldViolateMaxWeeklyWorkDaysReturnsFalseUnderMax() {
		boolean errored=false;
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
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(assignedOneDay,assignedOneDayShift1);
	    boolean violates =true;
		try {
			violates =fixture.getAssignmentWouldViolateMaxWeeklyWorkDays(compatibility);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(violates);
	}
	
	@Test
	public void getAssignmentWouldViolateMaxWeeklyWorkDaysReturnsTrueAtMax() {
		boolean errored=false;
		Employee assignedFiveDay = new Employee("Assigned","FiveDay");
	    assignedFiveDay.setId("assignedFiveDay");
	    Shift assignedFiveDayShift1 = new Shift();
	    assignedFiveDayShift1.setStaffId("assignedFiveDay");
	    assignedFiveDayShift1.setClientId("assignedFiveDayClient");
	    assignedFiveDayShift1.setStartDate("2018-02-10");
	    assignedFiveDayShift1.setEndDate("2018-02-10");
	    assignedFiveDayShift1.setStartTime("02:00");
	    assignedFiveDayShift1.setEndTime("10:00");
	    assignedFiveDayShift1.setStartMonth(2);
	    
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(assignedFiveDay,assignedFiveDayShift1);
	    boolean violates =true;
		try {
			violates =fixture.getAssignmentWouldViolateMaxWeeklyWorkDays(compatibility);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(violates);
	}
	
	@Test
	public void getAssignmentWouldViolateMaxWeeklyWorkDaysReturnsFalseAtMaxForDuplicateDay() {
		boolean errored=false;
		Employee assignedFiveDay = new Employee("Assigned","FiveDay");
	    assignedFiveDay.setId("assignedFiveDay");
	    Shift assignedFiveDayShift1 = new Shift();
	    assignedFiveDayShift1.setStaffId("assignedFiveDay");
	    assignedFiveDayShift1.setClientId("assignedFiveDayClient2");
	    assignedFiveDayShift1.setStartDate("2018-02-09");
	    assignedFiveDayShift1.setEndDate("2018-02-09");
	    assignedFiveDayShift1.setStartTime("12:00");
	    assignedFiveDayShift1.setEndTime("22:00");
	    assignedFiveDayShift1.setStartMonth(2);
	    
	    EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(assignedFiveDay,assignedFiveDayShift1);
	    boolean violates =true;
		try {
			violates =fixture.getAssignmentWouldViolateMaxWeeklyWorkDays(compatibility);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(violates);
	}
	
	@Test
	public void isCompatibleWithThrowsProcessingExceptionForNullEmployee() {
		boolean errored=false;
		
		try {
			fixture.isCompatibleWith(null,new Shift());
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void isCompatibleWithThrowsProcessingExceptionForNullShift() {
		boolean errored=false;
		
		try {
			fixture.isCompatibleWith(new Employee(),null);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void isCompatibleWithReturnsFalseWithMissingMedpass() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setMedPass(true);
		Shift shift = new Shift();
		try {
			compatible = fixture.isCompatibleWith(new Employee(),shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertFalse(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsFalseWithDisallowedFemale() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setNoFemale(true);
		Shift shift = new Shift();
		try {
			compatible = fixture.isCompatibleWith(new Employee(),shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertFalse(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsFalseWithDisallowedMale() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setGender(Gender.MALE);
		Client client = new Client();
		client.setNoMale(true);
		Shift shift = new Shift();
		try {
			compatible = fixture.isCompatibleWith( employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertFalse(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsFalseWithForbiddenSmoker() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setSmoker(true);
		Client client = new Client();
		client.setNoSmokers(true);
		Shift shift = new Shift();
		try {
			compatible = fixture.isCompatibleWith(employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertFalse(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsFalseWithForbiddenCats() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setNoCats(true);
		Client client = new Client();
		client.setOwnCats(true);
		Shift shift = new Shift();
		try {
			compatible = fixture.isCompatibleWith(employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertFalse(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsFalseWithMissingSigning() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setSigningOnly(true);
		Shift shift = new Shift();
		try {
			compatible = fixture.isCompatibleWith(employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertFalse(compatible);
	}

	@Test
	public void isCompatibleWithReturnsTrueForCompatibilePeopleWithNoCustomData1() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		
		try {
			compatible = fixture.isCompatibleWith(new Employee(),shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertTrue(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsTrueForCompatibilePeopleWithNoCustomData2() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setMedPassCertified(true);
		Client client = new Client();
		client.setMedPass(true);
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		try {
			compatible = fixture.isCompatibleWith(employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsTrueForCompatibilePeopleWithNoCustomData3() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setNoSmokers(true);
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		try {
			compatible = fixture.isCompatibleWith(employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsTrueForCompatibilePeopleWithNoCustomData4() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setNoMale(true);
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		
		try {
			compatible = fixture.isCompatibleWith(employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsTrueForCompatibilePeopleWithNoCustomData5() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setNoMale(true);
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		
		try {
			compatible = fixture.isCompatibleWith(employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsTrueForCompatibilePeopleWithNoCustomData6() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setGender(Gender.MALE);
		Client client = new Client();
		client.setNoFemale(true);
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		
		try {
			compatible = fixture.isCompatibleWith(employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	public void isCompatibleWithReturnsTrueForCompatibilePeopleWithNoCustomData7() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setNoCats(true);
		Client client = new Client();
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		
		try {
			compatible = fixture.isCompatibleWith(employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsTrueForCompatibilePeopleWithNoCustomData8() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setOwnCats(true);
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		
		try {
			compatible = fixture.isCompatibleWith(employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsTrueForCompatibilePeopleWithNoCustomData9() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setSigning(true);
		Client client = new Client();
		client.setSigningOnly(true);	
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		
		try {
			compatible = fixture.isCompatibleWith(employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsTrueForCompatibilePeopleWithNoCustomData10() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setSigning(true);
		Client client = new Client();
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		
		try {
			compatible = fixture.isCompatibleWith(employee,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsFalseWithFailingCustomClientRequirement() throws ProccessingException, CorruptDataException {
		boolean errored=false;
		boolean compatible = false;
		
		Employee onAlways = new Employee("On","Always");
		onAlways.setId("onAlways");
		CustomFieldData onAlwaysData = new CustomFieldData();
		onAlwaysData.setOwnerId("onAlways");
		onAlwaysData.setCustomFieldId("woodId");
		
		
		when(employeeCrud.findOne("onAlways")).thenReturn(onAlways);

		Client onAlwaysClient = new Client("On","AlwaysClient");
		onAlwaysClient.setId("onAlwaysClient");
		CustomFieldData onAlwaysClientData = new CustomFieldData();
		onAlwaysClientData.setOwnerId("onAlwaysClient");
		onAlwaysClientData.setCustomFieldId("woodId");
		onAlwaysClientData.setBooleanData(true);
		
		
		CustomField woodField = new CustomField();
		woodField.setClientRequirement(true);
		woodField.setId("woodId");
		
		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		customFields.add(woodField);
		when(customFieldRepository.findAll()).thenReturn(customFields);
		
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlways,woodField)).thenReturn(false);
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlwaysClient,woodField)).thenReturn(true);
		
		ArrayList<CustomFieldData> customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysData);

		customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysClientData);
		
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("onAlwaysClient");
		when(clientRepository.findOne("onAlwaysClient")).thenReturn(onAlwaysClient);
		
		try {
			compatible = fixture.isCompatibleWith(onAlways,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsFalseWithFailingCustomEmployeeRequirement() throws ProccessingException, CorruptDataException {
		boolean errored=false;
		boolean compatible = false;
		
		Employee onAlways = new Employee("On","Always");
		onAlways.setId("onAlways");
		CustomFieldData onAlwaysData = new CustomFieldData();
		onAlwaysData.setOwnerId("onAlways");
		onAlwaysData.setCustomFieldId("woodId");
		onAlwaysData.setBooleanData(true);
		
		when(employeeCrud.findOne("onAlways")).thenReturn(onAlways);

		Client onAlwaysClient = new Client("On","AlwaysClient");
		onAlwaysClient.setId("onAlwaysClient");
		CustomFieldData onAlwaysClientData = new CustomFieldData();
		onAlwaysClientData.setOwnerId("onAlwaysClient");
		onAlwaysClientData.setCustomFieldId("woodId");
		onAlwaysClientData.setBooleanData(false);
		
		
		CustomField woodField = new CustomField();
		woodField.setEmployeeRequirement(true);
		woodField.setId("woodId");
		
		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		customFields.add(woodField);
		when(customFieldRepository.findAll()).thenReturn(customFields);
		
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlways,woodField)).thenReturn(true);
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlwaysClient,woodField)).thenReturn(false);
		
		ArrayList<CustomFieldData> customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysData);

		customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysClientData);
		
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("onAlwaysClient");
		when(clientRepository.findOne("onAlwaysClient")).thenReturn(onAlwaysClient);
		
		try {
			compatible = fixture.isCompatibleWith(onAlways,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsTrueWithCustomEmployeeRequirement() throws ProccessingException, CorruptDataException {
		boolean errored=false;
		boolean compatible = false;
		
		Employee onAlways = new Employee("On","Always");
		onAlways.setId("onAlways");
		CustomFieldData onAlwaysData = new CustomFieldData();
		onAlwaysData.setOwnerId("onAlways");
		onAlwaysData.setCustomFieldId("woodId");
		onAlwaysData.setBooleanData(true);
		
		when(employeeCrud.findOne("onAlways")).thenReturn(onAlways);

		Client onAlwaysClient = new Client("On","AlwaysClient");
		onAlwaysClient.setId("onAlwaysClient");
		CustomFieldData onAlwaysClientData = new CustomFieldData();
		onAlwaysClientData.setOwnerId("onAlwaysClient");
		onAlwaysClientData.setCustomFieldId("woodId");
		onAlwaysClientData.setBooleanData(true);
		
		
		CustomField woodField = new CustomField();
		woodField.setEmployeeRequirement(true);
		woodField.setId("woodId");
		
		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		customFields.add(woodField);
		when(customFieldRepository.findAll()).thenReturn(customFields);
		
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlways,woodField)).thenReturn(true);
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlwaysClient,woodField)).thenReturn(true);
		
		ArrayList<CustomFieldData> customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysData);

		customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysClientData);
		
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("onAlwaysClient");
		when(clientRepository.findOne("onAlwaysClient")).thenReturn(onAlwaysClient);
		
		try {
			compatible = fixture.isCompatibleWith(onAlways,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void isCompatibleWithReturnsTrueWithCustomClientRequirement() throws ProccessingException, CorruptDataException {
		boolean errored=false;
		boolean compatible = false;
		
		Employee onAlways = new Employee("On","Always");
		onAlways.setId("onAlways");
		CustomFieldData onAlwaysData = new CustomFieldData();
		onAlwaysData.setOwnerId("onAlways");
		onAlwaysData.setCustomFieldId("woodId");
		onAlwaysData.setBooleanData(true);
		
		when(employeeCrud.findOne("onAlways")).thenReturn(onAlways);

		Client onAlwaysClient = new Client("On","AlwaysClient");
		onAlwaysClient.setId("onAlwaysClient");
		CustomFieldData onAlwaysClientData = new CustomFieldData();
		onAlwaysClientData.setOwnerId("onAlwaysClient");
		onAlwaysClientData.setCustomFieldId("woodId");
		onAlwaysClientData.setBooleanData(true);
		
		
		CustomField woodField = new CustomField();
		woodField.setClientRequirement(true);
		woodField.setId("woodId");
		
		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		customFields.add(woodField);
		when(customFieldRepository.findAll()).thenReturn(customFields);
		
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlways,woodField)).thenReturn(true);
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlwaysClient,woodField)).thenReturn(true);
		
		ArrayList<CustomFieldData> customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysData);

		customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysClientData);
		
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("onAlwaysClient");
		when(clientRepository.findOne("onAlwaysClient")).thenReturn(onAlwaysClient);
		
		try {
			compatible = fixture.isCompatibleWith(onAlways,shift);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void getCompatibleThrowsProcessingExceptionForNullEmployee() {
		boolean errored=false;
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(null,new Shift());
		
		try {
			fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getCompatibleThrowsProcessingExceptionForNullShift() {
		boolean errored=false;
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(new Employee(),null);
		try {
			fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void getCompatibleReturnsFalseWithMissingMedpass() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setMedPass(true);
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertFalse(compatible);
	}
	
	@Test
	public void getCompatibleReturnsFalseWithDisallowedFemale() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setNoFemale(true);
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertFalse(compatible);
	}
	
	@Test
	public void getCompatibleReturnsFalseWithDisallowedMale() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setGender(Gender.MALE);
		Client client = new Client();
		client.setNoMale(true);
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertFalse(compatible);
	}
	
	@Test
	public void getCompatibleReturnsFalseWithForbiddenSmoker() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setSmoker(true);
		Client client = new Client();
		client.setNoSmokers(true);
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertFalse(compatible);
	}
	
	@Test
	public void getCompatibleReturnsFalseWithForbiddenCats() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setNoCats(true);
		Client client = new Client();
		client.setOwnCats(true);
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertFalse(compatible);
	}
	
	@Test
	public void getCompatibleReturnsFalseWithMissingSigning() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setSigningOnly(true);
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertFalse(compatible);
	}

	@Test
	public void getCompatibleReturnsTrueForCompatibilePeopleWithNoCustomData1() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(employee.getMedPassCertified());
		assertTrue(compatible);
	}
	
	@Test
	public void getCompatibleReturnsTrueForCompatibilePeopleWithNoCustomData2() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setMedPassCertified(true);
		Client client = new Client();
		client.setMedPass(true);
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void getCompatibleReturnsTrueForCompatibilePeopleWithNoCustomData3() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setNoSmokers(true);
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void getCompatibleReturnsTrueForCompatibilePeopleWithNoCustomData4() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setNoMale(true);
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void getCompatibleReturnsTrueForCompatibilePeopleWithNoCustomData5() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setNoMale(true);
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void getCompatibleReturnsTrueForCompatibilePeopleWithNoCustomData6() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setGender(Gender.MALE);
		Client client = new Client();
		client.setNoFemale(true);
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	public void getCompatibleReturnsTrueForCompatibilePeopleWithNoCustomData7() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setNoCats(true);
		Client client = new Client();
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void getCompatibleReturnsTrueForCompatibilePeopleWithNoCustomData8() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		Client client = new Client();
		client.setOwnCats(true);
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void getCompatibleReturnsTrueForCompatibilePeopleWithNoCustomData9() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setSigning(true);
		Client client = new Client();
		client.setSigningOnly(true);	
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void getCompatibleReturnsTrueForCompatibilePeopleWithNoCustomData10() {
		boolean errored=false;
		boolean compatible = false;
		Employee employee = new Employee();
		employee.setSigning(true);
		Client client = new Client();
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("inOvertimeClient");
		when(clientRepository.findOne("inOvertimeClient")).thenReturn(client);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(employee,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void getCompatibleReturnsFalseWithFailingCustomClientRequirement() throws ProccessingException, CorruptDataException {
		boolean errored=false;
		boolean compatible = false;
		
		Employee onAlways = new Employee("On","Always");
		onAlways.setId("onAlways");
		CustomFieldData onAlwaysData = new CustomFieldData();
		onAlwaysData.setOwnerId("onAlways");
		onAlwaysData.setCustomFieldId("woodId");
		
		
		when(employeeCrud.findOne("onAlways")).thenReturn(onAlways);

		Client onAlwaysClient = new Client("On","AlwaysClient");
		onAlwaysClient.setId("onAlwaysClient");
		CustomFieldData onAlwaysClientData = new CustomFieldData();
		onAlwaysClientData.setOwnerId("onAlwaysClient");
		onAlwaysClientData.setCustomFieldId("woodId");
		onAlwaysClientData.setBooleanData(true);
		
		
		CustomField woodField = new CustomField();
		woodField.setClientRequirement(true);
		woodField.setId("woodId");
		
		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		customFields.add(woodField);
		when(customFieldRepository.findAll()).thenReturn(customFields);
		
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlways,woodField)).thenReturn(false);
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlwaysClient,woodField)).thenReturn(true);
		
		ArrayList<CustomFieldData> customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysData);

		customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysClientData);
		
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("onAlwaysClient");
		when(clientRepository.findOne("onAlwaysClient")).thenReturn(onAlwaysClient);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(onAlways,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(compatible);
	}
	
	@Test
	public void getCompatibleReturnsFalseWithFailingCustomEmployeeRequirement() throws ProccessingException, CorruptDataException {
		boolean errored=false;
		boolean compatible = false;
		
		Employee onAlways = new Employee("On","Always");
		onAlways.setId("onAlways");
		CustomFieldData onAlwaysData = new CustomFieldData();
		onAlwaysData.setOwnerId("onAlways");
		onAlwaysData.setCustomFieldId("woodId");
		onAlwaysData.setBooleanData(true);
		
		when(employeeCrud.findOne("onAlways")).thenReturn(onAlways);

		Client onAlwaysClient = new Client("On","AlwaysClient");
		onAlwaysClient.setId("onAlwaysClient");
		CustomFieldData onAlwaysClientData = new CustomFieldData();
		onAlwaysClientData.setOwnerId("onAlwaysClient");
		onAlwaysClientData.setCustomFieldId("woodId");
		onAlwaysClientData.setBooleanData(false);
		
		
		CustomField woodField = new CustomField();
		woodField.setEmployeeRequirement(true);
		woodField.setId("woodId");
		
		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		customFields.add(woodField);
		when(customFieldRepository.findAll()).thenReturn(customFields);
		
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlways,woodField)).thenReturn(true);
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlwaysClient,woodField)).thenReturn(false);
		
		ArrayList<CustomFieldData> customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysData);

		customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysClientData);
		
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("onAlwaysClient");
		when(clientRepository.findOne("onAlwaysClient")).thenReturn(onAlwaysClient);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(onAlways,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(compatible);
	}
	
	@Test
	public void getCompatibleReturnsTrueWithCustomEmployeeRequirement() throws ProccessingException, CorruptDataException {
		boolean errored=false;
		boolean compatible = false;
		
		Employee onAlways = new Employee("On","Always");
		onAlways.setId("onAlways");
		CustomFieldData onAlwaysData = new CustomFieldData();
		onAlwaysData.setOwnerId("onAlways");
		onAlwaysData.setCustomFieldId("woodId");
		onAlwaysData.setBooleanData(true);
		
		when(employeeCrud.findOne("onAlways")).thenReturn(onAlways);

		Client onAlwaysClient = new Client("On","AlwaysClient");
		onAlwaysClient.setId("onAlwaysClient");
		CustomFieldData onAlwaysClientData = new CustomFieldData();
		onAlwaysClientData.setOwnerId("onAlwaysClient");
		onAlwaysClientData.setCustomFieldId("woodId");
		onAlwaysClientData.setBooleanData(true);
		
		
		CustomField woodField = new CustomField();
		woodField.setEmployeeRequirement(true);
		woodField.setId("woodId");
		
		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		customFields.add(woodField);
		when(customFieldRepository.findAll()).thenReturn(customFields);
		
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlways,woodField)).thenReturn(true);
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlwaysClient,woodField)).thenReturn(true);
		
		ArrayList<CustomFieldData> customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysData);

		customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysClientData);
		
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("onAlwaysClient");
		when(clientRepository.findOne("onAlwaysClient")).thenReturn(onAlwaysClient);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(onAlways,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void getCompatibleReturnsTrueWithCustomClientRequirement() throws ProccessingException, CorruptDataException {
		boolean errored=false;
		boolean compatible = false;
		
		Employee onAlways = new Employee("On","Always");
		onAlways.setId("onAlways");
		CustomFieldData onAlwaysData = new CustomFieldData();
		onAlwaysData.setOwnerId("onAlways");
		onAlwaysData.setCustomFieldId("woodId");
		onAlwaysData.setBooleanData(true);
		
		when(employeeCrud.findOne("onAlways")).thenReturn(onAlways);

		Client onAlwaysClient = new Client("On","AlwaysClient");
		onAlwaysClient.setId("onAlwaysClient");
		CustomFieldData onAlwaysClientData = new CustomFieldData();
		onAlwaysClientData.setOwnerId("onAlwaysClient");
		onAlwaysClientData.setCustomFieldId("woodId");
		onAlwaysClientData.setBooleanData(true);
		
		
		CustomField woodField = new CustomField();
		woodField.setClientRequirement(true);
		woodField.setId("woodId");
		
		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		customFields.add(woodField);
		when(customFieldRepository.findAll()).thenReturn(customFields);
		
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlways,woodField)).thenReturn(true);
		when(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(onAlwaysClient,woodField)).thenReturn(true);
		
		ArrayList<CustomFieldData> customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysData);

		customFieldData = new ArrayList<CustomFieldData>();
		customFieldData.add(onAlwaysClientData);
		
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("onAlwaysClient");
		when(clientRepository.findOne("onAlwaysClient")).thenReturn(onAlwaysClient);
		EmployeeShiftCompatibility compatibility= new EmployeeShiftCompatibility(onAlways,shift);
		
		try {
			compatible = fixture.getCompatible(compatibility);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(compatible);
	}
	
	@Test
	public void isAvailableForThrowsProccessingExceptionWithNullEmployee() {
		Shift shift = new Shift();
		boolean errored = false;
		try {
			fixture.isAvailableFor(null, shift);
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void isAvailableForThrowsProccessingExceptionWithNullShift() {
		Shift shift = new Shift();
		Employee employee = new Employee();
		
		boolean errored = false;
		try {
			fixture.isAvailableFor(employee,null);
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void isAvailableForThrowsCorruptDataExceptionWithInvalidShift() {
		Shift shift = new Shift();
		Employee employee = new Employee();
		
		boolean errored = false;
		try {
			fixture.isAvailableFor(employee,shift);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void isAvailableForReturnsFalseWithNoAvailability() {
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("onAlwaysClient");
		
		Employee employee = new Employee();
		
		boolean errored = false;
		boolean available = false;
		try {
			available = fixture.isAvailableFor(employee,shift);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(available);
	}
	
	@Test
	public void isAvailableForReturnsTrueWhenAvailabile() {
		Shift shift = new Shift();
		shift.setStartDate("2018-10-10");
		shift.setEndDate("2018-10-10");
		shift.setStartTime("10:00");
		shift.setEndTime("12:00");
		shift.setClientId("onAlwaysClient");
		
		Employee employee = new Employee();
		boolean[] availability = new boolean[24];
		availability[10]=true;
		availability[11]=true;
		employee.setWednesdaysAvailability(availability);
		boolean[] days= new boolean[7];
		days[3]=true;
		employee.setDaysAvailable(days);
		
		boolean errored = false;
		boolean available = false;
		try {
			available = fixture.isAvailableFor(employee,shift);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(available);
	}
	
	@Test
	public void isAvailableForReturnsTrueWhenAvailabileOvernightCrossWeeks() {
		Shift shift = new Shift();
		shift.setStartDate("2018-10-13");
		shift.setEndDate("2018-10-14");
		shift.setStartTime("23:00");
		shift.setEndTime("01:00");
		shift.setClientId("onAlwaysClient");
		
		Employee employee = new Employee();
		boolean[] availability = new boolean[24];
		availability[23]=true;
		availability[0]=true;
		employee.setSaturdaysAvailability(availability);
		employee.setSundaysAvailability(availability);
		boolean[] days= new boolean[7];
		days[6]=true;
		days[0]=true;
		employee.setDaysAvailable(days);
		
		boolean errored = false;
		boolean available = false;
		try {
			available = fixture.isAvailableFor(employee,shift);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertTrue(available);
	}
	
	@Test
	public void isAvailableForReturnsFalseWhenPartlyAvailable1() {
		Shift shift = new Shift();
		shift.setStartDate("2018-10-13");
		shift.setEndDate("2018-10-14");
		shift.setStartTime("23:00");
		shift.setEndTime("01:00");
		shift.setClientId("onAlwaysClient");
		
		Employee employee = new Employee();
		boolean[] availability = new boolean[24];
		availability[23]=true;
		availability[0]=true;
		employee.setSaturdaysAvailability(availability);
		employee.setSundaysAvailability(availability);
		boolean[] days= new boolean[7];
		days[6]=true;
		employee.setDaysAvailable(days);
		
		boolean errored = false;
		boolean available = false;
		try {
			available = fixture.isAvailableFor(employee,shift);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(available);
	}
	
	@Test
	public void isAvailableForReturnsFalseWhenPartlyAvailable2() {
		Shift shift = new Shift();
		shift.setStartDate("2018-10-13");
		shift.setEndDate("2018-10-14");
		shift.setStartTime("23:00");
		shift.setEndTime("01:00");
		shift.setClientId("onAlwaysClient");
		
		Employee employee = new Employee();
		boolean[] availability = new boolean[24];
		availability[23]=true;
		availability[0]=true;
		employee.setSaturdaysAvailability(availability);
		boolean[] days= new boolean[7];
		days[6]=true;
		days[0]=true;
		employee.setDaysAvailable(days);
		
		boolean errored = false;
		boolean available = false;
		try {
			available = fixture.isAvailableFor(employee,shift);
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		assertFalse(available);
	}
}
