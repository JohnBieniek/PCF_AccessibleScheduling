package org.cloudfoundry.samples.music.web;//Ignore complaints

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.security.sasl.AuthenticationException;

import org.cloudfoundry.samples.music.managers.AccessibleSecurityManager;
import org.cloudfoundry.samples.music.managers.EmployeeShiftMapManager;
import org.cloudfoundry.samples.music.managers.ScheduleManager;
import org.cloudfoundry.samples.music.managers.ShiftAssignmentManager;
import org.cloudfoundry.samples.music.managers.ShiftGenerationManager;
import org.cloudfoundry.samples.music.managers.ShiftManager;
import org.cloudfoundry.samples.music.managers.UpdateInfoManager;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoAccessRequestRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoClientRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoClientRequestRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldDataRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoCustomFieldRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoEmployeeRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoShiftRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.ScheduleStatusRepository;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonParseException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.constants.Constants;
import accessiblesolutions.accessiblescheduling.domain.AccessRequest;
import accessiblesolutions.accessiblescheduling.domain.CallAuth;
import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.ClientRequest;
import accessiblesolutions.accessiblescheduling.domain.CustomField;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.ScheduleStatus;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.domain.UpdateInfo;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.Availability;
import accessiblesolutions.accessiblescheduling.to.ScheduleOptions;
import accessiblesolutions.accessiblescheduling.util.Util;

@RestController
@RequestMapping(value = "/schedule")
public class ScheduleController {
	@Autowired 
	AccessibleSecurityManager securityManager;
	
    private ScheduleManager manager;
    
    @Autowired
    ShiftManager shiftManager;
    
    @Autowired
    ShiftGenerationManager generationManager;
    
    @Autowired
    ShiftAssignmentManager assignmentManager;
    
    @Autowired
    EmployeeShiftMapManager employeeShiftMapManager;
    
    @Autowired
    private CrudRepository<ScheduleStatus, String> scheduleStatusCrud;
    
    @Autowired
    ScheduleManager scheduleManager;
    
    @Autowired
    private ScheduleStatusRepository scheduleStatusRepository;   

    @Autowired
    private MongoClientRequestRepository requestRepository;
 
    @Autowired
    private MongoAccessRequestRepository accessRequestRepository;
    
    @Autowired
    private MongoClientRepository clientRepository;
    
    @Autowired
    private MongoShiftRepository shiftRepository;
    
    @Autowired
    private MongoEmployeeRepository employeeRepository;
    
    @Autowired 
    ShiftAssignmentManager shiftAssignmentManager;
    
    @Autowired
    private UpdateInfoManager updateInfoManager;
    
	@Autowired
    private CrudRepository<UpdateInfo, String> updateInfoRepository;
    
    @Autowired
    private MongoCustomFieldDataRepository customFieldDataRepository;
    @Autowired
    private MongoCustomFieldRepository customFieldRepository;
    
