package org.cloudfoundry.samples.music.managers;
import java.time.DayOfWeek;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;

import org.cloudfoundry.samples.music.repositories.mongodb.ScheduleStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.ClientRequest;
import accessiblesolutions.accessiblescheduling.domain.Event;
import accessiblesolutions.accessiblescheduling.domain.RecurringShiftNeed;
import accessiblesolutions.accessiblescheduling.domain.ScheduleStatus;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.domain.ShiftRequest;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.util.Util;

@Component
public class ShiftGenerationManager {
	private static final Logger logger = LoggerFactory.getLogger(ShiftGenerationManager.class);
    private CrudRepository<Event, String> eventRepository;//TODO switch to autowired
    private CrudRepository<Shift, String> shiftCrud;
    
    @Autowired
    private CrudRepository<ClientRequest, String> requestRepository;
    
    @Autowired
    private CrudRepository<ScheduleStatus, String> scheduleStatusCrud;
    
    @Autowired
    private ScheduleStatusRepository scheduleStatusRepository;    
    
    @Autowired
    private CrudRepository<ShiftRequest, String> shiftRequestCrud;
    private CrudRepository<ShiftRequest, String> shiftRequestRepository;    
    private CrudRepository<RecurringShiftNeed, String> recurringShiftNeedRepository;    
    
    @Autowired
    public ShiftGenerationManager(CrudRepository<Event, String> eventRepository,CrudRepository<Shift, String> shiftCrud,
    		CrudRepository<ShiftRequest, String> shiftRequestRepository, CrudRepository<RecurringShiftNeed, String> recurringShiftNeedRepository) {
        this.eventRepository = eventRepository;
        this.shiftCrud = shiftCrud;
        this.shiftRequestRepository = shiftRequestRepository;
        this.recurringShiftNeedRepository = recurringShiftNeedRepository;
    }
    
    public String generateShifts(String selectedMonth,String selectedYear) throws NumberFormatException{
    	String response = "Generated ";
    	int shiftsGenerated = 0;
    	Iterable<ClientRequest> requests = requestRepository.findAll();
    	System.out.println("generating shifts");
    	for(ClientRequest request: requests) {
    		try {
				shiftsGenerated+=generateShiftsForRequest(request,selectedMonth,Integer.parseInt(selectedYear));
			} catch (CorruptDataException e) {
				e.printStackTrace();
			} catch (ProccessingException e) {
				e.printStackTrace();
			}
    	}
    	System.out.println(response+shiftsGenerated);
    	
    	ScheduleStatus status = scheduleStatusCrud.findOne(selectedMonth);
    	
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(selectedMonth);
    	}
    	
    	scheduleStatusRepository.deleteByMonth(selectedMonth);
    	status.setGenerated(true);
    	scheduleStatusCrud.save(status);
    	
