package org.cloudfoundry.samples.music.managers;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.constants.Constants;
import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.CustomField;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibility;
import accessiblesolutions.accessiblescheduling.domain.RecurringShiftNeed;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.domain.ShiftRequest;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.ScheduleNotification;
import accessiblesolutions.accessiblescheduling.to.ShiftIssueTO;
import accessiblesolutions.accessiblescheduling.to.ShiftNotification;
import accessiblesolutions.accessiblescheduling.worker.ShiftWorker;

@Component
public class CleaningManager {
	@Autowired
	private EmployeeShiftCompatibilityManager employeeShiftCompatibilityManager;
	
	@Autowired
	EmployeeShiftManager employeeShiftManager;
	
	@Autowired
	private ShiftManager shiftManager;
	@Autowired
	private CrudRepository<CustomField,String> customFieldCrud;
	
	@Autowired
	private CrudRepository<Employee,String> employeeCrud;
	@Autowired
	private CrudRepository<RecurringShiftNeed,String> recurringShiftNeedCrud;
	@Autowired
	private CrudRepository<ShiftRequest,String> shiftRequestCrud;
	@Autowired
	private CrudRepository<Client,String> clientCrud;
	
    private MongoCustomFieldDataRepository customFieldDataRepository;
    private CrudRepository<CustomFieldData, String> customFieldDataCrud;
    
    @Autowired
    private CrudRepository<Shift,String> shiftCrud;
    @Autowired
    public CleaningManager(MongoCustomFieldDataRepository customFieldDataRepository, CrudRepository<CustomFieldData, String> customFieldDataCrud) {
        this.customFieldDataCrud = customFieldDataCrud;
        this.customFieldDataRepository=customFieldDataRepository;
    }
    

    public ArrayList<ShiftRequest> getOrphanedShiftRequests() {
    	ArrayList<ShiftRequest> orphans = new ArrayList<ShiftRequest>();
    	Iterable<ShiftRequest> table = shiftRequestCrud.findAll();
    	Iterable<Client> clients =clientCrud.findAll();
    	ArrayList<String> clientIds = new ArrayList<String>();
    	
    	
    	for(Client client: clients){
    		clientIds.add(client.getId());
    	}

    	for(ShiftRequest request:table){
    			if(!clientIds.contains(request.getClientId())){
    				orphans.add(request);
    			}
    	}
    	
    	return orphans;
	}
    
    public ArrayList<RecurringShiftNeed> getOrphanedRecurringShiftRequests() {
    	ArrayList<RecurringShiftNeed> orphans = new ArrayList<RecurringShiftNeed>();
    	Iterable<RecurringShiftNeed> table = recurringShiftNeedCrud.findAll();
    	Iterable<Client> clients =clientCrud.findAll();
    	ArrayList<String> clientIds = new ArrayList<String>();
    	
    	
    	for(Client client: clients){
    		clientIds.add(client.getId());
    	}

    	for(RecurringShiftNeed request:table){
    			if(!clientIds.contains(request.getClientId())){
    				orphans.add(request);
    			}
    	}
    	
    	return orphans;
	}
    
    public void removeOrphanedRecurringShiftRequests() {
    	ArrayList<RecurringShiftNeed> orphanedShiftRequests = getOrphanedRecurringShiftRequests();
    	for(RecurringShiftNeed request:orphanedShiftRequests){
    		recurringShiftNeedCrud.delete(request);
    	}
	}
    public void removeOrphanedSingleShiftRequests() {
    	ArrayList<ShiftRequest> orphanedShiftRequests = getOrphanedShiftRequests();
    	for(ShiftRequest request:orphanedShiftRequests){
    		shiftRequestCrud.delete(request);
    	}
	}
    
    public ArrayList<ShiftIssueTO> getAlternateWeekendOffIssues() throws ProccessingException, CorruptDataException{
    	ArrayList<ShiftIssueTO> issues = new ArrayList<ShiftIssueTO>();
    	
    	ArrayList<Shift> shifts = (ArrayList<Shift>) shiftCrud.findAll();
    	ArrayList<Shift> upcomingShifts = ShiftWorker.getUpcomingShifts(shifts);
    	for(Shift shift :upcomingShifts){
    		if(null!=shift.getStaffId()){
	    		ArrayList<Shift> priorShifts = ShiftWorker.getShiftsAssignedWeekendBefore(shift,shifts);
	    		
	    		if(null != priorShifts && !priorShifts.isEmpty()){
	    			ShiftIssueTO issue = new ShiftIssueTO();
	    			shift.setDisplayDate();
	    			issue.setShift(shift);
	    			issue.setDescription(Constants.workedLastWeekend);
	    			issues.add(issue);
	    		}
    		}
    	}
    	
    	return issues;
    }
    
