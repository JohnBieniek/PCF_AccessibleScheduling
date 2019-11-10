package org.cloudfoundry.samples.music.web;

import java.util.ArrayList;

import org.cloudfoundry.samples.music.managers.AccessibleSecurityManager;
import org.cloudfoundry.samples.music.managers.CleaningManager;
import org.cloudfoundry.samples.music.managers.CustomDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.AlternateWeekendsOffNotification;
import accessiblesolutions.accessiblescheduling.to.OverWeeklyDaysNotification;
import accessiblesolutions.accessiblescheduling.to.ScheduleNotification;
import accessiblesolutions.accessiblescheduling.to.ShiftNotification;

@RestController
@RequestMapping(value = "/cleaning")
public class CleaningController {
	@Autowired 
	AccessibleSecurityManager securityManager;
	
    @Autowired
    private CustomDataManager customDataManager;
    
    @Autowired
    private CleaningManager cleaningManager;
    
    public CleaningController() {
    }
    
    @RequestMapping(value = "/fixShiftNotification", method = RequestMethod.GET)
    public String fixShiftNotification(@RequestHeader(value="Authorization", required=false) String idToken, ShiftNotification shiftNotification) throws CorruptDataException, ProccessingException{
        return cleaningManager.fixShiftNotification(shiftNotification);
    }
    
    @RequestMapping(value = "/alternateWeeekendsNotifications", method = RequestMethod.GET)
    public ArrayList<AlternateWeekendsOffNotification> getAlternateWeekendsOffNotifications(@RequestHeader(value="Authorization", required=false) String idToken) throws CorruptDataException, ProccessingException{
        return cleaningManager.getAlternateWeekendsOffNotifications();
    }
    
    @RequestMapping(value = "/dailyMaxNotifications", method = RequestMethod.GET)
    public ArrayList<ShiftNotification> getDailyMaxNotifications(@RequestHeader(value="Authorization", required=false) String idToken) throws CorruptDataException, ProccessingException{
        return cleaningManager.getOverDailyShiftNotifications();
    }
    
    @RequestMapping(value = "/weeklyMaxNotifications", method = RequestMethod.GET)
    public ArrayList<OverWeeklyDaysNotification> getOverMaxDaysWeeklyNotifications(@RequestHeader(value="Authorization", required=false) String idToken) throws CorruptDataException, ProccessingException{
        return cleaningManager.getOverWeeklyDaysNotifications();
    }
    
    @RequestMapping(value = "/shiftNotifications", method = RequestMethod.GET)
    public ArrayList<ShiftNotification> getShiftNotifications(@RequestHeader(value="Authorization", required=false) String idToken) throws CorruptDataException, ProccessingException{
        return cleaningManager.getShiftNotifications();
    }
    
    @RequestMapping(value = "/orphanedCustomFieldData", method = RequestMethod.GET)
    public ArrayList<CustomFieldData> getOrphanedCustomFieldData(@RequestHeader(value="Authorization", required=false) String idToken) {
        return customDataManager.getOrphanedCustomFieldData();
    }

    @RequestMapping(value = "/removeOrphanedCustomFieldData", method = RequestMethod.GET)
    public void removeOrphanedCustomFieldData(@RequestHeader(value="Authorization", required=false) String idToken) {
        customDataManager.removeOrphanedCustomFieldData();
    }

    @RequestMapping(value = "/orphanedCustomFieldData", method = RequestMethod.DELETE)
    public void deleteOrphanedCustomFieldData(@RequestHeader(value="Authorization", required=false) String idToken) {
        customDataManager.removeOrphanedCustomFieldData();
    }
}