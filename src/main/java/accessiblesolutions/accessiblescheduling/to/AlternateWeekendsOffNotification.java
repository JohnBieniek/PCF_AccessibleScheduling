package accessiblesolutions.accessiblescheduling.to;

import java.util.ArrayList;
import java.util.HashMap;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.Shift;

//Shift issues can only exist for shifts that haven't already occur. The past troubles me not
public class AlternateWeekendsOffNotification {
    private String description="This person requested alternate weekends off but works at least two consecutive weekends due to the following shifts"; 
    private String staff = "";
    
    private ArrayList<AlternateWeekendOffInfo> weekendsWorked;
    
    public AlternateWeekendsOffNotification() {

    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStaff() {
		return staff;
	}

	public void setStaff(String staff) {
		this.staff = staff;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}

	public boolean[] getDaysWorked() {
		return daysWorked;
	}

	public void setDaysWorked(boolean[] daysWorked) {
		this.daysWorked = daysWorked;
	}

	public ArrayList<Shift>[] getDailyShifts() {
		return dailyShifts;
	}

	public void setDailyShifts(ArrayList<Shift>[] dailyShifts) {
		this.dailyShifts = dailyShifts;
	}
}
