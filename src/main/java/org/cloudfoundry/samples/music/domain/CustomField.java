package org.cloudfoundry.samples.music.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.hibernate.annotations.GenericGenerator;

import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomField {
    @Id
    @Column(length=40)
    @GeneratedValue(generator="randomId")
    @GenericGenerator(name="randomId", strategy="org.cloudfoundry.samples.music.domain.RandomIdGenerator")
    private String id;
    
    private String clientVariable;
    private String employeeVariable;
  
    private boolean clientRequirement;
    private boolean employeeRequirement;
    
    private boolean overrideable;
    
    public CustomField() {
    	clientVariable="";
    	employeeVariable="";
    	clientRequirement=false;
    	employeeRequirement=false;
    	overrideable=true;
    }
    
    public boolean getOverrideable(){
    	return overrideable;
    }
    
    public void setOverrideable(boolean overrideable){
    	this.overrideable= overrideable;
    }
    
    public String getClientVariable(){
    	return clientVariable;
    }
    
    public void setClientVariable(String clientVariable){
    	this.clientVariable = clientVariable;
    }
    
    public String getEmployeeVariable(){
    	return employeeVariable;
    }
    
    public void setEmployeeVariable(String employeeVariable){
    	this.employeeVariable = employeeVariable;
    }

    public boolean getClientRequirement(){
    	return clientRequirement;
    }
    
    public void setClientRequirement(boolean clientRequirement){
    	this.clientRequirement= clientRequirement;
    }
    
    public boolean getEmployeeRequirement(){
    	return employeeRequirement;
    }
    
    public void setEmployeeRequirement(boolean employeeRequirement){
    	this.employeeRequirement= employeeRequirement;
    }
	public String getId() {
		return id;
	}
}
