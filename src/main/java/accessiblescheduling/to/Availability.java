package accessiblescheduling.to;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class Availability {
	public LocalTime startTime;
	public LocalTime endTime;
	public DayOfWeek day;
	
	@Override
	public String toString() {
		return "Availability [startTime=" + startTime + ", endTime=" + endTime + ", day=" + day + "]";
	}
}
