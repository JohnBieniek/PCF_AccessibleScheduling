package accessiblesolutions.accessiblescheduling.domain;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shift {
    @Id
    @Column(length=40)
    @GeneratedValue(generator="randomId")
    @GenericGenerator(name="randomId", strategy="org.cloudfoundry.samples.music.domain.RandomIdGenerator")
    private String id;
    
    private boolean assigned;
    private String assignmentReason;
    private boolean recurring;

    private boolean event;
    private String eventId;
    private String eventName;
    
    private String clientId;
    private String clientName;
    
    private String staffId;
    private String staffName;

    private String requestedStaffId;
    private String requestedStaffName;
    
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private int startWeek;
    private int startMonth;
    private int startYear;
    
    private boolean weekend;
    
    public Shift() {
    	assigned=false;
    	recurring=false;
    	event = false;
    }

    public Shift(String client, String staff) {
        this.clientName = client;
        this.staffName = staff;
        
        assigned=false;
    	recurring=false;
    	event = false;
    }

    public float getDuration() throws CorruptDataException{
    	float duration = 0;

		int startHour = (int) Integer.parseInt(startTime.split(":")[0]);
		int startMin = (int) Integer.parseInt(startTime.split(":")[1]);
		
		int endHour = (int) Integer.parseInt(endTime.split(":")[0]);
		int endMin = (int) Integer.parseInt(endTime.split(":")[1]);
		
    	if(getOvernight()){
    		duration+=24;// (23-startHour) - (startMin/60) + endHour + (endMin/60);
    	}
    	//else{
    		duration+=(endHour-startHour) + ((endMin-startMin)/60);
    	//}
    	if(duration>24||duration<0){
    		System.out.println("ERROR: shift is inappropriate duration " +toString());
    		throw new CorruptDataException(Shift.class,this);
    	}
    	return duration;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setStartWeek(int startWeek){
    	this.startWeek=startWeek;
    }
    public int getStartWeek(){
    	return Util.getWeekOfDate(startDate);
    }
    public boolean getOvernight() throws CorruptDataException{
    	if(null==startDate || null == endDate){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	return !startDate.equalsIgnoreCase(endDate);
    }
    public int getStartDay(){
    	return (int) Integer.parseInt(startDate.split("-")[2]);
    }
    public boolean isWeekend(){
    	boolean weekend = false;
    	if(getStartsLocalDate().getDayOfWeek().getValue()==DayOfWeek.SATURDAY.getValue()){
    		weekend=true;
    	}
    	else if(getStartsLocalDate().getDayOfWeek().getValue()==DayOfWeek.SUNDAY.getValue()){
    		weekend=true;
    	}
    	else if(getEndsLocalDate().getDayOfWeek().getValue()==DayOfWeek.SUNDAY.getValue()){
    		weekend=true;
    	}
    	else if(getEndsLocalDate().getDayOfWeek().getValue()==DayOfWeek.SATURDAY.getValue()){
    		weekend=true;
    	}
    	return weekend;
    }
    public LocalDate getStartsLocalDate(){
    	return LocalDate.of(Integer.parseInt(startDate.split("-")[0]),Integer.parseInt(startDate.split("-")[1]),Integer.parseInt(startDate.split("-")[2]));
    }
    
    public LocalDateTime getStartsLocalDateTime(){
    	return getStartsLocalDate().atTime(Integer.parseInt(startTime.split(":")[0]), Integer.parseInt(startTime.split(":")[1]));
    }
    
    public LocalDate getEndsLocalDate(){
    	return LocalDate.of(Integer.parseInt(endDate.split("-")[0]),Integer.parseInt(endDate.split("-")[1]),Integer.parseInt(endDate.split("-")[2]));
    }
    
    public LocalDateTime getEndsLocalDateTime(){
    	return getEndsLocalDate().atTime(Integer.parseInt(endTime.split(":")[0]), Integer.parseInt(endTime.split(":")[1]));
    }
    public boolean getAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }
    
    public boolean getRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }
    
    public boolean getEvent() {
        return event;
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getRequestedStaffId() {
        return requestedStaffId;
    }

    public void setRequestedStaffId(String requestedStaffId) {
        this.requestedStaffId = requestedStaffId;
    }
    
    public String getRequestedStaffName() {
        return requestedStaffName;
    }

    public void setRequestedStaffName(String requestedStaffName) {
        this.requestedStaffName = requestedStaffName;
    }
    
    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
    
    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
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
    
    public int getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }
    
    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }
    
    @Override
    public String toString(){
    	String string = "Shift for ";
    	if(event){
    		string+= eventName+ " the event ";
    	}
    	else{
    		string+= clientName;
    	}
    	
    	string+= " starting "+startDate + " at - "+startTime;
    	string+= " and ending "+endDate + " at - " + endTime;
    	
    	if(requestedStaffName!=null){
    		string+= " with the requested staff member: "+requestedStaffName;
    	}
    	
    	if(staffName!=null){
    		string+= " with the assigned staff of "+ staffName;
    	}
    	
    	return string;
    }

	public void setAssignmentReason(String string) {
		this.assignmentReason=string;
	}
	public String getAssignmentReason() {
		return assignmentReason!=null?assignmentReason:"";
	}
}
