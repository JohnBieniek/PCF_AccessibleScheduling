package accessiblesolutions.accessiblescheduling.to;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.Shift;

//Shift issues can only exist for shifts that haven't already occur. The past troubles me not
public class AlternateWeekendsOffNotification {
    private String description="This person requested alternate weekends off but works at least two consecutive weekends due to the following shifts"; 
    private String staff = "";
    
    private Shift[][] weekendsWorked;
    
    public AlternateWeekendsOffNotification(Employee employee, Shift[][] shifts) {
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

	public Shift[][] getWeekendsWorked() {
		return weekendsWorked;
	}

	public void setWeekendsWorked(Shift[][] weekendsWorked) {
		this.weekendsWorked = weekendsWorked;
	}
}
