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
    	
    	for(ClientRequest request: requests) {
    		try {
				shiftsGenerated+=generateShifts(request,selectedMonth,Integer.parseInt(selectedYear));
			} catch (CorruptDataException e) {
				e.printStackTrace();
			}
    	}
    	
    	return response+shiftsGenerated;
    }
    
    public int generateShifts(ClientRequest request,String selectedMonth, int selectedYear) throws CorruptDataException {
    	ArrayList<Shift> shifts = new ArrayList<Shift>();
    	String startDate = null;
		String month = null;
		String[] splitDate = null;
		
		startDate = request.getStartDate();
		
		if(startDate!=null) {
			splitDate = startDate.split("-");
			
			if(splitDate.length>1){
				month = splitDate[1];
			}
			
			if(!request.isRepeats()) {
    			try {
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for(LocalDateTime time:times) {
					shifts.add(getShiftForRequestAtTime(request,time));
				}
			}
    	}
		else {
			System.out.println("ERROR: Corrupt time for shift provided");
		}
    	
    	if(shifts.size()>0) {
    		shiftCrud.save(shifts);
    	}
    	
    	return shifts.size();
    }
    
    public Shift getShiftForRequestAtTime(ClientRequest request,LocalDateTime time) {
    	Shift shift = new Shift();
    	
    	return shift;
    }
    
    public ArrayList<LocalDateTime> getDatesForRequestDuringMonth(ClientRequest request, int selectedMonth, int selectedYear) throws CorruptDataException{
    	ArrayList<LocalDateTime> times = new ArrayList<LocalDateTime>();
    	LocalDateTime initialTime = request.getStartsLocalDateTime();
    	LocalDateTime timeCursor = initialTime;
    	int increment = Integer.parseInt(request.getRepeatsEvery());
    	
    	if(initialTime.getYear()<=selectedYear) {
	    	if(initialTime.getMonthValue()<selectedMonth) {
	    		while(timeCursor.getMonthValue()<selectedMonth) {
	    			if(request.getInterval().contains("day")) {
		    			timeCursor=timeCursor.plusDays(increment);
	    			}
	    			else if(request.getInterval().contains("week")) {
	    				
	    			}
	    			else if(request.getInterval().contains("month")) {
	    				if(request.getMonthInterval().contains("day")) {
	    					
	    				}
	    				else {//weeks
	    					
	    				}
	    			}
	    			else {
	    				if(request.getYearInterval().contains("day")) {
	    					
	    				}
	    				else {//weeks
	    					
	    				}
	    			}
	    			

	    		}
	    	}
	    	
	    	for(int index =1;index<32;index++) {
	    		
	    	}
    	}
    	
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