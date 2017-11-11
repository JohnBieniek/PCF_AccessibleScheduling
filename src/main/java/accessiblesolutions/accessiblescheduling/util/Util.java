package accessiblesolutions.accessiblescheduling.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import accessiblesolutions.accessiblescheduling.domain.Event;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;

public abstract class Util {
    public static boolean eventArrayListContainsEvent(ArrayList<Event> events, String eventId){
    	boolean containsEvent = false;
    	
    	if(null!=events && !events.isEmpty() && null!=eventId){
    		for(Event event: events){
        		if(event.getId().equals(eventId)){
        			containsEvent=true;
        		}
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
	public static JSONArray getDatesForMonth(int year,int month) throws ProccessingException {
		if(month<1||month>12){
			throw new ProccessingException(Util.class,month);
		}
		
		JSONArray dates= new JSONArray();
		JSONObject week = new JSONObject();
		
		for(int selectedWeek = 1; selectedWeek<7;selectedWeek++){
			if(getWeekInMonth(year,month,selectedWeek)){
				week = new JSONObject();
				try {
					week.append("period", getPeriodOfWeek(year,month,selectedWeek));
					week.append("days", getDaysForWeek(year,month,selectedWeek));
					
					dates.put(selectedWeek-1,week);//Index is for date, it needs shifted for storage and display to avoid null at dates[0]
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return dates;
	}
	
	  private static String getPeriodOfWeek(int year,int month, int week){
			String period = "";
			LocalDate startDate = LocalDate.of(year, month, 1);
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
		
	  
	private static JSONArray getDaysForWeek(int year, int month, int selectedWeek) {
		JSONArray daysOfWeek = new JSONArray();
		
		for(int selectedDay=0;selectedDay<7;selectedDay++){
			JSONObject day = new JSONObject();
			day = getDayOfWeekForMonth(year,month,selectedWeek,selectedDay);
			
			try {
				daysOfWeek.put(selectedDay, day);
			} catch (JSONException e) {
				e.printStackTrace();//TODO improve
			}
		}
		
		return daysOfWeek;
	}
	
	private static JSONObject getDayOfWeekForMonth(int year, int month, int selectedWeek, int selectedDay) {
		JSONObject day = new JSONObject();
		LocalDate startDate = LocalDate.of(year, month, 1);
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
	
	public static int getWeekAfterDate(String date) throws ProccessingException{
		LocalDate dayCursor = getLocalDateOfString(date);
		dayCursor = dayCursor.plusWeeks(1);
		
		return getWeekOfDate(dayCursor.toString());
	}
	
	private static LocalDate getLocalDateOfString(String date) throws ProccessingException {
		if(null==date){
    		throw new ProccessingException(String.class,date);
    	}
    	
    	String[] parsedDate = date.split("-");
    	int year = 0;
    	int month = 0;
    	int day = 0;
    	
    	if(parsedDate.length!=3){
    		throw new  ProccessingException(String.class,date);
    	}
    	
    	if(yearIsValid(parsedDate[0])){
    		year = Integer.parseInt(parsedDate[0]);
    	}else{
    		throw new  ProccessingException(String.class,date);
    	}
    	
    	if(monthIsValid(parsedDate[1])){
    		month = Integer.parseInt(parsedDate[1]);
    	}else{
    		throw new  ProccessingException(String.class,date);
    	}
    	
    	if(dayIsValid(parsedDate[2])){
    		day = Integer.parseInt(parsedDate[2]);
    	}else{
    		throw new  ProccessingException(String.class,date);
    	}
    	
    	return LocalDate.of(year, month, day);
	}


	public static int getWeekBeforeDate (String date) throws ProccessingException{
		LocalDate dayCursor = getLocalDateOfString(date);
		dayCursor = dayCursor.minusWeeks(1);
		
		return getWeekOfDate(dayCursor.toString());
	}
	
	private static boolean getWeekInMonth(int year,int month, int selectedWeek) {
		LocalDate startDate = LocalDate.of(year, month, 1);
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
	
	public static int getWeekOfDate(String date) throws ProccessingException{
		if(null==date){
    		throw new ProccessingException(String.class,date);
    	}
    	
    	String[] parsedDate = date.split("-");
    	int year = 0;
    	int month = 0;
    	int day = 0;
    	
    	if(parsedDate.length!=3){
    		throw new  ProccessingException(String.class,date);
    	}
    	
    	if(yearIsValid(parsedDate[0])){
    		year = Integer.parseInt(parsedDate[0]);
    	}else{
    		throw new  ProccessingException(String.class,date);
    	}
    	
    	if(monthIsValid(parsedDate[1])){
    		month = Integer.parseInt(parsedDate[1]);
    	}else{
    		throw new  ProccessingException(String.class,date);
    	}
    	
    	if(dayIsValid(parsedDate[2])){
    		day = Integer.parseInt(parsedDate[2]);
		}else{
    		throw new  ProccessingException(String.class,date);
    	}
    	
		int weekCursor = 0;
		
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
	
	//For use inside proccessingException throwing determination
	private static boolean dayIsValid(String day) {
		return !(day.length()!=2 || !day.matches("^[0-9]{2}$"));
	}
	
	//For use inside proccessingException throwing determination
	private static boolean monthIsValid(String month) {
		return !(month.length()!=2 || !month.matches("^[0-9]{2}$") || Integer.parseInt(month)>12);
	}

	//For use inside proccessingException throwing determination
	private static boolean yearIsValid(String year){
		return !(year.length()!=4 || !year.matches("^[0-9]{4}$"));
	}
	
	public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
		boolean overlapping = false;
		
		overlapping = start1.isBefore(end2) && end1.isAfter(start2);
		
		return overlapping;
	}
}
