package accessiblesolutions.accessiblescheduling.to;

import java.time.LocalDate;
import java.util.ArrayList;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;

//Shift issues can only exist for shifts that haven't already occur. The past troubles me not
public class AlternateWeekendsOffNotification {
    private String staff = "";
    
    private ArrayList<ArrayList<Shift>> weekendsWorked;
    
    private WeekendsNotificationInfo[] weekends;
    
    public AlternateWeekendsOffNotification() {
    	
    }
    
    public AlternateWeekendsOffNotification(Employee employee, ArrayList<ArrayList<Shift>> shifts) throws CorruptDataException {
    	staff = employee.getFirst() + " " + employee.getInitial();
    	setWeekendsWorked(shifts);
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

	public void setWeekendsWorked(ArrayList<ArrayList<Shift>> weekendsWorked) throws CorruptDataException {
		this.weekendsWorked = weekendsWorked;
		
		for(ArrayList<Shift> weekend:weekendsWorked){
			WeekendsNotificationInfo weekendInfo = new WeekendsNotificationInfo();
			LocalDate date =weekend.get(0).getStartsLocalDate();
			LocalDate saturday;
			LocalDate sunday;
			
			if(date.getDayOfWeek().getValue()<6){
				date.plusDays(1);
			}
			
			if(date.getDayOfWeek().getValue()==6){
				saturday=date;
				sunday=date.plusDays(1);
			}
			else{
				saturday=date.minusDays(1);
				sunday=date;
			}
			
			weekendInfo.setDisplayInfo(saturday.toString() +" and " +sunday.toString());
			
			ArrayList<DaysNotificationInfo> dayInfo = new ArrayList<DaysNotificationInfo>();
			DaysNotificationInfo saturdaysInfo = new DaysNotificationInfo();
			DaysNotificationInfo sundaysInfo = new DaysNotificationInfo();
			
			for(Shift shift :weekend){
				//TODO add shifts to each day
			}
			
			weekendInfo.setDays((DaysNotificationInfo[]) dayInfo.toArray());
		}
		
	}

	public WeekendsNotificationInfo[] getWeekends() {
		return weekends;
	}

	public void setWeekends(WeekendsNotificationInfo[] weekends) {
		this.weekends = weekends;
	}
}
