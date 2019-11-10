package accessiblesolutions.accessiblescheduling.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;

import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.util.Util;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shift implements Comparable{
    @Id
    @Column(length=40)
    @GeneratedValue(generator="randomId")
    @GenericGenerator(name="randomId", strategy="org.cloudfoundry.samples.music.domain.RandomIdGenerator")
    private String id;
    
    private boolean assigned;
    private String assignmentReason;
    private String creationReason;
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
    
    private String date;
    private String time;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private int startWeek;
    private int startMonth;
    private int startYear;
    
    private boolean weekend;
    
	private String lastUpdated;
    
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
  
    public void setDisplayDate() throws CorruptDataException{
		if(getOvernight()){
			setDate(getStartDate()+" to " + getEndDate());
		}
		else{
			setDate(getStartDate());
		}
		
		setTime(getStartTime()+"-"+getEndTime());
    }

    public boolean isValid(){
    	boolean valid=true;

    	try{
    		getDuration();
    	}
    	catch(CorruptDataException e){
    		valid=false;
    	}
    	
    	if(!event&& null==clientId){
    		valid=false;
    	}

    	return valid;
    }
    
    public boolean getAssigned() {
        return assigned;
    }
    
    public String getAssignmentReason() {
		return assignmentReason!=null?assignmentReason:"";
	}

    public String getClientId() {
        return clientId;
    }
    
    public String getClientName() {
        return clientName;
    }
    
    public float getDuration() throws CorruptDataException{
    	if(null==startTime || null==endTime){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	float duration = 0;

		int startHour = (int) Integer.parseInt(startTime.split(":")[0]);
		int startMin = (int) Integer.parseInt(startTime.split(":")[1]);
		
		int endHour = (int) Integer.parseInt(endTime.split(":")[0]);
		int endMin = (int) Integer.parseInt(endTime.split(":")[1]);
		
		//System.out.println("startHour"+startHour);
		//System.out.println("startMin"+startMin);
				//System.out.println("endHour"+endHour);
						//System.out.println("endMin"+endMin);
    	if(getOvernight()){
    		duration+=24;
    	}
//    	LocalDateTime startTime = getStartsLocalDateTime();
//    	LocalDateTime endTime = getEndsLocalDateTime();
//    	
//    	//Weeks ending sunday causes a wrapping issue calculating time on monday that makes it
//    	//appear as though a sunday to monday shift ends before it starts
//    	if(startTime.getDayOfWeek().getValue()==7 && endTime.getDayOfWeek().getValue()==1) {
//    		startTime=startTime.minusDays(2);
//    		endTime = startTime.plusDays(1);
//    		System.out.println("start tweaked: "+startTime + " end tweaked: " +endTime);
//    	}
    	
    	
    	if(getEndsLocalDateTime().isBefore(getStartsLocalDateTime())){
    		System.out.println("ERROR: shift ends before starting " +toString() + " duration:"+duration);
    		throw new CorruptDataException(Shift.class,this);
    	}
    	//System.out.println("Getting duration");
    	duration+=(endHour-startHour) + ((endMin-startMin)/60.0);
    	//System.out.println("Got duration:"+duration);
    	if(duration>24||duration<=0){
    		System.out.println("ERROR: shift is inappropriate duration " +toString() + " duration:"+duration);
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
    	return duration;
    }
    public String getEndDate() {
        return endDate;
    }
    public LocalDate getEndsLocalDate() throws CorruptDataException{
    	if(null==endDate){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
    	String[] parsedDate = endDate.split("-");
    	int year = 0;
    	int month = 0;
    	int day = 0;
    	
    	if(parsedDate.length!=3){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
    	if(parsedDate[0].length()!=4 || !parsedDate[0].matches("^[0-9]{4}$")){
    		throw new CorruptDataException(Shift.class,this);
    	}else{
    		year = Integer.parseInt(parsedDate[0]);
    	}
    	
    	if(parsedDate[1].length()!=2 || !parsedDate[1].matches("^[0-9]{2}$") || Integer.parseInt(parsedDate[1])>12){
    		throw new CorruptDataException(Shift.class,this);
    	}else{
    		month = Integer.parseInt(parsedDate[1]);
    	}
    	
    	if(parsedDate[2].length()!=2 || !parsedDate[2].matches("^[0-9]{2}$")){
    		throw new CorruptDataException(Shift.class,this);
    	}else{
    		day = Integer.parseInt(parsedDate[2]);
    	}
    	
    	return LocalDate.of(year,month,day);
    }
    public LocalDateTime getEndsLocalDateTime() throws CorruptDataException{
    	if(null==endTime){
    		throw new CorruptDataException(Shift.class,this);
    	}

    	String[] parsedTime = endTime.split(":");
    	int hour = -1;
    	int minute = -1;
    	
    	if(parsedTime.length!=2){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
    	if(parsedTime[0].length()!=2 || !parsedTime[0].matches("^[0-9]{2}$") || Integer.parseInt(parsedTime[0])>23){
    		throw new CorruptDataException(Shift.class,this);
    	}else{
    		hour = Integer.parseInt(parsedTime[0]);
    	}
    	
    	if(parsedTime[1].length()!=2 || !parsedTime[1].matches("^[0-9]{2}$") || Integer.parseInt(parsedTime[0])>59){
    		throw new CorruptDataException(Shift.class,this);
    	}else{
    		minute = Integer.parseInt(parsedTime[1]);
    	}
    	
    	return getEndsLocalDate().atTime(hour,minute);
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public boolean getEvent() {
        return event;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public String getId() {
        return id;
    }

    public boolean getOvernight() throws CorruptDataException{
    	if(null==startDate || null == endDate){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	return !startDate.equalsIgnoreCase(endDate);
    }
    
    public boolean getRecurring() {
        return recurring;
    }

    public String getRequestedStaffId() {
        return requestedStaffId;
    }
    
    public String getRequestedStaffName() {
        return requestedStaffName;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getStartDate() {
        return startDate;
    }
    
    public int getStartDay(){
    	return (int) Integer.parseInt(startDate.split("-")[2]);
    }

    public int getStartMonth() {
        return (int) Integer.parseInt(startDate.split("-")[1]);
    }
    
    public LocalDate getStartsLocalDate() throws CorruptDataException{
    	if(null==startDate){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
    	String[] parsedDate = startDate.split("-");
    	int year = 0;
    	int month = 0;
    	int day = 0;
    	
    	if(parsedDate.length!=3){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
    	if(parsedDate[0].length()!=4 || !parsedDate[0].matches("^[0-9]{4}$")){
    		throw new CorruptDataException(Shift.class,this);
    	}else{
    		year = Integer.parseInt(parsedDate[0]);
    	}
    	
    	if(parsedDate[1].length()!=2 || !parsedDate[1].matches("^[0-9]{2}$") || Integer.parseInt(parsedDate[1])>12){
    		throw new CorruptDataException(Shift.class,this);
    	}else{
    		month = Integer.parseInt(parsedDate[1]);
    	}
    	
    	if(parsedDate[2].length()!=2 || !parsedDate[2].matches("^[0-9]{2}$")){
    		throw new CorruptDataException(Shift.class,this);
    	}else{
    		day = Integer.parseInt(parsedDate[2]);
    	}
    	
    	return LocalDate.of(year,month,day);
    }

    public LocalDateTime getStartsLocalDateTime() throws CorruptDataException{
    	if(null==startTime){
    		throw new CorruptDataException(Shift.class,this);
    	}

    	String[] parsedTime = startTime.split(":");
    	int hour = -1;
    	int minute = -1;
    	
    	if(parsedTime.length!=2){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
    	if(parsedTime[0].length()!=2 || !parsedTime[0].matches("^[0-9]{2}$") || Integer.parseInt(parsedTime[0])>23){
    		throw new CorruptDataException(Shift.class,this);
    	}else{
    		hour = Integer.parseInt(parsedTime[0]);
    	}
    	
    	if(parsedTime[1].length()!=2 || !parsedTime[1].matches("^[0-9]{2}$") || Integer.parseInt(parsedTime[0])>59){
    		throw new CorruptDataException(Shift.class,this);
    	}else{
    		minute = Integer.parseInt(parsedTime[1]);
    	}
    	
    	return getStartsLocalDate().atTime(hour,minute);
    }
    
    public String getStartTime() {
        return startTime;
    }

    public int getStartWeek() throws CorruptDataException{
    	if(null==startDate){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
    	String[] parsedDate = startDate.split("-");
    	int year = 0;
    	int month = 0;
    	int day = 0;
    	
    	if(parsedDate.length!=3){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
    	if(parsedDate[0].length()!=4 || !parsedDate[0].matches("^[0-9]{4}$")){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
    	if(parsedDate[1].length()!=2 || !parsedDate[1].matches("^[0-9]{2}$") || Integer.parseInt(parsedDate[1])>12){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
    	if(parsedDate[2].length()!=2 || !parsedDate[2].matches("^[0-9]{2}$")){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
    	int week = -1;
    	System.out.println("getting start week for "+toString());
    	try{
    		week = Util.getWeekOfDate(startDate);
    	}
    	catch(ProccessingException e){
    		throw new CorruptDataException(e);
    	}

    	return week;
    }

    public int getStartYear() {
        return (int) Integer.parseInt(startDate.split("-")[0]);
    }

    public boolean isWeekend() throws CorruptDataException{
    	if(null==startDate || null == endDate){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	
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
    
    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public void setAssignmentReason(String string) {
		this.assignmentReason=string;
	}
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    public void setEvent(boolean event) {
        this.event = event;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public void setRequestedStaffId(String requestedStaffId) {
        this.requestedStaffId = requestedStaffId;
    }
    
    public void setRequestedStaffName(String requestedStaffName) {
        this.requestedStaffName = requestedStaffName;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
    
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public void setStartWeek(int startWeek){
    	this.startWeek=startWeek;
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

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getCreationReason() {
		return creationReason;
	}

	public void setCreationReason(String creationReason) {
		this.creationReason = creationReason;
	}

	@Override
	public int compareTo(Object arg0) {
		int result = 0;
		Shift shift = (Shift) arg0;
		try {
			if(getStartsLocalDateTime().isAfter(shift.getStartsLocalDateTime())){
				result=1;
			}
			else if(shift.getStartsLocalDateTime().isAfter(getStartsLocalDateTime())) {
				result=-1;
			}
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	@JsonIgnore
	public void setLastUpdatedToNow() {
    	setLastUpdated(LocalDateTime.now().toString());
    }
	
	@JsonIgnore
	public LocalDateTime getLastUpdatedTime() {
		return Util.getLocalDateTimeFromString(getLastUpdated());
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
