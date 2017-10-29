package org.cloudfoundry.samples.music.domain;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class EmployeeShiftCompatibilities {
	public ArrayList<EmployeeShiftCompatibility> compatibilities;
	
	public EmployeeShiftCompatibilities(){}
	
	public EmployeeShiftCompatibilities(ArrayList<EmployeeShiftCompatibility> compatibilities){
		this.compatibilities = compatibilities;
	}
}
