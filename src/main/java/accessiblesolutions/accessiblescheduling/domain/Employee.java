package accessiblesolutions.accessiblescheduling.domain;

import java.time.LocalTime;
import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.DayOfWeek;

import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.Availability;
import accessiblesolutions.accessiblescheduling.util.Util;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee implements Comparable{
	public String[] getStartTimes() {
		return startTimes;
	}

	public void setStartTimes(String[] startTimes) {
		this.startTimes = startTimes;
	}

	public String[] getEndTimes() {
		return endTimes;
	}

	public void setEndTimes(String[] endTimes) {
		this.endTimes = endTimes;
	}

	public String[] getDays() {
		return days;
	}

	public void setDays(String[] days) {
		this.days = days;
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

	@Id
	@Column(length = 40)
	@GeneratedValue(generator = "randomId")
	@GenericGenerator(name = "randomId", strategy = "org.cloudfoundry.samples.music.domain.RandomIdGenerator")
	private String id;
	private String first;
	private String initial;
	private String hireDate;
	private String gender;

	public String compatibile;// For proccessing only, needs refactored out

	private boolean noCats;
	private boolean smoker;

	private boolean signing;

	private boolean medPassCertified;

	private boolean inactive;
	private boolean offAlternateWeekends;
	private boolean requestsExtraShifts;
	private boolean fixedSchedule;

	private int minHours;
	private int maxHours;

	private String[] requestedOff;
	private String[] startTimes;
	private String[] endTimes;
	private String[] days;

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
	@Override
	public int compareTo(Object arg0) {
		Employee employee = (Employee)arg0;
		
		return getFirst().toLowerCase().compareTo(employee.getFirst().toLowerCase());
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

	public boolean getSigning() {
		return signing;
	}

	public boolean getSmoker() {
		return smoker;
	}


	public boolean isFemale() {
		return gender.equals(Gender.FEMALE);
	}

	public boolean isMale() {
		return gender.equals(Gender.MALE);
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

	public void setSigning(boolean signing) {
		this.signing = signing;
	}

	public void setSmoker(boolean smoker) {
		this.smoker = smoker;
	}

	public String toString() {
		return getFirst() + " " + getInitial() + " id:" + getId();
	}
}
