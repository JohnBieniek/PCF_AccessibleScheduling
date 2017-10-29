package org.cloudfoundry.samples.music.domain;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.cloudfoundry.samples.music.domain.Employee;
@Entity
public class ShiftRequest {
  	@Id
    @Column(length=40)
    @GeneratedValue(generator="randomId")
    @GenericGenerator(name="randomId", strategy="org.cloudfoundry.samples.music.domain.RandomIdGenerator")
    private String id;

    private String clientName;
    private String staffName;
    private String clientId;
    private boolean requestEmployee;
    private String staffId;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    

    public ShiftRequest() {
    }

    public ShiftRequest(String client, String staff) {
        this.clientName = client;
        this.staffName = staff;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
    
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean getRequestEmployee() {
        return requestEmployee;
    }

    public void setRequestEmployee(boolean requestEmployee) {
        this.requestEmployee = requestEmployee;
    }
    
    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
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
    
    @Override
	public String toString() {
		return "ShiftRequest [id=" + id + ", clientName=" + clientName + ", staffName=" + staffName + ", clientId="
				+ clientId + ", requestEmployee=" + requestEmployee + ", staffId=" + staffId + ", startDate="
				+ startDate + ", startTime=" + startTime + ", endDate=" + endDate + ", endTime=" + endTime + "]";
	}
}
