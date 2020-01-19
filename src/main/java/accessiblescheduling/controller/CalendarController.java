package accessiblescheduling.controller;

import javax.security.sasl.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accessiblescheduling.constants.Constants;
import accessiblescheduling.exception.ProccessingException;
import accessiblescheduling.util.Util;
import accessiblescheduling.manager.AccessibleSecurityManager;

@RestController
@RequestMapping(value = "/calendar")
public class CalendarController {
	@Autowired 
	AccessibleSecurityManager securityManager;
	
    public CalendarController() {}

    @RequestMapping(value = "/getDatesForMonth", method = RequestMethod.GET)
    public String getShiftsForOfMonth(@RequestHeader(value="Authorization", required=false) String idToken,
    									@RequestParam("year") String year, @RequestParam("month") String month) throws ProccessingException, AuthenticationException {
    	securityManager.authorize(idToken, Constants.USER);
        return Util.getDatesForMonth(Integer.parseInt(year),Integer.parseInt(month)).toString();
    }
}