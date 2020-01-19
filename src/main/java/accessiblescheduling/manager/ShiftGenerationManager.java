package accessiblescheduling.manager;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import accessiblescheduling.constants.Constants;
import accessiblescheduling.domain.ClientRequest;
import accessiblescheduling.domain.ScheduleStatus;
import accessiblescheduling.domain.Shift;
import accessiblescheduling.exception.CorruptDataException;
import accessiblescheduling.exception.ProccessingException;
import accessiblescheduling.repositories.mongodb.ScheduleStatusRepository;
import accessiblescheduling.util.Util;

@Component
public class ShiftGenerationManager {
	@Autowired
    private CrudRepository<Shift, String> shiftCrud;
    
    @Autowired
    private CrudRepository<ClientRequest, String> requestRepository;
    
    @Autowired
    private CrudRepository<ScheduleStatus, String> scheduleStatusCrud;
    
    @Autowired
    private ScheduleStatusRepository scheduleStatusRepository;    
    
    @Autowired
    private UpdateInfoManager updateInfoManager;
    
    @Autowired
    ShiftAssignmentManager assignmentManager;
    
    public ShiftGenerationManager() {
    }
    
    public String generateShifts(String selectedMonth,String selectedYear){
    	String response = "Generated ";
    	int shiftsGenerated = 0;
    	
    	ScheduleStatus status = assignmentManager.scheduleStatus(selectedMonth);
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(selectedMonth);
    	}
    	
    	if(!status.isGenerating() && !status.isGenerated() && !status.isAssigning() && !status.isAssigned() && !status.isStopped() && !status.isErrored()) {
	    	scheduleStatusRepository.deleteByMonthAndYear(selectedMonth,selectedYear);
	    	status.setGenerating(true);
	    	scheduleStatusCrud.save(status);
	        updateInfoManager.set(Constants.STATUS);

	    	Iterable<ClientRequest> requests = requestRepository.findAll();
	    	System.out.println("generating shifts for "+selectedMonth);
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
	    	status.setGenerating(false);
	    	status.setGenerated(true);
	    	scheduleStatusRepository.deleteByMonth(selectedMonth);
	    	scheduleStatusCrud.save(status);
	        updateInfoManager.set(Constants.STATUS);
    	}
    	
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
				System.out.println("considering adding shift for"+request.getStartsLocalDateTime());
				System.out.println("requested month:"+Integer.parseInt(month)+Integer.parseInt(selectedMonth));
    			try {
    				if(Integer.parseInt(month)==Integer.parseInt(selectedMonth)){
    					System.out.println("adding shift for"+request.getStartsLocalDateTime());
						Shift shift = getShiftForRequestAtTime(request,request.getStartsLocalDateTime());
						shifts.add(shift);
    				}
	    		} catch (CorruptDataException e) {
					System.out.println("ERROR: Corrupt time for shift provided"+e.getMessage());
				}
    		}
			else {
				System.out.println("considering adding recurring shift for"+request.getStartsLocalDateTime());
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
						System.out.println("generating shifts for request:"+request.toString()+" at time:"+time);
						System.out.println("included:"+included+" month:"+Integer.parseInt(month)+"time.getMonthValue()"+time.getMonthValue());
						if(included && Integer.parseInt(selectedMonth)==time.getMonthValue()) {
							System.out.println("getting shift for :"+ time+included);
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
    	System.out.println("about to call getweek of date while generating shift for request at time:"+time.toString());
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
		    					for(int index=0;index<7;index++){
		    						System.out.println("trying ot find out when to generate a shift for:"+request.toString()+" looking at:"+timeCursor.toString());

		    						timeCursor=timeCursor.plusWeeks(1);

		    						if(Util.getWeekOfDate(Util.getDateFromLocalDateTime(initialTime))==
		    								Util.getWeekOfDate(Util.getDateFromLocalDateTime(timeCursor))){
		    							break;
		    						}
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
	    		while(timeCursor.minusDays(6).getMonthValue()<=selectedMonth && timeCursor.minusDays(6).getYear()<=selectedYear) {
	    			System.out.println("Finding times for week of :"+timeCursor.toString());
	    			for(int index = 0; index<7;index++) {
	    				boolean working = request.getDays()[index];
	    				LocalDateTime selectedDay = timeCursor.minusDays(6-index);
	    				System.out.println("making shifts for day "+selectedDay.getDayOfWeek().toString() + " "+working +" monthValue:"+selectedDay.getMonthValue()+" selectedMonth"+selectedMonth);
	    				if(working && selectedDay.getMonthValue()==selectedMonth && (selectedDay.isAfter(request.getStartsLocalDateTime()) || selectedDay.isEqual(request.getStartsLocalDateTime()))) {
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
}