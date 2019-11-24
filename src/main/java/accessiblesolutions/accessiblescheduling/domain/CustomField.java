package accessiblescheduling.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    
    private String lastUpdated;
    
    
    public CustomField() {
    	clientVariable="";
    	employeeVariable="";
    	clientRequirement=false;
    	employeeRequirement=false;
    	overrideable=true;
    	lastUpdated=null;
    }
    
	public String getLastUpdated() {
		return lastUpdated;
	}

	@JsonIgnore
	public void setLastUpdatedToNow() {
    	setLastUpdated(LocalDateTime.now().toString());
    }

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
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
	public void setId(String id) {
		this.id= id;
	}

	@Override
	public String toString() {
		return "CustomField [id=" + id + ", clientVariable=" + clientVariable + ", employeeVariable=" + employeeVariable
				+ ", clientRequirement=" + clientRequirement + ", employeeRequirement=" + employeeRequirement
				+ ", overrideable=" + overrideable + ", lastUpdated=" + lastUpdated + "]";
	}

}
