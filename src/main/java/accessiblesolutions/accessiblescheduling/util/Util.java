package accessiblescheduling.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import accessiblescheduling.domain.Client;
import accessiblescheduling.domain.Employee;
import accessiblescheduling.domain.Event;
import accessiblescheduling.exception.ProccessingException;

public abstract class Util {
	//For use inside proccessingException throwing determination
	private static boolean dayIsValid(String day) {
		return !(day.length()!=2 || !day.matches("^[0-9]{2}$"));
	}
	
	/**TODO fix null pointer error vulnerability, test
	 * Takes in a String LocalDateTime with nanosecond precision.
	 * @param time
	 * @return LocalDateTime time
	 */
	
	public static LocalDateTime getLocalDateTimeFromString(String time) {
		LocalDateTime dateTime = null;
		
		if(time!=null) {
			String[] splitString = time.split("-");
	    	String year =splitString[0];
	    	if(splitString.length>2) {
	        	String month = splitString[1];
	        	splitString = splitString[2].split("T");
	        	String day = splitString[0];
	        	if(splitString.length>1) {
	            	splitString = splitString[1].split(":");
	            	if(splitString.length>2) {
		            	String hour = splitString[0];
		            	String minute = splitString[1];
		            	String second = splitString[2].substring(0, 2);
		            	String nanoSecond =splitString[2].substring(3);
		            	dateTime = LocalDateTime.of(Integer.parseInt(year),
		        				Integer.parseInt(month),
		        				Integer.parseInt(day),
		        				Integer.parseInt(hour),
		        				Integer.parseInt(minute),
		        				Integer.parseInt(second),
		        				Integer.parseInt(nanoSecond)*1000000);
	            	}
	        	}
	    	}
		}
		
		return dateTime;
	}
	
	/**Returns if the event is present in the list
	 * 
	 * @param events an ArrayList<Event> of valid events
	 * @param eventId the unique id of the event
	 * @return boolean if the event is present in the list
	 * @Tested
	 */
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
    
    //TODO test
	public static String getDateFromLocalDateTime(LocalDateTime time) throws ProccessingException{
		if(null==time){
			throw new ProccessingException("No time supplied to getDateFromLocalDateTime");
		}
		
		String date = "";
		date+=time.getYear()+"-";
		int month = time.getMonthValue();
		if(month<10){
			date+="0"+month+"-";
		}
		else{
			date+=month+"-";
		}
		int day = time.getDayOfMonth();
		if(day<10){
			date+="0"+day;
		}
		else{
			date+=day;
		}
		
		return date;
	}
	
	/**Returns a calendar formated in the way the UI uses to display a schedule for clients/staff
	 * 
	 * @param year
	 * @param month 1-12
	 * @return JSONArray UI Formated data for displaying dates
	 * @throws ProccessingException for invalid month
	 * @Tested
	 */
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
	
	//TODO Test
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
		
	//TODO Test
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
	
	//TODO Test
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
	
	
	/**Returns the ID after it casts the object provided to an Employee or Client
	 * 
	 * @param individual a Client or Employee
	 * @return String Id of the Client or Employee
	 * @throws ProccessingException Null or Invalid individual provided to getIdFromEmployeeOrClient
	 * @Tested
	 */
	public static String getIdFromEmployeeOrClient(Object individual) throws ProccessingException {
		String id;
		
		if(individual!=null) {
			if(individual.getClass()==Employee.class) {
	    		Employee employee = (Employee) individual;
	    		id = employee.getId();
	    	}
	    	else if(individual.getClass()==Client.class) {
	    		Client client = (Client) individual;
	    		id = client.getId();
	    	}
	    	else {
	    		throw new ProccessingException("Invalid individual provided to getIdFromEmployeeOrClient");
	    	}
		}
		else {
    		throw new ProccessingException("Null individual provided to getIdFromEmployeeOrClient");
    	}
		
		return id;
	}
	
