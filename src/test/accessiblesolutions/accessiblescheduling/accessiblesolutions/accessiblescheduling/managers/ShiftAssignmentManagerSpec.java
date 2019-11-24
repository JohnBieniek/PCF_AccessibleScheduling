package accessiblesolutions.accessiblescheduling.managers;

import static org.junit.Assert.assertEquals;
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
import accessiblescheduling.manager.EmployeeShiftMapManager;
import accessiblescheduling.manager.ShiftAssignmentManager;
import accessiblescheduling.manager.ShiftGenerationManager;
import accessiblescheduling.manager.ShiftManager;

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
		ArrayList<Shift> availableShifts = null;
		try {
			availableShifts = fixture.getOnPrestaffedShifts(null);
		} catch (ProccessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(availableShifts.size(),0);
	}
	
	@Test
	public void getOnPrestaffedShiftsReturnsEmptyListForRemovedEmployee() {
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		Shift shift = new Shift();
		
		shift.setStartDate("2018-04-01");
		shift.setEndDate("2018-04-01");
		shift.setRequestedStaffId("nonExistantEmployee");
		shift.setStartTime("10:00");
		shift.setEndTime("14:00");
		shift.setClientId("generic");
		shifts.add(shift);
		
		ArrayList<Shift> availableShifts = null;
		try {
			availableShifts = fixture.getOnPrestaffedShifts(shifts);
		} catch (ProccessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(availableShifts.size(),0);
	}
	
	@Test
	public void getOnPrestaffedShiftsReturnsEmptyListForRequestedOffShift() {
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		Shift shift = new Shift();
		
		shift.setStartDate("2018-04-01");
		shift.setEndDate("2018-04-01");
		shift.setRequestedStaffId("offOnce");
		shift.setStartTime("10:00");
		shift.setEndTime("14:00");
		shift.setClientId("generic");
		shifts.add(shift);
		
		ArrayList<Shift> availableShifts = null;
		try {
			availableShifts = fixture.getOnPrestaffedShifts(shifts);
		} catch (ProccessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		shift.setStartTime("10:00");
		shift.setEndTime("14:00");
		shift.setClientId("generic");
		shifts.add(shift);
		
		shift = new Shift();
		shift.setStartDate("2018-04-01");
		shift.setEndDate("2018-04-01");
		shift.setRequestedStaffId("onAlways");
		shift.setStartTime("10:00");
		shift.setEndTime("14:00");
		shift.setClientId("generic");
		shifts.add(shift);
		
		try {
			availableShifts  = fixture.getOnPrestaffedShifts(shifts);
		} catch (ProccessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(availableShifts.size(),1);
	}
}
