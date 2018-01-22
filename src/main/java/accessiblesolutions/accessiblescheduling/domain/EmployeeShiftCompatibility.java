package accessiblesolutions.accessiblescheduling.domain;

import org.springframework.stereotype.Component;

@Component
public class EmployeeShiftCompatibility {
	private Employee employee;
	private Shift shift;
	public Client client;//Why public?
	
	public EmployeeShiftCompatibility(){}
	
	public EmployeeShiftCompatibility(Employee employee, Shift shift){
		this.employee = employee;
		this.shift= shift;
	}
	
	public EmployeeShiftCompatibility(Employee employee, Shift shift,Client client){
		this.employee = employee;
		this.shift= shift;
		this.client=client;
	}
	
	
	public Shift getShift(){
		return shift;
	}

	public Employee getEmployee(){
		return employee;
	}
	
	@Override
	public String toString(){
		String out = "";
		
//		if(null!=shift){
//			out+=shift.toString();
//		}
//		if(null!=client){
//			out+=client.toString();
//		}
		if(null!=employee){
			out+=employee.toString();
		}
		
		return out;
	}
}
