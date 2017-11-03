package accessiblesolutions.accessiblescheduling.domain;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import accessiblesolutions.accessiblescheduling.domain.Employee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
@Entity
@JsonIgnoreProperties(ignoreUnknown = true) 
public class RecurringShiftNeed {
    @Id
    @Column(length=40)
    @GeneratedValue(generator="randomId")
    @GenericGenerator(name="randomId", strategy="org.cloudfoundry.samples.music.domain.RandomIdGenerator")
    private String id;

    private String clientName;
    private boolean fixedStaff;
    private String staffName;
    private String clientId;
    private String staffId;
    private int day;
    private String startDay;
    private String startTime;
    private String endDay;
    private String endTime;
    
    public RecurringShiftNeed() {
    }

    public RecurringShiftNeed(String client, String staff) {
        this.clientName = client;
        this.staffName = staff;
    }
    
    public void setDay(int day){
    	this.day = day;
    }
    
    public int getDay(){
    	int day = -1;
    	if(startDay!=null){
    		day=Util.getDayInt(startDay);
    		if(day==7){
    			day=0;
    		}
    	}
    	
    	return day;
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
    
    public boolean getFixedStaff() {
        return fixedStaff;
    }

    public void setFixedStaff(boolean fixedStaff) {
        this.fixedStaff = fixedStaff;
    }
    
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
    
    public String getStartDay() {
        return startDay;
    }

    public void setStartDay(String startDay) {
        this.startDay = startDay;
    }
    
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndDay() {
        return endDay;
    }

    public void setEndDay(String endDay) {
        this.endDay = endDay;
    }
    
    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
