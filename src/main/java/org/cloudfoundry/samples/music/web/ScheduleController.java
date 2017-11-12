package org.cloudfoundry.samples.music.web;//Ignore complaints

import java.util.ArrayList;

import accessiblesolutions.accessiblescheduling.domain.Shift;
import org.cloudfoundry.samples.music.managers.EmployeeShiftMapManager;
import org.cloudfoundry.samples.music.managers.ScheduleManager;
import org.cloudfoundry.samples.music.managers.ShiftManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;

@RestController
@RequestMapping(value = "/schedule")
public class ScheduleController {
    private ScheduleManager manager;
    
    @Autowired
    ShiftManager shiftManager;
    
    @Autowired
    EmployeeShiftMapManager employeeShiftMapManager;
    
    @Autowired
    public ScheduleController(ScheduleManager manager) {
        this.manager=manager;
    }

    @RequestMapping(value = "/generateShifts", method = RequestMethod.GET)
    public String generateShifts(@RequestParam("month") String month) throws CorruptDataException {
        return manager.generateShifts(month);
    }
    
    @RequestMapping(value = "/staffShifts", method = RequestMethod.GET)
    public String staffShifts(@RequestParam("month") String month) throws CorruptDataException, ProccessingException {
        return manager.staffShifts(month);
    }
    
    @RequestMapping(value = "/getShiftsForMonth", method = RequestMethod.GET)
    public String getShiftsForOfMonth(@RequestParam("month") String month) {
        return shiftManager.getShiftsForMonth(Integer.parseInt(month)).toString();
    }
    
    @RequestMapping(value = "/getShiftsPerEmployeeForMonth", method = RequestMethod.GET)
    public String getShiftsPerEmployeeForOfMonth(@RequestParam("month") String month) {
        return employeeShiftMapManager.getAssignedShiftsPerEmployeeForMonth(Integer.parseInt(month)).toString();
    }
    
    @RequestMapping(value = "/getAssignedShiftsForEmployeeForMonth", method = RequestMethod.GET)
    public ArrayList<Shift> getAssignedShiftsForEmployeeForMonth(@RequestParam("employee") String employee) {
    	return employeeShiftMapManager.getAssignedShiftsPerEmployeeForMonth(4).get(employee);
    	//return manager.getAssignedShiftsForEmployeeForMonth("584890232b3acf554ef8d88f", Integer.parseInt(month));
    }
    
    @RequestMapping(value = "/getAssignedShiftsForEmployeeForMonth2", method = RequestMethod.GET)
    public String getAssignedShiftsForEmployeeForMonth2(@RequestParam("month") String month) {
    	return employeeShiftMapManager.getAssignedShiftsPerEmployeeForMonth(Integer.parseInt(month)).keySet().toArray().toString();
    	//return manager.getAssignedShiftsForEmployeeForMonth("584890232b3acf554ef8d88f", Integer.parseInt(month));
    }
}