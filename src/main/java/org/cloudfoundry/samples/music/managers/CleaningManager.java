package org.cloudfoundry.samples.music.managers;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.constants.Constants;
import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibilities;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibility;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.AlternateWeekendsOffNotification;
import accessiblesolutions.accessiblescheduling.to.OverWeeklyDaysNotification;
import accessiblesolutions.accessiblescheduling.to.ScheduleNotification;
import accessiblesolutions.accessiblescheduling.to.ScheduleOptions;
import accessiblesolutions.accessiblescheduling.to.ShiftIssueTO;
import accessiblesolutions.accessiblescheduling.to.ShiftNotification;
import accessiblesolutions.accessiblescheduling.to.Weekend;
import accessiblesolutions.accessiblescheduling.worker.ShiftWorker;

@Component
public class CleaningManager {
	@Autowired
	private EmployeeShiftCompatibilityManager employeeShiftCompatibilityManager;
	
	@Autowired
	EmployeeShiftManager employeeShiftManager;
	
	
	@Autowired
	private CrudRepository<Employee,String> employeeCrud;

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
    
    public Weekend getWeekendOfShift(Shift shift) throws CorruptDataException{
    	Weekend weekend = new Weekend();
    	if(shift.isWeekend()){
    		LocalDate saturday;
    		LocalDate sunday;
    		
	    	if(shift.getStartsLocalDate().getDayOfWeek().getValue()==6){
	    		saturday=shift.getStartsLocalDate();
	    		sunday=saturday.plusDays(1);
	    	}
	    	else{
	    		sunday=shift.getStartsLocalDate();
	    		saturday=sunday.minusDays(1);
	    	}
	    	
	    	weekend.setMonth(saturday.getMonth());
	    	weekend.setSaturday(saturday);
	    	weekend.setSunday(sunday);
    	}
    	return weekend;
    }
    
    public Weekend[] getWeekendArray(ArrayList<Weekend> weekendList){
    	Weekend[] weekends = null;
    	
    	if(weekendList!=null && weekendList.size()>0){
    		weekends= new Weekend[weekendList.size()];
    		
    		for(int i = 0; i<weekendList.size();i++){
    			weekends[i]=weekendList.get(i);
    		}
//    		
//    		weekends = new Weekend[weekendList.size()];
//    		int index = 0;
//    		
//    		while(!weekendList.isEmpty()){
//    			Weekend earliest = null;
//    			
//    			for(Weekend weekend : weekendList){
//    				if(null==earliest || earliest.getSaturday().isAfter(weekend.getSaturday())){
//    					earliest = weekend;
//    				}
//    			}
//    			weekendList.remove(earliest);
//    			weekends[index] = earliest;
//    		}
    	}
    	
    	return weekends;
    }
    
    public Weekend[] getUpcomingWeekends() throws ProccessingException, CorruptDataException{
    	ArrayList<Weekend> weekends = new ArrayList<Weekend>();
    	
    	Iterable<Shift> shiftsDb = shiftCrud.findAll();
    	ArrayList<Shift> shifts = ShiftWorker.getUpcomingShifts(shiftsDb);
    	
    	for(Shift shift:shifts){
    		if(shift.isWeekend()){
    			boolean present = false;
    			Weekend weekend = getWeekendOfShift(shift);
    			
    			for(Weekend selectedWeekend: weekends){
    				if(selectedWeekend.getSaturday().getDayOfYear()==weekend.getSaturday().getDayOfYear()){
        				present=true;
        			}
    			}
    			if(!present && weekend.isValid()){
    				System.out.println("upcoming weekend"+weekend.toString());
    				weekends.add(weekend);
    			}
    		}
    	}
    	
    	return getWeekendArray(weekends);
    }
    public String fixAlternateWeekendOffNotifications() throws ProccessingException, CorruptDataException {
    	String result = "";
    	ArrayList<AlternateWeekendsOffNotification> issues = getAlternateWeekendsOffNotifications();
    	
    	for(AlternateWeekendsOffNotification issue:issues) {
    		result += fixAlternateWeekendOffNotification(issue);
    	}
    	
    	return result;
    }
    
