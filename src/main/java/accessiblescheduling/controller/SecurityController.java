package accessiblescheduling.controller;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.security.sasl.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accessiblescheduling.constants.Constants;
import accessiblescheduling.manager.AccessibleSecurityManager;
import accessiblescheduling.manager.AlertManager;
import accessiblescheduling.to.User;

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
	
	@RequestMapping(value = "/linkEmployee",method = RequestMethod.GET)
    public String linkEmployee(@RequestParam String idtoken, @RequestParam String email, @RequestParam String employeeId) throws AuthenticationException {
		return manager.linkEmployee(email,employeeId).toString();
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
