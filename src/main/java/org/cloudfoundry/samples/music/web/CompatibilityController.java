package org.cloudfoundry.samples.music.web;

import java.io.IOException;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

import org.cloudfoundry.samples.music.managers.AccessibleSecurityManager;
import org.cloudfoundry.samples.music.managers.CustomDataManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftCompatibilityManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftManager;
import org.cloudfoundry.samples.music.managers.ScheduleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonParseException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.constants.Constants;
import accessiblesolutions.accessiblescheduling.domain.CallAuth;
import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.CustomField;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.EmployeeShiftCompatibility;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.CompatibilityDTO;
import accessiblesolutions.accessiblescheduling.to.EmployeeUpdateTO;
import accessiblesolutions.accessiblescheduling.to.ScheduleOptions;
import accessiblesolutions.accessiblescheduling.to.UpdateTO;

@RestController
@RequestMapping(value = "/compatibility")
public class CompatibilityController {
	@Autowired 
	AccessibleSecurityManager securityManager;
	
	@Autowired
	EmployeeShiftManager employeeShiftManager;
	
	@Autowired
	ScheduleManager manager;
	
	@Autowired
	CustomDataManager customDataManager;
	
	@Autowired
	EmployeeShiftCompatibilityManager employeeShiftCompatibilityManager;
	
    public CompatibilityController() {}