    public String fixAlternateWeekendOffNotification(AlternateWeekendsOffNotification issue) {
    	String result = "";
    	
    	return result;
    }
    public ArrayList<AlternateWeekendsOffNotification> getAlternateWeekendsOffNotifications() throws ProccessingException, CorruptDataException{
    	ArrayList<AlternateWeekendsOffNotification> notifications = new ArrayList<AlternateWeekendsOffNotification>();
    	Weekend[] upcomingWeekends = getUpcomingWeekends();
    	Iterable<Employee> employees = employeeCrud.findAll();
    	Iterable<Shift> shiftsDb = shiftCrud.findAll();
    	ArrayList<Shift> shifts = ShiftWorker.getUpcomingShifts(shiftsDb);
    	
    	for(Employee employee: employees){
    		System.out.println("getting notifications for " + employee.toString() + employee.getOffAlternateWeekends());
    		if(employee.getOffAlternateWeekends()){
    			System.out.println(employee.getFirst() + " needs alternate weekends off");
    			ArrayList<Weekend> coveredWeekends = new ArrayList<Weekend>();
    			
    			for(Weekend weekend:upcomingWeekends){
    				if(weekend.isValid()){
	    				boolean coveredContains = false;
						for(Weekend coveredWeekend : coveredWeekends){
							if(coveredWeekend.getSaturday().getDayOfYear()==weekend.getSaturday().getDayOfYear()){
								coveredContains=true;
							}
						}
	    				if(null!=weekend && !coveredContains){
	    					System.out.println("checking notifications for weekend:"+weekend.toString());
		    				AlternateWeekendsOffNotification notification = null;
		    				
		    				ArrayList<Shift> assignedShifts = ShiftWorker.getAssignedShiftsFor(shifts, employee.getId());
		    				System.out.println(employee.getFirst() + " has " + assignedShifts.size() + " shifts assigned");
		    		    	boolean worksWeekend = false;
		    				for(Shift shift :assignedShifts){
		    					if(null!=shift && shift.isValid() &&
		    						shift.getStartsLocalDate().getDayOfYear() == weekend.getSaturday().getDayOfYear() ||
									shift.getStartsLocalDate().getDayOfYear() == weekend.getSunday().getDayOfYear() ||
									shift.getEndsLocalDate().getDayOfYear() == weekend.getSaturday().getDayOfYear() ||
									shift.getEndsLocalDate().getDayOfYear() == weekend.getSunday().getDayOfYear()){
		    						System.out.println(employee.getFirst() + " works the weekend of " + weekend.toString());
		    						worksWeekend=true;
		    					}
		    				}
		    				
		    				if(worksWeekend){
		    					System.out.println(employee.getFirst() + " works the weekend of " + weekend.toString() + ". Checking surrounding weekends");
		    					ArrayList<Shift> shiftsLastWeekend = getShiftsLastWeekend(weekend,employee);
		    					
		    					if(shiftsLastWeekend.size()>0){
		    						notification = new AlternateWeekendsOffNotification(employee,getInitialWeekendsWorked(weekend,employee));
		    						if(!coveredWeekends.contains(weekend)){
		    							coveredWeekends.add(weekend);
		    						}
		    					}
		    					int itteration=1;
		    					Weekend selectedWeekend = weekend;
		    					while(itteration<10){//if there was a shift next weekend check recursively and add
		    						System.out.println("itterating over future weekend " + itteration + " weekends covered" + coveredWeekends.size());
		    						selectedWeekend=getNextWeekend(selectedWeekend);
		    						if(selectedWeekend.isValid()){
			    						coveredContains = false;
			    						for(Weekend coveredWeekend : coveredWeekends){
			    							if(coveredWeekend.getSaturday().getDayOfYear()==selectedWeekend.getSaturday().getDayOfYear()){
			    								coveredContains=true;
			    							}
			    						}
			    						if(!coveredContains){
			    							
				    						ArrayList<Shift> shiftsSelectedWeekend = getShiftsForWeekend(selectedWeekend,employee);
				    						System.out.println("evaulating weekend: " + weekend.toString() +" with shift array size:"+shiftsSelectedWeekend.size());
				    						if(shiftsSelectedWeekend.size()>0){
				    							if(notification==null){
				    								System.out.println("Making a notification");
				    								ArrayList<Weekend> weekends = new ArrayList<Weekend>();
				    								weekends.add(weekend);
				    								weekends.add(selectedWeekend);
				    								
				    								ArrayList<ArrayList<Shift>>  weekendsWorked = getWeekendsWorked(weekends,employee);
				    								notification = new AlternateWeekendsOffNotification(employee,weekendsWorked);
				    	    							coveredWeekends.add(selectedWeekend);
				    	    							System.out.println(notification.toString());
				    							}else{
				    								System.out.println("updating a notification");
				    								ArrayList<ArrayList<Shift>>  weekendsWorked = notification.getWeekendsWorked();
				    								
				    								weekendsWorked.add(getShiftsForWeekend(selectedWeekend,employee));
				    								
				    								notification.setWeekendsWorked(weekendsWorked);
		
				    	    							coveredWeekends.add(selectedWeekend);
				    								System.out.println(notification.toString());
				    								
				    							}
				        					}
				    						else{
				    							itteration=10;
				    							break;
				    						}
			    						}
			    						itteration++;
		    						}
		    					}
		    					if(notification!=null){
		    						notifications.add(notification);
		    					}
		    				}
	    				}
    				}
    			}
    		}
    	}
    	
    	return notifications;
    }
    
