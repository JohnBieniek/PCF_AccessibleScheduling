package org.cloudfoundry.samples.music.web;
import java.util.ArrayList;

import org.cloudfoundry.samples.music.managers.AlertManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.domain.AccessRequest;

@RestController
@RequestMapping(value = "/alerts")
public class AlertController {
	@Autowired
	AlertManager manager;
	
	@RequestMapping(value = "/findAll",method = RequestMethod.GET)
    public ArrayList<AccessRequest> findAllAlerts() {
		return manager.getAlerts();
    }
}
