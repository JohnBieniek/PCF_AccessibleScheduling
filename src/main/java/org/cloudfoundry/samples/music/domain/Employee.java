package org.cloudfoundry.samples.music.domain;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.DayOfWeek;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {
	private static final String MALE= "male";
	private static final String FEMALE = "female";
	
    @Id
    @Column(length=40)
    @GeneratedValue(generator="randomId")
    @GenericGenerator(name="randomId", strategy="org.cloudfoundry.samples.music.domain.RandomIdGenerator")
    private String id;
    public String compatibile;
    private boolean offAlternateWeekends;
    private boolean inactive;
    private String first;
    private String initial;
    private String hireDate;
    private String gender;
    private boolean signing;
    private boolean noCats;
    private boolean smoker;
    private boolean medPassCertified;
    private boolean requestsExtraShifts;
    private boolean fixedSchedule;
    private int minHours;
    private int maxHours;
    private String[] requestedOff;
    private boolean[] daysAvailable;
    private boolean[] sundaysAvailability;
    private boolean[] mondaysAvailability;
    private boolean[] tuesdaysAvailability;
    private boolean[] wednesdaysAvailability;
    private boolean[] thursdaysAvailability;
    private boolean[] fridaysAvailability;
    private boolean[] saturdaysAvailability;

    public Employee() {
    	requestedOff= new String[0];
    	gender=FEMALE;
    	minHours=1;
    	maxHours=40;
    	inactive=false;
    	fixedSchedule=false;
        daysAvailable= new boolean[7];
        sundaysAvailability = new boolean[24];
        mondaysAvailability = new boolean[24];
        tuesdaysAvailability = new boolean[24];
        wednesdaysAvailability = new boolean[24];
        thursdaysAvailability = new boolean[24];
        fridaysAvailability = new boolean[24];
        saturdaysAvailability = new boolean[24];
    }
    public Employee(String first, String initial) {
    	requestedOff= new String[0];
    	gender=FEMALE;
    	minHours=1;
    	maxHours=40;
    	inactive=false;
    	fixedSchedule=false;
        daysAvailable= new boolean[7];
        this.first = first;
        this.initial = initial;
        sundaysAvailability = new boolean[24];
        mondaysAvailability = new boolean[24];
        tuesdaysAvailability = new boolean[24];
        wednesdaysAvailability = new boolean[24];
        thursdaysAvailability = new boolean[24];
        fridaysAvailability = new boolean[24];
        saturdaysAvailability = new boolean[24];
    }

    public boolean[] getAvailabilityFor(int day){
		boolean[] availability = new boolean[7];
		
		switch (day){
			case 0: availability=sundaysAvailability;
            		break;
			case 1: availability=mondaysAvailability;
    				break;
			case 2: availability=tuesdaysAvailability;
    				break;
			case 3: availability=wednesdaysAvailability;
    				break;
			case 4: availability=thursdaysAvailability;
    				break;
			case 5: availability=fridaysAvailability;
    				break;
			case 6: availability=saturdaysAvailability;
    				break;
		}
    	return availability;
    }
    public boolean[] getdaysAvailable(){
    	return daysAvailable;
    }
    public String getFirst() {
        return first;
    }
    public boolean getFixedSchedule() {
        return fixedSchedule;
    }
    public boolean[] getFridaysAvailability(){
    	return fridaysAvailability;
    }
    
   
    public String getGender(){
    	return gender;
    }

    public String getHireDate() {
        return hireDate;
    }
    public int getHoursAvailable(){
    	int hoursAvailable=0;
    	
    	for(int i = 0; i < 7; i++){
    		hoursAvailable+=getHoursAvailable(i);
    	}
    	
    	return hoursAvailable;
    }
    public int getHoursAvailable(int day){
    	int hoursAvailable = 0;
    	
    	if(daysAvailable[day]){
    		boolean[] availability = new boolean[7];
    		
    		switch (day){
    			case 0: availability=sundaysAvailability;
                		break;
    			case 1: availability=mondaysAvailability;
        				break;
    			case 2: availability=tuesdaysAvailability;
        				break;
    			case 3: availability=wednesdaysAvailability;
        				break;
    			case 4: availability=thursdaysAvailability;
        				break;
    			case 5: availability=fridaysAvailability;
        				break;
    			case 6: availability=saturdaysAvailability;
        				break;
    		}
    		
    		for(int i = 0; i < 24;i++){
    			hoursAvailable+= availability[i]?1:0;
    		}
    	}
    	return hoursAvailable;
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

    public int getMaxHoursAvailable(){
    	return maxHours;
    }
    
    public boolean getMedPassCertified() {
        return medPassCertified;
    }

    public int getMinHours() {
        return minHours;
    }
    
    public boolean[] getMondaysAvailability(){
    	return mondaysAvailability;
    }

    public boolean getNoCats() {
        return noCats;
    }
    
    public boolean getOffAlternateWeekends() {
        return offAlternateWeekends;
    }

    public String[] getRequestedOff(){
    	return requestedOff;
    }
    public boolean getRequestsExtraShifts() {
        return requestsExtraShifts;
    }

    public boolean[] getSaturdaysAvailability(){
    	return saturdaysAvailability;
    }
    
    public boolean getSigning() {
        return signing;
    }

    public boolean getSmoker() {
        return smoker;
    }
    
    public boolean[] getSundaysAvailability(){
    	return sundaysAvailability;
    }
    
    public boolean[] getThursdaysAvailability(){
    	return thursdaysAvailability;
    }
    
    public boolean[] getTuesdaysAvailability(){
    	return tuesdaysAvailability;
    }

    public boolean[] getWednesdaysAvailability(){
    	return wednesdaysAvailability;
    }

    public boolean isFemale() {
        return gender.equals(FEMALE);
    }

    public boolean isMale() {
        return gender.equals(MALE);
    }

    public boolean requestedOff(Shift shift){
    	boolean off= false;

		for(String dayOff : requestedOff){
			if(shift.getStartDate().equals(dayOff) || shift.getEndDate().equals(dayOff)){
				off=true;
			}
		}

    	return off;
    }

    public void setdaysAvailable(boolean[] daysAvailable){
    	this.daysAvailable = daysAvailable;
    }
    
    public void setFirst(String first) {
        this.first = first;
    }

    public void setFixedSchedule(boolean fixedSchedule) {
        this.fixedSchedule = fixedSchedule;
    }
    
    public void setFridaysAvailability(boolean[] fridaysAvailability){
    	this.fridaysAvailability = fridaysAvailability;
    }

    public void setGender(String gender){
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
    
    public void setMondaysAvailability(boolean[] mondaysAvailability){
    	this.mondaysAvailability = mondaysAvailability;
    }
    
    public void setNoCats(boolean noCats) {
        this.noCats = noCats;
    }
    
    public void setOffAlternateWeekends(boolean offAlternateWeekends) {
        this.offAlternateWeekends = offAlternateWeekends;
    }
    
    public void setRequestedOff(String[] requestedOff){
    	this.requestedOff = requestedOff;
    }
    
    public void setRequestsExtraShifts(boolean requestsExtraShifts) {
        this.requestsExtraShifts = requestsExtraShifts;
    }
    
    public void setSaturdaysAvailability(boolean[] saturdaysAvailability){
    	this.saturdaysAvailability = saturdaysAvailability;
    }
    
    public void setSigning(boolean signing) {
        this.signing = signing;
    }
    
    public void setSmoker(boolean smoker) {
        this.smoker = smoker;
    }
    
    public void setSundaysAvailability(boolean[] sundaysAvailability){
    	this.sundaysAvailability = sundaysAvailability;
    }
    
    public void setThursdaysAvailability(boolean[] thursdaysAvailability){
    	this.thursdaysAvailability = thursdaysAvailability;
    }
    
    public void setTuesdaysAvailability(boolean[] tuesdaysAvailability){
    	this.tuesdaysAvailability = tuesdaysAvailability;
    }
    
    public void setWednesdaysAvailability(boolean[] wednesdaysAvailability){
    	this.wednesdaysAvailability = wednesdaysAvailability;
    }
    
    public String toString(){
    	return getFirst() +" "+ getInitial() + " id:"+getId();
    }
}
