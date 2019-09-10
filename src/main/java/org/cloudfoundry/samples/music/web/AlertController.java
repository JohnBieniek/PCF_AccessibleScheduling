package org.cloudfoundry.samples.music.web;
import javax.security.sasl.AuthenticationException;

import org.cloudfoundry.samples.music.managers.AccessibleSecurityManager;
import org.cloudfoundry.samples.music.managers.AlertManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.constants.Constants;

@RestController
@RequestMapping(value = "/alerts")
public class AlertController {
	@Autowired
	AlertManager manager;
	
	@Autowired 
	AccessibleSecurityManager securityManager;
	
	@RequestMapping(value = "/findAll",method = RequestMethod.GET)
    public ResponseEntity<?> findAllAlerts(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException {
		securityManager.authorize(idToken, Constants.ADMIN);
		
		return new ResponseEntity(manager.getAlerts(),HttpStatus.OK);
    }
}