    public ArrayList<ShiftIssueTO> getUnavailableDayIssues() throws ProccessingException, CorruptDataException{
    	ArrayList<ShiftIssueTO> issues = new ArrayList<ShiftIssueTO>();
    	
    	Iterable<Shift> shifts = shiftCrud.findAll();
    	ArrayList<Shift> upcomingShifts = ShiftWorker.getUpcomingShifts(shifts);
    	upcomingShifts=ShiftWorker.getSameDayShifts(upcomingShifts);
    	for(Shift shift :upcomingShifts){
    		if(null!=shift.getStaffId()){
	    		Employee employee = employeeCrud.findOne(shift.getStaffId());
	    		if(null!=employee && !employeeShiftCompatibilityManager.isAvailableFor(employee, shift)){
	    			ShiftIssueTO issue = new ShiftIssueTO();
	    			shift.setDisplayDate();
	    			issue.setShift(shift);
	    			issue.setDescription("This employee does not work the hours of the day this shift runs through.");
	    			issues.add(issue);
	    		}
    		}
    	}
    	
    	return issues;
    }
    public ArrayList<ShiftNotification> getOverDailyShiftNotifications() throws ProccessingException, CorruptDataException{
    	ArrayList<ShiftNotification> notifications = new ArrayList<ShiftNotification>();
    	
    	ZoneId defaultZoneId = ZoneId.systemDefault();
        //toString() append +8 automatically.
        Date date = new Date();

        //1. Convert Date -> Instant
        Instant instant = date.toInstant();

        //3. Instant + system default time zone + toLocalDateTime() = LocalDateTime
        LocalDateTime now = instant.atZone(defaultZoneId).toLocalDateTime();//Update without +1 glitch
    	int month = now.getMonthValue();
    	
    	ArrayList<Employee> employees = (ArrayList<Employee>) employeeCrud.findAll();
		LocalDate monthStart = LocalDate.of(2018,month,1);
    	
    	LocalDate day = monthStart;
		while(day.getMonthValue()==month){
			System.out.println(day.toString());
			for(Employee employee: employees){
				System.out.println(employee.toString());
				ArrayList<Shift> workedShifts = new ArrayList<Shift>();
				ArrayList<Shift> shifts = new ArrayList<Shift>();
				Iterable<Shift> shiftdb = shiftCrud.findAll();
				if(null!=shiftdb){
					shifts=ShiftWorker.getUpcomingShifts(shiftdb);
					if(null!=shifts){
						shifts=ShiftWorker.getAssignedShiftsFor(shifts, employee.getId());
						for(Shift shift : shifts){
							if(shift.getStartDate().equals(day.toString()) ||
								shift.getEndDate().equals(day.toString())){
								System.out.println(shift.getStartDate() +shift.getEndDate()+ " day"+day.toString());
								workedShifts.add(shift);
							}
						}
						if(workedShifts.size()>2){
							ArrayList<ShiftIssueTO> issues = new ArrayList<ShiftIssueTO>();
							
							for(Shift shift : workedShifts){
								System.out.println(shift.toString());
								ShiftIssueTO issue = new ShiftIssueTO();
								issue.setDescription(Constants.shiftExceedsDailyMax);
								shift.setDisplayDate();
								issue.setShift(shift);
								issues.add(issue);
							}
							ShiftNotification notification = new ShiftNotification();
							notification.setDescription(Constants.shiftsExceedDailyMax);
							notification.setIssues(issues);
							System.out.println(notification.toString());
							notifications.add(notification);
						}
					}
				}
			}
			day=day.plusDays(1);
		}
		
		return notifications;
	}
	
    public ArrayList<ShiftIssueTO> getViolatesCallOffIssues() throws ProccessingException, CorruptDataException{
    	ArrayList<ShiftIssueTO> issues = new ArrayList<ShiftIssueTO>();
    	
    	Iterable<Shift> shifts = shiftCrud.findAll();
    	ArrayList<Shift> upcomingShifts = ShiftWorker.getUpcomingShifts(shifts);
    	for(Shift shift :upcomingShifts){
    		if(null!=shift.getStaffId()){
	    		Employee employee = employeeCrud.findOne(shift.getStaffId());
	    		if(null!=employee && employee.requestedOff(shift)){
	    			ShiftIssueTO issue = new ShiftIssueTO();
	    			shift.setDisplayDate();
	    			issue.setShift(shift);
	    			issue.setDescription(Constants.violatesCallOff);
	    			issues.add(issue);
	    		}
    		}
    	}
    	
    	return issues;
    }
    
