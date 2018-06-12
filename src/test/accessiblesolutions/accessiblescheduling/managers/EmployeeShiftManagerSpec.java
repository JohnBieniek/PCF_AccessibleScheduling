package managers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import javax.annotation.Resource;

import org.cloudfoundry.samples.music.managers.CustomDataManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftCompatibilityManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftMapManager;
import org.cloudfoundry.samples.music.managers.ShiftAssignmentManager;
import org.cloudfoundry.samples.music.managers.ShiftGenerationManager;
import org.cloudfoundry.samples.music.managers.ShiftManager;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.repository.CrudRepository;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.Shift;

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
	    
	    Shift assignedOneDayShift2 = new Shift();
	    assignedOneDayShift2.setStaffId("assignedOneDay");
	    assignedOneDayShift2.setStartDate("2018-04-02");
	    assignedOneDayShift2.setEndDate("2018-04-02");
	    
	    Shift assignedOneDayShift3 = new Shift();
	    assignedOneDayShift3.setStaffId("assignedOneDay");
	    assignedOneDayShift3.setStartDate("2018-04-02");
	    assignedOneDayShift3.setEndDate("2018-04-03");
	    
	    assignedOneDayShifts.add(assignedOneDayShift1);
	    assignedOneDayShifts.add(assignedOneDayShift2);
	    assignedOneDayShifts.add(assignedOneDayShift3);
	    when(shiftRepository.findByStartMonth(4)).thenReturn(assignedOneDayShifts);
	    when(employeeCrud.findOne("assignedOneDay")).thenReturn(assignedOneDay);
	    
	    Employee crossMonthEmployee = new Employee("Cross","Month");
	    ArrayList<Shift> crossMonthShifts = new ArrayList<Shift>();
	    Shift crossMonthShift1 = new Shift();
	    crossMonthShift1.setStaffId("crossMonth");
	    crossMonthShift1.setStartDate("2018-05-31");
	    crossMonthShift1.setEndDate("2018-06-01");
	    when(shiftRepository.findByStartMonth(5)).thenReturn(crossMonthShifts);
	    when(employeeCrud.findOne("crossMonth")).thenReturn(crossMonthEmployee);
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForMonthReturnsShiftsWhenAssigned() {
		ArrayList<Shift> assignedShifts = fixture.getAssignedShiftsForEmployeeForMonth("assignedOneDay", 4);
		assertEquals(assignedShifts.size(),3);
		
//		Shift assignedOneDayShift4 = new Shift();
//		assignedOneDayShift4.setStaffId("assignedOneDay");
//	    assignedOneDayShift4.setStartDate("2018-04-05");
//	    assignedOneDayShift4.setEndDate("2018-04-05");
//		ArrayList<Shift> assignedOneDayShifts = (ArrayList<Shift>) shiftRepository.findByStartMonth(4);
//	    
//	    assignedOneDayShifts.add(assignedOneDayShift4);
//	    when(shiftRepository.findByStartMonth(4)).thenReturn(assignedOneDayShifts);
	    
	    
//	    assignedShifts = fixture.getAssignedShiftsForEmployeeForMonth("offOnce", 4);
//		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForMonthReturnsEmptyWhenUnassigned() {
		ArrayList<Shift> assignedShifts = fixture.getAssignedShiftsForEmployeeForMonth("offOnce", 4);

		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForMonthReturnsEmptyWithNullId() {
		ArrayList<Shift> assignedShifts = fixture.getAssignedShiftsForEmployeeForMonth(null, 4);

		assertEquals(0,assignedShifts.size());
	}
	
	@Test
	public void getAssignedShiftsForEmployeeForMonthReturnsShiftsCrossingMonths() {
		ArrayList<Shift> assignedShifts = fixture.getAssignedShiftsForEmployeeForMonth("crossMonth", 6);

		assertEquals(1,assignedShifts.size());
	}
}