	/**Returns the last day of the selected week for the given month and year
	 * 
	 * @param year
	 * @param month
	 * @param selectedWeek
	 * @return LocalDate The last day of the selected week for the given month and year
	 * @Tested
	 */
	public static LocalDate getLocalDateOfDayInWeek(int year,int month, int selectedWeek){
		LocalDate sampleDay = null;
		
		if(month>0 && month<13&& selectedWeek <8 && selectedWeek>-1 && year>2000) {
			LocalDate dateCursor = LocalDate.of(year,month,1);
			
			for(int day =0 ; day<32;day++) {
				System.out.println("gettinig last day of week for week"+selectedWeek);
				try {
					if(getWeekOfDate(dateCursor.toString())==selectedWeek && dateCursor.getMonthValue()==month) {
						sampleDay = dateCursor;
					}
				} catch (ProccessingException e) {
					e.printStackTrace();
				}
				
				dateCursor = dateCursor.plusDays(1);
			}
		}
		
		return sampleDay;
	}

	//TODO Test
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
	
	//TODO test
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
	
	//TODO test
	public static String getTimeFromLocalDateTime(LocalDateTime dateTime) throws ProccessingException{
		if(null==dateTime){
			throw new ProccessingException("No time supplied to getDateFromLocalDateTime");
		}
		String time = "";
		time+=dateTime.getHour()+":";
		time+=dateTime.getMinute();
		
		return time;
	}
	
	/**Returns 0 indexed week of the month after the specified date provided
	 * 
	 * @param date
	 * @return int 0 indexed week of the month
	 * @throws ProccessingException null date
	 * @Tested
	 */
	public static int getWeekAfterDate(String date) throws ProccessingException{
		LocalDate dayCursor = getLocalDateOfString(date);
		dayCursor = dayCursor.plusWeeks(1);
		System.out.println("gettingWeekAfterDAte"+date.toString());
		return getWeekOfDate(dayCursor.toString());
	}
	
	/**Returns 0 indexed week of the month before the specified date provided
	 * 
	 * @param date
	 * @return int 0 indexed week of the month
	 * @throws ProccessingException null date
	 * @Tested
	 */
	public static int getWeekBeforeDate (String date) throws ProccessingException{
		LocalDate dayCursor = getLocalDateOfString(date);
		dayCursor = dayCursor.minusWeeks(1);
		System.out.println("gettingWeekBeforeDAte"+date.toString());
		return getWeekOfDate(dayCursor.toString());
	}
	
	//TODO test
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

	/**Returns the 0 indexed week int for the provided date string of the form YYYY-MM.DD.
	 * 
	 * @param date yyyy-mm-dd
	 * @return
	 * @throws ProccessingException when the date is malformed
	 */
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
		System.out.println("getting week of date:"+date.toString());
		while(weekCursor < 6){
			int dayCursorInt = dayCursor.getDayOfMonth();
			
			if(dayCursorInt==day){
				System.out.println("week of date:"+date.toString()+ " is : "+weekCursor);
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
	
	/**Returns if the two shifts occur during the same time
	 * 
	 * @param start1 when shift 1 begins
	 * @param end1 when shift 1 ends
	 * @param start2 when shift 2 begins
	 * @param end2 when shift 2 ends
	 * @return boolean if the two shifts occur during the same time
	 * @throws ProccessingException null time
	 * @Tested
	 */
	public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) throws ProccessingException {
		if(null==start1){
			throw new ProccessingException(LocalDateTime.class,start1);
		}
		if(null==end1 || end1.isBefore(start1)){
			throw new ProccessingException(LocalDateTime.class,end1);
		}
		if(null==start2){
			throw new ProccessingException(LocalDateTime.class,start2);
		}
		if(null==end2 || end2.isBefore(start2)){
			throw new ProccessingException(LocalDateTime.class,end2);
		}
		
		boolean overlapping = false;
		
		overlapping = start1.isBefore(end2) && end1.isAfter(start2);
		
		return overlapping;
	}
	
	//For use inside proccessingException throwing determination
	private static boolean monthIsValid(String month) {
		return !(month.length()!=2 || !month.matches("^[0-9]{2}$") || Integer.parseInt(month)>12);
	}
	
	//For use inside proccessingException throwing determination
	private static boolean yearIsValid(String year){
		return !(year.length()!=4 || !year.matches("^[0-9]{4}$"));
	}
}
