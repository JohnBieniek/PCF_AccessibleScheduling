package org.cloudfoundry.samples.music.web;//Ignore complaints

import java.util.ArrayList;

import org.cloudfoundry.samples.music.managers.EmployeeShiftMapManager;
import org.cloudfoundry.samples.music.managers.ScheduleManager;
import org.cloudfoundry.samples.music.managers.ShiftAssignmentManager;
import org.cloudfoundry.samples.music.managers.ShiftManager;
import org.cloudfoundry.samples.music.repositories.mongodb.ScheduleStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.domain.ScheduleStatus;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.ScheduleOptions;

@RestController
@RequestMapping(value = "/schedule")
public class ScheduleController {
    private ScheduleManager manager;
    
    @Autowired
    ShiftManager shiftManager;
    
    @Autowired
    ShiftAssignmentManager assignmentManager;
    
    @Autowired
    EmployeeShiftMapManager employeeShiftMapManager;
    
    @Autowired
    private CrudRepository<ScheduleStatus, String> scheduleStatusCrud;
    
    @Autowired
    ScheduleManager scheduleManager;
    
    @Autowired
    private ScheduleStatusRepository scheduleStatusRepository;   
    
    @Autowired
    public ScheduleController(ScheduleManager manager) {
        this.manager=manager;
    }

    @RequestMapping(value = "/byMonth", method = RequestMethod.DELETE)
    public ArrayList<Shift> deleteByMonth(@RequestParam("month") String  month) {
        ScheduleStatus status = new ScheduleStatus();
        
    	status.setMonth(month);
    	    	
    	scheduleStatusRepository.deleteByMonth(month);
    	scheduleStatusCrud.save(status);
    	
        return shiftManager.deleteShiftsForMonth(Integer.parseInt(month));
    }
    
    @RequestMapping(value = "/durationOfWeeksShifts", method = RequestMethod.GET)
    public float durationOfWeeksShifts(@RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
        return shiftManager.getDurationOfShiftsStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
    }
    
//    @RequestMapping(value = "/staffWeekdayShift", method = RequestMethod.GET)
//    public String staffWeekdayShift(@RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
//        return assignmentManager.scheduleWeekdayShiftStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
//    }
//    
//    @RequestMapping(value = "/staffWeekdayShifts", method = RequestMethod.GET)
//    public String staffWeekdayShifts(@RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
//        return assignmentManager.scheduleWeekdayShiftsStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
//    }
//    
//    @RequestMapping(value = "/staffWeekendShift", method = RequestMethod.GET)
//    public String staffWeekendShift(@RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
//        return assignmentManager.scheduleWeekendShiftStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
//    }
//    
//    @RequestMapping(value = "/staffWeekendShifts", method = RequestMethod.GET)
//    public String staffWeekendShifts(@RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
//        return assignmentManager.scheduleWeekendShiftsStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
//    }
//    
//    @RequestMapping(value = "/staffWeeksShifts", method = RequestMethod.GET)
//    public String staffWeeksShifts(@RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
//        return assignmentManager.scheduleShiftsStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
//    }
    
    @RequestMapping(value = "/staffPreassignedShifts", method = RequestMethod.GET)
    public String staffPreassignedShifts(@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
    	ScheduleOptions options = new ScheduleOptions();
    	options.setMonth(month);
    	options.setYear(year);
    	return assignmentManager.staffPreassignedShifts(options);
    }
    
