package accessiblesolutions.accessiblescheduling.managers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
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
import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldDataRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;

import accessiblesolutions.accessiblescheduling.constants.Constants;
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
public class CustomDataManagerSpec {
	@Mock
	private Resource resource;

	@Mock
	private CrudRepository<Shift, String> shiftCrud;
    
	@Mock
	MongoShiftRepository shiftRepository;
	
	@Mock
    CrudRepository<Client,String> clientRepository;
	
	@Mock
	CrudRepository<CustomField, String> customFieldRepository;
	
	@Mock
	MongoCustomFieldDataRepository customFieldDataRepository;
	
	@Mock
	CrudRepository<CustomFieldData, String> customFieldDataCrud;
	
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
	private CustomDataManager fixture;
	
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
	    
	    EmployeeShiftManager customEmployeeShiftManager = new EmployeeShiftManager(employeeCrud, shiftRepository);
	    customEmployeeShiftManager.shiftRepository=shiftRepository;
	    when(employeeCrud.findAll()).thenReturn(itterable);
	     
		fixture = new CustomDataManager();
		fixture.customFieldDataRepository = customFieldDataRepository;
		fixture.customFieldDataCrud = customFieldDataCrud;
	}
	
	@Test
	public void setCustomFieldDataThrowProccessingExceptionWithNullIndividual() {
		boolean errored =false;
		
		try {
			fixture.setCustomFieldData(null, new CustomField(), false);
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
	public void setCustomFieldDataThrowProccessingExceptionWithNullField() {
		boolean errored =false;
		
		try {
			fixture.setCustomFieldData(new Employee(),null, false);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void setCustomFieldDataThrowCorruptDataExceptionWithNullEmployeeId() {
		boolean errored =false;
		
		try {
			fixture.setCustomFieldData(new Employee(),new CustomField(), false);
		} catch (ProccessingException e) {
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void setCustomFieldDataThrowCorruptDataExceptionWithNullClientId() {
		boolean errored =false;
		
		try {
			fixture.setCustomFieldData(new Client(),new CustomField(), false);
		} catch (ProccessingException e) {
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertTrue(errored);
	}
	
	@Test
	public void setCustomFieldDataInsertsWhenFieldNotFound1() throws ProccessingException {
		boolean errored =false;
		boolean success=false;
		
		Client client = new Client();
		client.setId("client");
		
		CustomField customField = new CustomField();
		customField.setId("test");
		
	    when(fixture.customFieldDataCrud.save(any(CustomFieldData.class))).thenThrow(new RuntimeException());

		
		try {
			fixture.setCustomFieldData(client,customField, true);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		catch(Exception e) {
			success=true;
		}
		
		assertFalse(errored);
		
		assertTrue(success);
	}
	
	@Test
	public void setCustomFieldDataInsertsWhenFieldNotFound2() throws ProccessingException {
		boolean errored =false;
		
		Client client = new Client();
		client.setId("client");
		
		CustomField customField = new CustomField();
		customField.setId("test");
		
		CustomFieldData output = null;
		try {
			output = fixture.setCustomFieldData(client,customField, true);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		
		assertFalse(errored);
		
		assertTrue(output.getBooleanData());
	}
	
	@Test
	public void setCustomFieldDataUpdatesWhenFieldFound1() throws ProccessingException {
		boolean errored =false;
		boolean success=false;
		Client client = new Client();
		client.setId("client");
		
		CustomField customField = new CustomField();
		customField.setId("test");
		
		CustomFieldData data = new CustomFieldData();
		data.setId("sample");
		data.setOwnerId("client");
		data.setCustomFieldId("test");
		data.setBooleanData(false);
		
		ArrayList<CustomFieldData> list = new ArrayList<CustomFieldData>();
		list.add(data);
		
	    when(fixture.customFieldDataRepository.findByOwnerId("client")).thenReturn(list);
	    when(fixture.customFieldDataCrud.save(any(CustomFieldData.class))).thenThrow(new RuntimeException());
		
		try {
			fixture.setCustomFieldData(client,customField, true);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
		catch(Exception e) {
			success=true;
		}
		
		assertFalse(errored);
		assertTrue(success);
	}
	
	@Test
	public void setCustomFieldDataUpdatesWhenFieldFound2() throws ProccessingException {
		boolean errored =false;
		Client client = new Client();
		client.setId("client");
		
		CustomField customField = new CustomField();
		customField.setId("test");
		
		CustomFieldData data = new CustomFieldData();
		data.setId("sample");
		data.setOwnerId("client");
		data.setCustomFieldId("test");
		data.setBooleanData(false);
		
		ArrayList<CustomFieldData> list = new ArrayList<CustomFieldData>();
		list.add(data);
		
		CustomFieldData output = null;
		
	    when(fixture.customFieldDataRepository.findByOwnerId("client")).thenReturn(list);
		
		try {
			output = fixture.setCustomFieldData(client,customField, true);
		} catch (ProccessingException e) {
			errored=true;
			e.printStackTrace();
		} catch (CorruptDataException e) {
			errored=true;
			e.printStackTrace();
		}
	
		assertFalse(errored);
		assertTrue(output.getBooleanData());
	}
}
