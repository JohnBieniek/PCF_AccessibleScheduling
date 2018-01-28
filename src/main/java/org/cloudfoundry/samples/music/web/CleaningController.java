package org.cloudfoundry.samples.music.web;

import java.util.ArrayList;

import org.cloudfoundry.samples.music.managers.CleaningManager;
import org.cloudfoundry.samples.music.managers.CustomDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;
import accessiblesolutions.accessiblescheduling.domain.RecurringShiftNeed;
import accessiblesolutions.accessiblescheduling.domain.ShiftRequest;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.ScheduleNotification;
import accessiblesolutions.accessiblescheduling.to.ShiftNotification;

@RestController
@RequestMapping(value = "/cleaning")
public class CleaningController {
    @Autowired
    private CustomDataManager customDataManager;
    @Autowired
    private CleaningManager cleaningManager;
    public CleaningController() {
    }
    @RequestMapping(value = "/scheduleNotifications", method = RequestMethod.GET)
    public ArrayList<ScheduleNotification> getScheduleNotifications() throws CorruptDataException, ProccessingException{
        return cleaningManager.getScheduleNotifications();
    }
    @RequestMapping(value = "/shiftNotifications", method = RequestMethod.GET)
    public ArrayList<ShiftNotification> getShiftNotifications() throws CorruptDataException, ProccessingException{
        return cleaningManager.getShiftNotifications();
    }
    @RequestMapping(value = "/orphanedCustomFieldData", method = RequestMethod.GET)
    public ArrayList<CustomFieldData> getOrphanedCustomFieldData() {
        return customDataManager.getOrphanedCustomFieldData();
    }
    @RequestMapping(value = "/orphanedShiftRequests", method = RequestMethod.GET)
    public ArrayList<ShiftRequest> getOrphanedShiftRequests() {
        return cleaningManager.getOrphanedShiftRequests();
    }
    @RequestMapping(value = "/orphanedRecurringShiftRequests", method = RequestMethod.GET)
    public ArrayList<RecurringShiftNeed> getOrphanedRecurringShiftRequests() {
        return cleaningManager.getOrphanedRecurringShiftRequests();
    }
    @RequestMapping(value = "/removeOrphanedCustomFieldData", method = RequestMethod.GET)
    public void removeOrphanedCustomFieldData() {
        customDataManager.removeOrphanedCustomFieldData();
    }
    @RequestMapping(value = "/removeOrphanedShiftRequests", method = RequestMethod.GET)
    public void removeOrphanedShiftRequests() {
        cleaningManager.removeOrphanedSingleShiftRequests();
    }
    @RequestMapping(value = "/removeOrphanedRecurringShiftRequests", method = RequestMethod.GET)
    public void removeOrphanedRecurringShiftRequests() {
       cleaningManager.removeOrphanedRecurringShiftRequests();
    }
    @RequestMapping(value = "/orphanedCustomFieldData", method = RequestMethod.DELETE)
    public void deleteOrphanedCustomFieldData() {
        customDataManager.removeOrphanedCustomFieldData();
    }
    @RequestMapping(value = "/orphanedShiftRequests", method = RequestMethod.DELETE)
    public void deleteOrphanedShiftRequests() {
        cleaningManager.removeOrphanedSingleShiftRequests();
    }
    @RequestMapping(value = "/orphanedRecurringShiftRequests", method = RequestMethod.DELETE)
    public void deleteOrphanedRecurringShiftRequests() {
       cleaningManager.removeOrphanedRecurringShiftRequests();
    }
}