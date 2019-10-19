package accessiblesolutions.accessiblescheduling.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;

import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.Availability;
import accessiblesolutions.accessiblescheduling.util.Util;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee implements Comparable<Employee>{
	@Id
	@Column(length = 40)
	@GeneratedValue(generator = "randomId")
	@GenericGenerator(name = "randomId", strategy = "org.cloudfoundry.samples.music.domain.RandomIdGenerator")
	private String id;
	
	private boolean admin;
	
	@JsonIgnore
    public String compatibile;// For proccessing only, needs refactored out
    
	private String[] days;
	private String[] startTimes;
	private String[] endTimes;

	private String name;
	private String first;

	private boolean fixedSchedule;

	private String gender;

	private String hireDate;
	
	private boolean inactive;
	
	private String initial;

	private String lastUpdated;
	private boolean manager;
	private int maxHours;
	private boolean medPassCertified;
	private int minHours;
	private boolean noCats;
	private boolean offAlternateWeekends;
	private String[] requestedOff;
	private boolean requestsExtraShifts;
	
	private String role;

	private boolean signing;
	private boolean smoker;

	private String userId;

	public Employee() {
		requestedOff = new String[0];
		startTimes = new String[0];
		endTimes = new String[0];
		days = new String[0];
		gender = Gender.FEMALE;
		minHours = 1;
		maxHours = 40;
		inactive = false;
		fixedSchedule = false;
	}
	public Employee(String first, String initial) {
		requestedOff = new String[0];
		startTimes = new String[0];
		endTimes = new String[0];
		days = new String[0];
		gender = Gender.FEMALE;
		minHours = 1;
		maxHours = 40;
		inactive = false;
		fixedSchedule = false;
		this.first = first;
		this.initial = initial;
	}
	public String getName() {
		String employeeName = "";
		if(name!=null && name!="") {
			employeeName=name;
		}
		else {
			if(this.first!=null) {
				employeeName =  this.first;
			}
			
			if(this.initial!=null) {
				employeeName+= " "+ this.initial;
			}
		}
		
		return employeeName;
	}
	
	@JsonIgnore
	public LocalDateTime getLastUpdatedTime() {
		return Util.getLocalDateTimeFromString(getLastUpdated());
	}
	
	@JsonIgnore
	public JSONObject nameInfo() throws JSONException {
		JSONObject json = new JSONObject();
		
		if(name!=null && name!="") {
			json.put("name", this.name);
		}
		else {
			json.put("name", this.first + " "+ this.initial);
		}
		json.put("id", id);
		
		return json;
	}
	
	public int cleanDuplicateVacationDays() {
		int daysRemoved = 0;
		//System.out.println("clean in:"+requestedOff.toString());
		
		if(null!=requestedOff) {	
			ArrayList<String> vacation = new ArrayList<String>();
			
			for (int i = 0; i< requestedOff.length ; i++) {
				boolean duplicate = false;
				String requestedDay = requestedOff[i];
	
				if(!vacation.isEmpty()) {
					for (String vacationDay : vacation) {
						//System.out.println("vac" +vacationDay + " req:"+requestedDay);
						if(null!=vacationDay && vacationDay.equalsIgnoreCase(requestedDay)) {
							//System.out.println("duplicate");
							duplicate=true;
						}
					}
				}
				
				if(null!= requestedDay && !duplicate && !requestedDay.trim().isEmpty()) {
					vacation.add(requestedDay);
				}
				else {
					daysRemoved++;
				}
	
			}
			
			requestedOff =  vacation.toArray(requestedOff);
		}
		
		//System.out.println("days removed:"+daysRemoved);
		//System.out.println("clean out:"+requestedOff.toString());
		return daysRemoved;
	}
	@Override
	public int compareTo(Employee employee) {
		return getFirst().toLowerCase().compareTo(employee.getFirst().toLowerCase());
	}

	public void fixInvalidAvailability() {
		for(int index=0; index< startTimes.length;index++) {
			LocalTime start = LocalTime.of(Integer.parseInt(startTimes[index].split(":")[0]), Integer.parseInt(startTimes[index].split(":")[1]));
			LocalTime end = LocalTime.of(Integer.parseInt(endTimes[index].split(":")[0]), Integer.parseInt(endTimes[index].split(":")[1]));
			if(start.isAfter(end)) {
				startTimes[index]=endTimes[index];
			}
			if(end.isBefore(start)) {
				endTimes[index]=startTimes[index];
			}
		}
	}
	
	public Availability setAvailability(Availability availability, int index) {
		days[index]=availability.day.toString();
		startTimes[index]=availability.startTime.toString();
		endTimes[index]=availability.endTime.toString();
		
		return availability;
	}
	
	public Availability getAvailability(int index) {
		Availability availability = new Availability();
		System.out.println("get availability for day:"+getDays()[index]);
		availability.day= DayOfWeek.of(Util.getDayInt(getDays()[index]));
		System.out.println("day:"+availability.day.toString());
		System.out.println("startTimes:"+getStartTimes().toString());
		availability.startTime=LocalTime.parse(getStartTimes()[index]);
		System.out.println("startTime:"+availability.startTime.toString());
		availability.endTime=LocalTime.parse(getEndTimes()[index]);
		System.out.println("availability:"+availability.toString());
		return availability;
	}

	public String getCompatibile() {
		return compatibile;
	}
	public String[] getDays() {
		return days;
	}
	public String[] getEndTimes() {
		return endTimes;
	}
	public String getFirst() {
		return first;
	}

	public boolean getFixedSchedule() {
		return fixedSchedule;
	}

	public String getGender() {
		return gender;
	}
	public String getHireDate() {
		return hireDate;
	}
	public String getId() {
		return id;
	}

	public boolean getInactive() {
		return inactive;
	}
	
	public String getInitial() {
		return initial;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public int getMaxHours() {
		return maxHours;
	}

	public int getMaxHoursAvailable() {
		return maxHours;
	}

	public boolean getMedPassCertified() {
		return medPassCertified;
	}

	public int getMinHours() {
		return minHours;
	}

	public boolean getNoCats() {
		return noCats;
	}

	public boolean getOffAlternateWeekends() {
		return offAlternateWeekends;
	}

	public String[] getRequestedOff() {
		return requestedOff;
	}

	public boolean getRequestsExtraShifts() {
		return requestsExtraShifts;
	}

	public String getRole() {
		return role;
	}

	public boolean getSigning() {
		return signing;
	}

	public boolean getSmoker() {
		return smoker;
	}

	public String[] getStartTimes() {
		return startTimes;
	}

	public String getUserId() {
		return userId;
	}

	public boolean isAdmin() {
		return admin;
	}

	public boolean isFemale() {
		return gender.equals(Gender.FEMALE);
	}


	public boolean isMale() {
		return gender.equals(Gender.MALE);
	}

	public boolean isManager() {
		return manager;
	}

	/**Returns if the employee has requested the day/days of the shift off regarless of their normal schedule
	 * 
	 * @param shift a valid shift
	 * @return boolean if the employee has requested the day/days of the shift off
	 * @throws ProccessingException null shift
	 * @throws CorruptDataException invalid shift
	 * @Tested
	 */
	public boolean requestedOff(Shift shift) throws ProccessingException, CorruptDataException {
		boolean off = false;
		
		if(null==shift) {
			throw new ProccessingException("Null shift provided to requestedOff");
		}
		else if(!shift.isValid()) {
			throw new CorruptDataException("Invalid shift provided to requestedOff");
		}
		
		for (String dayOff : requestedOff) {
			if (shift.getStartDate().equals(dayOff) || shift.getEndDate().equals(dayOff)) {
				off = true;
			}
		}

		return off;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public void setCompatibile(String compatibile) {
		this.compatibile = compatibile;
	}

	public void setDays(String[] days) {
		this.days = days;
	}

	public void setEndTimes(String[] endTimes) {
		this.endTimes = endTimes;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public void setFixedSchedule(boolean fixedSchedule) {
		this.fixedSchedule = fixedSchedule;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public void setHireDate(String hireDate) {
		this.hireDate = hireDate;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	public void setInitial(String initial) {
		this.initial = initial;
	}

	public void setLastUpdatedToNow() {
    	setLastUpdated(LocalDateTime.now().toString());
    }

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public void setManager(boolean manager) {
		this.manager = manager;
	}

	public void setMaxHours(int maxHours) {
		this.maxHours = maxHours;
	}

	public void setMedPassCertified(boolean medPassCertified) {
		this.medPassCertified = medPassCertified;
	}

	public void setMinHours(int minHours) {
		this.minHours = minHours;
	}

	public void setNoCats(boolean noCats) {
		this.noCats = noCats;
	}

	public void setOffAlternateWeekends(boolean offAlternateWeekends) {
		this.offAlternateWeekends = offAlternateWeekends;
	}

	public void setRequestedOff(String[] requestedOff) {
		this.requestedOff = requestedOff;
	}

	public void setRequestsExtraShifts(boolean requestsExtraShifts) {
		this.requestsExtraShifts = requestsExtraShifts;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void setSigning(boolean signing) {
		this.signing = signing;
	}

	public void setSmoker(boolean smoker) {
		this.smoker = smoker;
	}

	public void setStartTimes(String[] startTimes) {
		this.startTimes = startTimes;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void sortCallOffs() throws ParseException {
		ArrayList<Date> callOffs = new ArrayList<Date>();
		
		for(String date :requestedOff) {
			callOffs.add(new SimpleDateFormat("yyyy-MM-dd").parse(date));
		}
		
		Collections.sort(callOffs);
		
		String pattern = "yyyy-MM-dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		for(int index = 0; index<callOffs.size();index++) {
			String date = simpleDateFormat.format(callOffs.get(index));
			requestedOff[callOffs.size()-1-index]=date;
		}
	}

	@Override
	public String toString() {
		return "Employee [id=" + id + ", userId=" + userId + ", manager=" + manager + ", admin=" + admin + ", role="
				+ role + ", first=" + first + ", initial=" + initial + ", hireDate=" + hireDate + ", gender=" + gender
				+ ", compatibile=" + compatibile + ", noCats=" + noCats + ", smoker=" + smoker + ", signing=" + signing
				+ ", medPassCertified=" + medPassCertified + ", inactive=" + inactive + ", offAlternateWeekends="
				+ offAlternateWeekends + ", requestsExtraShifts=" + requestsExtraShifts + ", fixedSchedule="
				+ fixedSchedule + ", minHours=" + minHours + ", maxHours=" + maxHours + ", requestedOff="
				+ Arrays.toString(requestedOff) + ", startTimes=" + Arrays.toString(startTimes) + ", endTimes="
				+ Arrays.toString(endTimes) + ", days=" + Arrays.toString(days) + "]";
	}

	public JSONObject userSafeEmployeeData() throws JSONException {
		JSONObject json = new JSONObject();
		
		json.append("id", getId());
		json.append("first", getFirst());
		json.append("initial", getInitial());
		json.append("requestedOff", getRequestedOff());
		json.append("startTimes", getStartTimes());
		json.append("endTimes", getEndTimes());
		json.append("days", getDays());
		
		return json;
	}
}
