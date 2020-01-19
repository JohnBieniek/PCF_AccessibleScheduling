package accessiblescheduling.to;

import java.util.ArrayList;
import java.util.HashMap;

import accessiblescheduling.domain.Employee;
import accessiblescheduling.domain.Shift;

//Shift issues can only exist for shifts that haven't already occur. The past troubles me not
public class OverWeeklyDaysNotification {
    private String description="The max days an employee can work per week is 5. This week contains too many days worked."; 
    private String staff = "";
    private int week;
    
    private boolean[] daysWorked = new boolean[7];
    private ArrayList<Shift>[] dailyShifts = new ArrayList[7];
    
    public OverWeeklyDaysNotification(HashMap<Integer,ArrayList<Shift>> info,Employee employee,int selectedWeek) {
    	staff= employee.getFirst() + " " + employee.getInitial();
    	week=selectedWeek;
    	for(int day = 1; day <8;day++){
    		if(info.containsKey(day)){
    			daysWorked[day-1]=true;
    			dailyShifts[day-1] = info.get(day);
    		}
    	}
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
