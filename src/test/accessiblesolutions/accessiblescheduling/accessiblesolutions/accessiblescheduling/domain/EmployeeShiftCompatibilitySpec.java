package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import accessiblescheduling.domain.Client;
import accessiblescheduling.domain.Employee;
import accessiblescheduling.domain.Shift;
import accessiblescheduling.to.EmployeeShiftCompatibility;
public class EmployeeShiftCompatibilitySpec {
	@Test
	public void emptyConstructorInitializesVariables() {
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility();
		
		assertNull(compatibility.client);//TODO why doesn't this have a getter?
		assertNull(compatibility.getEmployee());
		assertNull(compatibility.getShift());
	}
	
	@Test
	public void constructorInitializesVariables1() {
		Employee employee = new Employee();
		Shift shift = new Shift();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,shift);
		
		assertNull(compatibility.client);//TODO why doesn't this have a getter?
		assertNotNull(compatibility.getEmployee());
		assertNotNull(compatibility.getShift());
	}
		
	@Test
	public void constructorInitializesVariables2() {
		Employee employee = new Employee();
		Shift shift = new Shift();
		Client client = new Client();
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,shift,client);
		
		assertNotNull(compatibility.client);//TODO why doesn't this have a getter?
		assertNotNull(compatibility.getEmployee());
		assertNotNull(compatibility.getShift());
	}
}