    private ArrayList<ArrayList<Shift>> getWeekendsWorked(ArrayList<Weekend> weekends, Employee employee) throws ProccessingException, CorruptDataException {
    	ArrayList<ArrayList<Shift>> weekendsWorked = new ArrayList<ArrayList<Shift>>();
    	
    	for(Weekend weekend: weekends){
    		weekendsWorked.add(getShiftsForWeekend(weekend,employee));
    	}
		
		return weekendsWorked;
	}


	private Weekend getNextWeekend(Weekend weekend){
    	Weekend nextWeekend = new Weekend();
    	
    	if(weekend.isValid()){
	    	nextWeekend.setMonth(weekend.getSaturday().plusWeeks(1).getMonth());
	    	nextWeekend.setSaturday(weekend.getSaturday().plusWeeks(1));
	    	nextWeekend.setSunday(weekend.getSunday().plusWeeks(1));
    	}
    	return nextWeekend;
    }
    private ArrayList<ArrayList<Shift>> getInitialWeekendsWorked(Weekend weekend, Employee employee) throws ProccessingException, CorruptDataException {
		ArrayList<ArrayList<Shift>> weekendsWorked = new ArrayList<ArrayList<Shift>>();
		Weekend lastWeekend = new Weekend();
		LocalDate lastSaturday = weekend.getSaturday().minusWeeks(1);
		
		lastWeekend.setMonth(lastSaturday.getMonth());
		lastWeekend.setSaturday(lastSaturday);
		lastWeekend.setSunday(lastSaturday.plusDays(1));
		
		weekendsWorked.add(getShiftsForWeekend(lastWeekend,employee));
		weekendsWorked.add(getShiftsForWeekend(weekend,employee));
		
		return weekendsWorked;
	}

