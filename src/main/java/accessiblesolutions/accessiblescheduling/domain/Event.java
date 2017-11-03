package accessiblesolutions.accessiblescheduling.domain;

import org.hibernate.annotations.GenericGenerator;

import accessiblesolutions.accessiblescheduling.domain.Employee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Event {
    @Id
    @Column(length=40)
    @GeneratedValue(generator="randomId")
    @GenericGenerator(name="randomId", strategy="org.cloudfoundry.samples.music.domain.RandomIdGenerator")
    private String id;

    private String name;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private int requestedStaff;
    private String[] staffIds;
    
    public Event() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName(){
    	return name;
    }
    
    public void setName(String name){
    	this.name = name;
    }
    
    public String[] getStaffIds() {
        return staffIds;
    }

    public void setStaffIds(String[] staffIds) {
        this.staffIds = staffIds;
    }

    public int getRequestedStaff() {
        return requestedStaff;
    }

    public void setRequestedStaff(int requestedStaff) {
        this.requestedStaff = requestedStaff;
    }
    
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
   
}