    	return response+shiftsGenerated;
    }
    
    public int generateShiftsForRequest(ClientRequest request,String selectedMonth, int selectedYear) throws CorruptDataException, ProccessingException {
    	ArrayList<Shift> shifts = new ArrayList<Shift>();
    	String startDate = null;
		String month = null;
		String[] splitDate = null;
		
		startDate = request.getStartDate();
		System.out.println("generating shifts for request:"+request.toString());
		if(startDate!=null) {
			splitDate = startDate.split("-");
			
			if(splitDate.length>1){
				month = splitDate[1];
			}
			
			if(!request.isRepeats()) {
    			try {
					Shift shift = getShiftForRequestAtTime(request,request.getStartsLocalDateTime());
					shifts.add(shift);
	    		} catch (CorruptDataException e) {
					System.out.println("ERROR: Corrupt time for shift provided"+e.getMessage());
				}
    		}
			else {
				ArrayList<LocalDateTime> times = new ArrayList<LocalDateTime>();
				
				try {
					times= getDatesForRequestDuringMonth(request,Integer.parseInt(selectedMonth),selectedYear);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (ProccessingException e) {
					e.printStackTrace();
				}
				
				for(LocalDateTime time:times) {
					boolean included = true;
					
					if(request!=null) {
						if(request.getExceptions()!=null && request.getExceptions().length>0) {
							for(String exception: request.getExceptions()) {
								if(exception.equalsIgnoreCase(Util.getDateFromLocalDateTime(time))) {
									included=false;
								}
							}
						}
						System.out.println("getting shift for :"+ time+included);
						if(included) {
							shifts.add(getShiftForRequestAtTime(request,time));
						}
					}
				}
			}
    	}
		else {
			System.out.println("ERROR: Corrupt time for shift provided");
		}
    	
    	if(shifts.size()>0) {
    		System.out.println("Saving:"+shifts.size()+" shifts for"+request.toString());
    		shiftCrud.save(shifts);
    	}
    	
    	return shifts.size();
    }
    
    public Shift getShiftForRequestAtTime(ClientRequest request,LocalDateTime time) throws CorruptDataException, ProccessingException {
    	Shift shift = new Shift();
    	shift.setClientId(request.getClientId());
    	shift.setClientName(request.getClientName());
    	shift.setRequestedStaffId(request.getStaffId());
    	shift.setRequestedStaffName(request.getStaffName());
    	shift.setTime(time.toLocalTime().toString());
    	shift.setDate(Util.getDateFromLocalDateTime(time));
    	shift.setStartDate(Util.getDateFromLocalDateTime(time));
    	shift.setStartTime(time.toLocalTime().toString());
    	if(request.getOvernight()) {
    		shift.setEndDate(Util.getDateFromLocalDateTime(shift.getStartsLocalDateTime().plusDays(1)));
    	}
    	else {
    		shift.setEndDate(Util.getDateFromLocalDateTime(shift.getStartsLocalDateTime()));
    	}
    	shift.setEndTime(request.getEndTime());
    	shift.setStartMonth(time.getMonthValue());
    	shift.setStartYear(time.getYear());
    	shift.setStartWeek(Util.getWeekOfDate(time.toLocalDate().toString()));
    	shift.setDisplayDate();
    	
    	return shift;
    }
    
    public ArrayList<LocalDateTime> getDatesForRequestDuringMonth(ClientRequest request, int selectedMonth, int selectedYear) throws CorruptDataException, ProccessingException{
    	ArrayList<LocalDateTime> times = new ArrayList<LocalDateTime>();
    	LocalDateTime initialTime = request.getStartsLocalDateTime();
    	LocalDateTime timeCursor = initialTime;
    	int increment = Integer.parseInt(request.getRepeatsEvery());
    	
    	if(initialTime.getYear()<=selectedYear) {
	    	if(initialTime.getMonthValue()<selectedMonth) {
	    		while(timeCursor.getMonthValue()<selectedMonth || timeCursor.getYear()<selectedYear) {
	    			if(request.getInterval().contains("day")) {//Gets to first shift of this Month
		    			timeCursor=timeCursor.plusDays(increment);/**working*/
	    			}
	    			else if(request.getInterval().contains("week")) {//Gets to first Saturday of this Month
	    				if(timeCursor.getDayOfWeek().getValue()==6) {//Look at Saturday since coming from before
	    					timeCursor=timeCursor.plusWeeks(increment);
	    				}
	    				else {
	    					timeCursor=timeCursor.plusDays(6-timeCursor.getDayOfWeek().getValue());
	    				}
	    			}
	    			else if(request.getInterval().contains("month") || request.getInterval().contains("year")) {
    					timeCursor=timeCursor.plusMonths(increment);
    					timeCursor=timeCursor.minusDays(timeCursor.getDayOfMonth()-1);//gets first day of month
    					
    					if(timeCursor.getMonthValue()==selectedMonth && timeCursor.getYear()==selectedYear) {
		    				if(request.getMonthInterval().contains("day")) {//on dayOfMonth
		    					timeCursor=timeCursor.plusDays(initialTime.getDayOfMonth()-1);//gives only shift
		    				}
		    				else {//on dayOfWeek for weekOfMonth of request
		    					while(Util.getWeekOfDate(Util.getDateFromLocalDateTime(timeCursor))>
		    							Util.getWeekOfDate(Util.getDateFromLocalDateTime(initialTime))){
		    						timeCursor=timeCursor.plusWeeks(1);
		    					}
		    					
		    					int shift = initialTime.getDayOfWeek().getValue()-timeCursor.getDayOfWeek().getValue();
		    					timeCursor=timeCursor.plusDays(shift);//gives only shift
		    				}
    					}    			    
	    			}
	    		}
	    	}
	    	times.add(timeCursor);
	    	
	    	if(request.getInterval().contains("day")) {
	    		for(int index =1;index<32;index++) {
	    			timeCursor=timeCursor.plusDays(increment);
	    			
	    			if(timeCursor.getMonthValue()==selectedMonth) {
	    				times.add(timeCursor);
	    			}
	    			else {
	    				break;
	    			}
		    	}
	    	}
	    	else if(request.getInterval().contains("week")) {
	    		times.clear();
	    		while(timeCursor.getDayOfWeek().getValue()!=6) {
	    			timeCursor=timeCursor.plusDays(1);
	    		}
	    		System.out.println("Starting week time generation at:"+timeCursor.toString()+ timeCursor.getDayOfWeek().toString());
	    		while(timeCursor.minusDays(6).getMonthValue()<=selectedMonth) {
	    			System.out.println("Finding times for week of :"+timeCursor.toString());
	    			for(int index = 0; index<7;index++) {
	    				boolean working = request.getDays()[index];
	    				LocalDateTime selectedDay = timeCursor.minusDays(6-index);
	    				System.out.println("making shifts for day "+selectedDay.getDayOfWeek().toString() + " "+working +" monthValue:"+selectedDay.getMonthValue()+" selectedMonth"+selectedMonth);
	    				if(working && selectedDay.getMonthValue()==selectedMonth) {
	    					times.add(selectedDay);
	    					System.out.println("Added time:"+selectedDay.toString());
	    				}
	    			}
	    			System.out.println("incrementing "+ increment + " weeks");
	    			timeCursor=timeCursor.plusWeeks(increment);
	    		}
	    	}
    	}
    	
    	System.out.println(request.toString()+ " has generated " +times.size()+ " times"+times.toString());
    	return times;
    }
    public String generateEventShifts(String selectedMonth) throws CorruptDataException {
    	String shiftResponse = "~Events~" + System.lineSeparator();
    	Iterable<Event> events = eventRepository.findAll();
    	ArrayList<Shift> shifts =new ArrayList<Shift>();
		shiftResponse+="# of events found: ";    	
    	if(null!=events){
    		shiftResponse+=((ArrayList<Event>)events).size()+ System.lineSeparator();
	    	for (Event event : events) {
	    		shiftResponse+="Evaluated need for shift for " +event.getName()+ System.lineSeparator();
	    		//TODO check to see if shifts have been made for this
	        	String startDate = null;
	    		String month = null;
	    		startDate = event.getStartDate();
	    		String[] splitDate = startDate.split("-");
	    		if(splitDate.length>1){
	    			month = splitDate[1];
	    		}
	    		shiftResponse+="Generating for month " +selectedMonth;
	    		shiftResponse+="while this event is for month "+month+ System.lineSeparator();
	    		if(month.equals(selectedMonth)||selectedMonth.equals("0"+month)){
		    		shiftResponse+="This event needed shifts, so they were added "+System.lineSeparator();
	    			for(int i=0; i < event.getRequestedStaff(); i++){
		    			Shift shift = new Shift();
		    			
		    			shift.setEvent(true);
		    			shift.setEventName(event.getName());
		    			shift.setEventId(event.getId());
		    			shift.setStartDate(event.getStartDate());
		    			shift.setStartTime(event.getStartTime());
		    			shift.setEndDate(event.getEndDate());
		    			shift.setEndTime(event.getEndTime());
		    			shift.setStartWeek(shift.getStartWeek());
		    			shift.setStartMonth(Integer.parseInt(month));
		    			shift.setStartYear(Integer.parseInt(splitDate[0]));
		    			shifts.add(shift);
	    			}
	    		}
	    	}
    	}
    	else{
    		shiftResponse+="0"+ System.lineSeparator();
    	}
    	shiftCrud.save(shifts);
    	shiftResponse+="~End Events~" + System.lineSeparator();
    	//TODO Set a flag saying shifts have been made for this event
    	return selectedMonth+shiftResponse;
    }

    public String generateRequestedShifts(String selectedMonth) throws CorruptDataException {
    	String singleResponse = "";
    	String recurringResponse = "";
    	ScheduleStatus status = scheduleStatusCrud.findOne(selectedMonth);
    	scheduleStatusRepository.deleteByMonth(selectedMonth);
    	
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(selectedMonth);
    	}
    	
    	if(!status.isGenerated()) {
    		status.setGenerated(true);
        	scheduleStatusCrud.save(status);
        	
    		singleResponse = generateRequestedSingleShifts(selectedMonth);
        	recurringResponse = generateRequestedRecurringShifts(selectedMonth);
    	}
    	
    	return singleResponse+recurringResponse;
    }
    
    public String generateRequestedSingleShifts(String selectedMonth) throws CorruptDataException {
    	Iterable<ShiftRequest> requests = shiftRequestRepository.findAll();
    	System.out.println("generating requested single shifts");
    	ArrayList<Shift> shifts =new ArrayList<Shift>();  
    	if(null!=requests && null!=selectedMonth){
    		System.out.println(((ArrayList<ShiftRequest>)requests).size() + " single shift requests to consider for month "+selectedMonth);
	    	for (ShiftRequest request : requests) {
	    		//if(!request.isRequested()){
	    			String startDate = null;
		    		String month = null;
		    		String year = null;
		    		startDate = request.getStartDate();
		    		if(null!=startDate){
			    		String[] splitDate = startDate.split("-");
			    		if(splitDate.length>1){
			    			month = splitDate[1];
			    			year=splitDate[0];
			    		}
			    		System.out.println("Checking if request is for this month" + year + " " + month + " " + request.toString());
			    		if(Integer.parseInt(selectedMonth)==Integer.parseInt(month) ){//&& year.contains("2019")
			    			System.out.println("Request confirmed to be for this month");
			    			Shift shift = new Shift();
			    			
			    			shift.setEvent(false);
			    			shift.setClientName(request.getClientName());
			    			shift.setClientId(request.getClientId());
			    			shift.setRequestedStaffId(request.getStaffId());
			    			shift.setRequestedStaffName(request.getStaffName());
			    			shift.setStartDate(request.getStartDate());
			    			shift.setStartTime(request.getStartTime());
			    			shift.setEndDate(request.getEndDate());
			    			shift.setEndTime(request.getEndTime());
			    			shift.setStartWeek(shift.getStartWeek());
			    			shift.setStartMonth(Integer.parseInt(month));
			    			shift.setStartYear(Integer.parseInt(splitDate[0]));
			    			String creationReason = request.getClientName() + " requested a shift on ";
			    			creationReason+= request.getStartDate() + " from "+request.getStartTime() + " to " +request.getEndTime();
			    			shift.setCreationReason(creationReason);
			    			shifts.add(shift);
			    			
//			    			request.setRequested(true);
//			    			shiftRequestCrud.save(request);
			    		}
		    		}
	    		//}
	    	}
    	}
    	shiftCrud.save(shifts);
    	return "Generated " + shifts.size() + " from single shift requests";
    }
    
    public String generateRequestedRecurringShifts(String selectedMonth) throws CorruptDataException {
    	Iterable<RecurringShiftNeed> requests = recurringShiftNeedRepository.findAll();
    	ArrayList<Shift> shifts  =new ArrayList<Shift>();  
    	if(null!=requests && null!=selectedMonth){
	    	for (RecurringShiftNeed request : requests) {
	    		logger.error("recurring shift requested for " + request.getClientName() + " on " + request.getStartDay() + ":" + request.getStartTime());
	    		//TODO check if a shift has been generated for this request
	    		//TODO check if a shift should be generated for this request
	    		int startDay = Util.getDayInt(request.getStartDay());
	    		int endDay = Util.getDayInt(request.getEndDay());
	    		boolean overnight = startDay!=endDay;

	    		LocalDate currentDay = LocalDate.of(2019, Integer.parseInt(selectedMonth), 01);
	    		DayOfWeek monthStartDay = currentDay.getDayOfWeek();
	    		
	            TemporalAdjuster startAdj = TemporalAdjusters.next(DayOfWeek.of(startDay));
	            TemporalAdjuster endAdj = TemporalAdjusters.next(DayOfWeek.of(endDay));
	            
	            ArrayList<LocalDate> startDates = new ArrayList<LocalDate>();
	            ArrayList<LocalDate> endDates = new ArrayList<LocalDate>();
	            if(monthStartDay ==DayOfWeek.of(startDay)){
	            	startDates.add(currentDay);
	            	if(!overnight){
		            	endDates.add(currentDay);	            		
	            	}
	            	else{
	            		endDates.add(currentDay.with(endAdj));
	            	}
	            }
	            for(int i = 0; i< 5; i++){
	            	currentDay = currentDay.with(startAdj);
	            	if(currentDay.getMonth().getValue()==Integer.parseInt(selectedMonth)){
	            		startDates.add(currentDay);
	            		if(!overnight){
			            	endDates.add(currentDay);	            		
		            	}
		            	else{
		            		endDates.add(currentDay.with(endAdj));
		            	}
	            	}
	            }

	            logger.error(startDates.size() + " start dates for recurring shift");
	            for(int i = 0; i < startDates.size();i++){
	    			Shift shift = new Shift();
	    			
	    			shift.setRecurring(true);
	    			shift.setClientName(request.getClientName());
	    			shift.setClientId(request.getClientId());
	    			shift.setRequestedStaffId(request.getStaffId());
	    			shift.setRequestedStaffName(request.getStaffName());
	    			shift.setStartDate(startDates.get(i).toString());
	    			shift.setStartTime(request.getStartTime());
	    			shift.setEndDate(endDates.get(i).toString());
	    			shift.setEndTime(request.getEndTime());
	    			shift.setStartWeek(shift.getStartWeek());
	    			shift.setStartMonth(Integer.parseInt(selectedMonth));
	    			shift.setStartYear(2019);//TODO update this to get the real value
	    			String creationReason = request.getClientName() + " requested a reccurring shift every ";
	    			creationReason+= request.getStartDay() + " from "+request.getStartTime() + " to " +request.getEndTime();
	    			creationReason+= ". This is the #" + (i+1) + " shift for this request for month "+selectedMonth;
	    			shift.setCreationReason(creationReason);
	    			shifts.add(shift);
	            }
	    	}
    	}
    	shiftCrud.save(shifts);
    	
    	return " Generated " +shifts.size() + " from recurring shift requests";
    }
}