    @Autowired
    public ScheduleController(ScheduleManager manager) {
        this.manager=manager;
    }

    
    @RequestMapping(value = "/employeeNames", method = RequestMethod.GET)
    public String employeeNames(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException, JSONException, InterruptedException {
    	securityManager.authorize(idToken, Constants.MANAGER);
	    UpdateInfo updateInfo = updateInfoRepository.findOne("employees");
		  List<Employee> employeeList = employeeRepository.findAll();
          Collections.sort(employeeList);
		  JSONObject employeeJson = new JSONObject();
		  JSONArray employeeInfoJson = new JSONArray();
		  JSONObject employeesInfoJson = new JSONObject();
		  for(Employee selectedEmployee : employeeList) {
			  employeeJson = new JSONObject();
			  employeeJson.put("name", selectedEmployee.getName());
			  employeeJson.put("id", selectedEmployee.getId());
			  employeeInfoJson.put(employeeJson);
		  }
		  
		  employeesInfoJson.put("names", employeeInfoJson);
		  employeesInfoJson.put("tableLastUpdated", updateInfo.getTime());
    	return employeeInfoJson.toString();
    }
    
    @RequestMapping(value = "/clientNames", method = RequestMethod.GET)
    public String clientNames(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException, JSONException {
    	securityManager.authorize(idToken, Constants.MANAGER);

	    UpdateInfo updateInfo = updateInfoRepository.findOne("clients");
	    List<Client> clientList = clientRepository.findAll();
        Collections.sort(clientList);
		JSONObject clientJson = new JSONObject();
		JSONArray clientInfoJson = new JSONArray();
		JSONObject clientsInfoJson = new JSONObject();
		for(Client selectedClient : clientList) {
		  clientJson = new JSONObject();
		  clientJson.put("name", selectedClient.getName());
		  clientJson.put("id", selectedClient.getId());
		  clientInfoJson.put(clientJson);
		}
	  
	    clientsInfoJson.put("names", clientInfoJson);
	    clientsInfoJson.put("tableLastUpdated", updateInfo.getTime());
    	return clientInfoJson.toString();
    }
    
    @RequestMapping(value = "/allUpdates",method = RequestMethod.GET)
    public String getAllUpdates(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String json) throws AuthenticationException, JSONException, JsonProcessingException, NumberFormatException, ProccessingException {
    	securityManager.authorize(idToken, Constants.USER);
    	System.out.println("called get allUpdates with json:"+json);
    	JSONObject jsonObject = new JSONObject(json);
    	System.out.println("called get allUpdates with jsonObject:"+jsonObject.toString());
    	JSONObject result = new JSONObject();
	    ObjectMapper mapper = new ObjectMapper();
    	Iterator keys = jsonObject.keys();

    	while(keys.hasNext()) {
    		try {
			    String id = null;
			    String date = null;
			    String day = null;
			    String month = null;
			    String year = null;
			    Employee employee = null;
			    Client client = null;
			    LocalDateTime tableLastUpdated = null;
			    LocalDateTime lastUpdated = null;
			    LocalDateTime currentUpdateTime = null;
	    	    String key = (String) keys.next();
	    	    
	    	    if (jsonObject.get(key) instanceof JSONObject) {
	    			JSONObject updateRequest = (JSONObject)jsonObject.get(key);
					
				    UpdateInfo updateInfo = updateInfoRepository.findOne(key);
				    String tableString = null;
				    
				    if(updateRequest.has("tableLastUpdated")){
				    	tableString = updateRequest.getString("tableLastUpdated");
				    	
					    if(null!=tableString && !"null".equalsIgnoreCase(tableString)) {
					    	tableLastUpdated = Util.getLocalDateTimeFromString(tableString);
					    }
				    }
				    
				    if(updateRequest.has("id")) {
				    	id=updateRequest.getString("id");
					    
					    if(updateRequest.has("lastUpdated")){
					    	String updateString = updateRequest.getString("lastUpdated");
					    	
						    if(null!=updateString && !"null".equalsIgnoreCase(updateString)) {
						    	lastUpdated = Util.getLocalDateTimeFromString(updateString);
						    }
					    }
					    
					    employee =employeeRepository.findOne(id);
					    
					    if(null==employee) {
					    	client = clientRepository.findOne(id);
					    	
					    	if(client.getLastUpdated()==null) {
					    		client.setLastUpdatedToNow();
					    		clientRepository.save(client);
					    	}
					    	System.out.println("client.getLastUpdated():"+client.getLastUpdated()+" lastUpdated:"+lastUpdated);
					    	if(key.equalsIgnoreCase(Constants.CLIENT) && 
					    			(client.getLastUpdated()==null || lastUpdated == null ||client.lastUpdatedTime().isAfter(lastUpdated))) {
						    	result.put(Constants.CLIENT, new JSONObject(mapper.writeValueAsString(client)));
						    	
								JSONArray clientCustomDataJson = new JSONArray();							    
							    Iterable<CustomFieldData> clientData = customFieldDataRepository.findByOwnerId(id);
							    Iterable<CustomField> fields = customFieldRepository.findAll();
							    for(CustomField clientField: fields) {
							    	for(CustomFieldData clientsData:clientData) {
							    		if(clientsData.getCustomFieldId().equalsIgnoreCase(clientField.getId())) {
							    			clientCustomDataJson.put(clientsData.getBooleanData());
							    		}
							    	}
							    }
							  	result.put("customFieldData", clientCustomDataJson);
						    }
					    }
					    else if(key.equalsIgnoreCase(Constants.EMPLOYEE) && 
					    		(employee.getLastUpdated()==null || lastUpdated == null ||employee.getLastUpdatedTime().isAfter(lastUpdated))) {
					    	if(employee.getLastUpdated()==null) {
					    		employee.setLastUpdatedToNow();
					    		employeeRepository.save(employee);
					    	}
					    	result.put(Constants.EMPLOYEE, new JSONObject(mapper.writeValueAsString(employee)));
					    	
							JSONArray employeeCustomDataJson = new JSONArray();							    
						    Iterable<CustomFieldData> employeeData = customFieldDataRepository.findByOwnerId(id);
						    Iterable<CustomField> fields = customFieldRepository.findAll();
						    for(CustomField field: fields) {
						    	for(CustomFieldData data:employeeData) {
						    		if(data.getCustomFieldId().equalsIgnoreCase(field.getId())) {
						    			employeeCustomDataJson.put(data.getBooleanData());
						    		}
						    	}
						    }
						  	result.put("customFieldData", employeeCustomDataJson);
					    }
				    }
				    
				    //Certain data is range based. Shifts currently need to know when they are for to get a managable result
				    if(updateRequest.has("date")) {
				    	date = updateRequest.getString("date");
				    	String[] splitDate = date.split("-");
				    	if(splitDate.length>2) {
					    	day = splitDate[2];
					    	month = splitDate[1];
					    	year = splitDate[0];			    		
				    	}
				    	
				    	if(key.equalsIgnoreCase("status")) {
							   JSONObject statusJson = new JSONObject();
							    
						       List<ScheduleStatus> statuses = scheduleStatusRepository.findAll();
						       Collections.sort(statuses);
						       JSONArray statusInfoJson = new JSONArray();
						    	    
						       for(ScheduleStatus status: statuses) {
						    	  statusInfoJson.put(new JSONObject(mapper.writeValueAsString(status)));
						       }
						       
						       if(tableLastUpdated == null || updateInfo.getTime().isAfter(tableLastUpdated)){
							       statusJson.put("info", statusInfoJson);
								   statusJson.put("tableLastUpdated", updateInfo.getTime());
						       }

						       statusJson.put("assigned", shiftManager.getAssignedShiftsForMonth(Integer.parseInt(month)).size());    
						       statusJson.put("unassigned", shiftManager.getUnassignedShiftsForMonth(Integer.parseInt(month)).size());    

							   result.put("status", statusJson);
				     	}
				    }
				    
				    //TODO consider removing
			    	if(updateInfo==null || updateInfo.getTime()==null) {
			    		updateInfoManager.set(key);
			    		updateInfo = updateInfoRepository.findOne(key);
			    	}
			    	
				    if(tableLastUpdated == null || updateInfo.getTime().isAfter(tableLastUpdated)) {
						switch(key)
						{
						   case Constants.EMPLOYEES :
							  List<Employee> employeeList = employeeRepository.findAll();
					          Collections.sort(employeeList);
							  JSONObject employeeJson = new JSONObject();
							  JSONArray employeeInfoJson = new JSONArray();
							  JSONObject employeesInfoJson = new JSONObject();
							  for(Employee selectedEmployee : employeeList) {
								  employeeJson = new JSONObject();
								  employeeJson.put("name", selectedEmployee.getName());
								  employeeJson.put("id", selectedEmployee.getId());
								  employeeInfoJson.put(employeeJson);
							  }
							  
							  employeesInfoJson.put("names", employeeInfoJson);
							  employeesInfoJson.put("tableLastUpdated", updateInfo.getTime());
							  
							  result.put(Constants.EMPLOYEES, employeesInfoJson);
						      break;
						   case Constants.CLIENTS :
							  List<Client> clientList = clientRepository.findAll();
					          Collections.sort(clientList);
							  JSONObject clientJson = new JSONObject();
							  JSONArray clientInfoJson = new JSONArray();
							  JSONObject clientsInfoJson = new JSONObject();
							  for(Client selectedClient : clientList) {
								  clientJson = new JSONObject();
								  clientJson.put("name", selectedClient.getName());
								  clientJson.put("id", selectedClient.getId());
								  clientInfoJson.put(clientJson);
							  }
							  
							  clientsInfoJson.put("names", clientInfoJson);
							  clientsInfoJson.put("tableLastUpdated", updateInfo.getTime());
							  
							  result.put(Constants.CLIENTS, clientsInfoJson);
						      break; 
						   case Constants.SHIFTS :
							  JSONObject shiftJson = new JSONObject();
						      JSONArray shiftInfoJson = new JSONArray();
							  Iterable<Shift> shifts = null;
							  System.out.println("got shifts for day:"+day+" month:"+month+ " year:"+year);
							  if(null!=employee) {
								  shifts = manager.getEmployeeShiftsForWeek(id,month,day,year);
							  }
							  else if(null!=client) {
								  shifts = manager.getClientShiftsForWeek(id, month, day, year);
							  }
							  
							  for(Shift shift : shifts) {
								  System.out.println(" found shift "+shift.toString());
						          if(null == currentUpdateTime || (shift.getLastUpdatedTime()!=null &&
						        		  currentUpdateTime.isAfter(shift.getLastUpdatedTime()))) {
						        	  currentUpdateTime=shift.getLastUpdatedTime();
						          }
						    	      
						    	  shiftInfoJson.put(new JSONObject(mapper.writeValueAsString(shift)));
						      }
						    	    

						    	    
						      if(currentUpdateTime==null || lastUpdated == null ||currentUpdateTime.isAfter(lastUpdated)) {
						      	    shiftJson.put("info", shiftInfoJson);
							        shiftJson.put("lastUpdated", currentUpdateTime);
								    shiftJson.put("tableLastUpdated", updateInfo.getTime());
									result.put(Constants.SHIFTS,  shiftJson);
						      }
							  break;
						   case Constants.REQUESTS :
							  JSONObject requestJson = new JSONObject();
							    
						      Iterable<ClientRequest> requests = requestRepository.findByClientId(id);

						      JSONArray requestInfoJson = new JSONArray();
						    	    
						      for(ClientRequest selectedClientRequest : requests) {
						          if(null == currentUpdateTime || (selectedClientRequest.getLastUpdatedTime() !=null &&
						        		  currentUpdateTime.isAfter(selectedClientRequest.getLastUpdatedTime()))) {
						        	  currentUpdateTime=selectedClientRequest.getLastUpdatedTime();
						          }
						    	      
						    	  requestInfoJson.put(new JSONObject(mapper.writeValueAsString(selectedClientRequest)));
						      }
						    	    
						      if(currentUpdateTime==null || lastUpdated == null ||currentUpdateTime.isAfter(lastUpdated)) {
					      	      requestJson.put("info", requestInfoJson);
							      requestJson.put("lastUpdated", currentUpdateTime);
								  requestJson.put("tableLastUpdated", updateInfo.getTime());
								  result.put(Constants.REQUESTS,  requestJson);
						      }
							  break; 
						   case Constants.ALERTS :
							  JSONObject alertJson = new JSONObject();
							    
						      Iterable<AccessRequest> alerts = accessRequestRepository.findAll();

						      JSONArray alertInfoJson = new JSONArray();
						    	    
						      for(AccessRequest alert:alerts) {
						    	  alertInfoJson.put(new JSONObject(mapper.writeValueAsString(alert)));
						      }
						    	    
				      	      alertJson.put("info", alertInfoJson);
							  alertJson.put("tableLastUpdated", updateInfo.getTime());


							  result.put(Constants.ALERTS, alertJson);
							  break; 
						   case Constants.CUSTOM_FIELDS :
							  JSONObject customFieldsJson = new JSONObject();
							    
						      Iterable<CustomField> customFields = customFieldRepository.findAll();

						      JSONArray customFieldsInfoJson = new JSONArray();
						    	    
						      for(CustomField customField: customFields) {
						    	  customFieldsInfoJson.put(new JSONObject(mapper.writeValueAsString(customField)));
						      }
						    	    
							  customFieldsJson.put("info", customFieldsInfoJson );
							  customFieldsJson.put("tableLastUpdated", updateInfo.getTime());

							  result.put(Constants.CUSTOM_FIELDS, customFieldsJson);
						      break; 
						}
				    }
	    	    }
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    	
    	return result.toString();
    }
    
    @RequestMapping(value = "/requestWasUpdated",method = RequestMethod.GET)
    public String requestRequiresUpdate(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String lastUpdated,@RequestParam String requestId) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.USER);
    	
    	String updated ="UNMODIFIED";
    	LocalDateTime time = null;
    	//2019-09-22T20:02:26.789Z
    	if(lastUpdated!=null && lastUpdated!="null") {
    		time=Util.getLocalDateTimeFromString(lastUpdated);
    	}

    	ClientRequest request = requestRepository.findOne(requestId);
    	
    	if(null!=request) {
    		if(time==null && request.getLastUpdated()!=null){
    			updated="UPDATED";
    		}
    		else {
        		if(!lastUpdated.equalsIgnoreCase(request.getLastUpdated())) {
                	if(time!=null && time.isBefore(Util.getLocalDateTimeFromString(request.getLastUpdated()))) {
                		updated="UPDATED";
                	}
        		}
    		}
    	}
    	else {
    		updated="DELETED";
    	}
		
    	return updated;
    }
    
    @RequestMapping(value = "/availabilityWasUpdated",method = RequestMethod.GET)
    public String availabilityRequiresUpdate(@RequestHeader(value="Authorization", required=false) String idToken,@RequestParam String param,@RequestParam String index) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.USER);
    	
    	String updated ="UNMODIFIED";
    	
    	Employee employee =null;

    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param, Employee.class);
			
