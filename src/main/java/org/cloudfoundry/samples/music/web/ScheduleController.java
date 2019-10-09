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
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.constants.Constants;
import accessiblesolutions.accessiblescheduling.domain.CallAuth;
import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.ClientRequest;
import accessiblesolutions.accessiblescheduling.domain.CustomFieldData;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.ScheduleStatus;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.domain.UpdateInfo;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
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
    public String employeeNames(@RequestHeader(value="Authorization", required=false) String idToken) throws AuthenticationException, JSONException {
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
    public String getAllUpdates(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String json) throws AuthenticationException, JSONException {
    	securityManager.authorize(idToken, Constants.USER);
    	System.out.println("called get allUpdates with json:"+json);
    	JSONObject jsonObject = new JSONObject(json);
    	System.out.println("called get allUpdates with jsonObject:"+jsonObject.toString());
    	JSONObject result = new JSONObject();
    	
    	Iterator keys = jsonObject.keys();

    	while(keys.hasNext()) {
    		try {
    			int index;
			    String id = null;
			    String date = null;
			    String day = null;
			    String month = null;
			    String year = null;
			    Employee employee = null;
			    Client client = null;
			    LocalDateTime tableLastUpdated = null;
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
				    	
					    employee =employeeRepository.findOne(id);
					    if(null==employee) {
					    	client = clientRepository.findOne(id);
					    }
				    }
				    
				    if(updateRequest.has("date")) {
				    	date = updateRequest.getString("date");
				    	String[] splitDate = date.split("-");
				    	if(splitDate.length>2) {
					    	day = splitDate[2];
					    	month = splitDate[1];
					    	year = splitDate[0];			    		
				    	}
				    }
				    
			    	if(updateInfo==null || updateInfo.getTime()==null) {
			    		updateInfoManager.set(key);
			    		updateInfo = updateInfoRepository.findOne(key);
			    	}
				    if(null!=tableLastUpdated)System.out.println("tableLastUpdated"+tableLastUpdated.toString());
				    if(null!=updateInfo)System.out.println("updateInfo:"+updateInfo.toString());
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
							  Iterable<Shift> shifts = null;
							  
							  if(null!=employee) {
								  shifts = manager.getEmployeeShiftsForWeek(id,day,month,year);
							  }
							  else if(null!=client) {
								  shifts = manager.getClientShiftsForWeek(id, month, day, year);
							  }
							  shiftJson.put("info", shifts);
							  shiftJson.put("tableLastUpdated", updateInfo.getTime());

							  result.put(Constants.SHIFTS,  shiftJson);
							  break;
						   case Constants.REQUESTS :
							  JSONObject requestJson = new JSONObject();
							  Iterable<ClientRequest> requests = null;
							  
							  if(null!=client) {
								  requests = requestRepository.findByClientId(id);
								  
								  requestJson.put("info", requests);
								  requestJson.put("tableLastUpdated", updateInfo.getTime());

								  result.put(Constants.REQUESTS,  requestJson);
							  }
							  break; 
						   case Constants.ALERTS :
							  result.put(Constants.ALERTS, accessRequestRepository.findAll());
							  break; 
						   case Constants.CUSTOM_FIELDS :
							  JSONObject customFieldsJson = new JSONObject();
							  customFieldsJson.put("info", customFieldRepository.findAll());
							  customFieldsJson.put("tableLastUpdated", updateInfo.getTime());

							  result.put(Constants.CUSTOM_FIELDS, customFieldsJson);
						      break; 
						   case Constants.EMPLOYEE :
							  result.put(Constants.EMPLOYEE, employee);
							  result.put("customFieldData", customFieldDataRepository.findByOwnerId(id));
							  break;
						   case Constants.CLIENT :
							  result.put(Constants.CLIENT, client);
							  result.put("customFieldData", customFieldDataRepository.findByOwnerId(id));
							  break;
						   case Constants.SHIFT :
							  result.put(Constants.SHIFT, shiftRepository.findOne(id));
							  break;
						   case Constants.REQUEST :
							  result.put(Constants.REQUEST, requestRepository.findOne(id));
							  break;
						   default : 
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
    public Iterable<ClientRequest> clientsRequests(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam String clientId) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	
    	return requestRepository.findByClientId(clientId);
    }

    @RequestMapping(value = "/byMonth", method = RequestMethod.DELETE)
    public Iterable<ScheduleStatus> deleteByMonth(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam("month") String  month) throws AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	ScheduleStatus status = new ScheduleStatus();
         
     	status.setMonth(month);
     	    	
     	scheduleStatusRepository.deleteByMonth(month);
     	scheduleStatusCrud.save(status);
     	
     
        shiftManager.deleteShiftsForMonth(Integer.parseInt(month));
    	return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/durationOfWeeksShifts", method = RequestMethod.GET)
    public float durationOfWeeksShifts(@RequestHeader(value="Authorization", required=false) String idToken, @RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException, AuthenticationException {
    	securityManager.authorize(idToken, Constants.MANAGER);
    	
    	return shiftManager.getDurationOfShiftsStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
    }
    
    @RequestMapping(value = "/staffShiftsSafely", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> staffShiftsSafely(@RequestHeader(value="Authorization", required=false) String idToken,
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
    	ScheduleStatus status = scheduleStatusCrud.findOne(month);
    	scheduleStatusRepository.deleteByMonth(month);
    	
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(month);
    	}
    
    	if(status.isGenerated()) {
    		status.setAssigning(true);
    		status.setLastUpdatedToNow();
        	scheduleStatusCrud.save(status);
            updateInfoManager.set("scheduleStatus");
    	}
    	
    	ScheduleOptions options = new ScheduleOptions(month, year, allowOvertime, allowInactive,allowUnavailable, 
    													prioritizeSecondShift, dailyMax,weeklyMax);
    	System.out.println("Schedule options:"+options.toString());
    	
		assignmentManager.scheduleShifts(options);

    	return scheduleStatusCrud.findAll();
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
        updateInfoManager.set("scheduleStatus");
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
            updateInfoManager.set("scheduleStatus");
    	}
    	
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/stopAssignment", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> stopAssignment(@RequestHeader(value="Authorization", required=false) String idToken,@RequestParam("month") String month) throws CorruptDataException, InterruptedException, AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
    	
    	ScheduleStatus status = scheduleStatusCrud.findOne(month);
    	scheduleStatusRepository.deleteByMonth(month);
    	
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(month);
    	}
    	
    	status.setGenerated(true);
		status.setAssigning(true);
		status.setStopped(true);
		status.setLastUpdatedToNow();
    	scheduleStatusCrud.save(status);
        updateInfoManager.set("scheduleStatus");
    	Thread.sleep(5000);//Can this be deleted?
    	finishAssignment(idToken, month);
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/generateShifts", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> generateShifts(@RequestHeader(value="Authorization", required=false) String idToken,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, AuthenticationException {
    	securityManager.authorize(idToken, Constants.ADMIN);
        updateInfoManager.set("scheduleStatus");
    	generationManager.generateShifts(month,year);
        return scheduleStatusCrud.findAll();
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