    @RequestMapping(method = RequestMethod.POST, value= "/shifts")
    public @ResponseBody EmployeeUpdateTO getShiftsScheduled(@RequestHeader(value="Authorization", required=false) String idToken, HttpServletRequest request) throws CorruptDataException, AuthenticationException{
    	CallAuth auth = securityManager.authorize(idToken, Constants.USER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	if(!auth.getEmployeeId().equalsIgnoreCase(employee.getId())) {
    		if(!auth.isAdmin() && !auth.isManager()) {
    			throw new AuthenticationException();
    		}
    	}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	to.setNumericResponse(employeeShiftManager.getAssignedShiftsForEmployeeForWeekOfMonth(employee.getId(),shift.getStartWeek(),shift.getStartMonth(),shift.getStartYear()).size());

    	return to;
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/hours")
    public @ResponseBody EmployeeUpdateTO getHoursScheduled(@RequestHeader(value="Authorization", required=false) String idToken,
    		HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	CallAuth auth = securityManager.authorize(idToken, Constants.USER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	if(!auth.getEmployeeId().equalsIgnoreCase(employee.getId())) {
    		if(!auth.isAdmin() && !auth.isManager()) {
    			throw new AuthenticationException();
    		}
    	}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	to.setNumericResponse(employeeShiftManager.getHoursScheduledWeekOfMonth(employee,shift.getStartWeek(),shift.getStartMonth(),shift.getStartYear()));

    	return to;
    }
    @RequestMapping(method = RequestMethod.POST, value= "/hoursNeeded")
    public @ResponseBody EmployeeUpdateTO getHoursNeeded(@RequestHeader(value="Authorization", required=false) String idToken, 
    		HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	try {
			to.setNumericResponse(employeeShiftCompatibilityManager.hoursNeededWeekOfShift(employee,shift));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return to;
    }
    @RequestMapping(method = RequestMethod.POST, value= "/hoursNeededAfterAssignment")
    public @ResponseBody EmployeeUpdateTO getHoursNeededAfterAssignment(@RequestHeader(value="Authorization", required=false) String idToken, 
    		HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	to.setNumericResponse(employeeShiftCompatibilityManager.getHoursNeededAfterAssignment(new EmployeeShiftCompatibility(employee,shift)));//.hoursNeededAfterAssignmentWeekOf(employee,shift));

    	return to;
    }
    @RequestMapping(method = RequestMethod.POST, value= "/hoursAvailableAfterAssignment")
    public @ResponseBody EmployeeUpdateTO getHoursAvailableAfterAssignment(@RequestHeader(value="Authorization", required=false) String idToken, 
    		HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	to.setNumericResponse(employeeShiftCompatibilityManager.hoursAvailableAfterAssignment(employee,shift));

    	return to;
    }
    @RequestMapping(method = RequestMethod.POST, value= "/hoursAvailable")
    public @ResponseBody EmployeeUpdateTO getHoursAvailable(@RequestHeader(value="Authorization", required=false) String idToken, HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	to.setNumericResponse(employeeShiftCompatibilityManager.hoursAvailableWeekOfShift(employee,shift));

    	return to;
    }
    @RequestMapping(method = RequestMethod.POST, value= "/unassigned")
    public @ResponseBody EmployeeUpdateTO getUnassigned(@RequestHeader(value="Authorization", required=false) String idToken, 
    		HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	to.setBooleanResponse(employeeShiftCompatibilityManager.isUnassignedFor(employee,shift));

    	return to;
    }
    @RequestMapping(method = RequestMethod.POST, value= "/requestedOff")
    public @ResponseBody EmployeeUpdateTO getRequestedOff(@RequestHeader(value="Authorization", required=false) String idToken,
    		HttpServletRequest request) throws ProccessingException, CorruptDataException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	to.setBooleanResponse(employee.requestedOff(shift));

    	return to;
    }
    @RequestMapping(method = RequestMethod.POST, value= "/assignmentViolatesOffAlternateWeekends")
    public @ResponseBody EmployeeUpdateTO getAssignmentViolatesOffAlternateWeekends(@RequestHeader(value="Authorization", required=false) String idToken, 
    		HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	to.setBooleanResponse(employeeShiftCompatibilityManager.getAssignmentWouldViolateAlternateWeekendsOff(new EmployeeShiftCompatibility(employee,shift)));

    	return to;
    }
    @RequestMapping(method = RequestMethod.POST, value= "/assignmentIncursOvertime")
    public @ResponseBody EmployeeUpdateTO getAssignmentIncursOvertime(@RequestHeader(value="Authorization", required=false) String idToken, 
    		HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	to.setBooleanResponse(employeeShiftCompatibilityManager.getAssignmentWouldIncurOvertime(new EmployeeShiftCompatibility(employee,shift)));

    	return to;
    }
    @RequestMapping(method = RequestMethod.POST, value= "/workedLastWeekend")
    public @ResponseBody EmployeeUpdateTO getWorkedLastWeekend(@RequestHeader(value="Authorization", required=false) String idToken, 
    		HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	to.setBooleanResponse(employeeShiftManager.getAssignedShiftsForEmployeeForWeekBeforeShift(employee.getId(),shift).size()>0);

    	return to;
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/workingNextWeekend")
    public @ResponseBody EmployeeUpdateTO getWorkingNextWeekend(@RequestHeader(value="Authorization", required=false) String idToken, HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	to.setBooleanResponse(employeeShiftManager.getAssignedShiftsForEmployeeForWeekAfterShift(employee.getId(),shift).size()>0);

    	return to;
    }
    @RequestMapping(method = RequestMethod.POST, value= "/assignability")
    public @ResponseBody EmployeeUpdateTO getAssignability(@RequestHeader(value="Authorization", required=false) String idToken, HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	ScheduleOptions options = new ScheduleOptions();
    	
    	
    	to.setBooleanResponse(employeeShiftCompatibilityManager.isAssignableFor(employee,shift,options));

    	return to;
    }
    @RequestMapping(method = RequestMethod.POST, value= "/validity")
    public @ResponseBody EmployeeUpdateTO getValidity(@RequestHeader(value="Authorization", required=false) String idToken,HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	ScheduleOptions options = new ScheduleOptions();
    	
    	to.setBooleanResponse(employeeShiftCompatibilityManager.isValidFor(employee,shift,options));

    	return to;
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/availability")
    public @ResponseBody EmployeeUpdateTO getAvailability(@RequestHeader(value="Authorization", required=false) String idToken, HttpServletRequest request) throws CorruptDataException, ProccessingException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO to = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		to.setEmployeeId(employee.getId());
    	}
    	
    	to.setBooleanResponse(employeeShiftCompatibilityManager.isAvailableFor(employee,shift));

    	return to;
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/compatibility")
    public @ResponseBody CompatibilityDTO getCompatibility(@RequestHeader(value="Authorization", required=false) String idToken, HttpServletRequest request) throws ProccessingException, CorruptDataException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	Shift shift =null;

    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("shift");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			shift = mapper.readValue(param2, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	CompatibilityDTO dto = new CompatibilityDTO();
    	
    	if(employee!=null){
    		dto.setEmployeeId(employee.getId());
    	}
    	if(shift!=null){
    		dto.setShiftId(shift.getId());
    	}
    	
    	dto.setCompatible(employeeShiftCompatibilityManager.isCompatibleWith(employee,shift));

    	return dto;
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/customFieldData")
    public @ResponseBody EmployeeUpdateTO getCustomFieldData(@RequestHeader(value="Authorization", required=false) String idToken, HttpServletRequest request) throws ProccessingException, CorruptDataException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	CustomField customField =null;
    	int index = 0;
    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("customField");
    	String param3= request.getParameter("index");
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			customField = mapper.readValue(param2, CustomField.class);
			index = mapper.readValue(param3, Integer.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	EmployeeUpdateTO dto = new EmployeeUpdateTO();
    	
    	if(employee!=null){
    		dto.setEmployeeId(employee.getId());
    	}
    	
    	dto.setBooleanResponse(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(employee,customField));
    	dto.setNumericResponse(index);
    	return dto;
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/clientCustomFieldData")
    public @ResponseBody UpdateTO getClientCustomFieldData(@RequestHeader(value="Authorization", required=false) String idToken, HttpServletRequest request) throws ProccessingException, CorruptDataException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Client client =null;
    	CustomField customField =null;
    	
    	int index = 0;
    	String param1= request.getParameter("client");
    	String param2= request.getParameter("customField");
    	String param3= request.getParameter("index");
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    	
    	try {
			client = mapper.readValue(param1, Client.class);
			customField = mapper.readValue(param2, CustomField.class);
			index = mapper.readValue(param3, Integer.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	UpdateTO dto = new UpdateTO();
    	
    	if(client!=null){
    		dto.setId(client.getId());
    	}
    	
    	dto.setBooleanResponse(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(client,customField));
    	dto.setNumericResponse(index);
    	return dto;
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/setClientCustomFieldData")
    public @ResponseBody UpdateTO setClientCustomFieldData(@RequestHeader(value="Authorization", required=false) String idToken, HttpServletRequest request) throws ProccessingException, CorruptDataException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Client client =null;
    	CustomField customField =null;
    	boolean value = true;
    	String param1= request.getParameter("client");
    	String param2= request.getParameter("customField");
    	String param3= request.getParameter("value");
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	try {
			client = mapper.readValue(param1, Client.class);
			customField = mapper.readValue(param2, CustomField.class);
			value = mapper.readValue(param3, Boolean.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	UpdateTO dto = new UpdateTO();
    	
    	if(client!=null){
    		customDataManager.setCustomFieldData(client,customField,value);
    		dto.setId(client.getId());
        	dto.setBooleanResponse(true);
    	}
    	

    	return dto;
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/employeeCustomFieldData")
    public @ResponseBody UpdateTO getEmployeeCustomFieldData(@RequestHeader(value="Authorization", required=false) String idToken, 
    		HttpServletRequest request) throws ProccessingException, CorruptDataException, AuthenticationException{
    	CallAuth auth = securityManager.authorize(idToken, Constants.USER);

    	Employee employee =null;
    	CustomField customField =null;
    	int index = 0;
    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("customField");
    	String param3= request.getParameter("index");
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    	
    	try {
			employee = mapper.readValue(param1, Employee.class);
			customField = mapper.readValue(param2, CustomField.class);
			index = mapper.readValue(param3, Integer.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	if(!auth.getEmployeeId().equalsIgnoreCase(employee.getId())) {
    		if(!auth.isAdmin() && !auth.isManager()) {
    			throw new AuthenticationException();
    		}
    	}

    	UpdateTO dto = new UpdateTO();
    	
    	if(employee!=null){
    		dto.setId(employee.getId());
    	}
    	
    	dto.setBooleanResponse(customDataManager.getCustomFieldDatasValueOrCreateIfMissing(employee,customField));
    	dto.setNumericResponse(index);
    	return dto;
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/setEmployeeCustomFieldData")
    public @ResponseBody UpdateTO setEmployeeCustomFieldData(@RequestHeader(value="Authorization", required=false) String idToken, HttpServletRequest request) throws ProccessingException, CorruptDataException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	CustomField customField =null;
    	boolean value = true;
    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("customField");
    	String param3= request.getParameter("value");
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	try {
			employee = mapper.readValue(param1, Employee.class);
			customField = mapper.readValue(param2, CustomField.class);
			value = mapper.readValue(param3, Boolean.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	UpdateTO dto = new UpdateTO();
    	
    	if(employee!=null){
    		customDataManager.setCustomFieldData(employee,customField,value);
    		dto.setId(employee.getId());
        	dto.setBooleanResponse(true);
    	}
    	

    	return dto;
    }
    
    @RequestMapping(method = RequestMethod.POST, value= "/setCustomFieldData")
    public @ResponseBody UpdateTO setCustomFieldData(@RequestHeader(value="Authorization", required=false) String idToken, HttpServletRequest request) throws ProccessingException, CorruptDataException, AuthenticationException{
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Employee employee =null;
    	CustomField customField =null;
    	boolean value = true;
    	String param1= request.getParameter("employee");
    	String param2= request.getParameter("customField");
    	String param3= request.getParameter("value");
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	try {
			employee = mapper.readValue(param1, Employee.class);
			customField = mapper.readValue(param2, CustomField.class);
			value = mapper.readValue(param3, Boolean.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	UpdateTO dto = new UpdateTO();
    	
    	if(employee!=null){
    		customDataManager.setCustomFieldData(employee,customField,value);
    		dto.setId(employee.getId());
        	dto.setBooleanResponse(true);
    	}
    	

    	return dto;
    }
}