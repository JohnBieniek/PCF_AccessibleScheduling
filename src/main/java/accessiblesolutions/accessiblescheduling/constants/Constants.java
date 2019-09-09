package accessiblesolutions.accessiblescheduling.constants;

public abstract class Constants {
	private static final String CLIENT_ID = "728962972693-5uudecgh1ir6if7imo2hb6cv9qa9nrk3728962972693-5uudecgh1ir6if7imo2hb6cv9qa9nrk3.apps.googleusercontent.com";

    //Shift Issues
	public static String shiftExceedsDailyMax = "This shift is staffed by an employee who already has two or more other shifts today. This brings them over the 2 shift daily maximum.";
	public static String shiftExceedsWeeklyMax = "This shift is staffed by an employee who already has five or more other shifts this week. This brings them over the 5 shift weekly maximum.";
	public static String workedLastWeekend = "This shift is staffed by an employee who worked last weekend. This shift violates the alternate weekends off requirement.";
	public static String worksNextWeekend = "This shift is staffed by an employee who works next weekend. This shift violates the alternate weekends off requirement.";
	
	public static String violatesCallOff = "This shift is staffed by an employee who is called off over its duration. This shift violates thier scheduled leave.";
	public static String violatesAvailableHours = "This shift is staffed by an employee who is not available over its duration. This shift violates the thier scheduled availability.";
	
	 //Shift Notifications
	public static String shiftsExceedDailyMax = "Together these shifts cause this employee to exceed the maximum daily of 2 shifts. It's recomended that one or more of these shifts be reassigned to another employee.";
	public static String shiftsExceedWeeklyMax = "Together these shifts cause this employee to exceed the weekly maximum of 2 shifts. It's recomended that one or more of these shifts be reassigned to another employee.";
	
	public static String reassignInvalidity = "This shift shouldn't be assigned to this employee. It's recomended that this be reassigned to another employee.";
}
