package org.cloudfoundry.samples.music.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.cloudfoundry.samples.music.domain.Event;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Util {
    public static boolean eventArrayListContainsEvent(ArrayList<Event> events, String eventId){
    	boolean containsEvent = false;
    	
    	for(Event event: events){
    		if(event.getId().equals(eventId)){
    			containsEvent=true;
    		}
    	}
    	
    	return containsEvent;
    }
	public static int getDayInt(String day){
    	int dayVal=-1;

    	if(day.equalsIgnoreCase("Sunday")){
    		dayVal=7;
    	}
    	else if(day.equalsIgnoreCase("Monday")){
    		dayVal=1;
    	}
    	else if(day.equalsIgnoreCase("Tuesday")){
    		dayVal=2;
    	}
    	else if(day.equalsIgnoreCase("Wednesday")){
    		dayVal=3;
    	}
    	else if(day.equalsIgnoreCase("Thursday")){
    		dayVal=4;
    	}
    	else if(day.equalsIgnoreCase("Friday")){
    		dayVal=5;
    	}
    	else if(day.equalsIgnoreCase("Saturday")){
    		dayVal=6;
    	}
    	
    	return dayVal;
    }
	
	public static int getWeekBeforeDate(String date){
		int day = (int)Integer.parseInt(date.split("-")[2]);
		int month =(int)Integer.parseInt(date.split("-")[1]);
		int year = (int)Integer.parseInt(date.split("-")[0]);
		
		LocalDate dayCursor = LocalDate.of(year,month, day).minusWeeks(1);
		
		return getWeekOfDate(dayCursor.getYear()+"-"+dayCursor.getMonthValue()+"-"+dayCursor.getDayOfMonth());
	}
	
	public static int getWeekAfterDate(String date){
		int day = (int)Integer.parseInt(date.split("-")[2]);
		int month =(int)Integer.parseInt(date.split("-")[1]);
		int year = (int)Integer.parseInt(date.split("-")[0]);
		
		LocalDate dayCursor = LocalDate.of(year,month, day).plusWeeks(1);
		
		return getWeekOfDate(dayCursor.getYear()+"-"+dayCursor.getMonthValue()+"-"+dayCursor.getDayOfMonth());
	}
	
    public static int getWeekOfDate(String date){
		int weekCursor = 0;
		
		int day = (int)Integer.parseInt(date.split("-")[2]);
		
		int month =(int)Integer.parseInt(date.split("-")[1]);
		int year = (int)Integer.parseInt(date.split("-")[0]);
		
		LocalDate dayCursor = LocalDate.of(year,month, 1);
		
		while(weekCursor < 6){
			int dayCursorInt = dayCursor.getDayOfMonth();
			
			if(dayCursorInt==day){
				return weekCursor;
			}
			
			DayOfWeek dayOfWeekForCursor = dayCursor.getDayOfWeek();
			
			if(dayOfWeekForCursor.getValue()==6){//Saturday is what I'm calling the last day of the week, suck it
				weekCursor++;
			}
			
			dayCursor=dayCursor.plusDays(1);
		}
		
		return weekCursor;//Unreachable
	}
	
	public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
		boolean overlapping = false;
		
		overlapping = start1.isBefore(end2) && end1.isAfter(start2);
		
		return overlapping;
	}
	
	public static String getPeriodOfWeek(int month, int week){
		String period = "";
		LocalDate startDate = LocalDate.of(2017, month, 1);
		DayOfWeek monthStart = startDate.getDayOfWeek(); 
		LocalDate firstSaturday = null;
		LocalDate firstSunday = null;
		LocalDate saturday = null;
		LocalDate sunday = null;
		
		if(monthStart.getValue()==7){
			firstSaturday = startDate.plusDays(6);
		}
		else{
			firstSaturday=startDate.plusDays(6-monthStart.getValue());
		}
		
		firstSunday=firstSaturday.minusDays(6);
		
		if(week==1){
			saturday=firstSaturday;
			sunday=firstSunday;
		}
		else{
			saturday=firstSaturday.plusWeeks(week-1);
			sunday=firstSunday.plusWeeks(week-1);
		}
		
		period=sunday.getMonthValue()+"/"+sunday.getDayOfMonth()+ "-"+saturday.getMonthValue()+"/"+saturday.getDayOfMonth()+"/17";
		
		return period;
	}
	
	private static boolean getWeekInMonth(int month, int selectedWeek) {
		LocalDate startDate = LocalDate.of(2017, month, 1);
		DayOfWeek monthStart = startDate.getDayOfWeek(); 
		LocalDate firstSaturday = null;
		LocalDate firstSunday = null;
		LocalDate sunday = null;
		
		if(monthStart.getValue()==7){
			firstSaturday = startDate.plusDays(6);
		}
		else{
			firstSaturday=startDate.plusDays(6-monthStart.getValue());
		}
		
		firstSunday=firstSaturday.minusDays(6);
		
		if(selectedWeek==1){
			sunday=firstSunday;
		}
		else{
			sunday=firstSunday.plusWeeks(selectedWeek-1);
		}
		boolean in = sunday.getMonthValue()==month;
		return in;
	}
	private static JSONObject getDayOfWeekForMonth(int month, int selectedWeek, int selectedDay) {
		JSONObject day = new JSONObject();
		LocalDate startDate = LocalDate.of(2017, month, 1);
		DayOfWeek monthStart = startDate.getDayOfWeek(); 
		LocalDate firstSaturday = null;
		LocalDate firstSunday = null;
		LocalDate saturday = null;
		LocalDate sunday = null;
		
		if(monthStart.getValue()==7){
			firstSaturday = startDate.plusDays(6);
		}
		else{
			firstSaturday=startDate.plusDays(6-monthStart.getValue());
		}
		
		firstSunday=firstSaturday.minusDays(6);
		
		if(selectedWeek==1){
			saturday=firstSaturday;
			sunday=firstSunday;
		}
		else{
			saturday=firstSaturday.plusWeeks(selectedWeek-1);
			sunday=firstSunday.plusWeeks(selectedWeek-1);
		}
		try {
			if(selectedDay==0){
				day.append("date", sunday);
				day.append("day", "Sunday");
			}
			else{
				day.append("date",sunday.plusDays(selectedDay));
				
				if(selectedDay==1){
					day.append("day", "Monday");
				}
				if(selectedDay==2){
					day.append("day", "Tuesday");
				}
				if(selectedDay==3){
					day.append("day", "Wednesday");
				}
				if(selectedDay==4){
					day.append("day", "Thursday");
				}
				if(selectedDay==5){
					day.append("day", "Friday");
				}
				if(selectedDay==6){
					day.append("day", "Saturday");
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return day;
	}
	public static JSONArray getDatesForMonth(int month) {
		JSONArray dates= new JSONArray();
		JSONObject week = new JSONObject();
		JSONObject day = new JSONObject();
		
		String period;
		try {
			for(int selectedWeek = 1; selectedWeek<7;selectedWeek++){
				week = new JSONObject();
				period = getPeriodOfWeek(month,selectedWeek);
				if(null!=period && period!="")week.append("period", period);
				
				JSONArray daysOfWeek = new JSONArray();
				for(int selectedDay=0;selectedDay<7;selectedDay++){
					day = new JSONObject();
					day = getDayOfWeekForMonth(month,selectedWeek,selectedDay);
					daysOfWeek.put(selectedDay, day);
				}
				week.append("days", daysOfWeek);
				if(week.length()>0 && (selectedWeek==1||getWeekInMonth(month,selectedWeek)))dates.put(selectedWeek,week);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONArray newDates = new JSONArray();
		for(int i = 0; i<dates.length();i++){
			if(!dates.isNull(i)){
				try {
					newDates.put(dates.get(i));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return newDates;
	}
}
