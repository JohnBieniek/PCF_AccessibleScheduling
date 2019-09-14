package org.cloudfoundry.samples.music.web;
import java.util.ArrayList;

import javax.security.sasl.AuthenticationException;

import org.cloudfoundry.samples.music.managers.AccessibleSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.domain.AccessRequest;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.domain.User;

@RestController
@RequestMapping(value = "/auth")
public class SecurityController {
	@Autowired
	AccessibleSecurityManager manager;
	
	@RequestMapping(value = "/tokensignin",method = RequestMethod.POST)
    public User tokenSignIn(@RequestParam String idtoken)throws Exception {
		User user = manager.getUserDetails(idtoken);
    	return user;
    }
	
	@RequestMapping(value = "/signup",method = RequestMethod.GET)
    public String signUp2(@RequestParam String idtoken, @RequestParam String name) throws AuthenticationException {
		System.out.println("id:"+idtoken);
		System.out.println("name"+name);
		return manager.signUp(idtoken,name).toString();
    }
	
	@RequestMapping(value = "/approve",method = RequestMethod.GET)
    public ArrayList<AccessRequest> approve(@RequestParam String userId,String employeeId) {
		return manager.approve(userId,employeeId);
    }
	
	@RequestMapping(value = "/deny",method = RequestMethod.GET)
    public ArrayList<AccessRequest> deny(@RequestParam String userId) {
		return manager.deny(userId);
    }
	
	@RequestMapping(value = "/signedup",method = RequestMethod.GET)
    public Boolean signUp2(@RequestParam String idtoken) throws AuthenticationException {
		System.out.println("id:"+idtoken);
		return manager.signedUp(idtoken);
    }
}