    public float getHoursScheduledWeekOfMonth(Employee employee, int week,int month) throws CorruptDataException{		
    	float hours = 80;
		if(null!=employee){
			hours= 0;
			ArrayList<Shift> shiftsForWeek = null;
			shiftsForWeek = employeeShiftManager.getAssignedShiftsForEmployeeForWeekOfMonth(employee.getId(),week,month);
			
			if(null!=shiftsForWeek){
				for(Shift scheduledShift : shiftsForWeek){
					hours+= scheduledShift.getDuration();
				}
			}
		}
		return hours;
	}
    
    public ArrayList<ScheduleNotification> getScheduleNotifications() throws ProccessingException, CorruptDataException{
    	ArrayList<ScheduleNotification> notifications = new ArrayList<ScheduleNotification>();
    	
        ZoneId defaultZoneId = ZoneId.systemDefault();
        //toString() append +8 automatically.
        Date date = new Date();

        //1. Convert Date -> Instant
        Instant instant = date.toInstant();

        //3. Instant + system default time zone + toLocalDateTime() = LocalDateTime
        LocalDateTime now = instant.atZone(defaultZoneId).toLocalDateTime();//Update without +1 glitch

        int month = now.getMonthValue();
    	
    	ArrayList<Employee> employees = (ArrayList<Employee>) employeeCrud.findAll();
		LocalDate monthStart = LocalDate.of(2018,month,1);
		
    	for(int i =0; i <6;i++){
			for(Employee employee: employees){
				float scheduled =getHoursScheduledWeekOfMonth(employee,i, month);
				
				if(!employee.getInactive()){
            		if(i>0&&monthStart.plusWeeks(i).getMonthValue()==month){
	        			if(scheduled<employee.getMinHours()){
	        				notifications.add(getMinNotification(employee,i,now.getMonth().name(),scheduled));
	        			}
            		}
				}
				
				if(monthStart.plusWeeks(i-1).getMonthValue()==month && scheduled>employee.getMaxHours()){
    				notifications.add(getMaxNotification(employee,i,now.getMonth().name(),scheduled));
    			}
    		}
    	}
    	
    	return notifications;
    }
    public ScheduleNotification getMinNotification(Employee employee,int week, String month, float scheduled){
    	ScheduleNotification notification = new ScheduleNotification();
		String description = "For week " +(week+1)+ " of "+ month+ " " +employee.getFirst() + " " + employee.getInitial();
		description+= " is under minimum hours with "+ scheduled + " scheduled of a required " + employee.getMinHours() +". ";
		description+= (employee.getMinHours()-scheduled) + " more hours are needed this week to meet the minimum.";
		notification.setDescription(description);
		notification.setStaff(employee.getFirst()+ " " + employee.getInitial());
    	return notification;
    }
    public ScheduleNotification getMaxNotification(Employee employee,int week, String month, float scheduled){
    	ScheduleNotification notification = new ScheduleNotification();
		String description = "For week " +(week+1)+ " of " +month+ " " +employee.getFirst() + " " + employee.getInitial();
		description+= " is over maximum hours with "+ scheduled + " scheduled of at most " + employee.getMaxHours() +". ";
		description+= (scheduled-employee.getMaxHours()) + " fewer hours are needed this week to avoid overtime.";
		notification.setDescription(description);
		notification.setStaff(employee.getFirst()+ " " + employee.getInitial());
		
    	return notification;
	}
    public ArrayList<ShiftNotification> getShiftNotifications() throws ProccessingException, CorruptDataException{
    	ArrayList<ShiftIssueTO> issues = new ArrayList<ShiftIssueTO>();
    	ArrayList<ShiftNotification> notifications = new ArrayList<ShiftNotification>();
    	
    	issues = getViolatesCallOffIssues();
    	issues.addAll(getUnavailableDayIssues());
    	for(ShiftIssueTO issue:issues){
    		ShiftNotification notification = new ShiftNotification();
    		
    		notification.setDescription(Constants.reassignInvalidity);
    		
    		ArrayList<ShiftIssueTO> notificationIssue = new ArrayList<ShiftIssueTO>();
    		notificationIssue.add(issue);
    		notification.setIssues(notificationIssue);
    		
    		notifications.add(notification);
    	}
    	
    	return notifications;
    }
}