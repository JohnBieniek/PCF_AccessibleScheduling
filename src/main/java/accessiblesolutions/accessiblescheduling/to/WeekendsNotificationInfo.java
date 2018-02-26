package accessiblesolutions.accessiblescheduling.to;

public class WeekendsNotificationInfo extends Weekend{
	private String displayInfo ="what weekend this is";
	private DaysNotificationInfo[] days;
	
    public WeekendsNotificationInfo() {
    }
	public DaysNotificationInfo[] getDays() {
		return days;
	}
	public void setDays(DaysNotificationInfo[] days) {
		this.days = days;
	}
	public String getDisplayInfo() {
		return displayInfo;
	}
	public void setDisplayInfo(String displayInfo) {
		this.displayInfo = displayInfo;
	}

}
