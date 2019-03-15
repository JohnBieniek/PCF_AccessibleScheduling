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
    
    private WeekendsNotificationInfo[] weekendNotificationInfo;
    
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
		//System.out.println("started setWeekendsWorked");
		ArrayList<WeekendsNotificationInfo> weekendNotificationArray = new ArrayList<WeekendsNotificationInfo>();
		
		for(ArrayList<Shift> weekend:weekendsWorked){
			WeekendsNotificationInfo weekendInfo = new WeekendsNotificationInfo();
			LocalDate date =weekend.get(0).getStartsLocalDate();
			LocalDate saturday;
			LocalDate sunday;
			
			if(date.getDayOfWeek().getValue()<6){
				date.plusDays(1);
			}
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
			
			ArrayList<Shift> saturdaysShifts = new ArrayList<Shift>();
			ArrayList<Shift> sundaysShifts = new ArrayList<Shift>();
			
			for(Shift shift :weekend){
				if(shift.isWeekend()){
					if(shift.getStartsLocalDate().getDayOfWeek().getValue()==7 || shift.getEndsLocalDate().getDayOfWeek().getValue()==7 ){
						shift.setDisplayDate();
						sundaysShifts.add(shift);
					}
					else if(shift.getStartsLocalDate().getDayOfWeek().getValue()==6 || shift.getEndsLocalDate().getDayOfWeek().getValue()==6){
						shift.setDisplayDate();
						saturdaysShifts.add(shift);
					}
				}
			}
			
			Shift[] saturdaysShiftArray = new Shift[saturdaysShifts.size()];
			
			for(int i = 0; i<saturdaysShifts.size();i++){
				saturdaysShiftArray[i]=saturdaysShifts.get(i);
			}
			saturdaysInfo.setShifts(saturdaysShiftArray);
			saturdaysInfo.setDate(saturday);
			
			Shift[] sundaysShiftArray = new Shift[sundaysShifts.size()];
			
			for(int i = 0; i<sundaysShifts.size();i++){
				sundaysShiftArray[i]=sundaysShifts.get(i);
			}
			sundaysInfo.setShifts(sundaysShiftArray);
			sundaysInfo.setDate(sunday);
			
			dayInfo.add(saturdaysInfo);
			dayInfo.add(sundaysInfo);
			
			DaysNotificationInfo[] daysNotificationInfo = new DaysNotificationInfo[dayInfo.size()];
			
			for(int i = 0; i<dayInfo.size();i++){
				daysNotificationInfo[i]=dayInfo.get(i);
			}
			weekendInfo.setDays(daysNotificationInfo);
			
			weekendNotificationArray.add(weekendInfo);
			
		}
		WeekendsNotificationInfo[] weekendsNotificationInfo = new WeekendsNotificationInfo[weekendNotificationArray.size()];
		
		for(int i = 0; i<weekendNotificationArray.size();i++){
			weekendsNotificationInfo[i]=weekendNotificationArray.get(i);
		}
		setWeekendNotificationInfo(weekendsNotificationInfo);
		//System.out.println("ended setWeekendsWorked");
	}

	public WeekendsNotificationInfo[] getWeekendNotificationInfo() {
		return weekendNotificationInfo;
	}

	public void setWeekendNotificationInfo(WeekendsNotificationInfo[] weekendNotificationInfo) {
		this.weekendNotificationInfo = weekendNotificationInfo;
	}
}
