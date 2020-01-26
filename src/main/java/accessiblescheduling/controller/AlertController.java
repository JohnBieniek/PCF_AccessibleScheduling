package accessiblescheduling.controller;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.security.sasl.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import accessiblescheduling.constants.Constants;
import accessiblescheduling.manager.AccessibleSecurityManager;
import accessiblescheduling.manager.AlertManager;

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
