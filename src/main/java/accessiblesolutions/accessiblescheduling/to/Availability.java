package accessiblesolutions.accessiblescheduling.to;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import accessiblesolutions.accessiblescheduling.util.Util;

public class Availability {
	public LocalTime startTime;
	public LocalTime endTime;
	public DayOfWeek day;
	
	public LocalDate getLocalDateForWeek(int week,int month,int year) {
		LocalDate date = null;//Util.getLocalDateOfDayInWeek(year, month, selectedWeek);
		
		
		
		return date;
	}

	@Override
	public String toString() {
		return "Availability [startTime=" + startTime + ", endTime=" + endTime + ", day=" + day + "]";
	}
}