    private ArrayList<Shift> getShiftsLastWeekend(Weekend weekend, Employee employee) throws ProccessingException, CorruptDataException {
    	Weekend lastWeekend = new Weekend();
		LocalDate lastSaturday = weekend.getSaturday().minusWeeks(1);
		
		lastWeekend.setMonth(lastSaturday.getMonth());
		lastWeekend.setSaturday(lastSaturday);
		lastWeekend.setSunday(lastSaturday.plusDays(1));
		
    	
		return getShiftsForWeekend(lastWeekend,employee);
	}

	private ArrayList<Shift> getShiftsForWeekend(Weekend weekend, Employee employee) throws ProccessingException, CorruptDataException {
    	Iterable<Shift> shiftsDb = shiftCrud.findAll();
    	ArrayList<Shift> assignedShifts = ShiftWorker.getAssignedShiftsFor(shiftsDb, employee.getId());
    	ArrayList<Shift> shiftsForWeekend = new ArrayList<Shift>();
    	
    	for(Shift shift: assignedShifts){
    		if(shift.getStartsLocalDate().isEqual(weekend.getSaturday())||
    				shift.getStartsLocalDate().isEqual(weekend.getSunday())	||
    				shift.getEndsLocalDate().isEqual(weekend.getSaturday())||
    				shift.getEndsLocalDate().isEqual(weekend.getSaturday())){
    			shiftsForWeekend.add(shift);
    		}
    	}
    	
		return shiftsForWeekend;
	}