    @RequestMapping(value = "/staffShiftsSafely", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> staffShiftsSafely(@RequestParam("month") String month
    													,@RequestParam("year") String year
    													,@RequestParam("allowOvertime") boolean allowOvertime
    													,@RequestParam("allowInactive") boolean allowInactive
    													,@RequestParam("allowUnavailable") boolean allowUnavailable
    													,@RequestParam("prioritizeSecondShift") boolean prioritizeSecondShift
    													,@RequestParam("dailyMax") boolean dailyMax
    													,@RequestParam("weeklyMax") boolean weeklyMax) {
    	ScheduleStatus status = scheduleStatusCrud.findOne(month);
    	scheduleStatusRepository.deleteByMonth(month);
    	
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(month);
    	}
    	
    	if(status.isGenerated()) {
    		status.setAssigning(true);
        	scheduleStatusCrud.save(status);
    	}
    	
    	ScheduleOptions options = new ScheduleOptions(month, year, allowOvertime, allowInactive,allowUnavailable, 
    													prioritizeSecondShift, dailyMax,weeklyMax);
    	
    	try {
			assignmentManager.scheduleShifts(options);
		} catch (ProccessingException | CorruptDataException e) {
			status = scheduleStatusCrud.findOne(month);
	    	scheduleStatusRepository.deleteByMonth(month);
	    	
	    	if(null==status) {
	    		status= new ScheduleStatus();
	    		status.setMonth(month);
	    	}
	    	
    		status.setAssigning(false);
    		status.setErrored(true);
        	scheduleStatusCrud.save(status);
		}
    	return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/statusList", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> scheduleStatusList() {
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/generateStatusList", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> generateStatusList() {
    	scheduleManager.generateStatusList();
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/stopAssignment", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> stopAssignment(@RequestParam("month") String month) throws CorruptDataException {
    	ScheduleStatus status = scheduleStatusCrud.findOne(month);
    	scheduleStatusRepository.deleteByMonth(month);
    	
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(month);
    	}
    	
    	status.setGenerated(true);
		status.setAssigning(false);
		status.setStopped(true);
    	scheduleStatusCrud.save(status);
    	
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/generateShifts", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> generateShifts(@RequestParam("month") String month) throws CorruptDataException {
        manager.generateShifts(month);
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/generateSingleShifts", method = RequestMethod.GET)
    public String generateSingleShifts(@RequestParam("month") String month) throws CorruptDataException {
        return manager.generateSingleShifts(month);
    }
    
//    @RequestMapping(value = "/staffShifts", method = RequestMethod.GET)
//    public String staffShifts(@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
//        ScheduleOptions options = new ScheduleOptions();
//        options.setMonth(month);
//        options.setYear(year);
//    	return manager.staffShifts(options);
//    }
    
    @RequestMapping(value = "/getShiftsForMonth", method = RequestMethod.GET)
    public String getShiftsForOfMonth(@RequestParam("month") String month) {
        return shiftManager.getShiftsForMonth(Integer.parseInt(month)).toString();
    }
    
    @RequestMapping(value = "/getShiftsPerEmployeeForMonth", method = RequestMethod.GET)
    public String getShiftsPerEmployeeForOfMonth(@RequestParam("month") String month) throws ProccessingException {
        return employeeShiftMapManager.getAssignedShiftsPerEmployeeForMonth(Integer.parseInt(month)).toString();
    }
    
    //TODO kill it
    @RequestMapping(value = "/getAssignedShiftsForEmployeeForMonth", method = RequestMethod.GET)
    public ArrayList<Shift> getAssignedShiftsForEmployeeForMonth(@RequestParam("employee") String employee) throws ProccessingException  {
    	return employeeShiftMapManager.getAssignedShiftsPerEmployeeForMonth(4).get(employee);
    	//return manager.getAssignedShiftsForEmployeeForMonth("584890232b3acf554ef8d88f", Integer.parseInt(month));
    }
    
  //TODO kill it
    @RequestMapping(value = "/getAssignedShiftsForEmployeeForMonth2", method = RequestMethod.GET)
    public String getAssignedShiftsForEmployeeForMonth2(@RequestParam("month") String month) throws ProccessingException {
    	return employeeShiftMapManager.getAssignedShiftsPerEmployeeForMonth(Integer.parseInt(month)).keySet().toArray().toString();
    	//return manager.getAssignedShiftsForEmployeeForMonth("584890232b3acf554ef8d88f", Integer.parseInt(month));
    }
}