package org.cloudfoundry.samples.music.managers;
import java.util.ArrayList;

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
    
    public ArrayList<ScheduleNotification> getScheduleNotifications() throws ProccessingException, CorruptDataException{
    	ArrayList<ScheduleNotification> notifications = new ArrayList<ScheduleNotification>();
    	
//    	for(int i =1; i <6;i++){
//    		ArrayList<Employee>
//    		getHoursScheduledWeek(Employee employee,int week, int month)
//    	}
    	
    	return notifications;
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