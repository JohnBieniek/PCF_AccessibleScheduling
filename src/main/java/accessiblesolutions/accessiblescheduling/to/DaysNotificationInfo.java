package accessiblesolutions.accessiblescheduling.to;

import java.time.LocalDate;

import accessiblesolutions.accessiblescheduling.domain.Shift;

public class DaysNotificationInfo {
	private LocalDate date;
	private  Shift[] shifts;
	
    public DaysNotificationInfo() {
    }
    
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public Shift[] getShifts() {
		return shifts;
	}
	public void setShifts(Shift[] shifts) {
		this.shifts = shifts;
	}
}
