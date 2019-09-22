package accessiblesolutions.accessiblescheduling.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import accessiblesolutions.accessiblescheduling.util.Util;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientRequest {
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
    private boolean repeats;
    private String repeatsEvery;
    private boolean[] days;
    private String interval;
    private String monthInterval;
    private String yearInterval;
    private String[] exceptions;
    private LocalDateTime lastUpdated;
    
    public ClientRequest() {
    }

    
    public boolean isValid(){
    	boolean valid = true;
    	float duration = 0;
    	//System.out.println("requstEmployee:"+requestEmployee + " staffId:"+staffId);
    	if(requestEmployee && (null==staffId ||staffId.length()==0)){
    		valid=false;
    	}
    	//System.out.println("valid:"+valid);
    	
    	try{
    		duration = getDuration();
    	}
    	catch(CorruptDataException e){
    		valid=false;
    	}
    	//System.out.println("valid:"+valid + " duration:"+duration);
    	return valid;
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
    
    public boolean getOvernight() throws CorruptDataException{
    	if(null==startDate || null == endDate){
    		throw new CorruptDataException(Shift.class,this);
    	}
    	return !startDate.equalsIgnoreCase(endDate);
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
    
    public boolean isRepeats() {
		return repeats;
	}

	public void setRepeats(boolean repeats) {
		this.repeats = repeats;
	}

	public String getRepeatsEvery() {
		return repeatsEvery;
	}

	public void setRepeatsEvery(String repeatsEvery) {
		this.repeatsEvery = repeatsEvery;
	}



	public boolean[] getDays() {
		return days;
	}

	public void setDays(boolean[] days) {
		this.days = days;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public String getYearInterval() {
		return yearInterval;
	}

	public void setYearInterval(String yearInterval) {
		this.yearInterval = yearInterval;
	}

	public String getMonthInterval() {
		return monthInterval;
	}

	public void setMonthInterval(String monthInterval) {
		this.monthInterval = monthInterval;
	}


	public String[] getExceptions() {
		return exceptions;
	}


	public void setExceptions(String[] exceptions) {
		this.exceptions = exceptions;
	}


	@Override
	public String toString() {
		return "ClientRequest [id=" + id + ", clientName=" + clientName + ", staffName=" + staffName + ", clientId="
				+ clientId + ", requestEmployee=" + requestEmployee + ", staffId=" + staffId + ", startDate="
				+ startDate + ", startTime=" + startTime + ", endDate=" + endDate + ", endTime=" + endTime
				+ ", repeats=" + repeats + ", repeatsEvery=" + repeatsEvery + ", days=" + Arrays.toString(days)
				+ ", interval=" + interval + ", monthInterval=" + monthInterval + ", yearInterval=" + yearInterval
				+ ", exceptions=" + Arrays.toString(exceptions) + "]";
	}


	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}


	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
