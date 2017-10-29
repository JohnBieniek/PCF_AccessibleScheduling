package org.cloudfoundry.samples.music.domain;

import org.springframework.stereotype.Component;

@Component
public class EmployeeShiftCompatibility {
	private String employeeId;
	private String shiftId;
	
	private Employee employee;
	private Shift shift;
	public Client client;
	
	public EmployeeShiftCompatibility(){}
	
	public EmployeeShiftCompatibility(Employee employee, Shift shift){
		employeeId = employee.getId();
		shiftId=shift.getId();
		this.employee = employee;
		this.shift= shift;
	}
	
	public EmployeeShiftCompatibility(Employee employee, Shift shift,Client client){
		employeeId = employee.getId();
		shiftId=shift.getId();
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
}
