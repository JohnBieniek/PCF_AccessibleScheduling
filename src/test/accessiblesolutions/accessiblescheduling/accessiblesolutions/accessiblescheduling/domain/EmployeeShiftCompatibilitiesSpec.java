package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.junit.Test;

import accessiblescheduling.domain.Employee;
import accessiblescheduling.domain.Shift;
import accessiblescheduling.to.EmployeeShiftCompatibilities;
import accessiblescheduling.to.EmployeeShiftCompatibility;
public class EmployeeShiftCompatibilitiesSpec {
	@Test
	public void emptyConstructorInitializesNoVariables() {
		EmployeeShiftCompatibilities compatibilities = new EmployeeShiftCompatibilities();
		
		assertNotNull(compatibilities);
		assertNull(compatibilities.compatibilities);
	}
	
	@Test
	public void constructorInitializesVariables1() {
		Employee employee = new Employee();
		Shift shift = new Shift();
		
		EmployeeShiftCompatibility compatibility = new EmployeeShiftCompatibility(employee,shift);
		ArrayList<EmployeeShiftCompatibility> array = new ArrayList<EmployeeShiftCompatibility>();
		array.add(compatibility);
		
		EmployeeShiftCompatibilities compatibilities = new EmployeeShiftCompatibilities(array);
		
		assertNotNull(compatibilities.compatibilities);
	}
}
