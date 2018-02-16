package accessiblesolutions.accessiblescheduling.to;

import java.util.ArrayList;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.Shift;

//Shift issues can only exist for shifts that haven't already occur. The past troubles me not
public class AlternateWeekendsOffNotification {
    private String description="This person requested alternate weekends off but works at least two consecutive weekends due to the following shifts"; 
    private String staff = "";
    
    private ArrayList<ArrayList<Shift>> weekendsWorked;
    
    public AlternateWeekendsOffNotification() {
    	
    }
    
    public AlternateWeekendsOffNotification(Employee employee, ArrayList<ArrayList<Shift>> shifts) {
    	staff = employee.getFirst() + " " + employee.getInitial();
    	setWeekendsWorked(shifts);
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

	public ArrayList<ArrayList<Shift>> getWeekendsWorked() {
		return weekendsWorked;
	}

	public void setWeekendsWorked(ArrayList<ArrayList<Shift>> weekendsWorked) {
		this.weekendsWorked = weekendsWorked;
	}
}
