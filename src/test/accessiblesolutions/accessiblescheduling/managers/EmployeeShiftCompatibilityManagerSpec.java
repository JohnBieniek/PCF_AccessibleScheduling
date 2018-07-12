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
	    Shift crossMonthShift1 = new Shift();
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
	    when(employeeShiftManager.shiftRepository.findByStartMonth(6)).thenReturn(crossMonthShifts);
	   
	    EmployeeShiftManager customEmployeeShiftManager = new EmployeeShiftManager(employeeCrud, shiftRepository);
	    customEmployeeShiftManager.shiftRepository=shiftRepository;
	    
	     fixture= new EmployeeShiftCompatibilityManager(clientRepository, employeeCrud);
	     fixture.employeeShiftManager=customEmployeeShiftManager;
	}
	
	@After
    public final void tearDown() {
		
	}

	@Test
	public void getHoursScheduledWeekOfMonthThrowsProccessingExceptionForNullEmployee() {
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
	public void getHoursScheduledWeekOfMonthReturns0ForUnassignedEmployee() {
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
////	
//	@Test
//	public void getHoursScheduledWeekOfMonthReturnsSumOfShiftsCrossWeekOrMonth() throws CorruptDataException, ProccessingException {
//		boolean errored = false;
//		float hours = -1;
//		Employee crossMonth = new Employee("Cross","Month");
//		crossMonth.setId("crossMonth");
//	    
//		Shift crossMonthShift1 = new Shift();
//	    crossMonthShift1.setStaffId("crossMonth");
//	    crossMonthShift1.setClientId("crossMonthClient");
//	    crossMonthShift1.setStartDate("2018-06-01");
//	    crossMonthShift1.setEndDate("2018-06-01");
//	    crossMonthShift1.setStartTime("20:00");
//	    crossMonthShift1.setEndTime("22:00");
//	    
//	    ArrayList<Shift> crossMonthShifts = new ArrayList<Shift>();
//	    crossMonthShifts.add(crossMonthShift1);
//	    
//	    EmployeeShiftManager customEmployeeShiftManager = new EmployeeShiftManager(employeeCrud, shiftRepository);
//	    customEmployeeShiftManager.shiftRepository=shiftRepository;
//	    //	    when(fixture.employeeShiftManager.shiftRepository.findByStartMonth(6)).thenReturn(crossMonthShifts);
////	    when(fixture.employeeShiftManager.shiftRepository.findByStartMonth(5)).thenReturn(crossMonthShifts);
//	   //when(customEmployeeShiftManager.shiftRepository.findByStartMonth(6)).thenReturn(crossMonthShifts);
//	    //when(customEmployeeShiftManager.shiftRepository.findByStartMonth(5)).thenReturn(crossMonthShifts);
//	    float customHours =customEmployeeShiftManager.getHoursScheduledWeekOfMonth(crossMonth,0,6);
//	    System.out.println("customHours"+customHours);
//	    when( employeeShiftManager.getHoursScheduledWeekOfMonth(crossMonth,0,6)).thenReturn(customHours);
//	   // when( employeeShiftManager.getHoursScheduledWeekOfMonth(crossMonth,0,6)).thenReturn((float) 10.0);
//		try {
//			hours = fixture.getHoursScheduledWeekOfShift(crossMonth,crossMonthShift1);
//		} catch (CorruptDataException e) {
//			errored=true;
//			e.printStackTrace();
//		} catch (ProccessingException e) {
//			errored=true;
//			e.printStackTrace();
//		}
//		
//		assertFalse(errored);
//		System.out.println("hours found" + hours);
//		assertTrue(10==hours);
//	}
	
}
