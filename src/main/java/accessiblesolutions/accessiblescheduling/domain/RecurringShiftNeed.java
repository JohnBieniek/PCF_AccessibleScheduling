package accessiblesolutions.accessiblescheduling.domain;

import org.hibernate.annotations.GenericGenerator;


import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import accessiblesolutions.accessiblescheduling.util.Util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

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
    
    public boolean isValid(){
    	boolean valid =true;
    	String reason = null;

    	if(null==startTime || null==endTime ||null==startDay || null==endDay){
    		valid=false;
    	}
    	else{
	    	DayOfWeek start = DayOfWeek.of(Util.getDayInt(startDay));
	    	DayOfWeek end = DayOfWeek.of(Util.getDayInt(endDay)); 
	    	
	    	//Weeks ending sunday causes a wrapping issue calculating time on monday that makes it
	    	//appear as though a sunday to monday shift ends before it starts
	    	if(start.getValue()==7 && end.getValue()==1) {
	    		start=start.minus(2);
	    		end = start.plus(1);
	    		System.out.println("start tweaked: "+start + " end tweaked: " +end);
	    	}
	    	
	    	if(end.compareTo(start)<0||end.compareTo(start)>1){
	    		valid=false;
	    		reason="Ends before starting";
	    	}
	    	int startHour = (int) Integer.parseInt(startTime.split(":")[0]);
			int startMin = (int) Integer.parseInt(startTime.split(":")[1]);
			
			int endHour = (int) Integer.parseInt(endTime.split(":")[0]);
			int endMin = (int) Integer.parseInt(endTime.split(":")[1]);
	    	if(valid){
	    		System.out.println(start.compareTo(end));
	    		if(start.compareTo(end)==-1){
	    			if(endMin>startMin){
	    				if(endHour>=startHour){
	    					valid=false;
	    					reason = "greater than 24 hours";
	    				}
	    			}
	    			else{
	    				if(endHour>startHour){
	    					valid=false;
	    					reason="greater than 24 hours";
	    				}
	    			}
	    		}
	    		else if(start.compareTo(end)==0){
	    			if(endHour<startHour){
	    				valid=false;
	    				reason ="Negative Duration";
	    			}
	    			else if(endHour==startHour){
	    				if(endMin<=startMin){
	    					valid=false;//Negative Duration
	    					reason ="Negative Duration";
	    				}
	    			}
	    			
	    		}
	    	}
    	}
    	
    	if(null==clientName){
    		valid=false;
    		reason="No Client";
    	}
    	
    	if(fixedStaff&&null==staffId){
    		valid=false;
    		reason="No Staff when one was requested";
    	}
    	
    	System.out.println(reason);
    	
    	return valid;
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