	public ArrayList<OverWeeklyDaysNotification> getOverWeeklyDaysNotifications() throws ProccessingException, CorruptDataException{
    	ArrayList<OverWeeklyDaysNotification> notifications = new ArrayList<OverWeeklyDaysNotification>();
    	
    	ZoneId defaultZoneId = ZoneId.systemDefault();
        //toString() append +8 automatically.
        Date date = new Date();

        //1. Convert Date -> Instant
        Instant instant = date.toInstant();

        //3. Instant + system default time zone + toLocalDateTime() = LocalDateTime
        LocalDateTime now = instant.atZone(defaultZoneId).toLocalDateTime();//Update without +1 glitch
    	int month = now.getMonthValue();
    	
    	ArrayList<Employee> employees = (ArrayList<Employee>) employeeCrud.findAll();
    	
    	Iterable<Shift> shiftdb = shiftCrud.findAll();
		for(int week =0; week<6;week++){
			for(Employee employee: employees){
				System.out.println("week:"+week+" "+employee.toString());
				ArrayList<Shift> shifts = new ArrayList<Shift>();
				HashMap<Integer,ArrayList<Shift>> weeklyOverage = new HashMap<Integer,ArrayList<Shift>>();
				
				if(null!=shiftdb){
					shifts=ShiftWorker.getUpcomingShifts(shiftdb);
					if(null!=shifts){
						shifts=ShiftWorker.getAssignedShiftsFor(shifts, employee.getId());
						if(null!=shifts){
							shifts = ShiftWorker.getShiftsStartingWeekOfMonth(shifts, week, month, 2018);
							
							if(shifts!=null){
								for(int selectedDay = 1;selectedDay<8;selectedDay++){
									System.out.println("day:"+selectedDay);
									for(Shift shift: shifts){
										int shiftsDay = shift.getStartsLocalDate().getDayOfWeek().getValue();
										if(shiftsDay==selectedDay){
											ArrayList<Shift> daysShifts =null;
											if(weeklyOverage.containsKey(selectedDay)){
												daysShifts = weeklyOverage.get(selectedDay);
											}else{
												daysShifts = new ArrayList<Shift>();
											}
											shift.setDisplayDate();
											daysShifts.add(shift);
											if(weeklyOverage.containsKey(selectedDay)){
												weeklyOverage.replace(selectedDay, daysShifts);
											}
											else{
												weeklyOverage.put(selectedDay, daysShifts);
											}
										}
									}
								}
							}
							
							if(weeklyOverage.keySet().size()>5){
								OverWeeklyDaysNotification notification = new OverWeeklyDaysNotification(weeklyOverage,employee,week+1);
								notifications.add(notification);
							}
						}
					}
				}
			}
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
        LocalDateTime now = instant.atZone(defaultZoneId).plusWeeks(2).toLocalDateTime();//Update without +1 glitch

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

	public String fixShiftNotification(ShiftNotification shiftNotification) throws CorruptDataException, ProccessingException {
		String output = "";
		ScheduleOptions options = new ScheduleOptions();
		Shift shift = shiftNotification.getIssues().get(0).getShift();
		
		if(null!=shift){
			Employee previousEmployee = employeeCrud.findOne(shift.getStaffId());
    		output+="Attempting to assign " +shift.toString();
    		System.out.println("Attempitng to assign " + shift.toString());
			EmployeeShiftCompatibilities shiftCompatibilities = employeeShiftCompatibilityManager.getValidCompatibilities(employeeShiftCompatibilityManager.getEmployeeShiftCompatibilitiesForShift(shift),options);
			output+=shiftCompatibilities.compatibilities.toString();
			Employee employee=null;
			//attemptAssigningOnlyCompatibility
			if(null!=shiftCompatibilities && shiftCompatibilities.compatibilities.size()==1){
				if(shiftCompatibilities.compatibilities.get(0).getEmployee().getId().equalsIgnoreCase(previousEmployee.getId())){
					employee=shiftCompatibilities.compatibilities.get(0).getEmployee();
					shift.setAssignmentReason("Only " + employee.getFirst() +" was compatible and available. ");
				}
			}
			//end attemptAssigningOnlyCompatibility
			if(employee==null){
				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
					if(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift())<compatibility.getEmployee().getMinHours()){
						if(compatibility.getEmployee().getId().equalsIgnoreCase(previousEmployee.getId())){
							employee=compatibility.getEmployee();
							shift.setAssignmentReason("Min");
						}
					}
				}
			}
			if(employee==null && employeeShiftCompatibilityManager.getEmployeeWithMostTimeBeforeOvertimeAfterAssignment(shiftCompatibilities).getId().equalsIgnoreCase(previousEmployee.getId())){
				employee=employeeShiftCompatibilityManager.getEmployeeWithMostTimeBeforeOvertimeAfterAssignment(shiftCompatibilities);
				shift.setAssignmentReason("Employee had the most time before overtime after assignment");
			}
			else if(shift.getAssignmentReason().length()<10){
				shift.setAssignmentReason("Employee had the most time until minimn was reached after assignment");
			}
			
			if(employee==null&&shiftCompatibilities!=null&&shiftCompatibilities.compatibilities!=null&&shiftCompatibilities.compatibilities.size()>0){
				float hours = 80;
				for(EmployeeShiftCompatibility compatibility :shiftCompatibilities.compatibilities){
					if(compatibility.getEmployee().getRequestsExtraShifts()){
						if(employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift())<hours){
							if(compatibility.getEmployee().getId().equalsIgnoreCase(previousEmployee.getId())){
								hours=employeeShiftCompatibilityManager.getHoursScheduledWeekOfShift(compatibility.getEmployee(),compatibility.getShift());
								employee=compatibility.getEmployee();
								shift.setAssignmentReason("All in overtime, they requested it and have least hours");
							}
						}
					}
				}
				
				if(employee==null){
					employee=shiftCompatibilities.compatibilities.get(0).getEmployee();
					shift.setAssignmentReason("One of those in overtime who was available");
				}
			}
			if(employee!=null){
				System.out.println("assigning " + employee.getFirst() + " to shift " + shift.getId() + " " + shift.toString()
				+ " beacause " + shift.getAssignmentReason());
				shift.setStaffId(employee.getId());
				shift.setStaffName(employee.getFirst());
				shift.setAssigned(true);
				shiftCrud.save(shift);
				output+=" Assigning to "+employee.getFirst() + " " + employee.getInitial() + " because " + shift.getAssignmentReason();
			}
		}
		return output;
	}
}