package managers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import javax.annotation.Resource;

import org.cloudfoundry.samples.music.managers.CustomDataManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftCompatibilityManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftMapManager;
import org.cloudfoundry.samples.music.managers.ShiftAssignmentManager;
import org.cloudfoundry.samples.music.managers.ShiftGenerationManager;
import org.cloudfoundry.samples.music.managers.ShiftManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.repository.CrudRepository;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.Shift;

public class ShiftAssignmentManagerSpec {
	@Mock
	private Resource resource;

	@Mock
	private CrudRepository<Shift, String> shiftCrud;
    
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
	private ShiftAssignmentManager fixture;
	
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

	}
	
	@Test
	public void getOnPrestaffedShiftsReturnsEmptyListForNull() {
		ArrayList<Shift> availableShifts  = fixture.getOnPrestaffedShifts(null);
		assertEquals(availableShifts.size(),0);
	}
	
	@Test
	public void getOnPrestaffedShiftsReturnsEmptyListForRemovedEmployee() {
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		Shift shift = new Shift();
		
		shift.setStartDate("2018-04-01");
		shift.setEndDate("2018-04-01");
		shift.setRequestedStaffId("nonExistantEmployee");
		shifts.add(shift);
		
		ArrayList<Shift> availableShifts  = fixture.getOnPrestaffedShifts(shifts);
		assertEquals(availableShifts.size(),0);
	}
	
	@Test
	public void getOnPrestaffedShiftsReturnsEmptyListForRequestedOffShift() {
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		Shift shift = new Shift();
		
		shift.setStartDate("2018-04-01");
		shift.setEndDate("2018-04-01");
		shift.setRequestedStaffId("offOnce");
		shifts.add(shift);
		
		ArrayList<Shift> availableShifts  = fixture.getOnPrestaffedShifts(shifts);
		assertEquals(availableShifts.size(),0);
	}
	
	@Test
	public void getOnPrestaffedShiftsReturnsAvailableListForShifts() {
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		Shift shift = new Shift();
		ArrayList<Shift> availableShifts =null;
		
		shift.setStartDate("2018-04-01");
		shift.setEndDate("2018-04-01");
		shift.setRequestedStaffId("offOnce");
		shifts.add(shift);
		
		shift = new Shift();
		shift.setStartDate("2018-04-01");
		shift.setEndDate("2018-04-01");
		shift.setRequestedStaffId("onAlways");
		shifts.add(shift);
		
		availableShifts  = fixture.getOnPrestaffedShifts(shifts);
		
		assertEquals(availableShifts.size(),1);
	}
}
