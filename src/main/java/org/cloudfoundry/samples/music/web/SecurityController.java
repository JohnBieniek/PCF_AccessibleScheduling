package org.cloudfoundry.samples.music.web;
import javax.security.sasl.AuthenticationException;

import org.cloudfoundry.samples.music.managers.AccessibleSecurityManager;
import org.cloudfoundry.samples.music.managers.AlertManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.constants.Constants;
import accessiblesolutions.accessiblescheduling.domain.User;

@RestController
@RequestMapping(value = "/auth")
public class SecurityController {
	@Autowired
	AccessibleSecurityManager manager;
	
	@Autowired
	AlertManager alertManager;
	
	@RequestMapping(value = "/tokensignin",method = RequestMethod.GET)
    public User tokenSignIn(@RequestHeader(value="Authorization", required=false) String idToken)throws Exception {
		User user = manager.getUserDetails(idToken);
    	return user;
    }
	
	@RequestMapping(value = "/signup",method = RequestMethod.GET)
    public String signUp2(@RequestParam String idtoken, @RequestParam String name) throws AuthenticationException {
		return manager.signUp(idtoken,name).toString();
    }
	
	@RequestMapping(value = "/approve",method = RequestMethod.GET)
    public String approve(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String userId,String employeeId) throws AuthenticationException {
		manager.authorize(idToken, Constants.ADMIN);
		
		return manager.approve(userId,employeeId);
    }
	
	@RequestMapping(value = "/deny",method = RequestMethod.GET)
    public String deny(@RequestHeader(value="Authorization", required=false) String idToken,@RequestParam String userId) throws AuthenticationException {
		manager.authorize(idToken, Constants.ADMIN);
		
		return manager.deny(userId);
    }
	
	@RequestMapping(value = "/signedup",method = RequestMethod.GET)
    public Boolean signUp2(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException {
		return manager.signedUp(idToken);
    }
}
