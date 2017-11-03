package org.cloudfoundry.samples.music.managers;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;

import accessiblesolutions.accessiblescheduling.domain.Event;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.domain.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import accessiblesolutions.accessiblescheduling.domain.RecurringShiftNeed;
import accessiblesolutions.accessiblescheduling.domain.ShiftRequest;

@Component
public class ShiftGenerationManager {
	private static final Logger logger = LoggerFactory.getLogger(ShiftGenerationManager.class);
    private CrudRepository<Event, String> eventRepository;//TODO switch to autowired
    private CrudRepository<Shift, String> shiftCrud;
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
    
    public String generateEventShifts(String selectedMonth) {
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

    public String generateRequestedShifts(String selectedMonth) {
    	String singleResponse = generateRequestedSingleShifts(selectedMonth);
    	String recurringResponse = generateRequestedRecurringShifts(selectedMonth);
    	
    	return singleResponse+recurringResponse;
    }
    
    public String generateRequestedSingleShifts(String selectedMonth) {
    	//Iterable<Client> clients = clientRepository.findAll();
    	Iterable<ShiftRequest> requests = shiftRequestRepository.findAll();
    	ArrayList<Shift> shifts =new ArrayList<Shift>();  
    	if(null!=requests && null!=selectedMonth){
	    	for (ShiftRequest request : requests) {
	    		//TODO check if a shift has been generated for this request
	    		//TODO check if a shift should be generated for this request
	    		String startDate = null;
	    		String month = null;
	    		startDate = request.getStartDate();
	    		if(null!=startDate){
		    		String[] splitDate = startDate.split("-");
		    		if(splitDate.length>1){
		    			month = splitDate[1];
		    		}
		    		if(Integer.parseInt(selectedMonth)==Integer.parseInt(month)){
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
		    		}
	    		}
	    	}
    	}
    	shiftCrud.save(shifts);
    	//TODO Set a flag saying shifts have been made for this event
    	return selectedMonth + " from single shift requests";
    }
    
    public String generateRequestedRecurringShifts(String selectedMonth) {
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

	    		LocalDate currentDay = LocalDate.of(2017, Integer.parseInt(selectedMonth), 01);
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
	    			shift.setStartYear(2017);//TODO update this to get the real value
	    			shifts.add(shift);
	            }
	    	}
    	}
    	shiftCrud.save(shifts);
    	 
    	return selectedMonth + " from recurring shift requests";
    }
}