	    	LocalDateTime time = null;
	    	//2019-09-22T20:02:26.789Z
	    	if(employee.getLastUpdated()!=null ) {
	    		time=Util.getLocalDateTimeFromString(employee.getLastUpdated());
	    	}

	    	Employee serverEmployee = employeeRepository.findOne(employee.getId());
	    	
	    	if(null!=serverEmployee) {
	    		if(time==null && serverEmployee.getLastUpdated()!=null){
	    			updated="EMPLOYEE_UPDATED";
	    		}
	    		else {
	        		if(!employee.getLastUpdatedTime().isEqual(serverEmployee.getLastUpdatedTime())) {
	                	if(time!=null && time.isBefore(Util.getLocalDateTimeFromString(serverEmployee.getLastUpdated()))) {
	                		updated="EMPLOYEE_UPDATED";
	                	}
	        		}
	    		}
	    	}
	    	else {
	    		updated="DELETED";
	    	}
	    	
	    	if(updated.equalsIgnoreCase("EMPLOYEE_UPDATED")) {
	    		if(employee.getStartTimes().length==serverEmployee.getStartTimes().length) {
	    			System.out.println("employee.getAvailability(Integer.parseInt(index))"+employee.getAvailability(Integer.parseInt(index)));
	    			System.out.println("serverEmployee.getAvailability(Integer.parseInt(index))"+serverEmployee.getAvailability(Integer.parseInt(index)));
	    			Availability availability = employee.getAvailability(Integer.parseInt(index));
	    			Availability serverAvailability = serverEmployee.getAvailability(Integer.parseInt(index));
	    			if(availability.day.getValue()!=serverAvailability.day.getValue()) {
	    				updated="UPDATED";
	    			}
	    			else if(!availability.startTime.toString().equalsIgnoreCase(serverAvailability.startTime.toString())) {
	    				updated="UPDATED";
	    			}
	    			else if(!availability.endTime.toString().equalsIgnoreCase(serverAvailability.endTime.toString())) {
	    				updated="UPDATED";
	    			}
	    		}
	    		else {
	    			updated="POSSIBLY_UPDATED";
	    		}
	    	}
    	}
    	catch(Exception e){
    		System.out.println(e);
    	}
		
    	return updated;
    }
    
    @RequestMapping(value = "/shiftWasUpdated",method = RequestMethod.GET)
    public String shiftRequiresUpdate(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String lastUpdated,@RequestParam String shiftId) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.USER);
    	
    	String updated ="UNMODIFIED";
    	LocalDateTime time = null;
    	//2019-09-22T20:02:26.789Z
    	if(lastUpdated!=null && lastUpdated!="null") {
    		time=Util.getLocalDateTimeFromString(lastUpdated);
    	}

    	Shift shift = shiftRepository.findOne(shiftId);
    	
    	if(null!=shift) {
    		if(time==null && shift.getLastUpdated()!=null){
    			updated="UPDATED";
    		}
    		else {
        		if(!lastUpdated.equalsIgnoreCase(shift.getLastUpdated())) {
                	if(time!=null && time.isBefore(Util.getLocalDateTimeFromString(shift.getLastUpdated()))) {
                		updated="UPDATED";
                	}
        		}
    		}
    	}
    	else {
    		updated="DELETED";
    	}
		
    	return updated;
    }
    
    @RequestMapping(value = "/employeeWasUpdated",method = RequestMethod.GET)
    public String employeeRequiresUpdate(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String lastUpdated,@RequestParam String employeeId) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.USER);
    	
    	String updated ="UNMODIFIED";
    	LocalDateTime time = null;
    	//2019-09-22T20:02:26.789Z
    	if(lastUpdated!=null && lastUpdated!="null") {
    		time=Util.getLocalDateTimeFromString(lastUpdated);
    	}

    	Employee employee = employeeRepository.findOne(employeeId);
    	
    	if(null!=employee) {
    		if(time==null && employee.getLastUpdated()!=null){
    			updated="UPDATED";
    		}
    		else {
        		if(!lastUpdated.equalsIgnoreCase(employee.getLastUpdated())) {
                	if(time!=null && time.isBefore(Util.getLocalDateTimeFromString(employee.getLastUpdated()))) {
                		updated="UPDATED";
                	}
        		}
    		}
    	}
		
    	return updated;
    }
    
    @RequestMapping(value = "/clientWasUpdated",method = RequestMethod.GET)
    public String clientRequiresUpdate(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String lastUpdated,@RequestParam String clientId) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.USER);
    	
    	String updated ="UNMODIFIED";
    	LocalDateTime time = null;
    	//2019-09-22T20:02:26.789Z
    	if(lastUpdated!=null && lastUpdated!="null") {
    		time=Util.getLocalDateTimeFromString(lastUpdated);
    	}

    	Client client = clientRepository.findOne(clientId);
    	
    	if(null!=client) {
    		System.out.println("client:"+client.toString());
    		
    		if(time==null && client.getLastUpdated()!=null){
    			updated="UPDATED";
    		}
    		else {
        		if(!lastUpdated.equalsIgnoreCase(client.getLastUpdated())) {
                	if(time!=null && time.isBefore(Util.getLocalDateTimeFromString(client.getLastUpdated()))) {
                		updated="UPDATED";
                	}
        		}
    		}
    	}
		
    	return updated;
    }
    
    @RequestMapping(value = "/customFieldData",method = RequestMethod.GET)
    public Iterable<CustomFieldData> customFieldData(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String param) throws AuthenticationException {
    	CallAuth auth = securityManager.authorize(idToken, Constants.USER);
    	
    	if(!auth.isAdmin() && !auth.isManager()) {
    		if(param!=auth.getEmployeeId()) {
    			throw new AuthenticationException();
    		}
    	}
    	
    	List<CustomFieldData> serverData = customFieldDataRepository.findByOwnerId(param);
    	System.out.println("customFieldData in ScheduleController gooble snerb fedarkle:"+serverData.size());
    	return serverData;
    }
    
    @RequestMapping(value = "/staffShift",method = RequestMethod.POST)
    public Employee staffShift(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String param) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	Shift shift =null;
    	System.out.println(param);
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			shift = mapper.readValue(param, Shift.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	System.out.println(shift.toString());
    	//shiftRepository.save(shift);
        ScheduleOptions options = new ScheduleOptions();
        options.setDailyMax(true);
        options.setWeeklyMax(true);
        options.setYear(shift.getStartYear()+"");
        options.setMonth(shift.getStartMonth()+"");
        
        Employee employee = null;
        try {
			employee = shiftAssignmentManager.suggestScheduleShiftSafely(shift, options);
		} catch (CorruptDataException | ProccessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return employee;
    }
    
    @RequestMapping(value = "/clientShiftsForWeek",method = RequestMethod.GET)
    public Iterable<Shift> clientShiftsForWeek(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String clientId, @RequestParam String month, @RequestParam String day, @RequestParam String year) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	return manager.getClientShiftsForWeek(clientId,month,day,year);
    }
     
    @RequestMapping(value = "/employeeShiftsForWeek",method = RequestMethod.GET)
    public Iterable<Shift> employeeShiftsForWeek(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String employeeId, @RequestParam String month, @RequestParam String day, @RequestParam String year) throws AuthenticationException {
    	CallAuth auth = securityManager.authorize(idToken, Constants.USER);
    	
    	if(!auth.getEmployeeId().equalsIgnoreCase(employeeId)) {
    		if(!auth.isAdmin() && !auth.isManager()) {
    			throw new AuthenticationException();
    		}
    	}
    	
    	Date maxMonthDate= new Date();
    	int maxMonth= maxMonthDate.getMonth()+1;
    	if(maxMonth>11) {
    		maxMonth=1;
    	}
    	
    	ArrayList<Shift> shifts = (ArrayList<Shift>) manager.getEmployeeShiftsForWeek(employeeId,month,day,year);
    	ArrayList<Shift> returnValue = new ArrayList<Shift>();
    	
    	if(null!=shifts && !shifts.isEmpty()) {
	    	if(!auth.isAdmin() && !auth.isManager()) {
		    	for(Shift shift: shifts) {
		    		if(shift.getStartMonth()<=maxMonth) {
		    			returnValue.add(shift);
		    		}
		    	}
	    	}
	    	else {
	    		returnValue=shifts;
	    	}
    	}
    	
    	return returnValue;
    }
    
    @RequestMapping(value = "/createEmployee",method = RequestMethod.POST)
    public Iterable<Employee> createEmployee(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	Employee employee =new Employee();
    	
    	employee.setFirst("An employee");
    	
    	employeeRepository.save(employee);

        List<Employee> employees = employeeRepository.findAll();
        Collections.sort(employees);
        
        updateInfoManager.set("employees");
		return employees;
    }    
    @RequestMapping(value = "/createClient",method = RequestMethod.POST)
    public Iterable<Client> createClient(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	Client client =new Client();
    	
    	client.setFirst("A client");
    	
        clientRepository.save(client);

        List<Client> clients = clientRepository.findAll();
        Collections.sort(clients);
        updateInfoManager.set("clients");
		return clients;
    }

    @RequestMapping(value = "/updateAvailability",method = RequestMethod.POST)
    public Employee updateAvailability(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String param, @RequestParam String index) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	Employee employee =null;

    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param, Employee.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	Employee serverEmployee = employeeRepository.findOne(employee.getId());
    	serverEmployee.setAvailability(employee.getAvailability(Integer.parseInt(index)), Integer.parseInt(index));
    	serverEmployee.setLastUpdatedToNow();
    	employeeRepository.save(serverEmployee);
    	updateInfoManager.set("employees");
        return employeeRepository.findOne(employee.getId());
    }
    
    @RequestMapping(value = "/removeAvailability",method = RequestMethod.POST)
    public Employee removeAvailability(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String param, @RequestParam String index) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	Employee employee =null;

    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param, Employee.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	Employee serverEmployee = employeeRepository.findOne(employee.getId());
    	serverEmployee.setAvailability(employee.getAvailability(Integer.parseInt(index)), Integer.parseInt(index));
    	serverEmployee.setLastUpdatedToNow();
    	employeeRepository.save(serverEmployee);
    	updateInfoManager.set("employees");
        return employeeRepository.findOne(employee.getId());
    }
    
    @RequestMapping(value = "/updateEmployee",method = RequestMethod.POST)
    public Employee updateEmployee(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String param) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	Employee employee =null;

    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param, Employee.class);
			if(employee.getRole().equalsIgnoreCase("manager")) {
				employee.setManager(true);
				employee.setAdmin(false);
			}
			else if(employee.getRole().equalsIgnoreCase("admin")) {
				employee.setAdmin(true);
				employee.setManager(true);
			}
			else {
				employee.setManager(false);
				employee.setAdmin(false);
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	employee.setLastUpdatedToNow();
    	employeeRepository.save(employee);
    	updateInfoManager.set("employees");
        return employeeRepository.findOne(employee.getId());
    }
    @RequestMapping(value = "/updateClient",method = RequestMethod.POST)
    public Client updateClient(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String param) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	Client client =null;

    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			client = mapper.readValue(param, Client.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	client.setLastUpdatedToNow();
        clientRepository.save(client);
        updateInfoManager.set("clients");
        return clientRepository.findOne(client.getId());
    }

    @RequestMapping(value = "/deleteRequest", method = RequestMethod.GET)
    public List<ClientRequest> deleteById(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String id) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	ClientRequest request = requestRepository.findOne(id);
    	String clientId=request.getClientId();
    	requestRepository.delete(id);
        
        return requestRepository.findByClientId(clientId);
    }
    
    @RequestMapping(value = "/clientsRequests", method = RequestMethod.GET)
    public String clientsRequests(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String clientId, @RequestParam String tableLastUpdated, @RequestParam String lastUpdated) throws AuthenticationException, JSONException, JsonProcessingException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
	    JSONObject requestJson = new JSONObject();
	    
	    UpdateInfo updateInfo = updateInfoRepository.findOne(Constants.REQUESTS);

    	if(updateInfo==null || updateInfo.getTime()==null) {
    		updateInfoManager.set(Constants.REQUESTS);
    		updateInfo = updateInfoRepository.findOne(Constants.REQUESTS);
    	}
	    	
    	LocalDateTime tableLastUpdatedTime = null;
    	LocalDateTime lastUpdatedTime = null;
    	if(null!=tableLastUpdated && !"null".equalsIgnoreCase(tableLastUpdated)){
    		tableLastUpdatedTime =Util.getLocalDateTimeFromString(tableLastUpdated);
    	}
    	if(null!=lastUpdated && !"null".equalsIgnoreCase(lastUpdated)){
        	lastUpdatedTime = Util.getLocalDateTimeFromString(lastUpdated);
    	}

    	if(tableLastUpdated == null || lastUpdated==null ||
    	   "null".equalsIgnoreCase(tableLastUpdated) || "null".equalsIgnoreCase(lastUpdated) || 
    	   updateInfo.getTime().isAfter(tableLastUpdatedTime)) {
    		
            Iterable<ClientRequest> requests = requestRepository.findByClientId(clientId);
    	    ObjectMapper mapper = new ObjectMapper();
    	    JSONArray requestInfoJson = new JSONArray();
    	    LocalDateTime currentUpdateTime = null;
    	    
    	    for(ClientRequest selectedClientRequest : requests) {
    	      if(null == currentUpdateTime || selectedClientRequest.getLastUpdatedTime().isAfter(currentUpdateTime)) {
    	    	  currentUpdateTime=selectedClientRequest.getLastUpdatedTime();
    	      }
    	      
    		  requestInfoJson.put(new JSONObject(mapper.writeValueAsString(selectedClientRequest)));
    	    }
    	    

    	    
    	    if(lastUpdatedTime==null || "null".equalsIgnoreCase(lastUpdated) || currentUpdateTime.isAfter(lastUpdatedTime)) {
        	    requestJson.put("info", requestInfoJson);
        	    requestJson.put("lastUpdated", currentUpdateTime);
        	    requestJson.put("tableLastUpdated", updateInfo.getTime());
    	    }
    	}

    	return requestJson.toString();
    }

    @RequestMapping(value = "/byMonth", method = RequestMethod.DELETE)
    public Iterable<ScheduleStatus> deleteByMonth(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam("month") String  month) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	ScheduleStatus status = new ScheduleStatus();
         
     	status.setMonth(month);
     	status.setDeleting(true);
     	scheduleStatusRepository.deleteByMonth(month);
     	status.setLastUpdatedToNow();
     	scheduleStatusCrud.save(status);
     	updateInfoManager.set(Constants.STATUS);
     
        shiftManager.deleteShiftsForMonth(Integer.parseInt(month));
     	status.setDeleting(false);
     	status.setErrored(false);
     	status.setAssigning(false);
     	status.setAssigned(false);
     	status.setGenerated(false);
     	scheduleStatusRepository.deleteByMonth(month);
     	status.setLastUpdatedToNow();
     	scheduleStatusCrud.save(status);
     	updateInfoManager.set(Constants.STATUS);
    	return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/durationOfWeeksShifts", method = RequestMethod.GET)
    public float durationOfWeeksShifts(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException, AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	return shiftManager.getDurationOfShiftsStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
    }
    
    @RequestMapping(value = "/staffShiftsSafely", method = RequestMethod.GET)
    public ResponseEntity<?> staffShiftsSafely(@RequestHeader(value="Authorization", required=false) String idToken,
    													@RequestParam("month") String month
    													,@RequestParam("year") String year
    													,@RequestParam("allowOvertime") boolean allowOvertime
    													,@RequestParam("allowInactive") boolean allowInactive
    													,@RequestParam("allowUnavailable") boolean allowUnavailable
    													,@RequestParam("prioritizeSecondShift") boolean prioritizeSecondShift
    													,@RequestParam("dailyMax") boolean dailyMax
    													,@RequestParam("weeklyMax") boolean weeklyMax) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	System.out.println("Starting assignment");
    	ScheduleStatus status = assignmentManager.scheduleStatus(month);
    	if(null==status) {
        	System.out.println("Status for assignment:null");
    		status= new ScheduleStatus();
    		status.setMonth(month);
    	}
    	System.out.println("Status for assignment:"+status.toString());
    	if(status.isGenerated() && !status.isAssigning()) {
    		status.setAssigning(true);
    		status.setAssigningThreadId(Thread.currentThread().getId());
    		status.setLastUpdatedToNow();
        	scheduleStatusRepository.deleteByMonth(month);
        	scheduleStatusCrud.save(status);
            updateInfoManager.set(Constants.STATUS);
            
        	ScheduleOptions options = new ScheduleOptions(month, year, allowOvertime, allowInactive,allowUnavailable, 
								prioritizeSecondShift, dailyMax,weeklyMax);
			System.out.println("Schedule options:"+options.toString());
			
			assignmentManager.scheduleShifts(options);
    	}
    	else {
    		if(status.isAssigning()) {
    			return new ResponseEntity<String>("Already scheduling",HttpStatus.TOO_MANY_REQUESTS);
    		}
    		if(!status.isGenerated()) {
    			return new ResponseEntity<String>("Cannot schedule without generating shifts",HttpStatus.PRECONDITION_REQUIRED);
    		}
    	}
    	
    	return new ResponseEntity<>(scheduleStatusCrud.findAll(),HttpStatus.OK);	
    }
    
    @RequestMapping(value = "/assigning", method = RequestMethod.GET)
    public boolean assigning(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam("month") String month) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	return assignmentManager.scheduleStatus(month).isAssigning();
    }
    
    @RequestMapping(value = "/statusList", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> scheduleStatusList(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/unscheduled", method = RequestMethod.GET)
    public int unscheduled(@RequestHeader(value="Authorization", required=false) String idToken,@RequestParam("month") String month) throws NumberFormatException, ProccessingException, AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	return shiftManager.getUnassignedShiftsForMonth(Integer.parseInt(month)).size();
    }
    
    @RequestMapping(value = "/scheduled", method = RequestMethod.GET)
    public int scheduled(@RequestHeader(value="Authorization", required=false) String idToken,@RequestParam("month") String month) throws NumberFormatException, ProccessingException, AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	return shiftManager.getAssignedShiftsForMonth(Integer.parseInt(month)).size();
    }
    
    @RequestMapping(value = "/generateStatusList", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> generateStatusList(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	scheduleManager.generateStatusList();
        updateInfoManager.set(Constants.STATUS);
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/finishAssignment", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> finishAssignment(@RequestHeader(value="Authorization", required=false) String idToken,@RequestParam("month") String month) throws CorruptDataException, AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	ScheduleStatus status = assignmentManager.scheduleStatus(month);
    	System.out.println("finishing assignment for "+month);
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(month);
    	}
    	
    	if(status.isAssigning()) {
    		status.setAssigning(false);
    		System.out.println("errored when trying to shut down");
    	}
    	if(status.isGenerated()) {
    		status.setAssigned(true);
    		status.setLastUpdatedToNow();
    		scheduleStatusRepository.deleteByMonth(month);
        	scheduleStatusCrud.save(status);
            updateInfoManager.set(Constants.STATUS);
    	}
    	
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/stopAssignment", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> stopAssignment(@RequestHeader(value="Authorization", required=false) String idToken,@RequestParam("month") String month) throws CorruptDataException, InterruptedException, AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	ScheduleStatus status = assignmentManager.scheduleStatus(month);
    	
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(month);
        	status.setGenerated(true);

    	}
    	
		status.setStopping(true);
		status.setLastUpdatedToNow();
    	scheduleStatusRepository.deleteByMonth(month);
    	scheduleStatusCrud.save(status);
        updateInfoManager.set(Constants.STATUS);
    	//Thread.sleep(5000);//Can this be deleted?
    	//finishAssignment(idToken, month);//What does this do?
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/generateShifts", method = RequestMethod.GET)
    public ResponseEntity<?> generateShifts(@RequestHeader(value="Authorization", required=false) String idToken,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	ScheduleStatus status = assignmentManager.scheduleStatus(month);
    	if(null!=status) {
        	if(status.isGenerating()  || status.isGenerated()) {
        		return new ResponseEntity<String>("Already generated",HttpStatus.TOO_MANY_REQUESTS);
        	}
    	}
    	generationManager.generateShifts(month,year);
    	List<Shift> shifts = (List<Shift>)shiftRepository.findByStartMonth(Integer.parseInt(month));
		return new ResponseEntity<Integer>(shifts.size(),HttpStatus.OK);
    }
    
    @RequestMapping(value = "/getShiftsForMonth", method = RequestMethod.GET)
    public String getShiftsForOfMonth(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam("month") String month) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	return shiftManager.getShiftsForMonth(Integer.parseInt(month)).toString();
    }
    
    @RequestMapping(value = "/getShiftsPerEmployeeForMonth", method = RequestMethod.GET)
    public String getShiftsPerEmployeeForOfMonth(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam("month") String month) throws ProccessingException, AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	return employeeShiftMapManager.getAssignedShiftsPerEmployeeForMonth(Integer.parseInt(month)).toString();
    }
}