package accessiblesolutions.accessiblescheduling.to;

import java.util.ArrayList;

//Shift issues can only exist for shifts that haven't already occur. The past troubles me not
public class ScheduleNotification {
    private String description="Undetermined"; 
    private String staff = "";
    public ScheduleNotification() {
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
}
